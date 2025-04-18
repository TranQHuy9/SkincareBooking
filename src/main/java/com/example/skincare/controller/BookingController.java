package com.example.skincare.controller;

import com.example.skincare.models.Booking;
import com.example.skincare.models.SkinTherapist;
import com.example.skincare.models.User;
import com.example.skincare.repositories.BookingRepository;
import com.example.skincare.repositories.ServiceRepository;
import com.example.skincare.repositories.SkinTherapistRepository;
import com.example.skincare.repositories.UserRepository;
import com.example.skincare.response.BookingDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Random;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private SkinTherapistRepository skinTherapistRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    @GetMapping
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('MANAGER')")
    public ResponseEntity<List<Booking>> getBookings(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails userDetails)) {
            return ResponseEntity.status(401).build();
        }

        String username = userDetails.getUsername();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Trả về lịch đặt của người dùng hiện tại, bất kể vai trò
        List<Booking> bookings = bookingRepository.findByCustomerId(user.getId());
        return ResponseEntity.ok(bookings);
    }
    @GetMapping("/all")
    @PreAuthorize("hasRole('MANAGER')or hasRole('STAFF')")
    public ResponseEntity<List<Booking>> getAllBookings(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails userDetails)) {
            return ResponseEntity.status(401).build();
        }

        // Trả về tất cả lịch đặt
        List<Booking> bookings = bookingRepository.findAll();
        return ResponseEntity.ok(bookings);
    }

    @PostMapping
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
            System.out.println("Finding service with ID: " + bookingDTO.getServiceId());
            booking.setService(serviceRepository.findById(bookingDTO.getServiceId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy dịch vụ với ID: " + bookingDTO.getServiceId())));

            // Gán chuyên viên
            if (bookingDTO.getTherapistId() != null) {
                System.out.println("Finding therapist with ID: " + bookingDTO.getTherapistId());
                booking.setTherapist(skinTherapistRepository.findById(bookingDTO.getTherapistId())
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy chuyên viên với ID: " + bookingDTO.getTherapistId())));
            } else {
                // Tự động gán một chuyên viên ngẫu nhiên
                List<SkinTherapist> therapists = skinTherapistRepository.findAll();
                if (therapists.isEmpty()) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Không có chuyên viên nào khả dụng.");
                }
                SkinTherapist randomTherapist = therapists.get(new Random().nextInt(therapists.size()));
                System.out.println("Auto-assigned therapist with ID: " + randomTherapist.getId());
                booking.setTherapist(randomTherapist);
            }

            booking.setBookingTime(bookingDTO.getBookingTime());
            booking.setStatus(bookingDTO.getStatus());

            System.out.println("Booking to be saved: " + booking);
            Booking savedBooking = bookingRepository.save(booking);
            System.out.println("Saved booking: " + savedBooking);
            return ResponseEntity.ok(savedBooking);
        } catch (Exception e) {
            System.out.println("Lỗi khi tạo đặt lịch: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Lỗi khi tạo đặt lịch: " + e.getMessage());
        }
    }

    @PostMapping("/{bookingId}/checkin")
    @PreAuthorize("hasRole('MANAGER')or hasRole('STAFF')")
    public ResponseEntity<String> checkIn(@PathVariable Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đặt lịch"));
        booking.setStatus("CHECKED_IN");
        bookingRepository.save(booking);
        return ResponseEntity.ok("Check-in thành công");
    }

    @PostMapping("/{bookingId}/checkout")
    @PreAuthorize("hasRole('MANAGER')or hasRole('STAFF')")
    public ResponseEntity<String> checkOut(@PathVariable Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đặt lịch"));
        booking.setStatus("COMPLETED");
        bookingRepository.save(booking);
        SkinTherapist therapist = booking.getTherapist();
        if (therapist != null) {
            therapist.getServices().remove(booking.getService());
            skinTherapistRepository.save(therapist);
        }
        return ResponseEntity.ok("Check-out thành công");
    }

    @PostMapping("/{bookingId}/cancel")
    @PreAuthorize("hasRole('MANAGER') or hasRole('STAFF')")
    public ResponseEntity<String> cancelBooking(@PathVariable Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đặt lịch"));
        booking.setStatus("CANCELLED");
        bookingRepository.save(booking);
        return ResponseEntity.ok("Đặt lịch đã được hủy");
    }
}