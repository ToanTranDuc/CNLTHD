package com.bt4.inventory_service.service;

import com.bt4.inventory_service.dto.InventoryResponseDto;
import com.bt4.inventory_service.dto.InventoryResquestDto;

import java.util.List;

public interface InventoryService {
    InventoryResponseDto getInventoryBySkuCode(String skucode);
    List<InventoryResponseDto> getAllInventories();
    InventoryResponseDto upsertInventory(InventoryResquestDto request);
    boolean isInStock(String skucode, int quantity);
    InventoryResponseDto decreaseInventory(InventoryResquestDto request);


}
