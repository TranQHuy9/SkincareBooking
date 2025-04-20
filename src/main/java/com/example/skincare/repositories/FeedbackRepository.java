package com.example.skincare.repositories;

import com.example.skincare.models.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    List<Feedback> findTop5ByOrderByIdDesc();

    @Query("SELECT f FROM Feedback f JOIN FETCH f.booking b JOIN FETCH b.customer")
    List<Feedback> findAllWithBookingAndCustomer();

    @Query("SELECT f FROM Feedback f JOIN FETCH f.booking b JOIN FETCH b.customer ORDER BY f.id DESC")
    List<Feedback> findTop5ByOrderByIdDescWithDetails();

    // Thêm phương thức mới
    @Query("SELECT f FROM Feedback f JOIN FETCH f.booking b JOIN FETCH b.customer WHERE f.booking.id = :bookingId")
    List<Feedback> findByBookingId(Long bookingId);
}