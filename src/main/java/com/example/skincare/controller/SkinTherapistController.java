package com.example.skincare.controller;
import com.example.skincare.models.SkinTherapist;
import com.example.skincare.repositories.SkinTherapistRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/therapists")
public class SkinTherapistController {

    @Autowired
    private SkinTherapistRepository skinTherapistRepository;

    // Lấy danh sách tất cả chuyên viên (công khai)
    @GetMapping
    public List<SkinTherapist> getAllTherapists() {
        return skinTherapistRepository.findAll();
    }

    // Lấy thông tin một chuyên viên theo ID (công khai)
    @GetMapping("/{id}")
    public ResponseEntity<SkinTherapist> getTherapistById(@PathVariable Long id) {
        Optional<SkinTherapist> therapist = skinTherapistRepository.findById(id);
        return therapist.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Tạo chuyên viên mới (chỉ MANAGER)
    @PostMapping
    @PreAuthorize("hasRole('MANAGER')")
    public SkinTherapist createTherapist(@RequestBody SkinTherapist therapist) {
        return skinTherapistRepository.save(therapist);
    }

    // Cập nhật chuyên viên (chỉ MANAGER hoặc chính THERAPIST đó)
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER') or hasRole('THERAPIST')")
    public ResponseEntity<SkinTherapist> updateTherapist(@PathVariable Long id, @RequestBody SkinTherapist updatedTherapist) {
        Optional<SkinTherapist> therapist = skinTherapistRepository.findById(id);
        if (therapist.isPresent()) {
            SkinTherapist existingTherapist = therapist.get();
            existingTherapist.setFullName(updatedTherapist.getFullName());
            existingTherapist.setExpertise(updatedTherapist.getExpertise());
            existingTherapist.setExperience(updatedTherapist.getExperience());
            return ResponseEntity.ok(skinTherapistRepository.save(existingTherapist));
        }
        return ResponseEntity.notFound().build();
    }

    // Xóa chuyên viên (chỉ MANAGER)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<Void> deleteTherapist(@PathVariable Long id) {
        if (skinTherapistRepository.existsById(id)) {
            skinTherapistRepository.deleteById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}
