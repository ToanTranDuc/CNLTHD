# Quick Reference: Troubleshooting & Commands

## 🚀 Chạy Test API

```powershell
# Chạy test API giữa các service
cd c:\Study\CNLTHD\baitap4
powershell -ExecutionPolicy Bypass -File .\test-api.ps1
```

**Kết quả mong đợi:** 12 PASS, 0 FAIL

---

## 🐳 Docker Commands

```powershell
# Kiểm tra tất cả container
docker-compose ps

# Khởi động tất cả service
docker-compose up -d --build

# Dừng tất cả service
docker-compose down

# Xem log của một service
docker logs -f order-service
docker logs -f inventory-service
docker logs -f product-service

# Restart một service
docker-compose restart order-service

# Xem log chi tiết
docker logs order-service | Select-String "inventory"
```

---

## 🔍 Kiểm Tra Services

### Health Check
```powershell
# Eureka
curl http://localhost:8761/actuator/health

# Product Service
curl http://localhost:8082/actuator/health

# Inventory Service
curl http://localhost:8083/actuator/health

# Order Service
curl http://localhost:8084/actuator/health
```

### Kiểm Tra Service Registration
```powershell
# Xem tất cả service đã đăng ký
curl http://localhost:8761/eureka/apps

# Xem chi tiết một service
curl http://localhost:8761/eureka/apps/product-service
curl http://localhost:8761/eureka/apps/inventory-service
curl http://localhost:8761/eureka/apps/order-service
```

---

## 📊 Test Manual APIs

### Product Service

```powershell
# Tạo sản phẩm
$product = @{
    name = "Test Laptop"
    price = 1299.99
}
Invoke-WebRequest -Uri "http://localhost:8082/api/products" `
    -Method POST `
    -Body ($product | ConvertTo-Json) `
    -ContentType "application/json" `
    -UseBasicParsing

# Lấy tất cả sản phẩm
Invoke-WebRequest -Uri "http://localhost:8082/api/products" `
    -UseBasicParsing

# Tìm kiếm
Invoke-WebRequest -Uri "http://localhost:8082/api/products/search?name=Laptop" `
    -UseBasicParsing
```

### Inventory Service

```powershell
# Tạo inventory
$inventory = @{
    skuCode = "SKU-LAPTOP-001"
    quantity = 100
}
Invoke-WebRequest -Uri "http://localhost:8083/api/inventory/updateQuantity" `
    -Method POST `
    -Body ($inventory | ConvertTo-Json) `
    -ContentType "application/json" `
    -UseBasicParsing

# Kiểm tra stock
Invoke-WebRequest -Uri "http://localhost:8083/api/inventory/check?skuCode=SKU-LAPTOP-001&quantity=10" `
    -UseBasicParsing

# Lấy tất cả inventory
Invoke-WebRequest -Uri "http://localhost:8083/api/inventory" `
    -UseBasicParsing
```

### Order Service

```powershell
# Tạo đơn hàng
$order = @{
    orderId = "ORD-001"
    skuCode = "SKU-LAPTOP-001"
    quantity = 2
}
Invoke-WebRequest -Uri "http://localhost:8084/api/order" `
    -Method POST `
    -Body ($order | ConvertTo-Json) `
    -ContentType "application/json" `
    -UseBasicParsing
```

---

## 🗄️ Database Commands

```powershell
# Kết nối MySQL
docker exec -it mysql mysql -u root -p08102004

# Xem databases
SHOW DATABASES;

# Xem tables trong product_service_bt4
USE product_service_bt4;
SHOW TABLES;

# Xem data
SELECT * FROM products;
SELECT * FROM inventory;
SELECT * FROM orders;
```

---

## ⚙️ Cấu Hình URLs

### Local Development
```properties
Product Service:   http://localhost:8082
Inventory Service: http://localhost:8083
Order Service:     http://localhost:8084
Eureka Service:    http://localhost:8761
```

### Inside Docker
```properties
Product Service:   http://product-service:8082
Inventory Service: http://inventory-service:8083
Order Service:     http://order-service:8084
Eureka Service:    http://eureka-service:8761
```

---

## 🔧 Debug Order Service Calling Inventory

```powershell
# Xem logs khi Order gọi Inventory
docker logs order-service | Select-String "inventory"

# Test từ Order container
docker exec order-service curl http://inventory-service:8083/api/inventory

# Xem environment variables
docker exec order-service env | Select-String "INVENTORY"
```

---

## 📈 Monitoring

### Zipkin (Distributed Tracing)
```
URL: http://localhost:9411
```

Xem chi tiết:
- Thời gian response mỗi service
- Order Service gọi Inventory Service
- Trace ID cho mỗi request

### Kafka (Event Streaming)
```
URL: http://localhost:9092
```

---

## 🐛 Common Errors & Fixes

### Error: "Connection refused on Inventory Service"
```powershell
# Giải pháp:
docker-compose restart inventory-service

# Verify:
curl http://localhost:8083/actuator/health
```

### Error: "Product is not in stock"
```powershell
# Tạo inventory trước:
$inventory = @{ skuCode = "SKU-TEST"; quantity = 100 }
Invoke-WebRequest -Uri "http://localhost:8083/api/inventory/updateQuantity" `
    -Method POST -Body ($inventory | ConvertTo-Json) `
    -ContentType "application/json" -UseBasicParsing

# Sau đó tạo order:
$order = @{ orderId = "ORD-001"; skuCode = "SKU-TEST"; quantity = 5 }
Invoke-WebRequest -Uri "http://localhost:8084/api/order" `
    -Method POST -Body ($order | ConvertTo-Json) `
    -ContentType "application/json" -UseBasicParsing
```

### Error: "Service not found in Eureka"
```powershell
# Kiểm tra service đã register chưa:
curl http://localhost:8761/eureka/apps

# Nếu chưa, check logs:
docker logs product-service | Select-String "Eureka\|eureka\|register"

# Restart service:
docker-compose restart product-service
```

### Error: "Timeout connecting to database"
```powershell
# Kiểm tra MySQL:
docker exec -it mysql mysql -u root -p08102004 -e "SHOW DATABASES;"

# Nếu lỗi, rebuild:
docker-compose down -v
docker-compose up -d --build
```

---

## 📝 Log Locations

```
Docker Logs:
- docker logs order-service
- docker logs inventory-service  
- docker logs product-service
- docker logs eureka-service

Application Logs (inside container):
- /var/log/app.log (nếu có)
- stdout/stderr (thông qua docker logs)
```

---

## 🎯 Kiểm Tra Giao Tiếp Inter-Service

### Phương pháp 1: Test Script (Recommended)
```powershell
.\test-api.ps1
# Check output: "Place Order with Available Stock" có PASS không
```

### Phương pháp 2: Manual Test
```powershell
# 1. Tạo inventory
$inventory = @{ skuCode = "TEST-SKU"; quantity = 50 }
Invoke-WebRequest -Uri "http://localhost:8083/api/inventory/updateQuantity" `
    -Method POST -Body ($inventory | ConvertTo-Json) `
    -ContentType "application/json" -UseBasicParsing

# 2. Tạo order (Order Service sẽ gọi Inventory Service)
$order = @{ orderId = "TEST-ORD"; skuCode = "TEST-SKU"; quantity = 5 }
$response = Invoke-WebRequest -Uri "http://localhost:8084/api/order" `
    -Method POST -Body ($order | ConvertTo-Json) `
    -ContentType "application/json" -UseBasicParsing

# 3. Kiểm tra response
Write-Host $response.Content
# Expected: "Order placed successfully"
```

### Phương pháp 3: Check Logs
```powershell
# Xem log Order Service
docker logs order-service | Select-String "inventory-service"

# Expected: Không có error liên quan đến gọi inventory
```

---

## 📊 Performance Check

```powershell
# Measure response time
$sw = [System.Diagnostics.Stopwatch]::StartNew()
Invoke-WebRequest -Uri "http://localhost:8082/api/products" -UseBasicParsing
$sw.Stop()
Write-Host "Response time: $($sw.ElapsedMilliseconds)ms"

# Expected: < 500ms cho localhost
```

---

## 🔐 Security & Best Practices

```
- Không expose database password trong logs
- Sử dụng environment variables cho secrets
- Kiểm tra logs thường xuyên để detect anomalies
- Maintain circuit breaker configuration
- Set appropriate timeouts
```

---

**Cheat Sheet Updated:** 2024-03-21
**All Tests:** PASSING ✅
