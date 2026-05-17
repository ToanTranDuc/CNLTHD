# 📚 Hướng Dẫn Test API trên Postman

## 1. Chuẩn Bị Postman

### Bước 1: Tạo Workspace
1. Mở **Postman**
2. Click **"Create New"** → **"Workspace"**
3. Đặt tên: `Microservices Testing`
4. Click **"Create Workspace"**

### Bước 2: Tạo Environment

1. Click **"Environments"** (trái sidebar)
2. Click **"Create New"** → **"Environment"**
3. Đặt tên: `Local Development`

#### Thêm Variables:
```
EUREKA_URL      = http://localhost:8761
PRODUCT_URL     = http://localhost:8082
INVENTORY_URL   = http://localhost:8083
ORDER_URL       = http://localhost:8084
```

**Cách làm:**
- Variable: `EUREKA_URL`
- Initial value: `http://localhost:8761`
- Current value: `http://localhost:8761`
- Click **"Save"**

Lặp lại với 3 biến còn lại.

#### Kết Quả Environment

```json
{
  "name": "Local Development",
  "values": [
    {
      "key": "EUREKA_URL",
      "value": "http://localhost:8761",
      "type": "string",
      "enabled": true
    },
    {
      "key": "PRODUCT_URL",
      "value": "http://localhost:8082",
      "type": "string",
      "enabled": true
    },
    {
      "key": "INVENTORY_URL",
      "value": "http://localhost:8083",
      "type": "string",
      "enabled": true
    },
    {
      "key": "ORDER_URL",
      "value": "http://localhost:8084",
      "type": "string",
      "enabled": true
    }
  ]
}
```

---

## 2. Tạo Collection & Requests

### Cơ Cấu Collection

```
Microservices API
├── Health Checks
│   ├── Eureka Health
│   ├── Product Service Health
│   ├── Inventory Service Health
│   └── Order Service Health
├── Product Service
│   ├── Create Product
│   ├── Get All Products
│   ├── Get Product by ID
│   ├── Search Products
│   ├── Update Product
│   └── Delete Product
├── Inventory Service
│   ├── Create/Update Inventory
│   ├── Get All Inventory
│   ├── Get Inventory by SKU
│   ├── Check Stock
│   └── Decrease Inventory
└── Order Service
    ├── Place Order (With Stock)
    └── Place Order (Without Stock)
```

### Bước 3: Tạo Collection

1. Click **"Collections"** (left sidebar)
2. Click **"+"** → **"Create new collection"**
3. Đặt tên: `Microservices API`
4. Click **"Create"**

---

## 3. Health Checks

### 3.1 Eureka Health
```
Method: GET
URL:    {{EUREKA_URL}}/actuator/health
```

**Steps:**
1. Click **"+"** để tạo request mới
2. Request name: `Eureka Health`
3. Method: **GET**
4. URL: `{{EUREKA_URL}}/actuator/health`
5. Click **"Send"**

**Expected Response (200):**
```json
{
  "status": "UP"
}
```

### 3.2 Product Service Health
```
Method: GET
URL:    {{PRODUCT_URL}}/actuator/health
```

### 3.3 Inventory Service Health
```
Method: GET
URL:    {{INVENTORY_URL}}/actuator/health
```

### 3.4 Order Service Health
```
Method: GET
URL:    {{ORDER_URL}}/actuator/health
```

---

## 4. Product Service APIs

### 4.1 Create Product

```
Method: POST
URL:    {{PRODUCT_URL}}/api/products
```

**Steps:**
1. New request: `Create Product`
2. Method: **POST**
3. URL: `{{PRODUCT_URL}}/api/products`
4. Go to **"Body"** tab
5. Select **"raw"** → **"JSON"**
6. Paste body:

```json
{
  "name": "Test Laptop",
  "price": 1299.99,
  "description": "High-performance laptop"
}
```

7. Click **"Send"**

**Expected Response (200):**
```json
{
  "success": true,
  "message": "Tạo sản phẩm thành công",
  "data": {
    "id": "1",
    "name": "Test Laptop",
    "price": 1299.99,
    "description": "High-performance laptop"
  }
}
```

**💾 Lưu Product ID:** 
- Sao chép `id` từ response
- Dùng cho các request sau (update, delete, get by ID)

### 4.2 Get All Products

```
Method: GET
URL:    {{PRODUCT_URL}}/api/products
```

**Steps:**
1. New request: `Get All Products`
2. Method: **GET**
3. URL: `{{PRODUCT_URL}}/api/products`
4. Click **"Send"**

**Expected Response (200):**
```json
{
  "success": true,
  "message": "Lấy danh sách sản phẩm thành công",
  "data": [
    {
      "id": "1",
      "name": "Test Laptop",
      "price": 1299.99,
      "description": "High-performance laptop"
    }
  ]
}
```

### 4.3 Get Product by ID

```
Method: GET
URL:    {{PRODUCT_URL}}/api/products/{id}
```

**Steps:**
1. New request: `Get Product by ID`
2. Method: **GET**
3. URL: `{{PRODUCT_URL}}/api/products/1`
4. Click **"Send"**

**Expected Response (200):**
```json
{
  "success": true,
  "message": "Lấy sản phẩm thành công",
  "data": {
    "id": "1",
    "name": "Test Laptop",
    "price": 1299.99
  }
}
```

### 4.4 Search Products

```
Method: GET
URL:    {{PRODUCT_URL}}/api/products/search?name=Laptop
```

**Steps:**
1. New request: `Search Products`
2. Method: **GET**
3. URL: `{{PRODUCT_URL}}/api/products/search?name=Test`
4. Click **"Send"**

### 4.5 Update Product

```
Method: PUT
URL:    {{PRODUCT_URL}}/api/products/{id}
```

**Steps:**
1. New request: `Update Product`
2. Method: **PUT**
3. URL: `{{PRODUCT_URL}}/api/products/1`
4. Body (raw JSON):

```json
{
  "name": "Updated Laptop",
  "price": 1499.99,
  "description": "Updated description"
}
```

5. Click **"Send"**

### 4.6 Delete Product

```
Method: DELETE
URL:    {{PRODUCT_URL}}/api/products/{id}
```

**Steps:**
1. New request: `Delete Product`
2. Method: **DELETE**
3. URL: `{{PRODUCT_URL}}/api/products/1`
4. Click **"Send"**

---

## 5. Inventory Service APIs

### 5.1 Create/Update Inventory

```
Method: POST
URL:    {{INVENTORY_URL}}/api/inventory/updateQuantity
```

**Body (raw JSON):**
```json
{
  "skuCode": "SKU-LAPTOP-001",
  "quantity": 100
}
```

**Expected Response (201 for new, 200 for update):**
```json
{
  "skuCode": "SKU-LAPTOP-001",
  "quantity": 100
}
```

**💾 Lưu SKU Code:** Dùng cho các request sau

### 5.2 Get All Inventory

```
Method: GET
URL:    {{INVENTORY_URL}}/api/inventory
```

**Expected Response:**
```json
[
  {
    "skuCode": "SKU-LAPTOP-001",
    "quantity": 100
  }
]
```

### 5.3 Get Inventory by SKU Code

```
Method: GET
URL:    {{INVENTORY_URL}}/api/inventory/SKU-LAPTOP-001
```

**Expected Response:**
```json
{
  "skuCode": "SKU-LAPTOP-001",
  "quantity": 100
}
```

### 5.4 Check Stock (QUAN TRỌNG!)

```
Method: GET
URL:    {{INVENTORY_URL}}/api/inventory/check?skuCode=SKU-LAPTOP-001&quantity=10
```

**Expected Response:**
```
true  (nếu có hàng)
false (nếu không có hàng)
```

**Cách làm trong Postman:**
1. Method: **GET**
2. URL: `{{INVENTORY_URL}}/api/inventory/check?skuCode=SKU-LAPTOP-001&quantity=10`
3. Hoặc dùng **Params tab**:
   - Key: `skuCode` → Value: `SKU-LAPTOP-001`
   - Key: `quantity` → Value: `10`

### 5.5 Decrease Inventory

```
Method: POST
URL:    {{INVENTORY_URL}}/api/inventory/decrease
```

**Body (raw JSON):**
```json
{
  "skuCode": "SKU-LAPTOP-001",
  "quantity": 5
}
```

---

## 6. Order Service APIs (⭐ Inter-Service Test)

### 6.1 Place Order (With Stock)

```
Method: POST
URL:    {{ORDER_URL}}/api/order
```

**🔥 QUAN TRỌNG:** 
- Cần có inventory trước
- Order Service sẽ gọi Inventory Service

**Steps:**
1. New request: `Place Order (With Stock)`
2. Method: **POST**
3. URL: `{{ORDER_URL}}/api/order`
4. Body (raw JSON):

```json
{
  "orderId": "ORD-001",
  "skuCode": "SKU-LAPTOP-001",
  "quantity": 2,
  "price": 1299.99,
  "userDetails": {
    "email": "customer@example.com",
    "firstName": "John",
    "lastName": "Doe"
  }
}
```

**⚠️ Required Fields:**
- `price`: BigDecimal (e.g., 1299.99)
- `userDetails`: Object with email, firstName, lastName

5. Click **"Send"**

**Expected Response (201):**
```
"Order placed successfully"
```

**Điều gì xảy ra bên trong:**
1. Order Service nhận request
2. Order Service gọi Inventory Service: `GET /api/inventory/check?skuCode=SKU-LAPTOP-001&quantity=2`
3. Inventory Service kiểm tra database
4. Nếu có: Trả `true`, Order Service hoàn tất order
5. Nếu không: Trả `false`, Order Service trả lỗi

### 6.2 Place Order (Without Stock)

```
Method: POST
URL:    {{ORDER_URL}}/api/order
```

**Body (raw JSON):**
```json
{
  "orderId": "ORD-999",
  "skuCode": "SKU-NONEXISTENT",
  "quantity": 1000,
  "price": 999.99,
  "userDetails": {
    "email": "customer2@example.com",
    "firstName": "Jane",
    "lastName": "Smith"
  }
}
```

**Expected Response (400 hoặc 200 + error message):**
```
"Product is not in stock"
```

---

## 7. Test Flow: Từ A đến Z

### Scenario 1: Tạo Sản Phẩm → Tồn Kho → Đặt Hàng

**Bước 1:** Create Inventory
```
POST {{INVENTORY_URL}}/api/inventory/updateQuantity
{
  "skuCode": "SKU-TEST-FLOW",
  "quantity": 50
}
```
✅ Response: Inventory created

**Bước 2:** Check Stock
```
GET {{INVENTORY_URL}}/api/inventory/check?skuCode=SKU-TEST-FLOW&quantity=5
```
✅ Response: `true`

**Bước 3:** Place Order
```
POST {{ORDER_URL}}/api/order
{
  "orderId": "FLOW-001",
  "skuCode": "SKU-TEST-FLOW",
  "quantity": 5
}
```
✅ Response: "Order placed successfully"
✅ Order Service gọi Inventory Service thành công!

**Bước 4:** Check Stock Again
```
GET {{INVENTORY_URL}}/api/inventory/check?skuCode=SKU-TEST-FLOW&quantity=45
```
✅ Response: `true` (còn 45)

---

## 8. Sử Dụng Postman Features

### 8.1 Environment Variables

**Dùng `{{VARIABLE_NAME}}` trong requests:**

```
URL:  {{PRODUCT_URL}}/api/products
Body: {
  "skuCode": "{{SKU_CODE}}",
  "quantity": {{QUANTITY}}
}
```

### 8.2 Set Variable từ Response

**Mục đích:** Lưu ID/SKU từ response để dùng ở steps sau

**Cách làm:**
1. Vào tab **"Tests"**
2. Paste code:

```javascript
var jsonData = pm.response.json();
pm.environment.set("SKU_CODE", jsonData.skuCode);
pm.environment.set("PRODUCT_ID", jsonData.data.id);
```

3. Chạy request
4. Các requests sau dùng `{{SKU_CODE}}` hoặc `{{PRODUCT_ID}}`

### 8.3 Pre-request Script

**Mục đích:** Tự động generate dữ liệu random

**Tab "Pre-request Script":**

```javascript
var randomSku = "SKU-" + Math.random().toString(36).substring(7);
pm.environment.set("SKU_CODE", randomSku);

var randomOrder = "ORD-" + Math.random().toString(36).substring(7);
pm.environment.set("ORDER_ID", randomOrder);
```

### 8.4 Assertions (Tests)

**Tab "Tests":**

```javascript
// Check status code
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

// Check response body
pm.test("Response has success field", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData.success).to.eql(true);
});

// Check response time
pm.test("Response time is less than 500ms", function () {
    pm.expect(pm.response.responseTime).to.be.below(500);
});
```

---

## 9. Collection Export & Import

### Export Collection

1. Click **"..."** next to collection name
2. Select **"Export"**
3. Format: **"Collection v2.1"**
4. Click **"Export"**
5. Lưu file: `Microservices_API.json`

### Import Collection

1. Click **"Import"** (top-left)
2. Chọn file `Microservices_API.json`
3. Click **"Import"**

---

## 10. Chạy All Tests (Runner)

### Bước 1: Open Collection Runner

1. Click **"Collection"** → Select your collection
2. Click **"▶ Run"** button

### Bước 2: Configure

- **Environment:** `Local Development`
- **Requests:** Select all
- **Iterations:** 1
- **Delay:** 100ms (giữa requests)

### Bước 3: Run Collection

- Click **"Run Microservices API"**
- Xem tất cả requests chạy liên tiếp

### Expected: ✅ All Pass

---

## 11. Troubleshooting

### Error: "Could not get any response"

**Giải pháp:**
1. Kiểm tra: `docker-compose ps`
2. Khởi động lại: `docker-compose up -d`
3. Health check tất cả services

### Error: "Product is not in stock"

**Giải pháp:**
1. Tạo inventory trước (Request #5.1)
2. Kiểm tra SKU code giống nhau

### Error: "404 Not Found"

**Giải pháp:**
1. Kiểm tra URL đúng
2. Kiểm tra method (GET/POST/PUT/DELETE)
3. Kiểm tra {{VARIABLES}} có đúng không

### Slow Response

**Giải pháp:**
1. Kiểm tra MySQL connection
2. Xem logs: `docker logs inventory-service`
3. Restart service: `docker-compose restart inventory-service`

---

## 12. Export Postman Collection

Để chia sẻ với team:

```json
{
  "info": {
    "name": "Microservices API",
    "description": "Test API collection for Order, Product, Inventory services",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "Health Checks",
      "item": [
        {
          "name": "Eureka Health",
          "request": {
            "method": "GET",
            "url": "{{EUREKA_URL}}/actuator/health"
          }
        }
      ]
    },
    {
      "name": "Product Service",
      "item": [
        {
          "name": "Create Product",
          "request": {
            "method": "POST",
            "url": "{{PRODUCT_URL}}/api/products",
            "body": {
              "mode": "raw",
              "raw": "{\"name\":\"Test\",\"price\":99.99}"
            }
          }
        }
      ]
    }
  ],
  "variable": [
    {
      "key": "PRODUCT_URL",
      "value": "http://localhost:8082"
    },
    {
      "key": "INVENTORY_URL",
      "value": "http://localhost:8083"
    },
    {
      "key": "ORDER_URL",
      "value": "http://localhost:8084"
    }
  ]
}
```

---

## 📋 Checklist: Hoàn Thành Setup Postman

- [ ] Tải Postman
- [ ] Tạo Workspace: `Microservices Testing`
- [ ] Tạo Environment: `Local Development`
- [ ] Thêm 4 variables (EUREKA, PRODUCT, INVENTORY, ORDER)
- [ ] Tạo Collection: `Microservices API`
- [ ] Thêm 4 Health Check requests
- [ ] Thêm 6 Product Service requests
- [ ] Thêm 5 Inventory Service requests
- [ ] Thêm 2 Order Service requests
- [ ] Test tất cả requests
- [ ] Setup Pre-request Scripts (nếu cần)
- [ ] Setup Tests/Assertions (nếu cần)
- [ ] Export collection

---

## 🎯 Các Request Cần Test (Tóm Tắt)

### Setup Data (Order chạy trước):
```
1. POST /api/inventory/updateQuantity (Create SKU-TEST: quantity=50)
```

### Test Inter-Service Communication:
```
2. GET /api/inventory/check?skuCode=SKU-TEST&quantity=5 → true
3. POST /api/order (orderId=ORD-001, skuCode=SKU-TEST, quantity=5)
   → Order Service calls Inventory Service
   → Response: "Order placed successfully"
```

### Verify:
```
4. GET /api/inventory/check?skuCode=SKU-TEST&quantity=45 → true (số lượng giảm)
```

---

**Ngày cập nhật:** 2024-03-21  
**Version:** 1.0
