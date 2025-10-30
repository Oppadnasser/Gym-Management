package com.gym_back_end.Repository;

import com.gym_back_end.Models.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface AttendanceRepository extends JpaRepository<Attendance, Integer> {
    @Query("SELECT a from Attendance a WHERE a.subscriber.id = :id and a.attendance_date = :today and a.time_out IS NULL")
    Attendance findAttendance(int id, LocalDate today);

    @Query("SELECT a from Attendance a WHERE a.subscriber.id = :id order by a.attendance_date desc , a.time_in desc  ")
    List<Attendance> findHistoryOfAttendance(int id);
}

