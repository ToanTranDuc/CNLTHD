package com.bt4.product_service.service;

import com.bt4.product_service.dto.ProductResponseDto;
import com.bt4.product_service.dto.ProductResquestDto;
import com.bt4.product_service.external.client.InventoryClient;
import com.bt4.product_service.external.dto.InventoryRequest;
import com.bt4.product_service.external.dto.InventoryResponse;
import com.bt4.product_service.model.Product;
import com.bt4.product_service.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final InventoryClient InventoryClient;

    @Override
    public ProductResponseDto createProduct(ProductResquestDto request) {
        log.info("Thêm sản phẩm mới vào hệ thống....");
        Product product = Product.builder()
                .skuCode(request.getSkuCode())
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .build();
        productRepository.save(product);
        log.info("Sản phẩm đã được thêm thành công với id: {}", product.getId());

        InventoryResponse inventoryResponse = InventoryClient
                .upsertInventory(new InventoryRequest(request.getSkuCode(), request.getQuantity()))
                .getBody();

        int quantity = (inventoryResponse != null) ? inventoryResponse.getQuantity() : request.getQuantity();
        return maptoResponse(product, quantity);
    }

    @Override
    public ProductResponseDto getProductById(String id) {
        log.info("Lấy thông tin sản phẩm với id: {}", id);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với id: " + id));
        log.info("Thông tin sản phẩm đã được lấy thành công với id: {}", id);

        return maptoResponse(product);
    }
    @Override
    public void deleteProduct(String id) {
        log.info("Xóa sản phẩm với id: {}", id);
        if (!productRepository.existsById(id)) {
            log.warn("Không tìm thấy sản phẩm với id: {}, không thể xóa", id);
            throw new RuntimeException("Không tìm thấy sản phẩm với id: " + id);
        }
        productRepository.deleteById(id);
        log.info("Sản phẩm đã được xóa thành công với id: {}", id);
    }

    @Override
    public ProductResponseDto updateProduct(String id, ProductResquestDto request) {
        log.info("Cập nhật thông tin sản phẩm với id: {}", id);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với id: " + id));

        // skuCode immutable (updatable=false) theo entity, nên không set lại ở đây.
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        productRepository.save(product);

        // Nếu client gửi quantity thì đồng bộ sang inventory-service theo skuCode hiện có của product
        if (request.getQuantity() > 0) {
            InventoryClient.upsertInventory(new InventoryRequest(product.getSkuCode(), request.getQuantity()));
        }

        return maptoResponse(product);
    }

    @Override
    public List<ProductResponseDto> getProductsByNameContains(String name) {
        log.info("Lấy thông tin sản phẩm có tên chứa: {}", name);
        List<Product> products = productRepository.findByNameContainingIgnoreCase(name);
        log.info("Thông tin sản phẩm đã được lấy thành công với tên chứa: {}", name);

        return products.stream().map(this::maptoResponse).collect(Collectors.toList());
    }

    @Override
    public List<ProductResponseDto> getAllProducts() {
        log.info("Lấy thông tin tất cả sản phẩm trong hệ thống...");
        List<Product> products = productRepository.findAll();
        log.info("Thông tin tất cả sản phẩm đã được lấy thành công, tổng số sản phẩm: {}", products.size());


        return products.stream().map(this::maptoResponse).collect(Collectors.toList());
    }

    /**
     * Map Product -> ProductResponseDto, đồng thời lấy quantity từ inventory-service theo skuCode.
     */
    public ProductResponseDto maptoResponse(Product product) {
        int quantity = 0;
        try {
            if (product.getSkuCode() != null && !product.getSkuCode().isBlank()) {
                InventoryResponse inventory = InventoryClient.getInventoryBySkuCode(product.getSkuCode()).getBody();
                if (inventory != null) {
                    quantity = inventory.getQuantity();
                }
            }
        } catch (Exception ex) {
            // Không để lỗi inventory làm sập product-service khi chỉ map response
            log.warn("Không lấy được quantity từ inventory-service cho skuCode={}, reason={}", product.getSkuCode(), ex.getMessage());
        }

        return maptoResponse(product, quantity);
    }

    public ProductResponseDto maptoResponse(Product product, int quantity) {
        return ProductResponseDto.builder()
                .id(product.getId())
                .skuCode(product.getSkuCode())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .quantity(quantity)
                .build();
    }

}
