package com.gym_back_end.Repository;

import com.gym_back_end.Models.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttendanceRepository extends JpaRepository<Attendance, Integer> {}

