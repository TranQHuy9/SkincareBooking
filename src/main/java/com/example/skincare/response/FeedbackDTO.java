package com.example.skincare.response;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class FeedbackDTO {
    // Getters và Setters
    private Long bookingId;
    private Double rating;
    private String comment;

}