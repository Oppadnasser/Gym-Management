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

    @Query("SELECT s FROM Subscriber s WHERE LOWER(s.name) LIKE LOWER(CONCAT('%', :name, '%'))  and s.subscriptionEnd < :today")
    List<Subscriber> findExpiredByNameContainingIgnoreCase(String name , LocalDate today);

    @Query("SELECT s FROM Subscriber s WHERE LOWER(s.name) LIKE LOWER(CONCAT('%', :name, '%'))  and s.subscriptionEnd BETWEEN :today AND :threeDaysLater")
    List<Subscriber> findRenewalByNameContainingIgnoreCase(String name , LocalDate today, LocalDate threeDaysLater);


    // 2️⃣ Find by exact id
    Subscriber findById(int id);

    @Query("SELECT s FROM Subscriber s WHERE s.id = :id  and s.subscriptionEnd < :today")
    Subscriber findExpiredById(int id , LocalDate today);

    @Query("SELECT s FROM Subscriber s WHERE s.id = :id  and s.subscriptionEnd BETWEEN :today AND :threeDaysLater")
    Subscriber findExpiredSoonById(int id , LocalDate today, LocalDate threeDaysLater);

    @Query("SELECT a.id FROM Subscriber a where a.id/10000 >= year(current date ) ORDER BY a.id DESC LIMIT 1")
    Integer findMaxId();

    @Query("SELECT COUNT(*) FROM Subscriber s WHERE s.subscriptionEnd > CURRENT DATE ")
    int countActive();
}
