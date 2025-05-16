package com.example.demo.api.mapper;

import com.example.demo.api.dto.CategoryDTO;
import com.example.demo.api.dto.ProductDTO;
import com.example.demo.api.model.Product;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {
    public ProductDTO toDTO(Product product) {
        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        if (product.getCategory() != null) {
            CategoryDTO categoryDTO = new CategoryDTO();
            categoryDTO.setId(product.getCategory().getId());
            categoryDTO.setName(product.getCategory().getName());
            dto.setCategory(categoryDTO);
        }
        dto.setDescription(product.getDescription());
        dto.setStock(product.getStock());
        dto.setImage(product.getImage());
        dto.setTimestamp(product.getTimestamp());
        dto.setPrice(product.getPrice());
        dto.setAvailable(product.isAvailable());
        return dto;
    }

    public Product toEntity(ProductDTO dto) {
        Product product = new Product();
        product.setId(dto.getId());
        product.setName(dto.getName());
        // Category mapping would require CategoryService or repository, simplified here
        product.setDescription(dto.getDescription());
        product.setStock(dto.getStock());
        product.setImage(dto.getImage());
        product.setTimestamp(dto.getTimestamp());
        product.setPrice(dto.getPrice());
        product.setAvailable(dto.isAvailable());
        return product;
    }
}