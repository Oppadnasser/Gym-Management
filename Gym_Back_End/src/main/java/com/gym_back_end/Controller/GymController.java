package com.gym_back_end.Controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.gym_back_end.Models.*;
import com.gym_back_end.Service.GymService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.relational.core.sql.In;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/subscribers")
public class GymController {

    private final GymService service;


    public GymController(GymService service) {
        this.service = service;
    }

    // A
    @GetMapping
    public ResponseEntity<List<Subscriber>> getAllSubscribers() throws IOException {
        List<Subscriber> subscribers = service.getAllSubscribers();
        return ResponseEntity.ok(subscribers);
    }
    @GetMapping("/{id}")
    public ResponseEntity<Map<String,Object>> getSubscriber(@PathVariable Integer id) throws IOException {
        Map<String,Object> response = new HashMap<>();
        Subscriber subscriber = service.searchById(id,"");
        response.put("subscriber",subscriber);
        response.put("attendanceHistory",service.getAttendanceHistory(id));
        response.put("subscriptionHistory",service.getHistoricalSubscriptions(id));

        return ResponseEntity.ok(response);
    }
    // B
    @GetMapping("/expiring-soon")
    public List<Subscriber> getSubscribersExpiringSoon() throws IOException {
        return service.getSubscribersExpiringSoon();
    }

    // C
    @GetMapping("/expired")
    public List<Subscriber> getExpiredSubscribers() throws IOException {
        return service.getExpiredSubscribers();
    }

    // D
    @PostMapping("/new-subscriber")
    public Subscriber addSubscriber(
            @CookieValue(value = "token",required = false) String token,
            @RequestParam("subscriber") String subscriberJson,
            @RequestParam(value = "photo", required = false) MultipartFile photo,
            @RequestParam int price
    ) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        Subscriber subscriber = mapper.readValue(subscriberJson, Subscriber.class);
        System.out.println("in new");
        return service.addSubscriber(token,subscriber, photo, price);
    }

    // E
    @PutMapping("/update")
    public ResponseEntity<Subscriber> updateSubscriber(
            @CookieValue(value = "token",required = false) String token,
            @RequestParam("subscriber") String subscriberJson,
            @RequestParam(value = "photo", required = false) MultipartFile photo) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        Subscriber subscriber = mapper.readValue(subscriberJson, Subscriber.class);
        Subscriber updated = service.updateSubscriber(subscriber,photo,token);
        return ResponseEntity.ok(updated);
    }

    // G
    @PutMapping("/renew")
    public Subscriber renewSubscription(
            @CookieValue(value="token",required = false) String token,
            @RequestParam Integer id,
            @RequestParam Integer months,
            @RequestParam boolean today,
            @RequestParam double price
    ) {
        LocalDate start;

        if(today){
            start = LocalDate.now();
        }
        else {
            start = null;
        }
        return service.renewSubscription(id, start, months, price,token);
    }

    // H
    @PostMapping("/enter")
    public ResponseEntity<String> enterSubscriber(@RequestParam int id) {
        return service.enterSubscriber(id);
    }

    // I
    @PutMapping("/exit")
    public ResponseEntity<String> exitSubscriber(@RequestParam int id) {
        return service.exitSubscriber(id);
    }

    // Search by name
    @GetMapping("/search/name")
    public List<Subscriber> searchByName(@RequestParam String name, @RequestParam String type) throws IOException {
        return service.searchByName(name,type);
    }

    // Search by id
    @GetMapping("/search/id")
    public Subscriber searchById(@RequestParam int id, @RequestParam String type) throws IOException {
        return service.searchById(id,type);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteSubscriber(@CookieValue(value = "token" ,required = false) String token,@PathVariable Integer id) {
        return service.delete(id,token);
    }

    @GetMapping("/active")
    public ResponseEntity<Integer> countActive(){
        return service.countActiveSubscribers();
    }
    @GetMapping("/revenue")
    public ResponseEntity<Map<String,Double>> getRevenue(@RequestParam Integer year, @RequestParam Integer month , @RequestParam Integer day){
        return service.calculateRevenue(year,month,day);
    }
}
