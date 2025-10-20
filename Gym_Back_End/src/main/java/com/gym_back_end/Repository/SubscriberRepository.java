package com.gym_back_end.Repository;

import com.gym_back_end.Models.Subscriber;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.time.LocalDate;
import java.util.List;

public interface SubscriberRepository extends JpaRepository<Subscriber, Integer> {

    @Query("SELECT s FROM Subscriber s WHERE s.subscriptionEnd BETWEEN :today AND :threeDaysLater")
    List<Subscriber> findSubscribersExpiringSoon(LocalDate today, LocalDate threeDaysLater);

    @Query("SELECT s FROM Subscriber s WHERE s.subscriptionEnd < :today")
    List<Subscriber> findExpiredSubscribers(LocalDate today);

    // 1️⃣ Search by partial name (case insensitive)
    @Query("SELECT s FROM Subscriber s WHERE LOWER(s.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Subscriber> findByNameContainingIgnoreCase(String name);

    // 2️⃣ Find by exact id
    Subscriber findById(int id);
}
