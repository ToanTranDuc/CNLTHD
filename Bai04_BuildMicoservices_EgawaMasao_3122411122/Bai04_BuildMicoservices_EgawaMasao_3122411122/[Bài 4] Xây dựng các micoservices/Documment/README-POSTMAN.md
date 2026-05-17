# 📖 Postman Testing - Summary & Quick Links

## 📁 Các File Đã Tạo

### 1. **POSTMAN-QUICK-START.md** ⭐ (Bắt đầu từ đây!)
   - Cách import collection nhanh nhất
   - Setup environment
   - Chạy tests từng request
   - Test inter-service communication
   - Tips & tricks
   
   **👉 Bắt đầu:** Mở file này trước!

### 2. **POSTMAN-TESTING-GUIDE.md** (Chi tiết đầy đủ)
   - Tạo Workspace & Environment
   - Từng request với full body & headers
   - Cách sử dụng Postman features
   - Pre-request scripts
   - Tests/Assertions
   - Collection Runner
   - Troubleshooting chi tiết

### 3. **Microservices_API.postman_collection.json** (Collection File)
   - File collection ready to import
   - 17 pre-configured requests
   - Environment variables đã setup
   - Descriptions cho mỗi request
   - 📥 **Cách dùng:** Import file này vào Postman

### 4. **Test Reports** (Từ test script)
   - TEST-REPORT.md - Báo cáo test chi tiết
   - API-TESTING-GUIDE.md - Hướng dẫn test endpoints
   - INTER-SERVICE-COMMUNICATION.md - Chi tiết giao tiếp

---

## 🚀 3 Cách Setup Postman

### Option 1: Import Collection (FAST ✅ Recommended)
```
1. Mở Postman
2. Click "Import" → Chọn "Microservices_API.postman_collection.json"
3. ✅ Done! Tất cả requests đã được setup
4. Chọn environment "Local Development"
5. Chạy requests
```
⏱️ **Thời gian:** 2 phút

### Option 2: Manual Setup (DETAILED)
```
1. Tạo Workspace
2. Tạo Collection
3. Thêm 17 requests theo hướng dẫn
4. Setup environment variables
5. Chạy tests
```
⏱️ **Thời gian:** 15 phút

### Option 3: Hybrid (FAST + DETAILED)
```
1. Import collection (Option 1)
2. Customize requests nếu cần
3. Add tests/assertions (Optional)
4. Run collection
```
⏱️ **Thời gian:** 5 phút

---

## 📋 Quick Command Reference

### In Postman

```
Import Collection:
  → Click Import → Upload Microservices_API.postman_collection.json

Select Environment:
  → Right sidebar: "Local Development"

Run Single Request:
  → Click request → Click "Send"

Run All Requests:
  → Right-click collection → Run collection

Test Inter-Service:
  → Create Inventory (Inventory Service)
  → Place Order (Order Service calls Inventory)
  → Check response: "Order placed successfully"
```

### In Terminal (For Reference)

```powershell
# Kiểm tra services
docker-compose ps

# View logs
docker logs order-service
docker logs inventory-service

# Restart services
docker-compose restart

# Health check (curl)
curl http://localhost:8082/actuator/health
```

---

## 📊 Collection Structure

```
Microservices API Testing (17 requests)

1️⃣  Health Checks (4 requests)
    - Verify all services are running

2️⃣  Product Service (6 requests)
    - Product management APIs

3️⃣  Inventory Service (5 requests)
    - Inventory management APIs
    ⭐ IMPORTANT: "Create/Update Inventory" & "Check Stock"

4️⃣  Order Service - Inter-Service (2 requests)
    ⭐⭐ TEST THIS: Order service calls Inventory service
    - "Place Order - With Stock" (should succeed)
    - "Place Order - Without Stock" (should fail)
```

---

## 🎯 Test Workflow (Recommended Order)

### Phase 1: Health Check
```
1. Eureka Health
2. Product Service Health
3. Inventory Service Health
4. Order Service Health
```
✅ All should return `{"status": "UP"}`

### Phase 2: Basic API Testing
```
5. Get All Products
6. Create Product
7. Search Products

8. Get All Inventory
9. Create/Update Inventory
10. Check Stock Availability
```
✅ All should return 200 OK

### Phase 3: Inter-Service Communication (MAIN TEST)
```
11. Create/Update Inventory (Setup)
    → Verify inventory created: ✅ 201 Created
    
12. Check Stock Availability (Verify)
    → Verify stock available: ✅ Response: true
    
13. Place Order - With Available Stock (Test Order → Inventory call)
    → Order Service calls Inventory Service
    → Verify response: ✅ "Order placed successfully"
```

**🔥 Nếu step 13 success = Inter-service communication works!**

### Phase 4: Error Cases
```
14. Place Order - Without Available Stock
    → Verify error handling: ✅ "Product is not in stock"
```

---

## 📝 Important Notes

### ⚠️ Lưu Ý Quan Trọng

1. **Chạy setup request trước:** 
   - Phải chạy "Create/Update Inventory" trước khi "Place Order"
   - Nếu không: Order sẽ fail với "Product is not in stock"

2. **Verify Stock:**
   - Chạy "Check Stock Availability" để verify inventory
   - Nên chạy sau "Create/Update Inventory"

3. **Environment:**
   - Phải select "Local Development" environment
   - Variables auto fill {{PRODUCT_URL}}, {{INVENTORY_URL}}, etc.

4. **Order → Inventory Call:**
   - Order Service gọi Inventory Service HTTP API
   - Nằm bên trong "Place Order" request
   - Không thấy trong Postman UI, nhưng xảy ra bên backend
   - Kiểm tra logs: `docker logs order-service`

---

## 🔗 Giải Thích Gọi API

### Request Flow: Place Order

```
┌─ Postman Client ─────────────────┐
│ POST /api/order                  │
│ Body: {orderId, skuCode, qty}    │
│                                  │
│ http://localhost:8084           │
└──────────────┬────────────────────┘
               │
               ▼
┌─ Order Service :8084 ─────────────┐
│ 1. Nhận request                  │
│ 2. Gọi Inventory Service         │
│    GET /api/inventory/check      │
│    ?skuCode=...&quantity=...     │
│                                  │
│    http://inventory-service:8083 │
└──────────────┬────────────────────┘
               │
               ▼
┌─ Inventory Service :8083 ─────────┐
│ 1. Nhận request từ Order Service  │
│ 2. Kiểm tra database             │
│ 3. Trả lại: true/false           │
└──────────────┬────────────────────┘
               │
               ▼
┌─ Order Service (tiếp) ───────────┐
│ Nhận kết quả từ Inventory        │
│                                  │
│ Nếu true:                       │
│   → Lưu order                    │
│   → Giảm inventory              │
│   → Return: Success             │
│                                  │
│ Nếu false:                      │
│   → Không lưu order             │
│   → Return: Error               │
└──────────────┬────────────────────┘
               │
               ▼
┌─ Postman Client ─────────────────┐
│ Response 201 Created             │
│ "Order placed successfully"      │
└──────────────────────────────────┘
```

---

## 📚 File Reference

| File | Purpose | Read When |
|------|---------|-----------|
| **POSTMAN-QUICK-START.md** | Step-by-step import & test | 🔴 **FIRST** |
| **POSTMAN-TESTING-GUIDE.md** | Detailed guide | Need details |
| **Microservices_API.postman_collection.json** | Import file | Drag to Postman |
| **API-TESTING-GUIDE.md** | API endpoint details | Reference |
| **INTER-SERVICE-COMMUNICATION.md** | Architecture & flow | Understanding |
| **TEST-REPORT.md** | Test results & summary | Final report |
| **QUICK-REFERENCE.md** | Command cheatsheet | Quick lookup |

---

## ✅ Checklist: Setup Complete

When you finish, you should have:

- [ ] Postman installed
- [ ] Collection imported (`Microservices_API.postman_collection.json`)
- [ ] Environment created (`Local Development`)
- [ ] All 4 health checks pass
- [ ] Product Service requests work
- [ ] Inventory Service requests work
- [ ] **Order Service calls Inventory Service successfully** ⭐
- [ ] All 17 requests tested

---

## 🎓 Learning Path

1. **Understood Microservices?**
   → Read: INTER-SERVICE-COMMUNICATION.md

2. **Ready to Test?**
   → Read: POSTMAN-QUICK-START.md
   → Import: Microservices_API.postman_collection.json

3. **Need Detailed Guide?**
   → Read: POSTMAN-TESTING-GUIDE.md

4. **Want API Endpoint Reference?**
   → Read: API-TESTING-GUIDE.md

5. **Looking at Test Results?**
   → Read: TEST-REPORT.md

---

## 🎯 Success Criteria

### ✅ Test is SUCCESSFUL when:

1. ✅ All health checks return 200 OK
2. ✅ Product Service endpoints work
3. ✅ Inventory Service endpoints work
4. ✅ **Order Service can call Inventory Service**
   - Create inventory item
   - Check stock returns true
   - Place order returns "Order placed successfully"
5. ✅ No 500 errors
6. ✅ Response times < 500ms

### ❌ Test FAILED if:

- ❌ Any service health check fails
- ❌ "Could not get any response" error
- ❌ "Product is not in stock" when inventory exists
- ❌ Timeout errors
- ❌ 500 Internal Server Error

---

## 🆘 Help

### If Collection Won't Import
- File corrupted? → Use manual setup in POSTMAN-TESTING-GUIDE.md
- Wrong format? → Try pasting raw JSON in Import → "Paste Raw Text"

### If Services Don't Respond
```powershell
docker-compose ps
docker logs order-service
docker restart
```

### If Order Service Doesn't Call Inventory
- Check logs: `docker logs order-service | Select-String "inventory"`
- Check environment: `docker exec order-service env | grep INVENTORY`

### For Other Issues
- Read troubleshooting section in POSTMAN-TESTING-GUIDE.md
- Check API-TESTING-GUIDE.md endpoint descriptions

---

## 🎉 Next: How to Run Tests Offline

Không cần command line! Dùng Postman UI:

```
1. Open Collection
2. Click "▶ Run Microservices API Testing"
3. Environment: "Local Development"
4. Click "Run"
5. Xem all requests execute automatically
```

**Bonus:** Export test results as report

---

**Created:** 2024-03-21  
**Updated:** Latest test results all PASS ✅  
**Ready to Use:** YES 🚀
