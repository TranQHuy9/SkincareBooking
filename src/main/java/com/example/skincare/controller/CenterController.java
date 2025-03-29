package com.example.skincare.controller;
import com.example.skincare.models.Center;
import com.example.skincare.repositories.CenterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/centers")
public class CenterController {

    @Autowired
    private CenterRepository centerRepository;

    // Lấy danh sách tất cả trung tâm (công khai)
    @GetMapping
    public List<Center> getAllCenters() {
        return centerRepository.findAll();
    }

    // Lấy thông tin một trung tâm theo ID (công khai)
    @GetMapping("/{id}")
    public ResponseEntity<Center> getCenterById(@PathVariable Long id) {
        Optional<Center> center = centerRepository.findById(id);
        return center.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Tạo trung tâm mới (chỉ MANAGER)
    @PostMapping
    @PreAuthorize("hasRole('MANAGER')")
    public Center createCenter(@RequestBody Center center) {
        return centerRepository.save(center);
    }

    // Cập nhật trung tâm (chỉ MANAGER)
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<Center> updateCenter(@PathVariable Long id, @RequestBody Center updatedCenter) {
        Optional<Center> center = centerRepository.findById(id);
        if (center.isPresent()) {
            Center existingCenter = center.get();
            existingCenter.setName(updatedCenter.getName());
            existingCenter.setAddress(updatedCenter.getAddress());
            existingCenter.setPhone(updatedCenter.getPhone());
            existingCenter.setEmail(updatedCenter.getEmail());
            existingCenter.setOpenTime(updatedCenter.getOpenTime());
            existingCenter.setCloseTime(updatedCenter.getCloseTime());
            existingCenter.setDescription(updatedCenter.getDescription());
            return ResponseEntity.ok(centerRepository.save(existingCenter));
        }
        return ResponseEntity.notFound().build();
    }

    // Xóa trung tâm (chỉ MANAGER)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<Void> deleteCenter(@PathVariable Long id) {
        if (centerRepository.existsById(id)) {
            centerRepository.deleteById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}