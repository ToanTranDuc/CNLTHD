# Chi Tiết Giao Tiếp Giữa Các Microservices

## Sơ Đồ Kiến Trúc

```
┌─────────────────────────────────────────────────────────────┐
│                      API Gateway / Client                    │
└────────────┬──────────────────┬───────────────┬──────────────┘
             │                  │               │
             ▼                  ▼               ▼
      ┌────────────┐     ┌────────────┐  ┌────────────┐
      │  Product   │     │ Inventory  │  │   Order    │
      │  Service   │     │  Service   │  │  Service   │
      │ :8082      │     │ :8083      │  │ :8084      │
      └────┬───────┘     └────┬───────┘  └──────┬─────┘
           │                  ▲                  │
           │                  │                  │
           │    ◄─────────────┘                  │
           │                                     │
           └──────── Gọi HTTP API ──────────────┘
           
      ┌──────────────────┐
      │   Eureka Server  │  (Service Registry)
      │ :8761            │  
      └──────────────────┘
           ▲      ▲
           │      └──── Đăng ký service
           │
      Tất cả service đều đăng ký ở đây
```

## 1. Order Service → Inventory Service

### Điểm Gọi API: OrderService.placeOrder()

**Location:** `order-service/src/main/java/com/bt4/order_service/service/OrderService.java`

### Request Flow

```
Client
  └─► POST /api/order
       {
         "orderId": "ORD-001",
         "skuCode": "SKU-TEST",
         "quantity": 5
       }
       
       OrderService.placeOrder()
         └─► InventoryClient.isInStock(skuCode, quantity)
               └─► HTTP GET http://inventory-service:8083/api/inventory/check
                   ?skuCode=SKU-TEST&quantity=5
                   
                   InventoryService.isInStock()
                     └─► Kiểm tra database
                         └─◄ Return: true/false
                         
              └─► Nếu có hàng:
                  └─► Giảm tồn kho
                      └─► Hoàn tất đơn hàng
                      
              └─► Nếu không có hàng:
                  └─► Trả về lỗi "Product is not in stock"
```

### Response

```json
Success (201):
"Order placed successfully"

Error (400):
"Product is not in stock"
```

## 2. Cấu Hình FeignClient (Order → Inventory)

**File:** Tìm trong Order Service source code

Thông thường, Order Service dùng **FeignClient** để gọi Inventory Service:

```java
@FeignClient(name = "inventory-service", 
             url = "${INVENTORY_URL:http://inventory-service:8083}")
public interface InventoryClient {
    
    @GetMapping("/api/inventory/check")
    boolean isInStock(
        @RequestParam String skuCode,
        @RequestParam int quantity
    );
}
```

### Cấu Hình URL

**Development (localhost):**
```properties
# order-service/src/main/resources/application.properties
inventory.url=http://localhost:8083
```

**Docker/Production:**
```properties
# Trong docker-compose.yml environment
INVENTORY_URL=http://inventory-service:8083
```

## 3. Các API Endpoint Chi Tiết

### 3.1 Product Service

| Method | Endpoint | Request Body | Response | Mục Đích |
|--------|----------|--------------|----------|----------|
| POST | `/api/products` | `{name, price, description}` | `{id, name, price, ...}` | Tạo sản phẩm |
| GET | `/api/products` | - | `[{...}, {...}]` | Lấy tất cả |
| GET | `/api/products/{id}` | - | `{id, name, price, ...}` | Lấy 1 sản phẩm |
| GET | `/api/products/search?name=X` | - | `[{...}, {...}]` | Tìm kiếm |
| PUT | `/api/products/{id}` | `{name, price, ...}` | `{id, name, price, ...}` | Cập nhật |
| DELETE | `/api/products/{id}` | - | - | Xóa |

**Example Request:**
```bash
curl -X POST http://localhost:8082/api/products \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Laptop Dell XPS",
    "price": 1299.99,
    "description": "High-performance laptop"
  }'
```

### 3.2 Inventory Service

| Method | Endpoint | Query/Body | Response | Mục Đích |
|--------|----------|-----------|----------|----------|
| GET | `/api/inventory` | - | `[{skuCode, quantity}, ...]` | Lấy tất cả |
| GET | `/api/inventory/{skuCode}` | - | `{skuCode, quantity, ...}` | Lấy 1 item |
| GET | `/api/inventory/check` | `?skuCode=X&quantity=Y` | `true/false` | Kiểm tra có sẵn |
| POST | `/api/inventory/updateQuantity` | `{skuCode, quantity}` | `{skuCode, quantity, ...}` | Tạo/cập nhật |
| POST | `/api/inventory/decrease` | `{skuCode, quantity}` | `{skuCode, quantity, ...}` | Giảm số lượng |

**Example Request (Check Stock):**
```bash
curl "http://localhost:8083/api/inventory/check?skuCode=SKU-123&quantity=10"
# Response: true hoặc false
```

**Example Request (Update Inventory):**
```bash
curl -X POST http://localhost:8083/api/inventory/updateQuantity \
  -H "Content-Type: application/json" \
  -d '{
    "skuCode": "SKU-LAPTOP-01",
    "quantity": 50
  }'
```

### 3.3 Order Service

| Method | Endpoint | Request Body | Response | Mục Đích |
|--------|----------|--------------|----------|----------|
| POST | `/api/order` | `{orderId, skuCode, quantity}` | String message | Tạo đơn hàng |

**Example Request:**
```bash
curl -X POST http://localhost:8084/api/order \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": "ORD-20240321-001",
    "skuCode": "SKU-LAPTOP-01",
    "quantity": 2
  }'
```

## 4. Luồng Xử Lý Chi Tiết: Tạo Đơn Hàng

### Bước 1: Client gửi request
```
POST /api/order HTTP/1.1
Host: localhost:8084
Content-Type: application/json

{
  "orderId": "ORD-001",
  "skuCode": "SKU-LAPTOP-01",
  "quantity": 2
}
```

### Bước 2: Order Service xử lý
- Nhận request
- Validate dữ liệu
- Gọi InventoryClient để kiểm tra stock

### Bước 3: Order Service → Inventory Service (Inter-Service Call)
```
GET /api/inventory/check?skuCode=SKU-LAPTOP-01&quantity=2 HTTP/1.1
Host: inventory-service:8083
```

### Bước 4: Inventory Service phản hồi
```
200 OK
true
```
(hoặc `false` nếu không đủ hàng)

### Bước 5: Order Service xử lý kết quả

**Nếu có hàng:**
- Giảm inventory
- Lưu order vào database
- Publish event (nếu dùng Kafka)
- Return: "Order placed successfully"

**Nếu không có hàng:**
- Không lưu order
- Return: "Product is not in stock"

### Bước 6: Response trả về client
```
HTTP/1.1 201 Created
Content-Type: application/json

"Order placed successfully"
```

## 5. Cấu Hình Kết Nối Giữa Services

### Trong Docker Compose

```yaml
order-service:
  environment:
    # Sử dụng service name trong mạng docker
    INVENTORY_URL: http://inventory-service:8083
  depends_on:
    - inventory-service  # Đảm bảo Inventory start trước
  networks:
    - microservices-net
```

### Service Discovery (Eureka)

Ngoài cấu hình URL tĩnh, Order Service có thể gọi Inventory Service thông qua Eureka:

```java
// Trong FeignClient, không cần chỉ định url
@FeignClient(name = "inventory-service")  // Tìm qua Eureka
public interface InventoryClient {
    @GetMapping("/api/inventory/check")
    boolean isInStock(@RequestParam String skuCode, 
                      @RequestParam int quantity);
}
```

## 6. Xử Lý Lỗi & Resilience

### Circuit Breaker (Resilience4j)

Order Service sử dụng **Resilience4j** để bảo vệ khi Inventory Service không response:

```properties
resilience4j.circuitbreaker.instances.inventory.failureRateThreshold=50
resilience4j.circuitbreaker.instances.inventory.waitDurationInOpenState=5s
```

**Khi Inventory Service down:**
- 1-5 request đầu tiên: Retry
- Sau 50% requests fail: Circuit mở (fail fast)
- Chờ 5 giây
- Thử lại

## 7. Monitoring & Tracing (Zipkin)

Tất cả inter-service calls được ghi log trên **Zipkin** (port 9411):

```
Truy cập: http://localhost:9411
```

Có thể xem:
- Thời gian response của mỗi service
- Số lần retry
- Trace ID (kết nối request từ start đến finish)

## 8. Database cho mỗi Service

```
MySQL (port 3307)
├── product_service_bt4
│   └── Lưu Product entities
├── inventory_service_bt4
│   └── Lưu Inventory entities
└── order_service_bt4
    └── Lưu Order entities
```

**Mỗi service có database riêng** - Đây là pattern **Database per Service**.

## 9. Thứ Tự Khởi Động Services

1. **MySQL** - Tất cả service đều cần
2. **Eureka** - Service discovery
3. **Inventory Service** - Khởi động trước (Order phụ thuộc)
4. **Product Service** - Có thể khởi động bất kỳ lúc nào
5. **Order Service** - Khởi động cuối cùng

Điều này được cấu hình trong `docker-compose.yml`:
```yaml
depends_on:
  inventory-service:
    condition: service_healthy
```

## 10. Các Vấn Đề Có Thể Gặp & Giải Pháp

| Vấn Đề | Nguyên Nhân | Giải Pháp |
|-------|-----------|---------|
| `Connection refused` | Inventory Service không chạy | `docker-compose restart inventory-service` |
| `Timeout` | Network slow | Tăng timeout trong `OrderService` |
| `Service unavailable` | Circuit breaker mở | Chờ 5 giây hoặc restart |
| `Stock check returns false` | Inventory không có sẵn | Tạo inventory item trước |
| `Order not saved` | Database error | Kiểm tra MySQL connection |

## Test Script Kiểm Chứng Giao Tiếp

File `test-api.ps1` tự động:
1. ✓ Tạo inventory
2. ✓ Gọi Order Service
3. ✓ Order Service gọi Inventory Service
4. ✓ Kiểm tra kết quả

**Nếu test PASS** → Giao tiếp giữa services hoạt động tốt!

---

**Tóm tắt:** Order Service → HTTP GET → Inventory Service → Kiểm tra database → Return result → Order Service tạo order hoặc trả lỗi
