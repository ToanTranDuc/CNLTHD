# ⚡ Quick Start: Import Postman Collection

## 📥 Bước 1: Download Collection File

File collection đã được tạo: **`Microservices_API.postman_collection.json`**

Location: `c:\Study\CNLTHD\baitap4\Microservices_API.postman_collection.json`

---

## 🚀 Bước 2: Import vào Postman

### Cách 1: Drag & Drop (Nhanh nhất)

1. Mở **Postman**
2. Vào tab **"Collections"** (bên trái)
3. Kéo file `Microservices_API.postman_collection.json` vào sidebar
4. ✅ Collection tự động import

### Cách 2: Import Button

1. Mở **Postman**
2. Click **"Import"** (top-left)
3. Chọn **"Upload Files"**
4. Chọn `Microservices_API.postman_collection.json`
5. Click **"Import"**

### Cách 3: Copy & Paste Raw JSON

1. Mở file `Microservices_API.postman_collection.json` với text editor
2. Copy toàn bộ nội dung
3. Mở **Postman**
4. Click **"Import"**
5. Chọn tab **"Paste Raw Text"**
6. Paste nội dung
7. Click **"Import"**

---

## 📊 Cấu Trúc Collection (Sau Import)

```
Microservices API Testing
├── Health Checks (4 requests)
│   ├── Eureka Health
│   ├── Product Service Health
│   ├── Inventory Service Health
│   └── Order Service Health
│
├── Product Service (6 requests)
│   ├── Create Product
│   ├── Get All Products
│   ├── Get Product by ID
│   ├── Search Products
│   ├── Update Product
│   └── Delete Product
│
├── Inventory Service (5 requests)
│   ├── Create/Update Inventory ⭐
│   ├── Get All Inventory
│   ├── Get Inventory by SKU Code
│   ├── Check Stock Availability ⭐
│   └── Decrease Inventory
│
└── Order Service - Inter-Service (2 requests) ⭐⭐
    ├── Place Order - With Stock
    └── Place Order - Without Stock
```

**⭐ = Quan trọng cho inter-service testing**

---

## 🔧 Bước 3: Setup Environment

### Tạo Environment (Nếu chưa có)

1. Click **"Environments"** (left sidebar)
2. Click **"+"**
3. Đặt tên: `Local Development`

### Thêm Variables

Nhìn vào collection file, variables sau đã được tự động cấu hình:

```
EUREKA_URL    = http://localhost:8761
PRODUCT_URL   = http://localhost:8082
INVENTORY_URL = http://localhost:8083
ORDER_URL     = http://localhost:8084
```

Nếu các port khác nhau, cập nhật environment:

1. Vào **"Environments"** → **"Local Development"**
2. Sửa giá trị các variables
3. Click **"Save"**

### Chọn Environment Trước Khi Test

1. Top-right dropdown: Chọn **"Local Development"**
2. Giờ tất cả requests sẽ dùng environment variables này

---

## ✅ Bước 4: Chạy Tests

### Test Từng Request Một

1. Expand collection: **"Microservices API Testing"**
2. Click vào request bất kỳ, e.g., **"Eureka Health"**
3. Click **"Send"**
4. Xem response bên dưới

**Expected Response:**
```json
{
  "status": "UP"
}
```

### Test Inter-Service Communication (QUAN TRỌNG!)

**Thứ tự:** 
1. **Inventory Service** → **Create/Update Inventory**
2. **Inventory Service** → **Check Stock Availability**
3. **Order Service** → **Place Order - With Available Stock** ⭐

#### Request 1: Create Inventory
```
POST {{INVENTORY_URL}}/api/inventory/updateQuantity
Body: {
  "skuCode": "SKU-LAPTOP-001",
  "quantity": 100
}
```
✅ Response: Inventory created

#### Request 2: Check Stock
```
GET {{INVENTORY_URL}}/api/inventory/check?skuCode=SKU-LAPTOP-001&quantity=10
```
✅ Response: `true`

#### Request 3: Place Order (Order Service calls Inventory Service)
```
POST {{ORDER_URL}}/api/order
Body: {
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
✅ Response: `"Order placed successfully"`

**⚠️ IMPORTANT:** The following fields are REQUIRED:
- `price`: BigDecimal (e.g., 1299.99) → Order must have a price
- `userDetails`: Object with email, firstName, lastName → Customer information

**🎉 Nếu đến đây success → Inter-service communication hoạt động!**

### Chạy Collection Entir (Tất Cả Requests)

1. Nhấp **"▶"** button trên collection name
2. Click **"Run Microservices API Testing"**
3. Chọn **Environment:** `Local Development`
4. Click **"Run"**
5. Xem all requests chạy:

```
Health Checks:
  ✓ Eureka Health
  ✓ Product Service Health
  ✓ Inventory Service Health
  ✓ Order Service Health

Product Service:
  ✓ Create Product
  ✓ Get All Products
  ✓ Get Product by ID
  ✓ Search Products
  ✓ Update Product
  ✓ Delete Product

Inventory Service:
  ✓ Create/Update Inventory
  ✓ Get All Inventory
  ✓ Get Inventory by SKU Code
  ✓ Check Stock Availability
  ✓ Decrease Inventory

Order Service:
  ✓ Place Order - With Stock
  ✓ Place Order - Without Stock
```

---

## 🎯 Test Inter-Service Communication Một Cách Chi Tiết

### Scenario: Order Service gọi Inventory Service

#### Bước 1: Setup Inventory
```
Request: Create/Update Inventory
POST http://localhost:8083/api/inventory/updateQuantity

Body:
{
  "skuCode": "SKU-LAPTOP-001",
  "quantity": 100
}

Click: Send
Result: ✅ 201/200 Created/OK
```

#### Bước 2: Verify Stock
```
Request: Check Stock Availability
GET http://localhost:8083/api/inventory/check?skuCode=SKU-LAPTOP-001&quantity=50

Click: Send
Result: ✅ 200 OK
Response: true
```

#### Bước 3: Place Order (THE MAIN TEST!)
```
Request: Place Order - With Available Stock
POST http://localhost:8084/api/order

Body:
{
  "orderId": "ORD-001",
  "skuCode": "SKU-LAPTOP-001",
  "quantity": 2
}

Click: Send
Result: ✅ 201 CREATED
Response: "Order placed successfully"
```

**🔥 Điều gì xảy ra bên trong:**

```
Client Request
    ↓
Order Service (localhost:8084)
    ↓
Order Service gọi Inventory Service:
    GET http://inventory-service:8083/api/inventory/check?skuCode=SKU-LAPTOP-001&quantity=2
    ↓
Inventory Service (localhost:8083)
    ↓
Kiểm tra database: SELECT quantity FROM inventory WHERE sku_code = 'SKU-LAPTOP-001'
    ↓
Database trả lại: quantity = 100
    ↓
Inventory Service trả lại: true (vì 100 >= 2)
    ↓
Order Service nhận được: true
    ↓
Order Service lưu order vào database
    ↓
Order Service trả lại response: "Order placed successfully"
    ↓
Client nhận được response
```

#### Bước 4: Verify Quantity Giảm
```
Request: Check Stock Availability
GET http://localhost:8083/api/inventory/check?skuCode=SKU-LAPTOP-001&quantity=98

Click: Send
Result: ✅ 200 OK
Response: true (vì còn 98)
```

---

## 🔍 Kiểm Tra Response

### Postman UI

```
┌─────────────────────────────────────┐
│ Send | Save | Code | ...           │
└─────────────────────────────────────┘

URL: http://localhost:8084/api/order

Status: 201 Created  ← Kiểm tra cái này
Time: 150ms          ← Response time
Size: 48 B           ← Response size

Response Body:
"Order placed successfully"

Response Headers:
Content-Type: text/plain
Content-Length: 48
```

### Check Status Code

✅ Expect:
- Health checks: **200**
- Create requests: **200 hoặc 201**
- Get requests: **200**
- Update: **200**
- Delete: **200**
- Order success: **201**
- Order fail: **200 hoặc 400**

### Check Response Body

```json
Text Response:
"Order placed successfully"

JSON Response:
{
  "success": true,
  "message": "Tạo sản phẩm thành công",
  "data": {...}
}

Boolean Response:
true
```

---

## 📝 Tips & Tricks

### 1. Save Request Variables
Muốn lưu SKU code từ response để dùng ở request tiếp theo?

Vào tab **"Tests"** của "Create/Update Inventory" request:

```javascript
var jsonData = pm.response.json();
pm.environment.set("SKU_CODE", jsonData.skuCode);
```

Sau đó ở request khác, dùng: `{{SKU_CODE}}`

### 2. Add Assertions
Vào tab **"Tests"** để add validation:

```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("Response time is less than 500ms", function () {
    pm.expect(pm.response.responseTime).to.be.below(500);
});
```

### 3. Pre-request Script
Tự động generate random data:

```javascript
var timestamp = new Date().getTime();
pm.environment.set("ORDER_ID", "ORD-" + timestamp);
pm.environment.set("SKU_CODE", "SKU-" + Math.random().toString(36).substring(7));
```

### 4. Export Results
Click **"Save Response"** → JSON file để save response

---

## 🆘 Troubleshooting

| Problem | Solution |
|---------|----------|
| "Could not get any response" | Check docker-compose is running: `docker-compose ps` |
| "404 Not Found" | Check URL and method correct |
| "Product is not in stock" | Create inventory first (check step 1) |
| Variables showing as {{VAR}} | Select environment correctly |
| Slow response (>500ms) | Check service logs: `docker logs inventory-service` |

---

## 📚 Next Steps

1. ✅ Import collection
2. ✅ Setup environment
3. ✅ Run health checks
4. ✅ Test Product Service
5. ✅ Test Inventory Service
6. ✅ Test Order Service (inter-service)
7. ⏭️ (Optional) Add authentication
8. ⏭️ (Optional) Add more test cases
9. ⏭️ (Optional) Export collection for team

---

**Happy Testing! 🚀**

Nếu có vấn đề, tham khảo `POSTMAN-TESTING-GUIDE.md` để chi tiết hơn.
