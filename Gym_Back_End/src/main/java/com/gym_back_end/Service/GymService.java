package com.gym_back_end.Service;

import com.gym_back_end.Models.*;
import com.gym_back_end.Repository.*;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cglib.core.Local;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

@Service
public class GymService {

    private final SubscriberRepository subscriberRepo;
    private final AttendanceRepository attendanceRepo;
    private final HistoricalSubscriptionRepository historyRepo;;
    @Value("${file.upload-dir}")
    private String uploadDir;

    public GymService(SubscriberRepository subscriberRepo, AttendanceRepository attendanceRepo, HistoricalSubscriptionRepository historyRepo) {
        this.subscriberRepo = subscriberRepo;
        this.attendanceRepo = attendanceRepo;
        this.historyRepo = historyRepo;
    }

    // A. Get all subscribers
    public List<Subscriber> getAllSubscribers() throws IOException {
        List<Subscriber> subscribers = subscriberRepo.findAll();
//        for(Subscriber subscriber : subscribers){
//            System.out.println(subscriber.getPhoto_url());
//        }
        return attachPicture(subscribers);
    }

    public List<Subscriber> attachPicture(List<Subscriber> subscribers) throws IOException {
        if(subscribers == null || subscribers.isEmpty()) {
            return subscribers;
        }
        for (Subscriber subscriber : subscribers) {
            Path path = Paths.get("uploads/"+subscriber.getPhoto_url());
            byte[] bytes = Files.readAllBytes(path);
            subscriber.setPhoto_url(Base64.getEncoder().encodeToString(bytes));
        }
        return subscribers;
    }

    // B. Subscribers expiring soon
    public List<Subscriber> getSubscribersExpiringSoon() throws IOException {
        LocalDate today = LocalDate.now();
        LocalDate threeDaysLater = today.plusDays(3);
        return attachPicture(subscriberRepo.findSubscribersExpiringSoon(today, threeDaysLater));
    }

    // C. Expired subscribers
    public List<Subscriber> getExpiredSubscribers() throws IOException {
        return attachPicture(subscriberRepo.findExpiredSubscribers(LocalDate.now()));
    }

    // D. Add new subscriber (with photo)
    public Subscriber addSubscriber(Subscriber subscriber, MultipartFile photoFile , int price) throws IOException {
        // 1. Save subscriber first (without photo)
        subscriber.setSubscriptionEnd(subscriber.getSubscriptionEnd().minusDays(1));
        Subscriber saved = subscriberRepo.save(subscriber);
        HistoricalSubscription historicalSubscription = new HistoricalSubscription();
        historicalSubscription.setSubscriber(saved);
        historicalSubscription.setStartDate(subscriber.getSubscriptionStart());
        historicalSubscription.setEndDate(subscriber.getSubscriptionEnd());
        historicalSubscription.setPrice((double)price);
        historyRepo.save(historicalSubscription);
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        if (photoFile != null && !photoFile.isEmpty()) {
            // 3. Build file path (e.g., uploads/5.jpg)
            String fileExtension = getFileExtension(photoFile.getOriginalFilename());
            String newFileName = saved.getId() + (fileExtension.isEmpty() ? "" : "." + fileExtension);
            Path filePath = uploadPath.resolve(newFileName);

            // 4. Save the file
            Files.copy(photoFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // 5. Update subscriber’s photo URL
            saved.setPhoto_url(newFileName);
            subscriberRepo.save(saved);
        }
        else{
            saved.setPhoto_url("person.jpg");
            subscriberRepo.save(saved);
        }
        Path path = Paths.get("uploads/"+saved.getPhoto_url());
        byte[] bytes = Files.readAllBytes(path);
        saved.setPhoto_url(Base64.getEncoder().encodeToString(bytes));
        return saved;
    }

    private String getFileExtension(String fileName) {
        if (fileName == null) return "";
        int index = fileName.lastIndexOf('.');
        return (index == -1) ? "" : fileName.substring(index + 1);
    }

    // E. Update subscriber photo
    public Subscriber updateSubscriber(int id, String name, LocalDate startDate, LocalDate endDate, MultipartFile photo) {
        Subscriber subscriber = subscriberRepo.findById(id);

        subscriber.setName(name);
        subscriber.setSubscriptionStart(startDate);
        subscriber.setSubscriptionEnd(endDate);

        // Handle new photo if uploaded
        if (photo != null && !photo.isEmpty()) {
            try {
                // Create uploads folder if missing
                File directory = new File(uploadDir);
                if (!directory.exists()) {
                    directory.mkdirs();
                }

                // Define new file path: uploads/{id}.jpg
                String fileName = id + getExtension(photo.getOriginalFilename());
                Path filePath = Paths.get(uploadDir, fileName);
                subscriber.setPhoto_url(photo.getOriginalFilename());

                // Save new photo
                Files.write(filePath, photo.getBytes());


            } catch (IOException e) {
                throw new RuntimeException("Failed to upload photo: " + e.getMessage());
            }
        }

        return subscriberRepo.save(subscriber);
    }

    // Helper method to keep file extension (e.g. .jpg, .png)
    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "";
        return filename.substring(filename.lastIndexOf("."));
    }
    // G. Renew subscription
    public Subscriber renewSubscription(Integer id, LocalDate newStart, int months, Double price) {
        Subscriber sub = subscriberRepo.findById(id).orElseThrow();
        if(newStart == null){
            newStart = sub.getSubscriptionEnd();
        }
        LocalDate newEnd =newStart.plusMonths(months).minusDays(1);
        HistoricalSubscription history = new HistoricalSubscription();
        history.setSubscriber(sub);
        history.setStartDate(newStart);
        history.setEndDate(newEnd);
        history.setPrice(price);
        historyRepo.save(history);

        // Update new subscription
        sub.setSubscriptionStart(newStart);
        sub.setSubscriptionEnd(newEnd);
        return subscriberRepo.save(sub);
    }

    // H. Entering the subscriber
    public ResponseEntity<String> enterSubscriber(Integer subscriberId) {
        Subscriber sub = subscriberRepo.findById(subscriberId).orElseThrow();
        Attendance attendance = attendanceRepo.findAttendance(subscriberId, LocalDate.now());
        if(attendance != null){
            return ResponseEntity.status(HttpStatus.CONFLICT).body("هذا المشترك سجل دخول اليوم ولم يسجل خروج");
        }
        attendance = new Attendance();
        attendance.setSubscriber(sub);
        attendance.setDate(LocalDate.now());
        attendance.setTimeIn(LocalTime.now().truncatedTo(ChronoUnit.MINUTES));
        attendanceRepo.save(attendance);
        return ResponseEntity.ok("done");
    }

    // I. Exit the subscriber
    public ResponseEntity<String> exitSubscriber(Integer id) {
        Attendance attendance = attendanceRepo.findAttendance(id, LocalDate.now());
        attendance.setTimeOut(LocalTime.now().truncatedTo(ChronoUnit.MINUTES));
        attendanceRepo.save(attendance);
        return ResponseEntity.ok("done");
    }

    public List<Subscriber> searchByName(String name) {
        return subscriberRepo.findByNameContainingIgnoreCase(name);
    }

    // 🔍 Search by ID
    public Subscriber searchById(int id) {
        return subscriberRepo.findById(id);
    }
}
