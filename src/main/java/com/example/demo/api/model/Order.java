package com.example.demo.api.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Order date and time is required")
    @Column(nullable = false)
    private LocalDateTime orderDateTime;

    @NotNull(message = "Total amount is required")
    @DecimalMin(value = "0.0", message = "Total amount cannot be negative")
    @Column(nullable = false)
    private BigDecimal totalAmount;

    @Column(nullable = true, length = 20)
    private Boolean status = true;

    @CreationTimestamp
    private LocalDateTime createdAt;
}