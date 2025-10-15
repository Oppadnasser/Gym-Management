package com.gym_back_end.Service;

import com.gym_back_end.Models.*;
import com.gym_back_end.Repository.*;
import org.springframework.beans.factory.annotation.Value;
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
    public List<Subscriber> getAllSubscribers() {
        return subscriberRepo.findAll();
    }

    // B. Subscribers expiring soon
    public List<Subscriber> getSubscribersExpiringSoon() {
        LocalDate today = LocalDate.now();
        LocalDate threeDaysLater = today.plusDays(3);
        return subscriberRepo.findSubscribersExpiringSoon(today, threeDaysLater);
    }

    // C. Expired subscribers
    public List<Subscriber> getExpiredSubscribers() {
        return subscriberRepo.findExpiredSubscribers(LocalDate.now());
    }

    // D. Add new subscriber (with photo)
    public Subscriber addSubscriber(Subscriber subscriber, MultipartFile photoFile) throws IOException {
        // 1. Save subscriber first (without photo)
        Subscriber saved = subscriberRepo.save(subscriber);

        if (photoFile != null && !photoFile.isEmpty()) {
            // 2. Create upload directory if it doesn't exist
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // 3. Build file path (e.g., uploads/5.jpg)
            String fileExtension = getFileExtension(photoFile.getOriginalFilename());
            String newFileName = saved.getId() + (fileExtension.isEmpty() ? "" : "." + fileExtension);
            Path filePath = uploadPath.resolve(newFileName);

            // 4. Save the file
            Files.copy(photoFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // 5. Update subscriber’s photo URL
            saved.setPhotoUrl("uploads/" + newFileName);
            subscriberRepo.save(saved);
        }

        return saved;
    }

    private String getFileExtension(String fileName) {
        if (fileName == null) return "";
        int index = fileName.lastIndexOf('.');
        return (index == -1) ? "" : fileName.substring(index + 1);
    }

    // E. Update subscriber photo
    public Subscriber updateSubscriber(int id, String name, LocalDate startDate, LocalDate endDate, MultipartFile photo) {
        Subscriber subscriber = subscriberRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Subscriber not found"));

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

                // Delete old photo if exists
                if (subscriber.getPhotoUrl() != null) {
                    Path oldPath = Paths.get(subscriber.getPhotoUrl());
                    Files.deleteIfExists(oldPath);
                }

                // Save new photo
                Files.write(filePath, photo.getBytes());

                // Update photo path in DB
                subscriber.setPhotoUrl(filePath.toString());

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
    public Subscriber renewSubscription(Integer id, LocalDate newStart, LocalDate newEnd, Double price) {
        Subscriber sub = subscriberRepo.findById(id).orElseThrow();
        // Save old subscription to history
        HistoricalSubscription history = new HistoricalSubscription();
        history.setSubscriber(sub);
        history.setStartDate(sub.getSubscriptionStart());
        history.setEndDate(sub.getSubscriptionEnd());
        history.setPrice(price);
        historyRepo.save(history);

        // Update new subscription
        sub.setSubscriptionStart(newStart);
        sub.setSubscriptionEnd(newEnd);
        return subscriberRepo.save(sub);
    }

    // H. Entering the subscriber
    public Attendance enterSubscriber(Integer subscriberId) {
        Subscriber sub = subscriberRepo.findById(subscriberId).orElseThrow();
        Attendance attendance = new Attendance();
        attendance.setSubscriber(sub);
        attendance.setDate(LocalDate.now());
        attendance.setTimeIn(LocalTime.now());
        return attendanceRepo.save(attendance);
    }

    // I. Exit the subscriber
    public Attendance exitSubscriber(Integer attendanceId) {
        Attendance attendance = attendanceRepo.findById(attendanceId).orElseThrow();
        attendance.setTimeOut(LocalTime.now());
        return attendanceRepo.save(attendance);
    }
}
