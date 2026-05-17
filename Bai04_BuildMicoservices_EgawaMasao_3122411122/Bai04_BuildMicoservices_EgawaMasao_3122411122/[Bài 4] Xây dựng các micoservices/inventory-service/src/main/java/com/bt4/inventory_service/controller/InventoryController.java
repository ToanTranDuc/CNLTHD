package com.bt4.inventory_service.controller;

import com.bt4.inventory_service.dto.InventoryResponseDto;
import com.bt4.inventory_service.dto.InventoryResquestDto;
import com.bt4.inventory_service.repository.InventoryRepository;
import com.bt4.inventory_service.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;
    private final InventoryRepository inventoryRepository;
    @GetMapping("/check")
    @ResponseStatus(HttpStatus.OK)
    public boolean isInStock(@RequestParam String skuCode, @RequestParam int quantity) {
        return inventoryService.isInStock(skuCode, quantity);
    }

    @GetMapping
    public List<InventoryResponseDto> getAllInventories() {
        return inventoryService.getAllInventories();
    }
    @GetMapping("/{skuCode}")
    public ResponseEntity<InventoryResponseDto> getInventoryBySkuCode(@PathVariable String skuCode) {
        return ResponseEntity.ok(inventoryService.getInventoryBySkuCode(skuCode));
    }

    @PostMapping("/updateQuantity")
    public ResponseEntity<InventoryResponseDto> upsertInventory(@RequestBody InventoryResquestDto request) {
        boolean isNew = !inventoryRepository.findBySkuCode(request.getSkuCode()).isPresent();
        InventoryResponseDto response = inventoryService.upsertInventory(request);

        return isNew
                ? ResponseEntity.status(HttpStatus.CREATED).body(response)
                : ResponseEntity.ok(response);
    }
    @PostMapping("/decrease")
    public ResponseEntity<InventoryResponseDto> decreaseInventory(@RequestBody InventoryResquestDto request) {
        return ResponseEntity.ok(inventoryService.decreaseInventory(request));
    }

}
