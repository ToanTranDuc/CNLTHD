package com.bt4.product_service.external.client;

import com.bt4.product_service.external.dto.InventoryRequest;
import com.bt4.product_service.external.dto.InventoryResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "inventory-service", url = "${inventory.service.url}")
public interface InventoryClient {
    @PostMapping("/api/inventory/updateQuantity")
    ResponseEntity<InventoryResponse> upsertInventory(@RequestBody InventoryRequest request);

    @GetMapping("/api/inventory/{skuCode}")
    public ResponseEntity<InventoryResponse> getInventoryBySkuCode(@PathVariable String skuCode);
}
