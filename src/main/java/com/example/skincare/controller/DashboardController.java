package com.example.skincare.controller;

import com.example.skincare.models.Booking;
import com.example.skincare.models.Feedback;
import com.example.skincare.repositories.BookingRepository;
import com.example.skincare.repositories.FeedbackRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private FeedbackRepository feedbackRepository;

    @GetMapping
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<Map<String, Object>> getDashboardData() {
        Map<String, Object> data = new HashMap<>();

        // Tổng số booking
        long totalBookings = bookingRepository.count();
        data.put("totalBookings", totalBookings);

        // Tổng doanh thu (tính từ giá dịch vụ của các booking)
        List<Booking> bookings = bookingRepository.findAll();
        double totalRevenue = bookings.stream()
                .mapToDouble(booking -> booking.getService().getPrice())
                .sum();
        data.put("totalRevenue", totalRevenue);

        // Điểm đánh giá trung bình
        List<Feedback> feedbacks = feedbackRepository.findAll();
        double averageRating = feedbacks.stream()
                .mapToDouble(Feedback::getRating)
                .average()
                .orElse(0.0);
        data.put("averageRating", averageRating);

        // Phản hồi gần đây (lấy 5 phản hồi mới nhất)
        List<Feedback> recentFeedbacks = feedbackRepository.findTop5ByOrderByIdDesc();
        List<Map<String, Object>> recentFeedbackData = recentFeedbacks.stream().map(feedback -> {
            Map<String, Object> feedbackData = new HashMap<>();
            feedbackData.put("bookingId", feedback.getBooking().getId());
            feedbackData.put("customer", feedback.getBooking().getCustomer().getUsername());
            feedbackData.put("rating", feedback.getRating());
            feedbackData.put("feedback", feedback.getComment());
            return feedbackData;
        }).collect(Collectors.toList());
        data.put("recentFeedback", recentFeedbackData);

        return ResponseEntity.ok(data);
    }
}