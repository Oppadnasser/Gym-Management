package com.gym_back_end.Repository;

import com.gym_back_end.Models.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AdminRepo extends JpaRepository<Admin, Integer> {
    Optional<Admin> findByUserName(String userName);
//    @Query("SELECT a from Admin a where a.name = :name")
    Admin findByName(String name);
    @Query("select a.userName from Admin a")
    List<String> findAllUserNames();
}

