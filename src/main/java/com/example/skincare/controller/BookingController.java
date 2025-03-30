package com.example.skincare.controller;

import com.example.skincare.models.Booking;
import com.example.skincare.repositories.BookingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    @Autowired
    private BookingRepository bookingRepository;

    // Lấy danh sách tất cả đơn đặt lịch (chỉ MANAGER hoặc STAFF)
    @GetMapping
    @PreAuthorize("hasRole('MANAGER') or hasRole('STAFF')")
    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    // Lấy thông tin một đơn đặt lịch theo ID (chỉ MANAGER, STAFF, CUSTOMER sở hữu, hoặc THERAPIST được phân công)
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER') or hasRole('STAFF') or @securityService.isOwnerOfBooking(#id, authentication) or @securityService.isAssignedTherapist(#id, authentication)")
    public ResponseEntity<Booking> getBookingById(@PathVariable Long id) {
        Optional<Booking> booking = bookingRepository.findById(id);
        return booking.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Tạo đơn đặt lịch mới (chỉ CUSTOMER)
    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public Booking createBooking(@RequestBody Booking booking) {
        return bookingRepository.save(booking);
    }

    // Cập nhật đơn đặt lịch (chỉ MANAGER, STAFF, hoặc CUSTOMER sở hữu)
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER') or hasRole('STAFF') or @securityService.isOwnerOfBooking(#id, authentication)")
    public ResponseEntity<Booking> updateBooking(@PathVariable Long id, @RequestBody Booking updatedBooking) {
        Optional<Booking> booking = bookingRepository.findById(id);
        if (booking.isPresent()) {
            Booking existingBooking = booking.get();
            existingBooking.setService(updatedBooking.getService());
            existingBooking.setTherapist(updatedBooking.getTherapist());
            existingBooking.setBookingTime(updatedBooking.getBookingTime());
            existingBooking.setStatus(updatedBooking.getStatus());
            existingBooking.setResult(updatedBooking.getResult());
            return ResponseEntity.ok(bookingRepository.save(existingBooking));
        }
        return ResponseEntity.notFound().build();
    }

    // Xóa đơn đặt lịch (chỉ MANAGER, STAFF, hoặc CUSTOMER sở hữu)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER') or hasRole('STAFF') or @securityService.isOwnerOfBooking(#id, authentication)")
    public ResponseEntity<Void> deleteBooking(@PathVariable Long id) {
        if (bookingRepository.existsById(id)) {
            bookingRepository.deleteById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}
