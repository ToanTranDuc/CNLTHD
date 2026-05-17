package com.bt4.product_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private String id;
    @Column(name = "skuCode", updatable = false, nullable = false)
    private String skuCode;
    @Column(name = "name", updatable = true, nullable = false)
    private String name;
    @Column(name = "description", updatable = true, nullable = true)
    private String description;
    @Column(name = "price", updatable = true, nullable = false)
    private BigDecimal price;
}

