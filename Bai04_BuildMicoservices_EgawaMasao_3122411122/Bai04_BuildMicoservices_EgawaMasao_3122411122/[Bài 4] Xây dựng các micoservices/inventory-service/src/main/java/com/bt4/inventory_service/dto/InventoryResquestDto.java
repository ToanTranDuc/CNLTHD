package com.bt4.inventory_service.dto;

import lombok.Data;

@Data

public class InventoryResquestDto {
    private String skuCode;
    private int quantity;
}
