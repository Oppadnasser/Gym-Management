package com.gym_back_end.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.gym_back_end.Models.*;
import com.gym_back_end.Service.GymService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/subscribers")

public class GymController {

    private final GymService service;

    @Autowired
    private GymService getService;

    public GymController(GymService service) {
        this.service = service;
    }

    // A
    @GetMapping
    public ResponseEntity<List<Subscriber>> getAllSubscribers() throws IOException {
        List<Subscriber> subscribers = service.getAllSubscribers();
        return ResponseEntity.ok(subscribers);
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
            @RequestParam("subscriber") String subscriberJson,
            @RequestParam(value = "photo", required = false) MultipartFile photo,
            @RequestParam int price
    ) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        Subscriber subscriber = mapper.readValue(subscriberJson, Subscriber.class);
        System.out.println("in new");
        return service.addSubscriber(subscriber, photo, price);
    }

    // E
    @PutMapping("/{id}")
    public ResponseEntity<Subscriber> updateSubscriber(
            @PathVariable int id,
            @RequestParam("name") String name,
            @RequestParam("subscriptionStart") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate subscriptionStart,
            @RequestParam("subscriptionEnd") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate subscriptionEnd,
            @RequestParam(value = "photo", required = false) MultipartFile photo) {

        Subscriber updated = service.updateSubscriber(id, name, subscriptionStart, subscriptionEnd, photo);
        return ResponseEntity.ok(updated);
    }

    // G
    @PutMapping("/renew")
    public Subscriber renewSubscription(
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
        return service.renewSubscription(id, start, months, price);
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
    public List<Subscriber> searchByName(@RequestParam String name) {
        return service.searchByName(name);
    }

    // Search by id
    @GetMapping("/subscribers/search/id")
    public Subscriber searchById(@RequestParam int id) {
        return service.searchById(id);
    }


}
