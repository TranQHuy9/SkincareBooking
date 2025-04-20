package com.example.skincare.controller;

import com.example.skincare.models.Booking;
import com.example.skincare.models.Service;
import com.example.skincare.models.SkinTherapist;
import com.example.skincare.models.User;
import com.example.skincare.repositories.BookingRepository;
import com.example.skincare.repositories.ServiceRepository;
import com.example.skincare.repositories.SkinTherapistRepository;
import com.example.skincare.repositories.UserRepository;
import com.example.skincare.response.BookingResponseDTO;
import com.example.skincare.response.BookingDTO;
import com.example.skincare.response.FeedbackResponseDTO;
import com.example.skincare.response.SkinTherapistDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class BookingController {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private SkinTherapistRepository skinTherapistRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    @GetMapping("/bookings")
    @PreAuthorize("hasRole('CUSTOMER')or hasRole('MANAGER')")
    public ResponseEntity<List<BookingResponseDTO>> getBookings() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User customer = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Booking> bookings = bookingRepository.findByCustomerIdWithDetails(customer.getId());
        List<BookingResponseDTO> bookingDTOs = bookings.stream().map(booking -> {
            BookingResponseDTO dto = new BookingResponseDTO();
            dto.setId(booking.getId());
            dto.setCustomerId(booking.getCustomer().getId());
            dto.setCustomerUsername(booking.getCustomer().getUsername());
            dto.setServiceId(booking.getService() != null ? booking.getService().getId() : null);
            dto.setServiceName(booking.getService() != null ? booking.getService().getName() : "N/A");
            dto.setTherapistId(booking.getTherapist() != null ? booking.getTherapist().getId() : null);
            dto.setTherapistFullName(booking.getTherapist() != null ? booking.getTherapist().getFullName() : "No preference");
            dto.setBookingTime(booking.getBookingTime());
            dto.setStatus(booking.getStatus());

            // Ánh xạ feedbacks
            List<FeedbackResponseDTO> feedbackDTOs = booking.getFeedbacks().stream().map(feedback -> {
                FeedbackResponseDTO feedbackDTO = new FeedbackResponseDTO();
                feedbackDTO.setId(feedback.getId());
                feedbackDTO.setBookingId(feedback.getBooking().getId());
                feedbackDTO.setCustomerUsername(feedback.getBooking().getCustomer().getUsername());
                feedbackDTO.setRating(feedback.getRating());
                feedbackDTO.setComment(feedback.getComment());
                return feedbackDTO;
            }).collect(Collectors.toList());
            dto.setFeedbacks(feedbackDTOs);

            return dto;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(bookingDTOs);
    }

    @GetMapping("/bookings/all")
    @PreAuthorize("hasRole('MANAGER') or hasRole('STAFF')")
    public ResponseEntity<List<Booking>> getAllBookings(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails userDetails)) {
            return ResponseEntity.status(401).build();
        }

        List<Booking> bookings = bookingRepository.findAll();
        return ResponseEntity.ok(bookings);
    }

    @PostMapping("/bookings")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<?> createBooking(@RequestBody BookingDTO bookingDTO) {
        try {
            System.out.println("Received booking DTO: " + bookingDTO);

            if (bookingDTO.getServiceId() == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("ID dịch vụ là bắt buộc.");
            }
            if (bookingDTO.getBookingTime() == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Thời gian đặt lịch là bắt buộc.");
            }
            if (bookingDTO.getStatus() == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Trạng thái là bắt buộc.");
            }

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Người dùng chưa được xác thực. Vui lòng đăng nhập.");
            }

            String username = authentication.getName();
            User customer = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng: " + username));

            Booking booking = new Booking();
            booking.setCustomer(customer);
            Service service = serviceRepository.findById(bookingDTO.getServiceId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy dịch vụ với ID: " + bookingDTO.getServiceId()));
            booking.setService(service);

            // Tìm chuyên viên khả dụng
            List<SkinTherapist> availableTherapists = skinTherapistRepository.findAvailableByServiceIdAndTime(
                    bookingDTO.getServiceId(), bookingDTO.getBookingTime());
            if (availableTherapists.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Không có chuyên viên nào khả dụng tại thời điểm: " + bookingDTO.getBookingTime());
            }

            // Gán chuyên viên
            if (bookingDTO.getTherapistId() != null) {
                SkinTherapist selectedTherapist = availableTherapists.stream()
                        .filter(t -> t.getId().equals(bookingDTO.getTherapistId()))
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException("Chuyên viên không khả dụng tại thời điểm này."));
                booking.setTherapist(selectedTherapist);
            } else {
                // Gán ngẫu nhiên một chuyên viên khả dụng
                SkinTherapist randomTherapist = availableTherapists.get(new Random().nextInt(availableTherapists.size()));
                booking.setTherapist(randomTherapist);
            }

            booking.setBookingTime(bookingDTO.getBookingTime());
            booking.setStatus(bookingDTO.getStatus());

            Booking savedBooking = bookingRepository.save(booking);

            // Tạo BookingResponseDTO để trả về
            BookingResponseDTO responseDTO = new BookingResponseDTO();
            responseDTO.setId(savedBooking.getId());
            responseDTO.setCustomerId(savedBooking.getCustomer().getId());
            responseDTO.setCustomerUsername(savedBooking.getCustomer().getUsername()); // Changed
            responseDTO.setServiceId(savedBooking.getService().getId());
            responseDTO.setServiceName(savedBooking.getService().getName());
            responseDTO.setTherapistId(savedBooking.getTherapist() != null ? savedBooking.getTherapist().getId() : null);
            responseDTO.setTherapistFullName(savedBooking.getTherapist() != null ? savedBooking.getTherapist().getFullName() : "Chưa phân công");
            responseDTO.setBookingTime(savedBooking.getBookingTime());
            responseDTO.setStatus(savedBooking.getStatus().toString());

            return ResponseEntity.ok(responseDTO);
        } catch (Exception e) {
            System.out.println("Lỗi khi tạo đặt lịch: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Lỗi khi tạo đặt lịch: " + e.getMessage());
        }
    }

    @PostMapping("/bookings/{bookingId}/checkin")
    @PreAuthorize("hasRole('MANAGER') or hasRole('STAFF')")
    public ResponseEntity<String> checkIn(@PathVariable Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đặt lịch"));
        booking.setStatus("CHECKED_IN");
        bookingRepository.save(booking);
        return ResponseEntity.ok("Check-in thành công");
    }

    @PostMapping("/bookings/{bookingId}/checkout")
    @PreAuthorize("hasRole('MANAGER') or hasRole('STAFF')")
    public ResponseEntity<String> checkOut(@PathVariable Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đặt lịch"));
        booking.setStatus("COMPLETED");
        bookingRepository.save(booking);
        // Đã bỏ phần xóa dịch vụ khỏi chuyên viên
        return ResponseEntity.ok("Check-out thành công");
    }

    @PostMapping("/bookings/{bookingId}/cancel")
    @PreAuthorize("hasRole('MANAGER') or hasRole('STAFF')")
    public ResponseEntity<String> cancelBooking(@PathVariable Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đặt lịch"));
        booking.setStatus("CANCELLED");
        bookingRepository.save(booking);
        return ResponseEntity.ok("Đặt lịch đã được hủy");
    }

    @GetMapping("/therapists/by-service")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<List<SkinTherapistDTO>> getTherapistsByService(
            @RequestParam Long serviceId,
            @RequestParam String bookingTime) {
        try {
            System.out.println("Fetching therapists for serviceId: " + serviceId + ", bookingTime: " + bookingTime);
            LocalDateTime bookingDateTime = LocalDateTime.parse(bookingTime);
            List<SkinTherapist> therapists = skinTherapistRepository.findAvailableByServiceIdAndTime(serviceId, bookingDateTime);
            System.out.println("Found therapists: " + therapists);
            List<SkinTherapistDTO> dtos = therapists.stream().map(therapist -> {
                SkinTherapistDTO dto = new SkinTherapistDTO();
                dto.setId(therapist.getId());
                dto.setFullName(therapist.getFullName());
                dto.setExpertise(therapist.getExpertise());
                return dto;
            }).collect(Collectors.toList());
            return ResponseEntity.ok(dtos);
        } catch (DateTimeParseException e) {
            System.out.println("Invalid bookingTime format: " + bookingTime);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Collections.singletonList(new SkinTherapistDTO()));
        }
    }
}