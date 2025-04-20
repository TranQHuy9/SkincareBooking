package com.example.skincare.controller;

import com.example.skincare.models.Booking;
import com.example.skincare.models.Feedback;
import com.example.skincare.repositories.BookingRepository;
import com.example.skincare.repositories.FeedbackRepository;
import com.example.skincare.response.BookingResponseDTO;
import com.example.skincare.response.FeedbackResponseDTO;
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
        List<Booking> bookings = bookingRepository.findAllWithDetails();
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
        List<Feedback> recentFeedbacks = feedbackRepository.findTop5ByOrderByIdDescWithDetails();
        List<FeedbackResponseDTO> recentFeedbackData = recentFeedbacks.stream().map(feedback -> {
            FeedbackResponseDTO dto = new FeedbackResponseDTO();
            dto.setId(feedback.getId());
            dto.setBookingId(feedback.getBooking().getId());
            dto.setCustomerUsername(feedback.getBooking().getCustomer().getUsername()); // Changed
            dto.setRating(feedback.getRating());
            dto.setComment(feedback.getComment());
            return dto;
        }).collect(Collectors.toList());
        data.put("recentFeedback", recentFeedbackData);

        // Danh sách bookings cho "Manage Bookings"
        List<BookingResponseDTO> bookingsData = bookings.stream().map(booking -> {
            BookingResponseDTO dto = new BookingResponseDTO();
            dto.setId(booking.getId());
            dto.setCustomerId(booking.getCustomer().getId());
            dto.setCustomerUsername(booking.getCustomer().getUsername()); // Changed
            dto.setServiceId(booking.getService().getId());
            dto.setServiceName(booking.getService().getName());
            dto.setTherapistId(booking.getTherapist() != null ? booking.getTherapist().getId() : null);
            dto.setTherapistFullName(booking.getTherapist() != null ? booking.getTherapist().getFullName() : "Chưa phân công");
            dto.setBookingTime(booking.getBookingTime());
            dto.setStatus(booking.getStatus().toString());
            return dto;
        }).collect(Collectors.toList());
        data.put("bookings", bookingsData);

        return ResponseEntity.ok(data);
    }
}