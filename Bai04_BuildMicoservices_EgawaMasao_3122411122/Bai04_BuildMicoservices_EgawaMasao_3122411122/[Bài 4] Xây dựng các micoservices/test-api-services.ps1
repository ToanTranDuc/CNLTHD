# ============================================================================
# API Testing Script for Microservices Architecture
# ============================================================================

param(
    [string]$EurekaUrl = "http://localhost:8761",
    [string]$ProductUrl = "http://localhost:8082",
    [string]$InventoryUrl = "http://localhost:8083",
    [string]$OrderUrl = "http://localhost:8084",
    [int]$TimeoutSeconds = 30
)

# Test results tracking
$TestResults = @{
    Passed = 0
    Failed = 0
}

# Helper function to test HTTP requests
function Test-HttpRequest {
    param(
        [string]$TestName,
        [string]$Method,
        [string]$Url,
        [object]$Body = $null,
        [int]$ExpectedStatusCode = 200
    )
    
    try {
        $Params = @{
            Uri = $Url
            Method = $Method
            TimeoutSec = $TimeoutSeconds
            ErrorAction = "Stop"
            Headers = @{"Content-Type" = "application/json"}
        }
        
        if ($Body) {
            $Params.Body = $Body | ConvertTo-Json -Depth 10
        }
        
        $Response = Invoke-WebRequest @Params
        $IsSuccess = $Response.StatusCode -eq $ExpectedStatusCode
        
        if ($IsSuccess) {
            Write-Host "[PASS] $TestName" -ForegroundColor Green
            $TestResults.Passed++
        } else {
            Write-Host "[WARN] $TestName - Status: $($Response.StatusCode)" -ForegroundColor Yellow
            $TestResults.Failed++
        }
        
        return $Response.Content | ConvertFrom-Json
    } catch {
        Write-Host "[FAIL] $TestName - $($_.Exception.Message)" -ForegroundColor Red
        $TestResults.Failed++
        return $null
    }
}

# Helper function to test service health
function Test-ServiceHealth {
    param([string]$ServiceName, [string]$Url)
    
    Write-Host "`n=== Testing $ServiceName ===" -ForegroundColor Cyan
    
    try {
        $Response = Invoke-WebRequest -Uri $Url -TimeoutSec $TimeoutSeconds -ErrorAction Stop
        Write-Host "[PASS] $ServiceName is reachable (Status: $($Response.StatusCode))" -ForegroundColor Green
        $TestResults.Passed++
        return $true
    } catch {
        Write-Host "[FAIL] $ServiceName is reachable - $($_.Exception.Message)" -ForegroundColor Red
        $TestResults.Failed++
        return $false
    }
}



# ============================================================================
# Check Services Health
# ============================================================================

Write-Host "`n╔════════════════════════════════════════════════════════════╗" -ForegroundColor Cyan
Write-Host "║           MICROSERVICES HEALTH CHECK                       ║" -ForegroundColor Cyan
Write-Host "╚════════════════════════════════════════════════════════════╝" -ForegroundColor Cyan

$EurekaHealthy = Test-ServiceHealth "Eureka Service" "$EurekaUrl/actuator/health"
$ProductHealthy = Test-ServiceHealth "Product Service" "$ProductUrl/actuator/health"
$InventoryHealthy = Test-ServiceHealth "Inventory Service" "$InventoryUrl/actuator/health"
$OrderHealthy = Test-ServiceHealth "Order Service" "$OrderUrl/actuator/health"

# ============================================================================
# Test Product Service
# ============================================================================

if ($ProductHealthy) {
    Write-Host "`n╔════════════════════════════════════════════════════════════╗" -ForegroundColor Cyan
    Write-Host "║         PRODUCT SERVICE API TESTS                           ║" -ForegroundColor Cyan
    Write-Host "╚════════════════════════════════════════════════════════════╝" -ForegroundColor Cyan
    
    # Create a test product
    $NewProduct = @{
        name = "Test Product $(Get-Date -Format 'yyyyMMddHHmmss')"
        price = 99.99
        description = "Test product"
    }
    
    $ProductResult = Test-HttpRequest "Create Product" "POST" "$ProductUrl/api/products" $NewProduct 200
    
    if ($ProductResult) {
        # Try to get ID from response
        $ProductId = $ProductResult.data.id
        if ($ProductId) {
            Test-HttpRequest "Get Product by ID" "GET" "$ProductUrl/api/products/$ProductId" $null 200 | Out-Null
        }
    }
    
    # Get all products
    Test-HttpRequest "Get All Products" "GET" "$ProductUrl/api/products" $null 200 | Out-Null
    
    # Search products
    Test-HttpRequest "Search Products by Name" "GET" "$ProductUrl/api/products/search?name=Test" $null 200 | Out-Null
}

# ============================================================================
# Test Inventory Service
# ============================================================================

if ($InventoryHealthy) {
    Write-Host "`n╔════════════════════════════════════════════════════════════╗" -ForegroundColor Cyan
    Write-Host "║         INVENTORY SERVICE API TESTS                         ║" -ForegroundColor Cyan
    Write-Host "╚════════════════════════════════════════════════════════════╝" -ForegroundColor Cyan
    
    # Create test inventory item
    $SkuCode = "SKU-TEST-$(Get-Date -Format 'yyyyMMddHHmmss')"
    $NewInventory = @{
        skuCode = $SkuCode
        quantity = 100
    }
    
    Test-HttpRequest "Create/Update Inventory" "POST" "$InventoryUrl/api/inventory/updateQuantity" $NewInventory 200 | Out-Null
    
    # Get all inventory
    Test-HttpRequest "Get All Inventory Items" "GET" "$InventoryUrl/api/inventory" $null 200 | Out-Null
    
    # Check if item is in stock
    Test-HttpRequest "Check Stock Availability" "GET" "$InventoryUrl/api/inventory/check?skuCode=$SkuCode&quantity=10" $null 200 | Out-Null
    
    # Get inventory by SKU
    Test-HttpRequest "Get Inventory by SKU Code" "GET" "$InventoryUrl/api/inventory/$SkuCode" $null 200 | Out-Null
}

# ============================================================================
# Test Order Service (Inter-Service Communication)
# ============================================================================

if ($OrderHealthy -and $InventoryHealthy) {
    Write-Host "`n╔════════════════════════════════════════════════════════════╗" -ForegroundColor Cyan
    Write-Host "║    ORDER SERVICE & INTER-SERVICE COMMUNICATION TEST        ║" -ForegroundColor Cyan
    Write-Host "╚════════════════════════════════════════════════════════════╝" -ForegroundColor Cyan
    
    # Create inventory for order test
    $OrderSkuCode = "SKU-ORDER-$(Get-Date -Format 'yyyyMMddHHmmss')"
    $InventorySetup = @{
        skuCode = $OrderSkuCode
        quantity = 50
    }
    
    Write-Host "Setting up inventory for order test..." -ForegroundColor Cyan
    Test-HttpRequest "Create Inventory for Order Test" "POST" "$InventoryUrl/api/inventory/updateQuantity" $InventorySetup 200 | Out-Null
    
    # Test placing an order (this calls inventory service)
    $OrderRequest = @{
        orderId = "ORD-$(Get-Date -Format 'yyyyMMddHHmmss')"
        skuCode = $OrderSkuCode
        quantity = 5
    }
    
    Test-HttpRequest "Place Order (With Available Stock)" "POST" "$OrderUrl/api/order" $OrderRequest 201 | Out-Null
}

# ============================================================================
# Summary Report
# ============================================================================

Write-Host "`n╔════════════════════════════════════════════════════════════╗" -ForegroundColor Cyan
Write-Host "║                    TEST SUMMARY REPORT                     ║" -ForegroundColor Cyan
Write-Host "╚════════════════════════════════════════════════════════════╝" -ForegroundColor Cyan

$Total = $TestResults.Passed + $TestResults.Failed
Write-Host "`nTotal Tests: $Total" -ForegroundColor Cyan
Write-Host "Passed: $($TestResults.Passed)" -ForegroundColor Green
Write-Host "Failed: $($TestResults.Failed)" -ForegroundColor Red

if ($Total -gt 0) {
    $SuccessRate = [math]::Round(($TestResults.Passed / $Total) * 100, 2)
    Write-Host "Success Rate: $SuccessRate%" -ForegroundColor Cyan
}

Write-Host "`n=== Test Execution Complete ===" -ForegroundColor $(if ($TestResults.Failed -eq 0) { "Green" } else { "Red" })

exit $(if ($TestResults.Failed -eq 0) { 0 } else { 1 })

