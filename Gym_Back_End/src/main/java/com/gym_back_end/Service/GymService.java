package com.gym_back_end.Service;

import com.gym_back_end.JwtUtil;
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
import java.util.*;

@Service
public class GymService {
    private final LogsService logsService;

    private final SubscriberRepository subscriberRepo;
    private final AttendanceRepository attendanceRepo;
    private final JwtUtil jwtUtil;
    private int numberSubscribers;
    private final HistoricalSubscriptionRepository historyRepo;;
    @Value("${file.upload-dir}")
    private String uploadDir;

    public GymService(SubscriberRepository subscriberRepo, AttendanceRepository attendanceRepo, HistoricalSubscriptionRepository historyRepo, LogsService logsService, JwtUtil jwtUtil) {
        this.subscriberRepo = subscriberRepo;
        this.attendanceRepo = attendanceRepo;
        this.historyRepo = historyRepo;
        this.logsService = logsService;
        Integer num = subscriberRepo.findMaxId();
        this.numberSubscribers = (num == null) ? 0 : num ;
        if(this.numberSubscribers > 1){
            this.numberSubscribers = this.numberSubscribers-(LocalDate.now().getYear()*10000);
        }
        this.jwtUtil = jwtUtil;
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
    public Subscriber addSubscriber(String token,Subscriber subscriber, MultipartFile photoFile , int price) throws IOException {
        // 1. Save subscriber first (without photo)
        if(numberSubscribers == 0){
            subscriber.setId(1);
        }
        else{
            subscriber.setId((LocalDate.now().getYear()*10000)+ numberSubscribers + 1);
        }
        subscriber.setSubscriptionEnd(subscriber.getSubscriptionEnd().minusDays(1));
        Subscriber saved = subscriberRepo.save(subscriber);
        numberSubscribers++;
        HistoricalSubscription historicalSubscription = new HistoricalSubscription();
        historicalSubscription.setPayDate(LocalDate.now());
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
        String adminName = jwtUtil.extractAdminName(token);
        int interval = (int)  Math.ceil(ChronoUnit.MONTHS.between(subscriber.getSubscriptionStart(), subscriber.getSubscriptionEnd()));
        logsService.saveLog("أضاف مشترك", adminName, subscriber.getName(), interval+1,(double) price,LocalTime.now());
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
    public Subscriber updateSubscriber(Subscriber subscriber1,MultipartFile photo, String token) throws IOException {
        Subscriber subscriber = subscriberRepo.findById(subscriber1.getId());
        if(subscriber == null){
            return null;
        }


        // Handle new photo if uploaded
        if (photo != null && !photo.isEmpty()) {
            try {
                // Create uploads folder if missing
                File directory = new File(uploadDir);
                if (!directory.exists()) {
                    directory.mkdirs();
                }

                // Define new file path: uploads/{id}.jpg
                String fileName = subscriber1.getId() + getExtension(photo.getOriginalFilename());
                Path filePath = Paths.get(uploadDir, fileName);
                subscriber1.setPhoto_url(fileName);


                // Save new photo
                Files.write(filePath, photo.getBytes());
                logsService.saveLog("تحديث الصورة",jwtUtil.extractAdminName(token),subscriber1.getName(),0,0.0,LocalTime.now());
                subscriberRepo.save(subscriber1);

            } catch (IOException e) {
                throw new RuntimeException("Failed to upload photo: " + e.getMessage());
            }
        } else{
                subscriber1.setPhoto_url(subscriber.getPhoto_url());
            }

        if(!Objects.equals(subscriber.getPhone(), subscriber1.getPhone())){
            logsService.saveLog("تحديث الرقم",jwtUtil.extractAdminName(token),subscriber1.getName(),0,0.0,LocalTime.now());
        }
        if(!subscriber.getSubscriptionEnd().equals(subscriber1.getSubscriptionEnd()) ){
            logsService.saveLog("تحديث تاريخ انتهاء الاشتراك",jwtUtil.extractAdminName(token),subscriber1.getName(),0,0.0,LocalTime.now());
        }
        if(!subscriber.getSubscriptionStart().equals(subscriber1.getSubscriptionStart())){
            logsService.saveLog("تحديث تاريخ بداية الاشتراك",jwtUtil.extractAdminName(token),subscriber1.getName(),0,0.0,LocalTime.now());
        }
        if(subscriber.getAge() != subscriber1.getAge()){
            logsService.saveLog("تحديث السن",jwtUtil.extractAdminName(token),subscriber1.getName(),0,0.0,LocalTime.now());
        }
        if(!Objects.equals(subscriber.getName(), subscriber1.getName())){
            logsService.saveLog("تحديث الاسم",jwtUtil.extractAdminName(token),subscriber1.getName(),0,0.0,LocalTime.now());
        }
        subscriberRepo.save(subscriber1);
        Path path = Paths.get("uploads/"+subscriber1.getPhoto_url());
        byte[] bytes = Files.readAllBytes(path);
        subscriber1.setPhoto_url(Base64.getEncoder().encodeToString(bytes));
        return subscriber1;
    }

    // Helper method to keep file extension (e.g. .jpg, .png)
    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "";
        return filename.substring(filename.lastIndexOf("."));
    }
    // G. Renew subscription
    public Subscriber renewSubscription(Integer id, LocalDate newStart, int months, Double price, String token) {
        Subscriber sub = subscriberRepo.findById(id).orElseThrow();
        if(newStart == null){
            newStart = sub.getSubscriptionEnd().plusDays(1);
        }
        LocalDate newEnd =newStart.plusMonths(months).minusDays(1);
        HistoricalSubscription history = new HistoricalSubscription();
        history.setSubscriber(sub);
        history.setStartDate(newStart);
        history.setPayDate(LocalDate.now());
        history.setEndDate(newEnd);
        history.setPrice(price);
        logsService.saveLog("تجديد الاشتراك",jwtUtil.extractAdminName(token),sub.getName(),months,price,LocalTime.now());
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
        if(attendance == null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("هذا اللاعب لم يسجل دخول اليوم او سجل دخول وخروج");
        }
        attendance.setTimeOut(LocalTime.now().truncatedTo(ChronoUnit.MINUTES));
        attendanceRepo.save(attendance);
        return ResponseEntity.ok("done");
    }

    public List<Subscriber> searchByName(String name, String searchType) throws IOException {
        return switch (searchType) {
            case "expired" -> attachPicture(subscriberRepo.findExpiredByNameContainingIgnoreCase(name, LocalDate.now()));
            case "renew" -> attachPicture(subscriberRepo.findRenewalByNameContainingIgnoreCase(name, LocalDate.now(),LocalDate.now().plusDays(3)));
            default -> attachPicture(subscriberRepo.findByNameContainingIgnoreCase(name));
        };
    }

    // 🔍 Search by ID
    public Subscriber searchById(int id, String searchType) throws IOException {
        Subscriber subscriber = null;
        switch (searchType){
            case "expired": subscriber = subscriberRepo.findExpiredById(id, LocalDate.now()); break;
            case "renew": subscriber = subscriberRepo.findExpiredSoonById(id,LocalDate.now(), LocalDate.now().plusDays(3));break;
            default: subscriber = subscriberRepo.findById(id);
        }
        if(subscriber == null){
            return null;
        }
        Path path = Paths.get("uploads/"+subscriber.getPhoto_url());
        byte[] bytes = Files.readAllBytes(path);
        subscriber.setPhoto_url(Base64.getEncoder().encodeToString(bytes));
        return subscriber;
    }

    public List<Attendance> getAttendanceHistory(int id) {
        return attendanceRepo.findHistoryOfAttendance(id);
    }

    public List<HistoricalSubscription> getHistoricalSubscriptions(int id) {
        return historyRepo.findHistoryOfSub(id);
    }

    public ResponseEntity<String> delete(int id,String token){
        Subscriber subscriber1 =  subscriberRepo.findById(id);
        String fileName = subscriber1.getId() + ".jpg";
        File file = new File(uploadDir,fileName);
        if(!file.exists()){
            subscriberRepo.deleteById(id);
            logsService.saveLog("حذف مشترك",jwtUtil.extractAdminName(token),subscriber1.getName(),0,0.0,LocalTime.now());
            return ResponseEntity.ok("done");
        }
       if( file.delete()){
           subscriberRepo.deleteById(id);
           logsService.saveLog("حذف مشترك",jwtUtil.extractAdminName(token),subscriber1.getName(),0,0.0,LocalTime.now());
           return ResponseEntity.ok("done");
       }
       return ResponseEntity.badRequest().body("error");
    }

    public ResponseEntity<Integer> countActiveSubscribers(){
        return ResponseEntity.ok(subscriberRepo.countActive());
    }

    public ResponseEntity<Map<String,Double>> calculateRevenue(int year, int month, int day){
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate dayDate = LocalDate.of(year, month, day);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
        Map<String,Double> map = new HashMap<>();
        map.put("monthRevenue",historyRepo.getRevenue(startDate,endDate));
        map.put("dayRevenue",historyRepo.getDayRevenue(dayDate));
        return ResponseEntity.ok(map);
    }
}
