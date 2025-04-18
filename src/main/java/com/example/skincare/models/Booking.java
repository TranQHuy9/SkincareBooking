package com.example.skincare.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "bookings")
@Getter
@Setter
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "booking_time")
    private LocalDateTime bookingTime;

    @Column(name = "status")
    private String status;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "customer_id")
    private User customer;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "service_id")
    private Service service;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "therapist_id")
    private SkinTherapist therapist;

    @OneToMany(mappedBy = "booking", fetch = FetchType.EAGER)
    @JsonManagedReference
    private List<Feedback> feedbacks;
}