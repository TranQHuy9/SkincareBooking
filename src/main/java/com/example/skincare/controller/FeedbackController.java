package com.example.skincare.controller;

import com.example.skincare.models.Booking;
import com.example.skincare.models.Feedback;
import com.example.skincare.repositories.BookingRepository;
import com.example.skincare.repositories.FeedbackRepository;
import com.example.skincare.response.FeedbackDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/feedbacks")
public class FeedbackController {

    @Autowired
    private FeedbackRepository feedbackRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @GetMapping
    @PreAuthorize("hasRole('MANAGER') or hasRole('STAFF')")
    public List<Feedback> getAllFeedbacks() {
        return feedbackRepository.findAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER') or hasRole('STAFF') or @securityService.isOwnerOfFeedback(#id, authentication)")
    public ResponseEntity<Feedback> getFeedbackById(@PathVariable Long id) {
        Optional<Feedback> feedback = feedbackRepository.findById(id);
        return feedback.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<?> createFeedback(@RequestBody FeedbackDTO feedbackDTO) {
        try {
            // Kiểm tra bookingId
            if (feedbackDTO.getBookingId() == null) {
                return ResponseEntity.badRequest().body("Booking ID là bắt buộc.");
            }

            // Kiểm tra rating
            if (feedbackDTO.getRating() == null || feedbackDTO.getRating() < 1.0 || feedbackDTO.getRating() > 5.0) {
                return ResponseEntity.badRequest().body("Rating phải từ 1.0 đến 5.0.");
            }

            // Tìm booking theo ID
            Booking booking = bookingRepository.findById(feedbackDTO.getBookingId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy booking với ID: " + feedbackDTO.getBookingId()));

            // Kiểm tra quyền sở hữu
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String username = userDetails.getUsername();
            if (!booking.getCustomer().getUsername().equals(username)) {
                return ResponseEntity.status(403).body("Bạn không có quyền gửi phản hồi cho booking này.");
            }

            // Tạo Feedback mới
            Feedback feedback = new Feedback();
            feedback.setBooking(booking);
            feedback.setRating(feedbackDTO.getRating());
            feedback.setComment(feedbackDTO.getComment());

            Feedback savedFeedback = feedbackRepository.save(feedback);
            return ResponseEntity.ok(savedFeedback);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi khi gửi phản hồi: " + e.getMessage());
        }
    }

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