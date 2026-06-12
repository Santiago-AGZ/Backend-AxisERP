# ============================================
# AxisERP - Inicio Local (7 microservicios)
# ============================================
# Requisitos: Java 21, Maven, Node.js 20+
# 
# Usa las BD de Neon del .env (no necesitas Postgres local)
# ============================================

param(
    [switch]$Frontend,
    [switch]$Backend
)

$ROOT = $PSScriptRoot

# ============================================
# Variables de entorno para modo LOCAL
# ============================================
$env:catalog-service-url = "http://localhost:8082"
$env:auth-service-url = "http://localhost:8081"
$env:inventory-service-url = "http://localhost:8083"
$env:sales-service-url = "http://localhost:8084"
$env:purchase-service-url = "http://localhost:8086"
$env:report-service-url = "http://localhost:8085"
$env:cors-allowed-origins = "http://localhost:3000,http://localhost:5173"
$env:internal-api-key = "hJPSHD9pn3Yhya54LRIHrvDGlbR2UzEwlE9nuTT8wz7"
$env:jwt-secret = "a1b2c3d4e5f6a7b8c9d0e1f2a3b4c5d6e7f8a9b0c1d2e3f4a5b6c7d8e9f0a1b2"
$env:jpa-ddl-auto = "validate"
$env:supabase-url = "https://hbtcusxbkkefphunarwn.supabase.co"
$env:supabase-jwt-issuer = "https://hbtcusxbkkefphunarwn.supabase.co/auth/v1"
$env:supabase-service-role-key = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImhidGN1c3hia2tlZnBodW5hcnduIiwicm9sZSI6InNlcnZpY2Vfcm9sZSIsImlhdCI6MTc3ODM3NTI1NiwiZXhwIjoyMDkzOTUxMjU2fQ.-3zQoXXLzFPKhJpW7SgzA-JE37N8J-xa6xymTpl_JtY"
$env:supabase-anon-key = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImhidGN1c3hia2tlZnBodW5hcnduIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzgzNzUyNTYsImV4cCI6MjA5Mzk1MTI1Nn0.FpKfLdzSlHBOtW4gpVFzxtzGAyfxWfcniWnEdwV63rw"

# ============================================
# BD URLs desde el .env
# ============================================
$auth-db-url = "jdbc:postgresql://ep-wispy-voice-aqodv5c1-pooler.c-8.us-east-1.aws.neon.tech/neondb?sslmode=require&channel_binding=require"
$auth-db-username = "neondb_owner"
$auth-db-password = "npg_3B6IOxjNFvoU"

$catalog-db-url = "jdbc:postgresql://ep-rapid-night-aq5mpoqi-pooler.c-8.us-east-1.aws.neon.tech/neondb?sslmode=require&channel_binding=require"
$catalog-db-username = "neondb_owner"
$catalog-db-password = "npg_W9Zr4nodvQih"

$inventory-db-url = "jdbc:postgresql://ep-still-resonance-apfxlmlm-pooler.c-7.us-east-1.aws.neon.tech/neondb?sslmode=require&channel_binding=require"
$inventory-db-username = "neondb_owner"
$inventory-db-password = "npg_4GmCpJk3bgqa"

$sales-db-url = "jdbc:postgresql://ep-misty-waterfall-apbais5g-pooler.c-7.us-east-1.aws.neon.tech/neondb?sslmode=require&channel_binding=require"
$sales-db-username = "neondb_owner"
$sales-db-password = "npg_qtvUFx6Ol8Ve"

$purchase-db-url = "jdbc:postgresql://ep-quiet-union-aqsh56i9-pooler.c-8.us-east-1.aws.neon.tech/neondb?sslmode=require&channel_binding=require"
$purchase-db-username = "neondb_owner"
$purchase-db-password = "npg_xTB0s1gojVbU"

$report-db-url = "jdbc:postgresql://ep-flat-dream-aphtqln9-pooler.c-7.us-east-1.aws.neon.tech/neondb?sslmode=require&channel_binding=require"
$report-db-username = "neondb_owner"
$report-db-password = "npg_hnYTb4BIov5X"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  AxisERP - Inicio Local" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# ============================================
# Gateway
# ============================================
function Start-Gateway {
    $env:auth-service-url = "http://localhost:8081"
    $env:catalog-service-url = "http://localhost:8082"
    $env:inventory-service-url = "http://localhost:8083"
    $env:sales-service-url = "http://localhost:8084"
    $env:purchase-service-url = "http://localhost:8086"
    $env:report-service-url = "http://localhost:8085"
    $env:cors-allowed-origins = "http://localhost:3000,http://localhost:5173"
    $env:server-port = "8080"
    Start-Process -WindowStyle Normal -FilePath "pwsh" -ArgumentList "-NoExit", "-Command", "cd '$ROOT/api-gateway'; Write-Host '[GATEWAY] Iniciando en :8080...' -ForegroundColor Green; mvn spring-boot:run -q"
}

# ============================================
# Auth Service
# ============================================
function Start-Auth {
    $env:auth-db-url = $auth-db-url
    $env:auth-db-username = $auth-db-username
    $env:auth-db-password = $auth-db-password
    $env:jpa-ddl-auto = "validate"
    $env:server-port = "8081"
    Start-Process -WindowStyle Normal -FilePath "pwsh" -ArgumentList "-NoExit", "-Command", "cd '$ROOT/auth-service'; Write-Host '[AUTH] Iniciando en :8081...' -ForegroundColor Green; mvn spring-boot:run -q"
}

# ============================================
# Catalog Service
# ============================================
function Start-Catalog {
    $env:catalog-db-url = $catalog-db-url
    $env:catalog-db-username = $catalog-db-username
    $env:catalog-db-password = $catalog-db-password
    $env:server-port = "8082"
    Start-Process -WindowStyle Normal -FilePath "pwsh" -ArgumentList "-NoExit", "-Command", "cd '$ROOT/catalog-service'; Write-Host '[CATALOG] Iniciando en :8082...' -ForegroundColor Green; mvn spring-boot:run -q"
}

# ============================================
# Inventory Service
# ============================================
function Start-Inventory {
    $env:inventory-db-url = $inventory-db-url
    $env:inventory-db-username = $inventory-db-username
    $env:inventory-db-password = $inventory-db-password
    $env:server-port = "8083"
    Start-Process -WindowStyle Normal -FilePath "pwsh" -ArgumentList "-NoExit", "-Command", "cd '$ROOT/inventory-service'; Write-Host '[INVENTORY] Iniciando en :8083...' -ForegroundColor Green; mvn spring-boot:run -q"
}

# ============================================
# Sales Service
# ============================================
function Start-Sales {
    $env:sales-db-url = $sales-db-url
    $env:sales-db-username = $sales-db-username
    $env:sales-db-password = $sales-db-password
    $env:server-port = "8084"
    Start-Process -WindowStyle Normal -FilePath "pwsh" -ArgumentList "-NoExit", "-Command", "cd '$ROOT/sales-service'; Write-Host '[SALES] Iniciando en :8084...' -ForegroundColor Green; mvn spring-boot:run -q"
}

# ============================================
# Report Service
# ============================================
function Start-Report {
    $env:report-db-url = $report-db-url
    $env:report-db-username = $report-db-username
    $env:report-db-password = $report-db-password
    $env:server-port = "8085"
    Start-Process -WindowStyle Normal -FilePath "pwsh" -ArgumentList "-NoExit", "-Command", "cd '$ROOT/report-service'; Write-Host '[REPORT] Iniciando en :8085...' -ForegroundColor Green; mvn spring-boot:run -q"
}

# ============================================
# Purchase Service
# ============================================
function Start-Purchase {
    $env:purchase-db-url = $purchase-db-url
    $env:purchase-db-username = $purchase-db-username
    $env:purchase-db-password = $purchase-db-password
    $env:server-port = "8086"
    Start-Process -WindowStyle Normal -FilePath "pwsh" -ArgumentList "-NoExit", "-Command", "cd '$ROOT/purchase-service'; Write-Host '[PURCHASE] Iniciando en :8086...' -ForegroundColor Green; mvn spring-boot:run -q"
}

# ============================================
# Frontend
# ============================================
function Start-Frontend {
    Start-Process -WindowStyle Normal -FilePath "pwsh" -ArgumentList "-NoExit", "-Command", "cd '$ROOT/Frontend-AxisERP'; Write-Host '[FRONTEND] Iniciando en :5173...' -ForegroundColor Green; npx vite --port 5173"
}

# ============================================
# Menu principal
# ============================================
Write-Host "Elige que levantar:" -ForegroundColor Yellow
Write-Host "  1) BACKEND COMPLETO (los 7 servicios)" -ForegroundColor White
Write-Host "  2) FRONTEND solo" -ForegroundColor White
Write-Host "  3) AMBOS (backend + frontend)" -ForegroundColor White
Write-Host "  4) Servicio especifico" -ForegroundColor White

if ($Frontend) { $choice = 2 }
elseif ($Backend) { $choice = 1 }
else { $choice = Read-Host "Opcion (1-4)" }

switch ($choice) {
    1 {
        Write-Host "Iniciando todos los servicios backend..." -ForegroundColor Cyan
        Start-Gateway
        Start-Sleep 2
        Start-Auth
        Start-Sleep 1
        Start-Catalog
        Start-Sleep 1
        Start-Inventory
        Start-Sleep 1
        Start-Sales
        Start-Sleep 1
        Start-Report
        Start-Sleep 1
        Start-Purchase
        Write-Host ""
        Write-Host "OK 7 servicios iniciados. Espera ~30 seg a que cada uno termine de compilar." -ForegroundColor Green
        Write-Host "   Gateway: http://localhost:8080" -ForegroundColor Yellow
        Write-Host "   Health:  http://localhost:8080/actuator/health" -ForegroundColor Yellow
    }
    2 {
        Start-Frontend
        Write-Host "   Frontend: http://localhost:5173" -ForegroundColor Yellow
    }
    3 {
        Write-Host "Iniciando backend + frontend..." -ForegroundColor Cyan
        Start-Gateway
        Start-Sleep 2
        Start-Auth
        Start-Sleep 1
        Start-Catalog
        Start-Sleep 1
        Start-Inventory
        Start-Sleep 1
        Start-Sales
        Start-Sleep 1
        Start-Report
        Start-Sleep 1
        Start-Purchase
        Start-Sleep 5
        Start-Frontend
        Write-Host ""
        Write-Host "OK Todo iniciado" -ForegroundColor Green
    }
    4 {
        Write-Host ""
        Write-Host "  a) api-gateway     (:8080)" -ForegroundColor White
        Write-Host "  b) auth-service    (:8081)" -ForegroundColor White
        Write-Host "  c) catalog-service (:8082)" -ForegroundColor White
        Write-Host "  d) inventory-svc   (:8083)" -ForegroundColor White
        Write-Host "  e) sales-service   (:8084)" -ForegroundColor White
        Write-Host "  f) report-service  (:8085)" -ForegroundColor White
        Write-Host "  g) purchase-svc    (:8086)" -ForegroundColor White
        Write-Host "  h) frontend        (:5173)" -ForegroundColor White
        $svc = Read-Host "Servicio (a-h)"
        switch ($svc) {
            'a' { Start-Gateway }
            'b' { Start-Auth }
            'c' { Start-Catalog }
            'd' { Start-Inventory }
            'e' { Start-Sales }
            'f' { Start-Report }
            'g' { Start-Purchase }
            'h' { Start-Frontend }
        }
    }
}
