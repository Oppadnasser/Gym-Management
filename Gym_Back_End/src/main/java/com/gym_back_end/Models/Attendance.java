package com.gym_back_end.Models;


import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private LocalDate attendance_date;
    private LocalTime time_in;
    private LocalTime time_out;

    @ManyToOne
    @JoinColumn(name = "subscriber_id")
    private Subscriber subscriber;

    // Getters and setters


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public LocalDate getDate() {
        return attendance_date;
    }

    public void setDate(LocalDate attendance_date) {
        this.attendance_date = attendance_date;
    }

    public LocalTime getTimeIn() {
        return time_in;
    }

    public void setTimeIn(LocalTime time_in) {
        this.time_in = time_in;
    }

    public LocalTime getTimeOut() {
        return time_out;
    }

    public void setTimeOut(LocalTime time_out) {
        this.time_out = time_out;
    }

    public Subscriber getSubscriber() {
        return subscriber;
    }

    public void setSubscriber(Subscriber subscriber) {
        this.subscriber = subscriber;
    }
}
