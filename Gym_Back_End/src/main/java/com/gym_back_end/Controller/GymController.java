package com.gym_back_end.Controller;

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

@RestController
@RequestMapping("/api/subscribers")
@CrossOrigin(origins = "*")
public class GymController {

    private final GymService service;

    @Autowired
    private GymService getService;

    public GymController(GymService service) {
        this.service = service;
    }

    // A
    @GetMapping
    public List<Subscriber> getAllSubscribers() {
        return service.getAllSubscribers();
    }

    // B
    @GetMapping("/expiring-soon")
    public List<Subscriber> getSubscribersExpiringSoon() {
        return service.getSubscribersExpiringSoon();
    }

    // C
    @GetMapping("/expired")
    public List<Subscriber> getExpiredSubscribers() {
        return service.getExpiredSubscribers();
    }

    // D
    @PostMapping(consumes = {"multipart/form-data"})
    public Subscriber addSubscriber(
            @RequestPart("subscriber") Subscriber subscriber,
            @RequestPart(value = "photo", required = false) MultipartFile photo
    ) throws IOException {
        return service.addSubscriber(subscriber, photo);
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
    @PutMapping("/{id}/renew")
    public Subscriber renewSubscription(
            @PathVariable Integer id,
            @RequestParam LocalDate newStart,
            @RequestParam LocalDate newEnd,
            @RequestParam Double price
    ) {
        return service.renewSubscription(id, newStart, newEnd, price);
    }

    // H
    @PostMapping("/{subscriberId}/enter")
    public Attendance enterSubscriber(@PathVariable Integer subscriberId) {
        return service.enterSubscriber(subscriberId);
    }

    // I
    @PutMapping("/exit/{attendanceId}")
    public Attendance exitSubscriber(@PathVariable Integer attendanceId) {
        return service.exitSubscriber(attendanceId);
    }
}
