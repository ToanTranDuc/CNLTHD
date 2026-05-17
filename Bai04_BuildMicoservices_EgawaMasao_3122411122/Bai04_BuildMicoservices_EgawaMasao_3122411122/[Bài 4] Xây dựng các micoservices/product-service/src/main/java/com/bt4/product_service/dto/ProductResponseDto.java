package com.bt4.product_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class ProductResponseDto {
    private String id;
    private String skuCode;
    private String name;
    private String description;
    private BigDecimal price;
    private int quantity;
}
