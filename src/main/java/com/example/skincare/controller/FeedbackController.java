package com.example.skincare.controller;

import com.example.skincare.models.Booking;
import com.example.skincare.models.Feedback;
import com.example.skincare.repositories.BookingRepository;
import com.example.skincare.repositories.FeedbackRepository;
import com.example.skincare.response.FeedbackDTO;
import com.example.skincare.response.FeedbackResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/feedbacks")
public class FeedbackController {

    @Autowired
    private FeedbackRepository feedbackRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @GetMapping
    @PreAuthorize("hasRole('MANAGER') or hasRole('STAFF')")
    public List<FeedbackResponseDTO> getAllFeedbacks() {
        List<Feedback> feedbacks = feedbackRepository.findAllWithBookingAndCustomer();
        return feedbacks.stream().map(feedback -> {
            FeedbackResponseDTO dto = new FeedbackResponseDTO();
            dto.setId(feedback.getId());
            dto.setBookingId(feedback.getBooking().getId());
            dto.setCustomerUsername(feedback.getBooking().getCustomer().getUsername());
            dto.setRating(feedback.getRating());
            dto.setComment(feedback.getComment());
            return dto;
        }).collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER') or hasRole('STAFF') or @securityService.isOwnerOfFeedback(#id, authentication)")
    public ResponseEntity<FeedbackResponseDTO> getFeedbackById(@PathVariable Long id) {
        Optional<Feedback> feedback = feedbackRepository.findById(id);
        if (feedback.isPresent()) {
            Feedback f = feedback.get();
            FeedbackResponseDTO dto = new FeedbackResponseDTO();
            dto.setId(f.getId());
            dto.setBookingId(f.getBooking().getId());
            dto.setCustomerUsername(f.getBooking().getCustomer().getUsername());
            dto.setRating(f.getRating());
            dto.setComment(f.getComment());
            return ResponseEntity.ok(dto);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/booking/{bookingId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<List<FeedbackResponseDTO>> getFeedbacksByBookingId(@PathVariable Long bookingId) {
        // Kiểm tra quyền sở hữu booking
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String username = userDetails.getUsername();

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy booking với ID: " + bookingId));
        if (!booking.getCustomer().getUsername().equals(username)) {
            return ResponseEntity.status(403).body(null);
        }

        List<Feedback> feedbacks = feedbackRepository.findByBookingId(bookingId);
        List<FeedbackResponseDTO> feedbackDTOs = feedbacks.stream().map(feedback -> {
            FeedbackResponseDTO dto = new FeedbackResponseDTO();
            dto.setId(feedback.getId());
            dto.setBookingId(feedback.getBooking().getId());
            dto.setCustomerUsername(feedback.getBooking().getCustomer().getUsername());
            dto.setRating(feedback.getRating());
            dto.setComment(feedback.getComment());
            return dto;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(feedbackDTOs);
    }

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<?> createFeedback(@RequestBody FeedbackDTO feedbackDTO) {
        try {
            if (feedbackDTO.getBookingId() == null) {
                return ResponseEntity.badRequest().body("Booking ID là bắt buộc.");
            }
            if (feedbackDTO.getRating() == null || feedbackDTO.getRating() < 1.0 || feedbackDTO.getRating() > 5.0) {
                return ResponseEntity.badRequest().body("Rating phải từ 1.0 đến 5.0.");
            }
            Booking booking = bookingRepository.findById(feedbackDTO.getBookingId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy booking với ID: " + feedbackDTO.getBookingId()));
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String username = userDetails.getUsername();
            if (!booking.getCustomer().getUsername().equals(username)) {
                return ResponseEntity.status(403).body("Bạn không có quyền gửi phản hồi cho booking này.");
            }
            Feedback feedback = new Feedback();
            feedback.setBooking(booking);
            feedback.setRating(feedbackDTO.getRating());
            feedback.setComment(feedbackDTO.getComment());
            Feedback savedFeedback = feedbackRepository.save(feedback);
            FeedbackResponseDTO responseDTO = new FeedbackResponseDTO();
            responseDTO.setId(savedFeedback.getId());
            responseDTO.setBookingId(savedFeedback.getBooking().getId());
            responseDTO.setCustomerUsername(savedFeedback.getBooking().getCustomer().getUsername());
            responseDTO.setRating(savedFeedback.getRating());
            responseDTO.setComment(savedFeedback.getComment());
            return ResponseEntity.ok(responseDTO);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi khi gửi phản hồi: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("@securityService.isOwnerOfFeedback(#id, authentication)")
    public ResponseEntity<FeedbackResponseDTO> updateFeedback(@PathVariable Long id, @RequestBody FeedbackDTO feedbackDTO) {
        Optional<Feedback> feedback = feedbackRepository.findById(id);
        if (feedback.isPresent()) {
            Feedback existingFeedback = feedback.get();
            existingFeedback.setRating(feedbackDTO.getRating());
            existingFeedback.setComment(feedbackDTO.getComment());
            Feedback updatedFeedback = feedbackRepository.save(existingFeedback);
            FeedbackResponseDTO dto = new FeedbackResponseDTO();
            dto.setId(updatedFeedback.getId());
            dto.setBookingId(updatedFeedback.getBooking().getId());
            dto.setCustomerUsername(updatedFeedback.getBooking().getCustomer().getUsername());
            dto.setRating(updatedFeedback.getRating());
            dto.setComment(updatedFeedback.getComment());
            return ResponseEntity.ok(dto);
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