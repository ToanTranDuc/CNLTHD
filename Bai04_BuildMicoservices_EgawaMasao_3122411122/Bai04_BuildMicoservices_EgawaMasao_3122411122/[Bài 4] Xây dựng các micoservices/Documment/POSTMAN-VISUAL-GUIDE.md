# 🎨 Postman Visual Guide - Step by Step

## Step 1: Import Collection

### Cách 1: Drag & Drop (Easiest)
```
┌─────────────────────────────────────────────┐
│ Postman Application                         │
├─────────────────────────────────────────────┤
│ [Collections] [Environments] [APIs]         │ ← Left sidebar
│                                             │
│ Collections                                 │
│ ┌──────────────────────────────────────┐   │
│ │ (Drag file here to import)           │   │
│ │                                      │   │
│ │ Microservices_API....json ────────┐ │   │
│ │                                    ↓ │   │
│ │                                      │   │
│ └──────────────────────────────────────┘   │
│                                             │
└─────────────────────────────────────────────┘

Result: ✅ Microservices API Testing collection added
```

### Cách 2: Import Button
```
┌──────────────────────────────────────────┐
│ [Import]  [Export]  ...                  │ ← Top left
│                                          │
│ Click [Import]                           │
│            ↓                             │
│ ┌──────────────────────────────────────┐│
│ │ Upload Files │ Paste Raw Text        ││
│ ├──────────────────────────────────────┤│
│ │ [Choose Files]                       ││
│ │                                      ││
│ │ Select: Microservices_API.json       ││
│ │ Click: Import                        ││
│ └──────────────────────────────────────┘│
│            ↓                             │
│ ✅ Collection imported successfully     │
└──────────────────────────────────────────┘
```

---

## Step 2: Select Environment

```
┌──────────────────────────────────────────────┐
│ [Environments]                               │ ← Left sidebar
├──────────────────────────────────────────────┤
│                                              │
│ Environments                                 │
│ ├── Local Development ← Select this          │
│ ├── Docker Production                        │
│                                              │
└──────────────────────────────────────────────┘

Also at top-right:
┌──────────────────────────────────────────┐
│ Environment: [Local Development ▼]       │
│             │                            │
│             └─ Eureka: http://loc... ✓  │
│             └─ Product: http://loc... ✓  │
│             └─ Inventory: http://loc... ✓│
│             └─ Order: http://loc... ✓    │
└──────────────────────────────────────────┘
```

---

## Step 3: View Collection Structure

```
┌─ COLLECTIONS PANEL ─────────────────────────┐
│                                             │
│ 📦 Microservices API Testing (17 requests) │
│ │                                          │
│ ├─ 📂 Health Checks (4)                   │
│ │   ├─ GET   Eureka Health               │
│ │   ├─ GET   Product Service Health      │
│ │   ├─ GET   Inventory Service Health    │
│ │   └─ GET   Order Service Health        │
│ │                                          │
│ ├─ 📂 Product Service (6)                  │
│ │   ├─ POST  Create Product              │
│ │   ├─ GET   Get All Products            │
│ │   ├─ GET   Get Product by ID           │
│ │   ├─ GET   Search Products             │
│ │   ├─ PUT   Update Product              │
│ │   └─ DEL   Delete Product              │
│ │                                          │
│ ├─ 📂 Inventory Service (5)                │
│ │   ├─ POST  Create/Update Inventory ⭐   │
│ │   ├─ GET   Get All Inventory           │
│ │   ├─ GET   Get Inventory by SKU Code   │
│ │   ├─ GET   Check Stock Availability ⭐ │
│ │   └─ POST  Decrease Inventory          │
│ │                                          │
│ └─ 📂 Order Service - Inter-Service (2)    │
│     ├─ POST  Place Order - With Stock ⭐⭐│
│     └─ POST  Place Order - NO Stock      │
│                                             │
└─────────────────────────────────────────────┘
```

---

## Step 4: Make a Request

### Example: Check Eureka Health

```
┌─ REQUEST BUILDER ──────────────────────┐
│                                        │
│ GET  http://localhost:8761/...        │
│       ↑                                │
│       Method selector                  │
│                                        │
│       http://{{EUREKA_URL}}/actuator/health
│       ────────────────────────────────│
│       URL (using environment variable) │
│                                        │
│ [Send] [Save] [Copy]                  │
│         ↓                              │
│         Click Send                     │
│                                        │
└────────────────────────────────────────┘

RESPONSE:
┌─ RESPONSE PANEL ───────────────────────┐
│ Status: ✅ 200 OK                      │
│ Time:   42 ms                          │
│ Size:   93 B                           │
│                                        │
│ Response Body (Pretty):                │
│ {                                      │
│   "status": "UP" ← Service is running! │
│ }                                      │
│                                        │
└────────────────────────────────────────┘
```

---

## Step 5: Test Inter-Service Communication

### Workflow

```
START: Test Inter-Service Call
│
├─ STEP 1: Create Inventory
│  Request: POST /api/inventory/updateQuantity
│  Body: {
│    "skuCode": "SKU-LAPTOP-001",
│    "quantity": 100
│  }
│  ┌─ Click Send ─→ ✅ 201 Created
│  │
│  └─ Save skuCode: SKU-LAPTOP-001
│
├─ STEP 2: Verify Stock Available
│  Request: GET /api/inventory/check?skuCode=SKU-LAPTOP-001&quantity=10
│  ┌─ Click Send ─→ ✅ 200 OK, Response: true
│  │
│  └─ Inventory has stock!
│
├─ STEP 3: Place Order (THE MAIN TEST!)
│  Request: POST /api/order
│  Body: {
│    "orderId": "ORD-001",
│    "skuCode": "SKU-LAPTOP-001",
│    "quantity": 2
│  }
│  ┌─ Click Send ─→ ✅ 201 Created
│  │              ✅ "Order placed successfully"
│  │
│  ├─ BEHIND THE SCENES:
│  │  Order Service received request
│  │          ↓
│  │  Called Inventory Service:
│  │  GET http://inventory-service:8083
│  │      /api/inventory/check?...
│  │          ↓
│  │  Inventory Service checked database
│  │          ↓
│  │  Returned: true (has stock)
│  │          ↓
│  │  Order Service saved order
│  │          ↓
│  │  Returned: "Order placed successfully"
│  │
│  └─ ✅ INTER-SERVICE COMMUNICATION WORKS!
│
└─ SUCCESS: Test completed!
```

---

## Step 6: Run All Tests (Collection Runner)

```
┌─ COLLECTION RUNNER ────────────────────┐
│                                        │
│ Right-click collection                 │
│ → Click "Run collection"               │
│           ↓                            │
│ ┌────────────────────────────────┐    │
│ │ Microservices API Testing      │    │
│ │ Environment: Local Development │    │
│ │ Iterations: 1                  │    │
│ │ Delay: 100ms                   │    │
│ │                                │    │
│ │ [Run]                          │    │
│ └────────────────────────────────┘    │
│           ↓                            │
│ ┌────────────────────────────────┐    │
│ │ Running Requests...             │    │
│ │                                │    │
│ │ ✓ Eureka Health                │    │
│ │ ✓ Product Service Health       │    │
│ │ ✓ Inventory Service Health     │    │
│ │ ✓ Order Service Health         │    │
│ │ ✓ Create Product               │    │
│ │ ✓ Get All Products             │    │
│ │ ✓ Get Product by ID            │    │
│ │ ✓ Search Products              │    │
│ │ ✓ Update Product               │    │
│ │ ✓ Delete Product               │    │
│ │ ✓ Create/Update Inventory      │    │
│ │ ✓ Get All Inventory            │    │
│ │ ✓ Get Inventory by SKU         │    │
│ │ ✓ Check Stock Availability     │    │
│ │ ✓ Decrease Inventory           │    │
│ │ ✓ Place Order - With Stock     │    │
│ │ ✓ Place Order - Without Stock  │    │
│ │                                │    │
│ └────────────────────────────────┘    │
│           ↓                            │
│ Results: 17 passed, 0 failed           │
│ Execution time: 2.5 seconds            │
│                                        │
│ ✅ ALL TESTS PASSED!                  │
│                                        │
└────────────────────────────────────────┘
```

---

## Step 7: Understand Request Body

### POST Request Example

```
┌─ REQUEST BUILDER ──────────────────────┐
│ POST http://localhost:8084/api/order   │
│                                        │
│ [Params] [Authorization] [Headers]    │
│ [Body] ← Click here                   │
│ [Pre-request Script] [Tests]          │
│                                        │
├─────────────────────────────────────────┤
│ [raw ▼] [JSON ▼]                        │
│                                        │
│ {                                      │
│   "orderId": "ORD-001",               │
│   "skuCode": "SKU-LAPTOP-001",        │
│   "quantity": 2                       │
│ }                                      │
│                                        │
│ [Send] [Save]                         │
│                                        │
└────────────────────────────────────────┘
```

### GET Request with Query Parameters

```
┌─ REQUEST BUILDER ──────────────────────┐
│ GET http://localhost:8083/api/...     │
│                                        │
│ [Params] ← Click here for query params │
│ [Authorization] [Headers]              │
│ [Body] [Pre-request Script] [Tests]   │
│                                        │
├─────────────────────────────────────────┤
│ Key              Value                  │
│ ─────────────────────────────────────  │
│ skuCode          SKU-LAPTOP-001        │
│ quantity         10                    │
│                                        │
│ URL auto updates:                      │
│ ...check?skuCode=SKU-LAPTOP-001&...   │
│                                        │
│ [Send]                                 │
│                                        │
└────────────────────────────────────────┘
```

---

## Step 8: Check Response Headers

```
┌─ RESPONSE PANEL ───────────────────────┐
│ Status: 201 Created                    │
│ Time:   150ms                          │
│ Size:   48 B                           │
│                                        │
│ [Body] [Cookies] [Headers] [Tests]    │
│         ← Click Headers                │
│                                        │
├─────────────────────────────────────────┤
│ Content-Type:      text/plain          │
│ Content-Length:    48                  │
│ Date:              Thu, 21 Mar 2024... │
│ Server:            Apache-Coyote/1.1   │
│ Transfer-Encoding: chunked             │
│                                        │
└────────────────────────────────────────┘
```

---

## Step 9: Fix Issues

### If Status is 500 (Internal Error)

```
Postman Response:
┌──────────────────────────────────────┐
│ Status: ❌ 500 Internal Server Error  │
│ Time:   5000ms (SLOW!)                │
│ Size:   1024 B                        │
│                                       │
│ Response:                             │
│ {                                     │
│   "error": "Product is not in stock"  │
│ }                                     │
│                                       │
└──────────────────────────────────────┘

FIX:
1. Check if Inventory exists
   → Run "Create/Update Inventory" first

2. Check service logs
   → docker logs inventory-service

3. Restart services
   → docker-compose restart
```

### If Status is 404 (Not Found)

```
Postman Response:
┌──────────────────────────────────────┐
│ Status: ❌ 404 Not Found               │
│                                       │
│ Response:                             │
│ {                                     │
│   "error": "Not Found"                │
│ }                                     │
│                                       │
└──────────────────────────────────────┘

FIX:
1. Check URL is correct
   {{PRODUCT_URL}}/api/products ✓
   
2. Check method (GET/POST/PUT/DELETE)

3. Check environment selected
   Environment: Local Development ✓
```

---

## Step 10: Export Results

```
┌─ SAVE TEST RESULTS ────────────────────┐
│                                        │
│ After running collection:              │
│                                        │
│ Results panel shows:                   │
│ ├─ Requests run: 17                   │
│ ├─ Passed: 17 ✓                       │
│ ├─ Failed: 0                          │
│ ├─ Execution time: 2.5s               │
│                                        │
│ Click "Save Results" button            │
│      ↓                                 │
│ Download as JSON or HTML report       │
│      ↓                                 │
│ Share with team!                       │
│                                        │
└────────────────────────────────────────┘
```

---

## Keyboard Shortcuts

```
Ctrl+S       Save request
Ctrl+B       Send request (same as Click Send)
Ctrl+E       Switch environment
Ctrl+Alt+L   Format body (JSON)
Ctrl+/       Find in collection
```

---

## Color Legend in Postman

```
🟢 Green   = Request methods working well
🔵 Blue    = GET requests
🟡 Orange  = POST requests
🟠 Orange  = PUT/PATCH requests
🔴 Red     = DELETE requests
⚪ White   = Status OK (2xx)
🟠 Orange  = Status warnings (3xx, 4xx)
🔴 Red     = Status errors (5xx)
```

---

## Final Checklist Visual

```
✅ Import Collection
├─ ✅ Postman opened
├─ ✅ Collection file ready
└─ ✅ Imported successfully

✅ Setup Environment
├─ ✅ "Local Development" created
├─ ✅ {{EUREKA_URL}} = http://localhost:8761
├─ ✅ {{PRODUCT_URL}} = http://localhost:8082
├─ ✅ {{INVENTORY_URL}} = http://localhost:8083
└─ ✅ {{ORDER_URL}} = http://localhost:8084

✅ Run Health Checks
├─ ✅ Eureka Health: UP
├─ ✅ Product Service: UP
├─ ✅ Inventory Service: UP
└─ ✅ Order Service: UP

✅ Test APIs
├─ ✅ Product Service requests: PASS
├─ ✅ Inventory Service requests: PASS
└─ ✅ Order Service requests: PASS

✅ Test Inter-Service Communication
├─ ✅ Create inventory: SUCCESS
├─ ✅ Check stock: true
└─ ✅ Place order: "Order placed successfully"

🎉 DONE! All tests passing!
```

---

**Remember:** 
- 🟢 Green status = Good
- 🔴 Red status = Problem
- Check logs if red
- Restart if stuck

**Happy Testing! 🚀**
