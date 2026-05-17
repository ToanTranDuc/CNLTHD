package com.bt4.order_service.external.dto;

public record InventoryRequest( String skuCode,
                                Integer quantity) {
}
