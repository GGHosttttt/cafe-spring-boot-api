
package com.example.demo.api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ProductDTO {
    private Long id;

    @NotBlank(message = "Product name is required")
    @Size(max = 100, message = "Product name must not exceed 100 characters")
    private String name;

    @NotNull(message = "Category is required")
    private CategoryDTO category; // Changed from categoryId to CategoryDTO

    @Size(max = 255, message = "Description must not exceed 255 characters")
    private String description;

    @Min(value = 0, message = "Stock cannot be negative")
    private Integer stock;

    private String image;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    private BigDecimal price;

    private boolean isAvailable;
    
    // ✅ Custom getter to expose category ID
    public Long getCategoryId() {
        return category != null ? category.getId() : null;
    }
}