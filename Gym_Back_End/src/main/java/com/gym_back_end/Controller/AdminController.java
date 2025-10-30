package com.gym_back_end.Controller;

import com.gym_back_end.JwtUtil;
import com.gym_back_end.Models.Admin;
import com.gym_back_end.Models.Logs;
import com.gym_back_end.Service.AdminService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    @Autowired
    private AdminService adminService;

    @Autowired
    private JwtUtil jwtUtil;


    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request, HttpServletResponse response) {
        try {
            String userName = request.get("userName");
            String password = request.get("password");
            Admin admin = adminService.login(userName, password);

            String token = jwtUtil.generateToken(admin.getName(), admin.getUserName());
            Cookie cookie = new Cookie("token", token);
            cookie.setPath("/");// Cookie accessible across your site
            cookie.setHttpOnly(true);                // Prevent JavaScript access (recommended for security)
            cookie.setMaxAge(60 * 60 * 24);               // Cookie expires in 1 hour
            cookie.setSecure(false);// Set to true if using HTTPS
            // Add cookie to the response
            response.addCookie(cookie);
            return ResponseEntity.ok(admin);
        } catch (Exception e) {
            return ResponseEntity.status(401).body("Invalid credentials");
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        Cookie cookie = new Cookie("token", null);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(0); // Expire immediately
        cookie.setSecure(false); // Set true if HTTPS

        response.addCookie(cookie);
        return ResponseEntity.ok("Logged out successfully");
    }
    @GetMapping("/check")
    public ResponseEntity<?> checkAuth(@CookieValue(value = "token", required = false) String token) {
        if (token == null || !jwtUtil.isTokenValid(token)) {
            return ResponseEntity.status(401).body("Not logged in");
        }

        Admin admin = adminService.findByName(jwtUtil.extractAdminName(token));
        if(admin == null){
            return ResponseEntity.status(401).body("Not logged in");
        }
        return ResponseEntity.ok(admin);
    }

    @GetMapping("/logs")
    public ResponseEntity<List<Logs>> getLogs(@RequestParam LocalDate date) {
        return adminService.getLogsByDate(date);
    }

    @PutMapping("/update")
    public ResponseEntity<?> update(@CookieValue(value = "token", required = false) String token,@RequestBody Map<String, String> request) {
        try {
            String userName = request.get("adminName");
            String oldPassword = request.get("oldPassword");
            String newPassword = request.get("newPassword");
            String name = jwtUtil.extractAdminName(token);
            Admin admin = adminService.update(userName,oldPassword,newPassword,name);
            if(admin!=null){
                return ResponseEntity.ok(admin);
            }
            else{
                return ResponseEntity.status(401).body("Invalid credentials");
        }
    }catch (Exception e){
        return ResponseEntity.status(401).body("Invalid credentials");}
    }

    @GetMapping("/show-summary")
    public ResponseEntity<Boolean> showSummary(@CookieValue(value = "token", required = false) String token, @RequestParam String password) {
        if (token == null || !jwtUtil.isTokenValid(token)) {
            return ResponseEntity.status(401).body(false);
        }
        String name =  jwtUtil.extractAdminName(token);
        return adminService.showSummary(name,password);
    }

    @PutMapping("/update-summary-password")
    public ResponseEntity<String> updateSummaryPassword(@CookieValue(value = "token", required = false) String token,@RequestBody Map<String, String> request) {
        if(token == null || !jwtUtil.isTokenValid(token)) {
            return ResponseEntity.status(401).body("Invalid credentials");
        }
        String name = jwtUtil.extractAdminName(token);
        if(name.equals("user")){
            return ResponseEntity.status(401).body("Invalid credentials");
        }
        return adminService.updateSummaryPassword(request.get("oldPassword"), request.get("newPassword"));
    }

    @GetMapping("/admin-names")
    public ResponseEntity<List<String>> getAdminNames(){
        return adminService.getAdminNames();
    }


}

