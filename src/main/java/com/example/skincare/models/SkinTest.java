package com.example.skincare.models;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "skin_tests")
@Getter
@Setter
public class SkinTest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String answers; // Lưu câu trả lời dưới dạng JSON hoặc chuỗi

    @Column(nullable = false)
    private String recommendedService;
}
