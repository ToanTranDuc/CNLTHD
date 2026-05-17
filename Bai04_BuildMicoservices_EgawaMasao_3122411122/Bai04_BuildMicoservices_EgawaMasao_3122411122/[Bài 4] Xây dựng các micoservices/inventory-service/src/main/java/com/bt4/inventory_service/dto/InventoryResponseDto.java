package com.bt4.inventory_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class InventoryResponseDto {
    private Long id;
    private String skuCode;
    private int quantity;

}
