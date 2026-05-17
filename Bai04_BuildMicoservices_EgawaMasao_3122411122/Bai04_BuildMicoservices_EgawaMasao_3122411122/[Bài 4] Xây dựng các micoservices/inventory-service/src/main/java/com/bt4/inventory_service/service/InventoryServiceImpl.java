package com.bt4.inventory_service.service;
import com.bt4.inventory_service.dto.InventoryResponseDto;
import com.bt4.inventory_service.dto.InventoryResquestDto;
import com.bt4.inventory_service.model.Inventory;
import com.bt4.inventory_service.repository.InventoryRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryServiceImpl implements InventoryService {
    @Autowired
    private final InventoryRepository inventoryRepository;
    @Override
    public List<InventoryResponseDto> getAllInventories() {
        return inventoryRepository.findAll().stream()
                .map(this::mapToResponseDto).collect(Collectors.toList());
    }
    @Override
    public boolean isInStock(String skuCode, int quantity) {
        return inventoryRepository.existsBySkuCodeAndQuantityIsGreaterThanEqual(skuCode, quantity);
    }
    @Override
    public InventoryResponseDto getInventoryBySkuCode(String skuCode) {
        log.info("Đang tìm kiếm inventory với SKU: {}", skuCode);
        Inventory inventory = inventoryRepository.findBySkuCode(skuCode)
                .orElseThrow(() -> new RuntimeException("Inventory not found for SKU: " + skuCode));
        log.info("Tìm kiếm sản phầm thành công: SKU: ");
        return mapToResponseDto(inventory);
    }
    @Override
    @Transactional
    public InventoryResponseDto upsertInventory(InventoryResquestDto request) {
        int updateCount=inventoryRepository.increaseInventoryQuantity(request.getSkuCode(), request.getQuantity());
        if (updateCount == 0) {
            Inventory newInventory = Inventory.builder()
                    .skuCode(request.getSkuCode())
                    .quantity(request.getQuantity())
                    .build();
            inventoryRepository.save(newInventory);
            log.info("Đã tạo mới inventory với SKU: {}, quantity: {}", request.getSkuCode(), request.getQuantity());
            return mapToResponseDto(newInventory);
        }
        log.info("Đã cập nhật inventory với SKU: {}, tăng thêm quantity: {}", request.getSkuCode(), request.getQuantity());
        Inventory updatedInventory = inventoryRepository.findBySkuCode(request.getSkuCode())
                .orElseThrow(() -> new RuntimeException("Inventory not found after update for SKU: " + request.getSkuCode()));
        return mapToResponseDto(updatedInventory);
    }
    @Override
    @Transactional
    public InventoryResponseDto decreaseInventory(InventoryResquestDto request) {
        int updateCount = inventoryRepository.decreaseInventoryQuantity(request.getSkuCode(), request.getQuantity());

        if (updateCount == 0) {
            boolean exists = inventoryRepository.findBySkuCode(request.getSkuCode()).isPresent();
            if (!exists) {
                throw new RuntimeException("Không tìm thấy SKU: " + request.getSkuCode());
            } else {
                throw new RuntimeException("Không đủ hàng với SKU: " + request.getSkuCode());
            }
        }
        log.info("Đã giảm inventory với SKU: {}, giảm quantity: {}", request.getSkuCode(), request.getQuantity());
        return inventoryRepository.findBySkuCode(request.getSkuCode())
                .map(this::mapToResponseDto)
                .orElseThrow(() -> new RuntimeException("Inventory not found after decrease for SKU: " + request.getSkuCode()));

    }
    public InventoryResponseDto mapToResponseDto(Inventory inventory) {
        return InventoryResponseDto.builder()
                .id(inventory.getId())
                .skuCode(inventory.getSkuCode())
                .quantity(inventory.getQuantity())
                .build();
    }






}
