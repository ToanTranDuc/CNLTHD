package com.bt4.product_service.external.dto;

import lombok.Data;

@Data

public class InventoryResponse {
    private Long id;
    private String skuCode;
    private int quantity;
}
