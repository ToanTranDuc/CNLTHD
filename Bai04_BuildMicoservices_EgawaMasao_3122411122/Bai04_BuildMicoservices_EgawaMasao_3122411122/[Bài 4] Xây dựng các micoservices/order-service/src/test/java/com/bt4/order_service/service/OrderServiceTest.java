package com.bt4.order_service.service;

import com.bt4.order_service.client.InventoryClient;
import com.bt4.order_service.dto.OrderRequest;
import com.bt4.order_service.event.OrderPlacedEvent;
import com.bt4.order_service.external.dto.InventoryRequest;
import com.bt4.order_service.external.dto.InventoryResponse;
import com.bt4.order_service.model.Order;
import com.bt4.order_service.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private InventoryClient inventoryClient;

    @Mock
    private KafkaTemplate<String, OrderPlacedEvent> kafkaTemplate;

    @InjectMocks
    private OrderService orderService;

    @Test
    void placeOrder_shouldReturnTrue_whenProductInStockAndInventoryDecreaseSuccess() {
        OrderRequest request = buildOrderRequest("SKU-1", 2, new BigDecimal("10.50"));
        when(inventoryClient.isInStock("SKU-1", 2)).thenReturn(true);
        when(inventoryClient.decreaseInventory(any(InventoryRequest.class)))
                .thenReturn(ResponseEntity.ok(new InventoryResponse(1L, "SKU-1", 8)));

        boolean result = orderService.placeOrder(request);

        assertThat(result).isTrue();

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderCaptor.capture());
        assertThat(orderCaptor.getValue().getSkuCode()).isEqualTo("SKU-1");
        assertThat(orderCaptor.getValue().getQuantity()).isEqualTo(2);
        assertThat(orderCaptor.getValue().getPrice()).isEqualByComparingTo("10.50");

        verify(inventoryClient).decreaseInventory(new InventoryRequest("SKU-1", 2));
        verify(kafkaTemplate).send(eq("order-placed"), any(OrderPlacedEvent.class));
    }

    @Test
    void placeOrder_shouldReturnFalse_whenProductNotInStock() {
        OrderRequest request = buildOrderRequest("SKU-OUT", 3, new BigDecimal("20.00"));
        when(inventoryClient.isInStock("SKU-OUT", 3)).thenReturn(false);

        boolean result = orderService.placeOrder(request);

        assertThat(result).isFalse();
        verify(orderRepository, never()).save(any(Order.class));
        verify(inventoryClient, never()).decreaseInventory(any(InventoryRequest.class));
        verify(kafkaTemplate, never()).send(any(String.class), any(OrderPlacedEvent.class));
    }

    private static OrderRequest buildOrderRequest(String skuCode, int quantity, BigDecimal price) {
        return new OrderRequest(
                1L,
                "ORD-1",
                skuCode,
                "P-1",
                quantity,
                price,
                new OrderRequest.UserDetails("user@example.com", "Unit", "Tester"));
    }
}
