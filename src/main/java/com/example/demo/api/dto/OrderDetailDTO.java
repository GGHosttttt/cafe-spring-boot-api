
package com.example.demo.api.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class OrderDetailDTO {
    private Long id;

    @NotNull(message = "Product ID is required")
    private Long productId;

//    @NotNull(message = "Quantity is required")
    @Min(value = 1)
    private Integer quantity;

    @DecimalMin(value = "0.0", message = "Unit price cannot be negative")
    private BigDecimal unitPrice;

    @DecimalMin(value = "0.0", message = "Subtotal cannot be negative")
    private BigDecimal subTotal;
    
    @Size(max = 255, message = "Message must not exceed 255 characters")
    private String message;
    

    // Explicit getter and setter for qty
    public Integer getQty() {
        return quantity;
    }

    public void setQty(Integer qty) {
        this.quantity = qty;
    }
}