package com.example.skincare.repositories;

import com.example.skincare.models.Booking;
import com.example.skincare.models.SkinTherapist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByCustomerId(Long customerId);

    @Query("SELECT b FROM Booking b JOIN FETCH b.customer JOIN FETCH b.service JOIN FETCH b.therapist")
    List<Booking> findAllWithDetails();

    @Query("SELECT b FROM Booking b JOIN FETCH b.customer JOIN FETCH b.service LEFT JOIN FETCH b.therapist LEFT JOIN FETCH b.feedbacks WHERE b.customer.id = :customerId")
    List<Booking> findByCustomerIdWithDetails(@Param("customerId") Long customerId);

    @Query("SELECT b FROM Booking b WHERE b.therapist = :therapist AND b.bookingTime = :bookingTime AND b.status IN :statuses")
    List<Booking> findByTherapistAndBookingTimeAndStatusIn(
            @Param("therapist") SkinTherapist therapist,
            @Param("bookingTime") LocalDateTime bookingTime,
            @Param("statuses") List<String> statuses);
}