package com.bt4.order_service.controller;

import com.bt4.order_service.dto.OrderRequest;
import com.bt4.order_service.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class OrderControllerIntegrationTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderService = mock(OrderService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new OrderController(orderService)).build();
    }

    @Test
    void placeOrder_shouldReturnSuccessMessage_whenOrderPlacedSuccessfully() throws Exception {
        when(orderService.placeOrder(any(OrderRequest.class))).thenReturn(true);

        OrderRequest request = new OrderRequest(
                1L,
                "ORD-100",
                "SKU-OK",
                "P-100",
                2,
                new BigDecimal("99.99"),
                new OrderRequest.UserDetails("user@example.com", "John", "Doe"));

        mockMvc.perform(post("/api/order")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().string("Order placed successfully"));
    }

    @Test
    void placeOrder_shouldReturnOutOfStockMessage_whenProductNotInStock() throws Exception {
        when(orderService.placeOrder(any(OrderRequest.class))).thenReturn(false);

        OrderRequest request = new OrderRequest(
                2L,
                "ORD-101",
                "SKU-NOT-FOUND",
                "P-200",
                1,
                new BigDecimal("10.00"),
                new OrderRequest.UserDetails("user@example.com", "Jane", "Doe"));

        mockMvc.perform(post("/api/order")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().string("Product is not in stock"));
    }
}
