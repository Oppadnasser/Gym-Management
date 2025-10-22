package com.gym_back_end.Repository;

import com.gym_back_end.Models.Attendance;
import com.gym_back_end.Models.HistoricalSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface HistoricalSubscriptionRepository extends JpaRepository<HistoricalSubscription, Integer> {
    @Query("SELECT a from HistoricalSubscription a WHERE a.subscriber.id = :id order by a.start_date desc")
    List<HistoricalSubscription> findHistoryOfSub(int id);

}
