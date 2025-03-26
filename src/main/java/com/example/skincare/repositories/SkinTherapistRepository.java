package com.example.skincare.repositories;
import com.example.skincare.models.SkinTherapist;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SkinTherapistRepository extends JpaRepository<SkinTherapist, Long> {
}