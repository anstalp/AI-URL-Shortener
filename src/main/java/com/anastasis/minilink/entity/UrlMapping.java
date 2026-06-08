package com.anastasis.minilink.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

import java.time.LocalDateTime;

@Entity // Indicates that this is a database
@Data //Lombok: Automatic Getters, Setters & Constructors
public class UrlMapping {

    @Id // Primary Key
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Auto-increment (1, 2, 3...)
    private Long id;

    @Column(nullable = false)
    @NotBlank(message = "Not empty link")
    @URL(message = "Enter a valid URL")
    private String originalUrl;

    @Column(nullable = false, unique = true)
    private String shortCode;

    private LocalDateTime createdAt = LocalDateTime.now();

    private int clickCount = 0;
}