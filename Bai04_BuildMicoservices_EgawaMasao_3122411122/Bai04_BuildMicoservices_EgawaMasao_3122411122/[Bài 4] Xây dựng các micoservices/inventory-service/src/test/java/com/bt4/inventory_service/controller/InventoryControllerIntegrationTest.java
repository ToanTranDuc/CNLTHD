package com.bt4.inventory_service.controller;

import com.bt4.inventory_service.dto.InventoryResponseDto;
import com.bt4.inventory_service.model.Inventory;
import com.bt4.inventory_service.repository.InventoryRepository;
import com.bt4.inventory_service.service.InventoryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class InventoryControllerIntegrationTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private InventoryService inventoryService;
    private InventoryRepository inventoryRepository;

    @BeforeEach
    void setUp() {
        inventoryService = mock(InventoryService.class);
        inventoryRepository = mock(InventoryRepository.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new InventoryController(inventoryService, inventoryRepository))
                .build();
    }

    @Test
    void isInStock_shouldReturnTrue_whenProductExistsAndEnoughQuantity() throws Exception {
        when(inventoryService.isInStock("SKU-1", 2)).thenReturn(true);

        mockMvc.perform(get("/api/inventory/check")
                .param("skuCode", "SKU-1")
                .param("quantity", "2"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    void isInStock_shouldReturnFalse_whenProductDoesNotExist() throws Exception {
        when(inventoryService.isInStock("SKU-NOT-FOUND", 1)).thenReturn(false);

        mockMvc.perform(get("/api/inventory/check")
                .param("skuCode", "SKU-NOT-FOUND")
                .param("quantity", "1"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    @Test
    void getAllInventories_shouldReturnList() throws Exception {
        InventoryResponseDto dto = InventoryResponseDto.builder().id(1L).skuCode("SKU-1").quantity(5).build();
        when(inventoryService.getAllInventories()).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/inventory"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].skuCode").value("SKU-1"))
                .andExpect(jsonPath("$[0].quantity").value(5));
    }

    @Test
    void getInventoryBySkuCode_shouldReturnItem() throws Exception {
        InventoryResponseDto dto = InventoryResponseDto.builder().id(2L).skuCode("SKU-2").quantity(10).build();
        when(inventoryService.getInventoryBySkuCode("SKU-2")).thenReturn(dto);

        mockMvc.perform(get("/api/inventory/SKU-2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.skuCode").value("SKU-2"));
    }

    @Test
    void upsertInventory_shouldReturnCreated_whenSkuIsNew() throws Exception {
        String payload = "{\"skuCode\":\"SKU-NEW\",\"quantity\":7}";
        InventoryResponseDto response = InventoryResponseDto.builder().id(9L).skuCode("SKU-NEW").quantity(7).build();

        when(inventoryRepository.findBySkuCode("SKU-NEW")).thenReturn(Optional.empty());
        when(inventoryService.upsertInventory(org.mockito.ArgumentMatchers.any())).thenReturn(response);

        mockMvc.perform(post("/api/inventory/updateQuantity")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.skuCode").value("SKU-NEW"));
    }

    @Test
    void upsertInventory_shouldReturnOk_whenSkuAlreadyExists() throws Exception {
        String payload = "{\"skuCode\":\"SKU-EXIST\",\"quantity\":3}";
        InventoryResponseDto response = InventoryResponseDto.builder().id(10L).skuCode("SKU-EXIST").quantity(13)
                .build();

        when(inventoryRepository.findBySkuCode("SKU-EXIST"))
                .thenReturn(Optional.of(new Inventory(10L, "SKU-EXIST", 10)));
        when(inventoryService.upsertInventory(org.mockito.ArgumentMatchers.any())).thenReturn(response);

        mockMvc.perform(post("/api/inventory/updateQuantity")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(13));
    }

    @Test
    void decreaseInventory_shouldReturnUpdatedInventory() throws Exception {
        String payload = objectMapper.writeValueAsString(new Inventory(11L, "SKU-DEC", 2));
        InventoryResponseDto response = InventoryResponseDto.builder().id(11L).skuCode("SKU-DEC").quantity(2).build();

        when(inventoryService.decreaseInventory(org.mockito.ArgumentMatchers.any())).thenReturn(response);

        mockMvc.perform(post("/api/inventory/decrease")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.skuCode").value("SKU-DEC"))
                .andExpect(jsonPath("$.quantity").value(2));
    }
}
