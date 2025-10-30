package com.gym_back_end;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtAuthFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String token = null;

        // Get token from cookies
        if (request.getCookies() != null) {
            token = Arrays.stream(request.getCookies())
                    .filter(c -> c.getName().equals("token"))
                    .findFirst()
                    .map(Cookie::getValue)
                    .orElse(null);
        }

        // Validate token
        if (token != null && jwtUtil.isTokenValid(token)) {
            // You can extract info here if needed:
            String adminName = jwtUtil.extractAdminName(token);
            request.setAttribute("adminName", adminName);
        } else if (requiresAuth(request)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Unauthorized or Invalid Token");
            return;
        }

        filterChain.doFilter(request, response);
    }

    // Allow public endpoints like login without token
    private boolean requiresAuth(HttpServletRequest request) {
        String path = request.getRequestURI();
        return !path.startsWith("/api/admin/login") && !path.startsWith("/public");
    }
}

