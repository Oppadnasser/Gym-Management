package com.gym_back_end.Service;

import com.gym_back_end.Models.Logs;
import com.gym_back_end.Repository.LogsRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class LogsService {
    @Autowired
    private LogsRepo logsRepo;

    public Logs saveLog(String action, String adminName, String subscriberName, Integer interval, Double price, LocalTime time) {
        Logs log = new Logs();
        log.setAction(action);
        log.setAdminName(adminName);
        log.setSubscriberName(subscriberName);
        log.setInterval_(interval);
        log.setPrice(price);
        log.setDate(LocalDate.now());
        log.setTime(time);
        return logsRepo.save(log);
    }

    public List<Logs> getAllLogs() {
        return logsRepo.findAll();
    }

    public List<Logs> getByDate(LocalDate date){
        return logsRepo.findByDate(date);
    }
}

