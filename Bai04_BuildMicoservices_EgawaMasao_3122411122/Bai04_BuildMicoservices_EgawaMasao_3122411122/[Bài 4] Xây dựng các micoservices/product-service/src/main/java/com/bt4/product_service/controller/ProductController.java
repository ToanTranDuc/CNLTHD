package com.bt4.product_service.controller;

import com.bt4.product_service.dto.ProductResponseDto;
import com.bt4.product_service.dto.ProductResquestDto;
import com.bt4.product_service.service.ProductService;
import com.bt4.product_service.util.ApiResponse;
import com.bt4.product_service.util.ProductUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/products")
@RequiredArgsConstructor
public class ProductController {
    @Autowired
    private final ProductService productService;

    @PostMapping
    public ApiResponse<ProductResponseDto> createProduct(@RequestBody ProductResquestDto request){
        // Validate dữ liệu trước khi tạo sản phẩm
        if (!ProductUtil.isValidProductName(request.getName())) {
            return ApiResponse.error("Tên sản phẩm không hợp lệ (tối thiểu 3 ký tự)");
        }
        if (!ProductUtil.isValidPrice(request.getPrice())) {
            return ApiResponse.error("Giá sản phẩm phải lớn hơn 0");
        }
//        if (!ProductUtil.isValidQuantity(request.getQuantity())) {
//            return ApiResponse.error("Số lượng không hợp lệ");
//        }

        try {
            ProductResponseDto product = productService.createProduct(request);
            return ApiResponse.success(product, "Tạo sản phẩm thành công");
        } catch (Exception e) {
            return ApiResponse.error("Lỗi khi tạo sản phẩm: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ApiResponse<ProductResponseDto> getProductById(@PathVariable("id") String id){
        try {
            ProductResponseDto product = productService.getProductById(id);
            if (product != null) {
                return ApiResponse.success(product, "Lấy sản phẩm thành công");
            }
            return ApiResponse.error("Sản phẩm không tồn tại");
        } catch (Exception e) {
            return ApiResponse.error("Lỗi: " + e.getMessage());
        }
    }

    @GetMapping("/search")
    public ApiResponse<List<ProductResponseDto>> getProductByNameContains(@RequestParam("name") String name){
        try {
            if (name == null || name.trim().isEmpty()) {
                return ApiResponse.error("Tên sản phẩm không được để trống");
            }
            List<ProductResponseDto> products = productService.getProductsByNameContains(name);
            return ApiResponse.success(products, "Tìm kiếm thành công");
        } catch (Exception e) {
            return ApiResponse.error("Lỗi: " + e.getMessage());
        }
    }

    @GetMapping()
    public ApiResponse<List<ProductResponseDto>> getAllProducts(){
        try {
            List<ProductResponseDto> products = productService.getAllProducts();
            return ApiResponse.success(products, "Lấy danh sách sản phẩm thành công");
        } catch (Exception e) {
            return ApiResponse.error("Lỗi: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ApiResponse<ProductResponseDto> updateProduct(@PathVariable("id") String id, @RequestBody ProductResquestDto request){
        try {
            // Validate dữ liệu
            if (!ProductUtil.isValidProductName(request.getName())) {
                return ApiResponse.error("Tên sản phẩm không hợp lệ (tối thiểu 3 ký tự)");
            }
            if (!ProductUtil.isValidPrice(request.getPrice())) {
                return ApiResponse.error("Giá sản phẩm phải lớn hơn 0");
            }

            ProductResponseDto product = productService.updateProduct(id, request);
            return ApiResponse.success(product, "Cập nhật sản phẩm thành công");
        } catch (Exception e) {
            return ApiResponse.error("Lỗi: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteProduct(@PathVariable("id") String id){
        try {
            productService.deleteProduct(id);
            return ApiResponse.success(null, "Xóa sản phẩm thành công");
        } catch (Exception e) {
            return ApiResponse.error("Lỗi: " + e.getMessage());
        }
    }
}
