package com.example.skincare.controller;

import com.example.skincare.models.SkinTest;
import com.example.skincare.repositories.SkinTestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/skin-tests")
public class SkinTestController {

    @Autowired
    private SkinTestRepository skinTestRepository;

    // Tạo kết quả trắc nghiệm mới (công khai, vì Guest cũng có thể làm trắc nghiệm)
    @PostMapping
    public SkinTest createSkinTest(@RequestBody SkinTest skinTest) {
        return skinTestRepository.save(skinTest);
    }

    // Lấy danh sách tất cả kết quả trắc nghiệm (chỉ MANAGER hoặc STAFF)
    @GetMapping
    @PreAuthorize("hasRole('MANAGER') or hasRole('STAFF')")
    public List<SkinTest> getAllSkinTests() {
        return skinTestRepository.findAll();
    }

    // Lấy thông tin một kết quả trắc nghiệm theo ID (chỉ MANAGER, STAFF, hoặc CUSTOMER sở hữu)
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER') or hasRole('STAFF') or @securityService.isOwnerOfSkinTest(#id, authentication)")
    public ResponseEntity<SkinTest> getSkinTestById(@PathVariable Long id) {
        Optional<SkinTest> skinTest = skinTestRepository.findById(id);
        return skinTest.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Cập nhật kết quả trắc nghiệm (chỉ MANAGER hoặc STAFF)
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER') or hasRole('STAFF')")
    public ResponseEntity<SkinTest> updateSkinTest(@PathVariable Long id, @RequestBody SkinTest updatedSkinTest) {
        Optional<SkinTest> skinTest = skinTestRepository.findById(id);
        if (skinTest.isPresent()) {
            SkinTest existingSkinTest = skinTest.get();
            existingSkinTest.setAnswers(updatedSkinTest.getAnswers());
            existingSkinTest.setRecommendedService(updatedSkinTest.getRecommendedService());
            return ResponseEntity.ok(skinTestRepository.save(existingSkinTest));
        }
        return ResponseEntity.notFound().build();
    }

    // Xóa kết quả trắc nghiệm (chỉ MANAGER hoặc STAFF)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER') or hasRole('STAFF')")
    public ResponseEntity<Void> deleteSkinTest(@PathVariable Long id) {
        if (skinTestRepository.existsById(id)) {
            skinTestRepository.deleteById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}