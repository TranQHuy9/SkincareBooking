package com.example.skincare.controller;

import com.example.skincare.models.SkinTherapist;
import com.example.skincare.repositories.SkinTherapistRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/therapists")
public class SkinTherapistController {

    @Autowired
    private SkinTherapistRepository skinTherapistRepository;

    @GetMapping
    public List<SkinTherapist> getAllTherapists() {
        return skinTherapistRepository.findAll();
    }

    @GetMapping("/by-service")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('MANAGER')")
    public ResponseEntity<List<SkinTherapist>> getTherapistsByService(@RequestParam Long serviceId) {
        List<SkinTherapist> therapists = skinTherapistRepository.findByServicesId(serviceId);
        return ResponseEntity.ok(therapists);
    }

    @PostMapping
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<SkinTherapist> createTherapist(@RequestBody SkinTherapist therapist) {
        SkinTherapist savedTherapist = skinTherapistRepository.save(therapist);
        return ResponseEntity.status(201).body(savedTherapist);
    }
}