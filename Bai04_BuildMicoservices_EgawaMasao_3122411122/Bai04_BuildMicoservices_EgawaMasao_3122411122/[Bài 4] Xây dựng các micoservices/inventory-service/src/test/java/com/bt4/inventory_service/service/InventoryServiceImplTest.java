package com.bt4.inventory_service.service;

import com.bt4.inventory_service.dto.InventoryResquestDto;
import com.bt4.inventory_service.model.Inventory;
import com.bt4.inventory_service.repository.InventoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventoryServiceImplTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @InjectMocks
    private InventoryServiceImpl inventoryService;

    @Test
    void isInStock_shouldReturnTrue_whenQuantityIsAvailable() {
        when(inventoryRepository.existsBySkuCodeAndQuantityIsGreaterThanEqual("SKU-1", 5)).thenReturn(true);

        boolean result = inventoryService.isInStock("SKU-1", 5);

        assertThat(result).isTrue();
    }

    @Test
    void isInStock_shouldReturnFalse_whenProductDoesNotExist() {
        when(inventoryRepository.existsBySkuCodeAndQuantityIsGreaterThanEqual("SKU-NOT-FOUND", 1)).thenReturn(false);

        boolean result = inventoryService.isInStock("SKU-NOT-FOUND", 1);

        assertThat(result).isFalse();
    }

    @Test
    void getAllInventories_shouldMapEntitiesToDtos() {
        Inventory first = Inventory.builder().id(1L).skuCode("SKU-1").quantity(10).build();
        Inventory second = Inventory.builder().id(2L).skuCode("SKU-2").quantity(20).build();
        when(inventoryRepository.findAll()).thenReturn(List.of(first, second));

        var result = inventoryService.getAllInventories();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getSkuCode()).isEqualTo("SKU-1");
        assertThat(result.get(1).getQuantity()).isEqualTo(20);
    }

    @Test
    void getInventoryBySkuCode_shouldReturnDto_whenInventoryExists() {
        Inventory inventory = Inventory.builder().id(10L).skuCode("SKU-10").quantity(50).build();
        when(inventoryRepository.findBySkuCode("SKU-10")).thenReturn(Optional.of(inventory));

        var result = inventoryService.getInventoryBySkuCode("SKU-10");

        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getSkuCode()).isEqualTo("SKU-10");
        assertThat(result.getQuantity()).isEqualTo(50);
    }

    @Test
    void getInventoryBySkuCode_shouldThrow_whenInventoryMissing() {
        when(inventoryRepository.findBySkuCode("SKU-MISSING")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> inventoryService.getInventoryBySkuCode("SKU-MISSING"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Inventory not found");
    }

    @Test
    void upsertInventory_shouldCreateNewInventory_whenSkuDoesNotExist() {
        InventoryResquestDto request = new InventoryResquestDto();
        request.setSkuCode("SKU-NEW");
        request.setQuantity(7);

        when(inventoryRepository.increaseInventoryQuantity("SKU-NEW", 7)).thenReturn(0);
        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(invocation -> {
            Inventory saved = invocation.getArgument(0);
            saved.setId(99L);
            return saved;
        });

        var result = inventoryService.upsertInventory(request);

        assertThat(result.getSkuCode()).isEqualTo("SKU-NEW");
        assertThat(result.getQuantity()).isEqualTo(7);
        verify(inventoryRepository).save(any(Inventory.class));
    }

    @Test
    void upsertInventory_shouldIncreaseExistingInventory_whenSkuExists() {
        InventoryResquestDto request = new InventoryResquestDto();
        request.setSkuCode("SKU-EXIST");
        request.setQuantity(5);

        Inventory updated = Inventory.builder().id(8L).skuCode("SKU-EXIST").quantity(25).build();

        when(inventoryRepository.increaseInventoryQuantity("SKU-EXIST", 5)).thenReturn(1);
        when(inventoryRepository.findBySkuCode("SKU-EXIST")).thenReturn(Optional.of(updated));

        var result = inventoryService.upsertInventory(request);

        assertThat(result.getQuantity()).isEqualTo(25);
        assertThat(result.getSkuCode()).isEqualTo("SKU-EXIST");
    }

    @Test
    void decreaseInventory_shouldDecreaseSuccessfully_whenEnoughStock() {
        InventoryResquestDto request = new InventoryResquestDto();
        request.setSkuCode("SKU-OK");
        request.setQuantity(2);

        Inventory updated = Inventory.builder().id(3L).skuCode("SKU-OK").quantity(6).build();

        when(inventoryRepository.decreaseInventoryQuantity("SKU-OK", 2)).thenReturn(1);
        when(inventoryRepository.findBySkuCode("SKU-OK")).thenReturn(Optional.of(updated));

        var result = inventoryService.decreaseInventory(request);

        assertThat(result.getQuantity()).isEqualTo(6);
    }

    @Test
    void decreaseInventory_shouldThrow_whenSkuMissing() {
        InventoryResquestDto request = new InventoryResquestDto();
        request.setSkuCode("SKU-MISSING");
        request.setQuantity(1);

        when(inventoryRepository.decreaseInventoryQuantity("SKU-MISSING", 1)).thenReturn(0);
        when(inventoryRepository.findBySkuCode("SKU-MISSING")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> inventoryService.decreaseInventory(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Không tìm thấy SKU");
    }

    @Test
    void decreaseInventory_shouldThrow_whenInsufficientStock() {
        InventoryResquestDto request = new InventoryResquestDto();
        request.setSkuCode("SKU-LOW");
        request.setQuantity(10);

        Inventory existing = Inventory.builder().id(5L).skuCode("SKU-LOW").quantity(3).build();

        when(inventoryRepository.decreaseInventoryQuantity("SKU-LOW", 10)).thenReturn(0);
        when(inventoryRepository.findBySkuCode("SKU-LOW")).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> inventoryService.decreaseInventory(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Không đủ hàng");
    }
}
