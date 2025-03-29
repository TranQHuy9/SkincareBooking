package com.example.skincare.controller;

import com.example.skincare.models.Feedback;
import com.example.skincare.repositories.FeedbackRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/feedbacks")
public class FeedbackController {

    @Autowired
    private FeedbackRepository feedbackRepository;

    // Lấy danh sách tất cả phản hồi (chỉ MANAGER hoặc STAFF)
    @GetMapping
    @PreAuthorize("hasRole('MANAGER') or hasRole('STAFF')")
    public List<Feedback> getAllFeedbacks() {
        return feedbackRepository.findAll();
    }

    // Lấy thông tin một phản hồi theo ID (chỉ MANAGER, STAFF, hoặc CUSTOMER sở hữu)
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER') or hasRole('STAFF') or @securityService.isOwnerOfFeedback(#id, authentication)")
    public ResponseEntity<Feedback> getFeedbackById(@PathVariable Long id) {
        Optional<Feedback> feedback = feedbackRepository.findById(id);
        return feedback.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Tạo phản hồi mới (chỉ CUSTOMER)
    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public Feedback createFeedback(@RequestBody Feedback feedback) {
        return feedbackRepository.save(feedback);
    }

    // Cập nhật phản hồi (chỉ CUSTOMER sở hữu)
    @PutMapping("/{id}")
    @PreAuthorize("@securityService.isOwnerOfFeedback(#id, authentication)")
    public ResponseEntity<Feedback> updateFeedback(@PathVariable Long id, @RequestBody Feedback updatedFeedback) {
        Optional<Feedback> feedback = feedbackRepository.findById(id);
        if (feedback.isPresent()) {
            Feedback existingFeedback = feedback.get();
            existingFeedback.setRating(updatedFeedback.getRating());
            existingFeedback.setComment(updatedFeedback.getComment());
            existingFeedback.setCreatedAt(updatedFeedback.getCreatedAt());
            return ResponseEntity.ok(feedbackRepository.save(existingFeedback));
        }
        return ResponseEntity.notFound().build();
    }

    // Xóa phản hồi (chỉ MANAGER hoặc CUSTOMER sở hữu)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER') or @securityService.isOwnerOfFeedback(#id, authentication)")
    public ResponseEntity<Void> deleteFeedback(@PathVariable Long id) {
        if (feedbackRepository.existsById(id)) {
            feedbackRepository.deleteById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}