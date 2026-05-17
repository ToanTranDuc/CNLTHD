package com.bt4.order_service.controller;

import com.bt4.order_service.dto.OrderRequest;
import com.bt4.order_service.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public String placeOrder(@RequestBody OrderRequest orderRequest) {
        if(orderService.placeOrder(orderRequest)){
            return "Order placed successfully";
        }
        else {;
            return "Product is not in stock";
        }
    }
}
