package com.gym_back_end.Repository;

import com.gym_back_end.Models.Summary;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SummaryRepo extends JpaRepository<Summary,Integer> {
    Summary findById(int id);
}
