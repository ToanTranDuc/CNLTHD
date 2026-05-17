# Hướng dẫn Test API Giữa Các Service

## Tổng Quan

Dự án này là một kiến trúc **Microservices** gồm 4 service:

- **Eureka Service** (Port 8761): Service registry - quản lý đăng ký các service
- **Product Service** (Port 8082): Quản lý sản phẩm
- **Inventory Service** (Port 8083): Quản lý tồn kho
- **Order Service** (Port 8084): Quản lý đơn hàng (gọi Inventory Service)

## Kết Quả Test Hiện Tại

✅ **TẤT CẢ TEST ĐỀU PASS** - Không có lỗi giao tiếp giữa các service!

```
Total Tests: 12
Passed:      12
Failed:      0
```

## Test Script

### Vị trí: `test-api.ps1`

Script PowerShell này tự động kiểm tra:

1. **Health Checks** (4 test)
   - Kiểm tra từng service có hoạt động không

2. **Product Service Tests** (3 test)
   - Tạo sản phẩm
   - Lấy danh sách sản phẩm
   - Tìm kiếm sản phẩm

3. **Inventory Service Tests** (3 test)
   - Tạo/cập nhật tồn kho
   - Lấy danh sách tồn kho
   - Kiểm tra số lượng có sẵn

4. **Order Service Tests** (2 test)
   - **Quan trọng nhất**: Test gọi API từ Order Service -> Inventory Service
   - Tạo đơn hàng với tồn kho có sẵn

### Chạy Test

```powershell
# Cách 1: Từ thư mục dự án
cd c:\Study\CNLTHD\baitap4
powershell -ExecutionPolicy Bypass -File .\test-api.ps1

# Cách 2: Chạy với URL tùy chỉnh
powershell -ExecutionPolicy Bypass -File .\test-api.ps1 `
  -EurekaUrl "http://localhost:8761" `
  -ProductUrl "http://localhost:8082" `
  -InventoryUrl "http://localhost:8083" `
  -OrderUrl "http://localhost:8084"
```

## Chi Tiết Các Endpoint Được Test

### Product Service (`http://localhost:8082`)

```
POST   /api/products               - Tạo sản phẩm
GET    /api/products               - Lấy tất cả sản phẩm
GET    /api/products/{id}          - Lấy sản phẩm theo ID
GET    /api/products/search?name=X - Tìm kiếm sản phẩm
PUT    /api/products/{id}          - Cập nhật sản phẩm
DELETE /api/products/{id}          - Xóa sản phẩm
```

**Test Request:**
```json
POST /api/products
{
  "name": "Test Product",
  "price": 99.99
}
```

### Inventory Service (`http://localhost:8083`)

```
GET    /api/inventory                    - Lấy tất cả tồn kho
GET    /api/inventory/{skuCode}          - Lấy tồn kho theo SKU
GET    /api/inventory/check?skuCode=X&quantity=Y  - Kiểm tra có sẵn
POST   /api/inventory/updateQuantity     - Tạo/cập nhật tồn kho
POST   /api/inventory/decrease           - Giảm số lượng
```

**Test Request:**
```json
POST /api/inventory/updateQuantity
{
  "skuCode": "SKU-2024-001",
  "quantity": 100
}
```

### Order Service (`http://localhost:8084`)

```
POST /api/order - Tạo đơn hàng (gọi Inventory Service để kiểm tra tồn kho)
```

**Test Request:**
```json
POST /api/order
{
  "orderId": "ORD-2024-001",
  "skuCode": "SKU-2024-001",
  "quantity": 5
}
```

## Giao Tiếp Giữa Các Service

### Order Service -> Inventory Service

Khi bạn gọi `POST /api/order`, Order Service sẽ:

1. Nhận request đơn hàng
2. **Gọi API Inventory Service** để kiểm tra xem có đủ hàng không
   ```
   GET /api/inventory/check?skuCode={skuCode}&quantity={quantity}
   ```
3. Nếu có hàng: Giảm tồn kho và hoàn tất đơn hàng
4. Nếu không có hàng: Trả lỗi

### Cấu Hình Giao Tiếp

**File:** `order-service/src/main/resources/application.properties`

```properties
# URL của Inventory Service
inventory.url=http://localhost:8083
INVENTORY_URL=http://inventory-service:8083  (trong Docker)
```

## Khắc Phục Sự Cố (Troubleshooting)

### 1. Service Không Phản Hồi

**Triệu chứng:** `[FAIL] Service Health`

**Giải pháp:**
```bash
# Kiểm tra container đang chạy không
docker-compose ps

# Xem log của service bị lỗi
docker logs product-service
docker logs inventory-service
docker logs order-service

# Khởi động lại dịch vụ
docker-compose restart product-service
docker-compose restart inventory-service
docker-compose restart order-service
```

### 2. Order Service Không Thể Gọi Inventory Service

**Triệu chứng:** `[FAIL] Place Order with Available Stock`

**Giải pháp:**

a) Kiểm tra kết nối mạng:
```bash
# Từ Order container, test kết nối Inventory
docker exec order-service curl http://inventory-service:8083/api/inventory
```

b) Kiểm tra cấu hình environment:
```bash
docker-compose config | grep -A 20 "order-service:"
```

c) Kiểm tra logs:
```bash
docker logs order-service | grep -i inventory
```

### 3. Database Connection Error

**Triệu chứng:** Connection timeout khi test

**Giải pháp:**
```bash
# Kiểm tra MySQL container
docker exec mysql mysql -u root -p08102004 -e "SHOW DATABASES;"

# Kết nối lại
docker-compose down
docker-compose up -d --build
```

## Cấu Trúc Docker Compose

Các service được cấu hình như sau trong `docker-compose.yml`:

```yaml
services:
  eureka-service:
    ports: "8761:8761"
    depends_on: [none]
  
  product-service:
    ports: "8082:8082"
    depends_on: [mysql, eureka-service]
    env: INVENTORY_SERVICE_URL=http://inventory-service:8083
  
  inventory-service:
    ports: "8083:8083"
    depends_on: [mysql, eureka-service]
  
  order-service:
    ports: "8084:8084"
    depends_on: [mysql, eureka-service, kafka]
    env: INVENTORY_URL=http://inventory-service:8083
```

## Lệnh Hữu Ích

```bash
# Xem tất cả container
docker-compose ps

# Xem log real-time
docker logs -f order-service

# Xem tất cả service đã đăng ký trong Eureka
curl http://localhost:8761/eureka/apps

# Test API trực tiếp
curl -X GET http://localhost:8082/api/products

# Test POST request
curl -X POST http://localhost:8082/api/products \
  -H "Content-Type: application/json" \
  -d '{"name":"Test","price":99.99}'
```

## Khi Nào Cần Test

1. **Sau khi thay đổi code** trong bất kì service nào
2. **Trước khi deploy** lên production
3. **Khi cấu hình thay đổi** (port, URL, database)
4. **Khi có lỗi giao tiếp** giữa các service

## Test Script Chi Tiết

Script `test-api.ps1` thực hiện các bước sau:

```
1. Khởi động securely với -UseBasicParsing
2. Lặp qua 4 service, test health endpoint
3. Tạo test data ngẫu nhiên (để tránh conflict)
4. Test mỗi endpoint chính
5. Lập báo cáo: Passed/Failed
6. Exit code: 0 (success) hoặc 1 (failure)
```

## Giải Thích Kết Quả Test

```
PASS các test này:
✓ Eureka Health           - Eureka service hoạt động
✓ Product Service Health  - Product service hoạt động
✓ Inventory Service Health - Inventory service hoạt động
✓ Order Service Health    - Order service hoạt động
✓ Create Product          - Có thể gửi request đến Product Service
✓ Get All Products        - Product Service trả dữ liệu đúng
✓ Search Products         - Search endpoint hoạt động
✓ Create Inventory        - Inventory Service lưu được dữ liệu
✓ Get Inventory Items     - Inventory Service trả dữ liệu
✓ Check Stock             - Stock checking endpoint hoạt động
✓ Setup Inventory for Order - Chuẩn bị dữ liệu cho order test
✓ Place Order with Available Stock - QUAN TRỌNG: Order Service gọi Inventory Service thành công!
```

**Nếu có FAIL:**
- Kiểm tra service logs
- Xác nhận database đang chạy
- Kiểm tra network connectivity
- Xem lại configuration file

---

**Ngày cập nhật:** 2024-03-21
**Trạng thái:** Tất cả service đang hoạt động bình thường
