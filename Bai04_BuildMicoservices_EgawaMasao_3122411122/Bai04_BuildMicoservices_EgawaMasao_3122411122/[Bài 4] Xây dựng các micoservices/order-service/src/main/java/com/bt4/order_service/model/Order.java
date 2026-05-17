package com.bt4.order_service.model;

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
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    @Column(name = "order_number", nullable = false)
    private String orderNumber;
    @Column(name = "sku_code", nullable = false)
    private String skuCode;
    @Column(name="quantity", nullable = false)
    private Integer quantity;
    @Column(name="price", nullable = false)
    private BigDecimal price;
}
