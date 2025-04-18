package com.example.skincare.models;

import jakarta.persistence.*;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonManagedReference; // Thay đổi ở đây

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "skin_therapists")
@Data
public class SkinTherapist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fullName;

    private String expertise;

    private String experience;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToMany
    @JoinTable(
            name = "therapist_services",
            joinColumns = @JoinColumn(name = "therapist_id"),
            inverseJoinColumns = @JoinColumn(name = "service_id")
    )
    @JsonManagedReference // Sử dụng @JsonManagedReference ở phía chủ sở hữu
    private Set<Service> services = new HashSet<>();

    // Constructors, getters, setters (tự động tạo bởi Lombok @Data)
}