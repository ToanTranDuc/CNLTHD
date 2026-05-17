package com.bt4.product_service.service;

import com.bt4.product_service.dto.ProductResponseDto;
import com.bt4.product_service.dto.ProductResquestDto;

import java.util.List;

public interface ProductService {
    ProductResponseDto createProduct(ProductResquestDto product);
    ProductResponseDto getProductById(String id);
    ProductResponseDto updateProduct(String id, ProductResquestDto product);
    List<ProductResponseDto> getProductsByNameContains(String name);

    List<ProductResponseDto> getAllProducts();
    void deleteProduct(String id);

}
