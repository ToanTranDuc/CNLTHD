#!/usr/bin/env pwsh

param(
    [string]$EurekaUrl = "http://localhost:8761",
    [string]$ProductUrl = "http://localhost:8082",
    [string]$InventoryUrl = "http://localhost:8083",
    [string]$OrderUrl = "http://localhost:8084"
)

$Passed = 0
$Failed = 0

function Test-Endpoint {
    param([string]$Name, [string]$Method, [string]$Uri, [object]$Body)
    
    try {
        $params = @{
            Uri = $Uri
            Method = $Method
            TimeoutSec = 20
            ErrorAction = "Stop"
            ContentType = "application/json"
            UseBasicParsing = $true
        }
        
        if ($Body) {
            $params.Body = $Body | ConvertTo-Json
        }
        
        $response = Invoke-WebRequest @params
        Write-Host "[PASS] $Name" -ForegroundColor Green
        $script:Passed++
        return $response
    } catch {
        Write-Host "[FAIL] $Name - $_" -ForegroundColor Red
        $script:Failed++
        return $null
    }
}

Write-Host "" 
Write-Host "==== MICROSERVICES API TEST ====" -ForegroundColor Cyan
Write-Host "Testing inter-service communication..." -ForegroundColor Cyan
Write-Host ""

# Check services health
Write-Host "1. Health Checks:" -ForegroundColor Yellow
Test-Endpoint "Eureka Health" "GET" "$EurekaUrl/actuator/health" $null | Out-Null
Test-Endpoint "Product Service Health" "GET" "$ProductUrl/actuator/health" $null | Out-Null
Test-Endpoint "Inventory Service Health" "GET" "$InventoryUrl/actuator/health" $null | Out-Null
Test-Endpoint "Order Service Health" "GET" "$OrderUrl/actuator/health" $null | Out-Null

# Test Product Service
Write-Host ""
Write-Host "2. Product Service Tests:" -ForegroundColor Yellow
$product = @{ name = "Test"; price = 99.99 }
Test-Endpoint "Create Product" "POST" "$ProductUrl/api/products" $product | Out-Null
Test-Endpoint "Get All Products" "GET" "$ProductUrl/api/products" $null | Out-Null
Test-Endpoint "Search Products" "GET" "$ProductUrl/api/products/search?name=Test" $null | Out-Null

# Test Inventory Service
Write-Host ""
Write-Host "3. Inventory Service Tests:" -ForegroundColor Yellow
$sku = "SKU-$(Get-Random)"
$inventory = @{ skuCode = $sku; quantity = 100 }
Test-Endpoint "Create Inventory" "POST" "$InventoryUrl/api/inventory/updateQuantity" $inventory | Out-Null
Test-Endpoint "Get Inventory Items" "GET" "$InventoryUrl/api/inventory" $null | Out-Null
Test-Endpoint "Check Stock" "GET" "$InventoryUrl/api/inventory/check?skuCode=$sku&quantity=10" $null | Out-Null

# Test Order Service (Inter-service call to Inventory)
Write-Host ""
Write-Host "4. Order Service Tests (Inter-Service Communication):" -ForegroundColor Yellow
$orderSku = "SKU-ORDER-$(Get-Random)"
$invSetup = @{ skuCode = $orderSku; quantity = 50 }
Write-Host "   - Setting up inventory..." -ForegroundColor Gray
Test-Endpoint "Setup Inventory for Order" "POST" "$InventoryUrl/api/inventory/updateQuantity" $invSetup | Out-Null

Write-Host "   - Placing order (calls Inventory Service)..." -ForegroundColor Gray
$order = @{ 
    orderId = "ORD-$(Get-Random)"; 
    skuCode = $orderSku; 
    quantity = 5;
    price = 99.99;
    userDetails = @{
        email = "customer@example.com"
        firstName = "John"
        lastName = "Doe"
    }
}
Test-Endpoint "Place Order with Available Stock" "POST" "$OrderUrl/api/order" $order | Out-Null

# Summary
Write-Host ""
Write-Host "==== TEST SUMMARY ====" -ForegroundColor Cyan
Write-Host "Total Tests: $($Passed + $Failed)" -ForegroundColor White
Write-Host "Passed: $Passed" -ForegroundColor Green
Write-Host "Failed: $Failed" -ForegroundColor Red

if ($Failed -eq 0) {
    Write-Host ""
    Write-Host "Success: All tests passed! Services are working." -ForegroundColor Green
    exit 0
} else {
    Write-Host ""
    Write-Host "Error: Some tests failed. Check service logs." -ForegroundColor Red
    exit 1
}
