package com.example.skincare.response;

import java.time.LocalDateTime;

public class BookingDTO {
    private Long serviceId;
    private Long therapistId; // therapistId có thể là null
    private LocalDateTime bookingTime;
    private String status;

    // Constructors
    public BookingDTO() {}

    public BookingDTO(Long serviceId, Long therapistId, LocalDateTime bookingTime, String status) {
        this.serviceId = serviceId;
        this.therapistId = therapistId;
        this.bookingTime = bookingTime;
        this.status = status;
    }

    // Getters and setters
    public Long getServiceId() {
        return serviceId;
    }

    public void setServiceId(Long serviceId) {
        this.serviceId = serviceId;
    }

    public Long getTherapistId() {
        return therapistId;
    }

    public void setTherapistId(Long therapistId) {
        this.therapistId = therapistId;
    }

    public LocalDateTime getBookingTime() {
        return bookingTime;
    }

    public void setBookingTime(LocalDateTime bookingTime) {
        this.bookingTime = bookingTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}