package com.example.skincare.services;
import com.example.skincare.models.*;
import com.example.skincare.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service("securityService")
public class SecurityService {

    @Autowired
    private SkinTestRepository skinTestRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private FeedbackRepository feedbackRepository;

    @Autowired
    private ScheduleRepository scheduleRepository;

    public boolean isOwnerOfSkinTest(Long skinTestId, Authentication authentication) {
        SkinTest skinTest = skinTestRepository.findById(skinTestId).orElse(null);
        if (skinTest == null || skinTest.getUser() == null) {
            return false;
        }
        String username = authentication.getName();
        return skinTest.getUser().getUsername().equals(username);
    }

    public boolean isOwnerOfBooking(Long bookingId, Authentication authentication) {
        Booking booking = bookingRepository.findById(bookingId).orElse(null);
        if (booking == null || booking.getCustomer() == null) {
            return false;
        }
        String username = authentication.getName();
        return booking.getCustomer().getUsername().equals(username);
    }

    public boolean isAssignedTherapist(Long bookingId, Authentication authentication) {
        Booking booking = bookingRepository.findById(bookingId).orElse(null);
        if (booking == null || booking.getTherapist() == null || booking.getTherapist().getUser() == null) {
            return false;
        }
        String username = authentication.getName();
        return booking.getTherapist().getUser().getUsername().equals(username);
    }

    public boolean isOwnerOfFeedback(Long feedbackId, Authentication authentication) {
        Feedback feedback = feedbackRepository.findById(feedbackId).orElse(null);
        if (feedback == null || feedback.getBooking() == null || feedback.getBooking().getCustomer() == null) {
            return false;
        }
        String username = authentication.getName();
        return feedback.getBooking().getCustomer().getUsername().equals(username);
    }

    public boolean isOwnerOfSchedule(Long scheduleId, Authentication authentication) {
        Schedule schedule = scheduleRepository.findById(scheduleId).orElse(null);
        if (schedule == null || schedule.getTherapist() == null || schedule.getTherapist().getUser() == null) {
            return false;
        }
        String username = authentication.getName();
        return schedule.getTherapist().getUser().getUsername().equals(username);
    }
}