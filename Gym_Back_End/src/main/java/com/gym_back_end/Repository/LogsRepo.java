package com.gym_back_end.Repository;

import com.gym_back_end.Models.Logs;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Repository
public interface LogsRepo extends JpaRepository<Logs, Integer> {
    @Query("select l from Logs l where l.date = :theDate order by l.date , l.time desc ")
    List<Logs> findByDate(LocalDate theDate);

    @Query("select l from Logs l where l.date between :startDate and :endDate order by l.date desc ")
    List<Logs> findByMonth(LocalDate startDate, LocalDate endDate);
}
