package com.example.skincare.repositories;

import com.example.skincare.models.SkinTherapist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface SkinTherapistRepository extends JpaRepository<SkinTherapist, Long> {
    @Query("SELECT t FROM SkinTherapist t JOIN t.services s WHERE s.id = :serviceId")
    List<SkinTherapist> findByServicesId(@Param("serviceId") Long serviceId);

    @Query("SELECT t FROM SkinTherapist t JOIN t.services s WHERE s.id = :serviceId AND t.id NOT IN (SELECT b.therapist.id FROM Booking b WHERE b.bookingTime = :bookingTime AND b.status IN ('PENDING', 'CHECKED_IN'))")
    List<SkinTherapist> findAvailableByServiceIdAndTime(
            @Param("serviceId") Long serviceId,
            @Param("bookingTime") LocalDateTime bookingTime);
}