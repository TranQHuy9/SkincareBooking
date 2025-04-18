package com.example.skincare.repositories;

import com.example.skincare.models.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    List<Feedback> findTop5ByOrderByIdDesc();
}