package com.example.demo.api.controller;

import com.example.demo.api.service.ProductService;
import com.example.demo.api.service.FileStorageService;
import com.example.demo.api.dto.ApiResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.validation.constraints.*;
import com.example.demo.api.dto.ProductDTO;
import com.example.demo.api.dto.CategoryDTO;
import java.util.List;
import java.math.BigDecimal;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    @Autowired
    private ProductService productService;

    @Autowired
    private FileStorageService fileStorageService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductDTO>>> getAllProducts() {
        List<ProductDTO> products = productService.getAllProducts();
        if (products.isEmpty()) {
            return ResponseEntity.ok(ApiResponse.empty("No products found"));
        }
        return ResponseEntity.ok(ApiResponse.success(products));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable Long id) {
        return productService.getProductById(id)
                .map(product -> ResponseEntity.ok(product))
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
    }


    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<ProductDTO>> createProduct(
            @RequestParam @NotBlank(message = "Product name is required") @Size(max = 100, message = "Product name must not exceed 100 characters") String name,
            @RequestParam @NotNull(message = "Category ID is required") Long categoryId,
            @RequestParam(required = false) @Size(max = 255, message = "Description must not exceed 255 characters") String description,
            @RequestParam(required = false) @Min(value = 0, message = "Stock cannot be negative") Integer stock,
            @RequestParam(required = false) MultipartFile image,
            @RequestParam @NotNull(message = "Price is required") @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0") BigDecimal price,
            @RequestParam(defaultValue = "true") boolean isAvailable) {
        ProductDTO productDTO = new ProductDTO();
        productDTO.setName(name);
        productDTO.setCategory(new CategoryDTO() {{ setId(categoryId); }});
        productDTO.setDescription(description);
        productDTO.setStock(stock);
        productDTO.setImage(fileStorageService.storeFile(image, null));
        productDTO.setPrice(price);
        productDTO.setAvailable(isAvailable);
        ProductDTO savedProduct = productService.createProduct(productDTO);
        return ResponseEntity.ok(ApiResponse.success(savedProduct, "Product created successfully"));
    }

    @PostMapping(value = "/{id}", consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<ProductDTO>> updateProduct(
            @PathVariable Long id,
            @RequestParam @NotBlank(message = "Product name is required") @Size(max = 100, message = "Product name must not exceed 100 characters") String name,
            @RequestParam @NotNull(message = "Category ID is required") Long categoryId,
            @RequestParam(required = false) @Size(max = 255, message = "Description must not exceed 255 characters") String description,
            @RequestParam(required = false) @Min(value = 0, message = "Stock cannot be negative") Integer stock,
            @RequestParam(required = false) MultipartFile image,
            @RequestParam @NotNull(message = "Price is required") @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0") BigDecimal price,
            @RequestParam(defaultValue = "true") boolean isAvailable) {
        ProductDTO productDTO = new ProductDTO();
        productDTO.setName(name);
        productDTO.setCategory(new CategoryDTO() {{ setId(categoryId); }});
        productDTO.setDescription(description);
        productDTO.setStock(stock);
        productDTO.setPrice(price);
        productDTO.setAvailable(isAvailable);
        ProductDTO updatedProduct = productService.updateProduct(id, productDTO, image);
        return ResponseEntity.ok(ApiResponse.success(updatedProduct, "Product updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Product deleted successfully"));
    }
}