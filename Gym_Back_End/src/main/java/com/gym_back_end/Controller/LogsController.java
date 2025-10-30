package com.gym_back_end.Controller;

import com.gym_back_end.Models.Logs;
import com.gym_back_end.Service.LogsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/logs")
public class LogsController {
    @Autowired
    private LogsService logsService;

    @GetMapping
    public List<Logs> getAllLogs() {
        List<Logs> logs =  logsService.getAllLogs();
        System.out.println(logs);
        return logs;
    }
}
