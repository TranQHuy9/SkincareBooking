package com.example.skincare.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FeedbackResponseDTO {
    private Long id;
    private Long bookingId;
    private String customerUsername; // Changed from customerFullName
    private Double rating;
    private String comment;
}