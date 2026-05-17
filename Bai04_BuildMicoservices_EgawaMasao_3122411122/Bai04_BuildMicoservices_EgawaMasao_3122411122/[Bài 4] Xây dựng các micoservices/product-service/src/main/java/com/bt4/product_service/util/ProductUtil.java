package com.bt4.product_service.util;

import java.math.BigDecimal;

public class ProductUtil {

    public static boolean isValidProductName(String productName) {
        return productName != null && !productName.trim().isEmpty() && productName.length() >= 3;
    }
    public static boolean isValidPrice(BigDecimal price) {
        return price != null && price.compareTo(BigDecimal.ZERO) >= 0;
    }
    public static boolean isValidQuantity(int quantity) {
        return quantity >= 0;
    }
    public static String formatPrice(double price) {
        return String.format("%.2f", price);
    }
    public static boolean isInStock(int quantity) {
        return quantity > 0;
    }
}

