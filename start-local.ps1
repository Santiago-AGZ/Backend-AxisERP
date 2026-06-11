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
$env:CATALOG_SERVICE_URL = "http://localhost:8082"
$env:AUTH_SERVICE_URL = "http://localhost:8081"
$env:INVENTORY_SERVICE_URL = "http://localhost:8083"
$env:SALES_SERVICE_URL = "http://localhost:8084"
$env:PURCHASE_SERVICE_URL = "http://localhost:8086"
$env:REPORT_SERVICE_URL = "http://localhost:8085"
$env:CORS_ALLOWED_ORIGINS = "http://localhost:3000,http://localhost:5173"
$env:INTERNAL_API_KEY = "hJPSHD9pn3Yhya54LRIHrvDGlbR2UzEwlE9nuTT8wz7"
$env:JWT_SECRET = "a1b2c3d4e5f6a7b8c9d0e1f2a3b4c5d6e7f8a9b0c1d2e3f4a5b6c7d8e9f0a1b2"
$env:JPA_DDL_AUTO = "validate"
$env:SUPABASE_URL = "https://hbtcusxbkkefphunarwn.supabase.co"
$env:SUPABASE_JWT_ISSUER = "https://hbtcusxbkkefphunarwn.supabase.co/auth/v1"
$env:SUPABASE_SERVICE_ROLE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImhidGN1c3hia2tlZnBodW5hcnduIiwicm9sZSI6InNlcnZpY2Vfcm9sZSIsImlhdCI6MTc3ODM3NTI1NiwiZXhwIjoyMDkzOTUxMjU2fQ.-3zQoXXLzFPKhJpW7SgzA-JE37N8J-xa6xymTpl_JtY"
$env:SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImhidGN1c3hia2tlZnBodW5hcnduIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzgzNzUyNTYsImV4cCI6MjA5Mzk1MTI1Nn0.FpKfLdzSlHBOtW4gpVFzxtzGAyfxWfcniWnEdwV63rw"

# ============================================
# BD URLs desde el .env
# ============================================
$AUTH_DB_URL = "jdbc:postgresql://ep-wispy-voice-aqodv5c1-pooler.c-8.us-east-1.aws.neon.tech/neondb?sslmode=require&channel_binding=require"
$AUTH_DB_USER = "neondb_owner"
$AUTH_DB_PASS = "npg_3B6IOxjNFvoU"

$CATALOG_DB_URL = "jdbc:postgresql://ep-rapid-night-aq5mpoqi-pooler.c-8.us-east-1.aws.neon.tech/neondb?sslmode=require&channel_binding=require"
$CATALOG_DB_USER = "neondb_owner"
$CATALOG_DB_PASS = "npg_W9Zr4nodvQih"

$INVENTORY_DB_URL = "jdbc:postgresql://ep-still-resonance-apfxlmlm-pooler.c-7.us-east-1.aws.neon.tech/neondb?sslmode=require&channel_binding=require"
$INVENTORY_DB_USER = "neondb_owner"
$INVENTORY_DB_PASS = "npg_4GmCpJk3bgqa"

$SALES_DB_URL = "jdbc:postgresql://ep-misty-waterfall-apbais5g-pooler.c-7.us-east-1.aws.neon.tech/neondb?sslmode=require&channel_binding=require"
$SALES_DB_USER = "neondb_owner"
$SALES_DB_PASS = "npg_qtvUFx6Ol8Ve"

$PURCHASE_DB_URL = "jdbc:postgresql://ep-quiet-union-aqsh56i9-pooler.c-8.us-east-1.aws.neon.tech/neondb?sslmode=require&channel_binding=require"
$PURCHASE_DB_USER = "neondb_owner"
$PURCHASE_DB_PASS = "npg_xTB0s1gojVbU"

$REPORT_DB_URL = "jdbc:postgresql://ep-flat-dream-aphtqln9-pooler.c-7.us-east-1.aws.neon.tech/neondb?sslmode=require&channel_binding=require"
$REPORT_DB_USER = "neondb_owner"
$REPORT_DB_PASS = "npg_hnYTb4BIov5X"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  AxisERP - Inicio Local" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# ============================================
# Gateway
# ============================================
function Start-Gateway {
    $env:AUTH_SERVICE_URL = "http://localhost:8081"
    $env:CATALOG_SERVICE_URL = "http://localhost:8082"
    $env:INVENTORY_SERVICE_URL = "http://localhost:8083"
    $env:SALES_SERVICE_URL = "http://localhost:8084"
    $env:PURCHASE_SERVICE_URL = "http://localhost:8086"
    $env:REPORT_SERVICE_URL = "http://localhost:8085"
    $env:CORS_ALLOWED_ORIGINS = "http://localhost:3000,http://localhost:5173"
    Start-Process -WindowStyle Normal -FilePath "pwsh" -ArgumentList "-NoExit", "-Command", "cd '$ROOT/api-gateway'; Write-Host '[GATEWAY] Iniciando en :8080...' -ForegroundColor Green; mvn spring-boot:run -q"
}

# ============================================
# Auth Service
# ============================================
function Start-Auth {
    $env:AUTH_DB_URL = $AUTH_DB_URL
    $env:AUTH_DB_USERNAME = $AUTH_DB_USER
    $env:AUTH_DB_PASSWORD = $AUTH_DB_PASS
    $env:JPA_DDL_AUTO = "validate"
    Start-Process -WindowStyle Normal -FilePath "pwsh" -ArgumentList "-NoExit", "-Command", "cd '$ROOT/auth-service'; Write-Host '[AUTH] Iniciando en :8081...' -ForegroundColor Green; mvn spring-boot:run -q"
}

# ============================================
# Catalog Service
# ============================================
function Start-Catalog {
    $env:CATALOG_DB_URL = $CATALOG_DB_URL
    $env:CATALOG_DB_USERNAME = $CATALOG_DB_USER
    $env:CATALOG_DB_PASSWORD = $CATALOG_DB_PASS
    Start-Process -WindowStyle Normal -FilePath "pwsh" -ArgumentList "-NoExit", "-Command", "cd '$ROOT/catalog-service'; Write-Host '[CATALOG] Iniciando en :8082...' -ForegroundColor Green; mvn spring-boot:run -q"
}

# ============================================
# Inventory Service
# ============================================
function Start-Inventory {
    $env:INVENTORY_DB_URL = $INVENTORY_DB_URL
    $env:INVENTORY_DB_USERNAME = $INVENTORY_DB_USER
    $env:INVENTORY_DB_PASSWORD = $INVENTORY_DB_PASS
    Start-Process -WindowStyle Normal -FilePath "pwsh" -ArgumentList "-NoExit", "-Command", "cd '$ROOT/inventory-service'; Write-Host '[INVENTORY] Iniciando en :8083...' -ForegroundColor Green; mvn spring-boot:run -q"
}

# ============================================
# Sales Service
# ============================================
function Start-Sales {
    $env:SALES_DB_URL = $SALES_DB_URL
    $env:SALES_DB_USERNAME = $SALES_DB_USER
    $env:SALES_DB_PASSWORD = $SALES_DB_PASS
    Start-Process -WindowStyle Normal -FilePath "pwsh" -ArgumentList "-NoExit", "-Command", "cd '$ROOT/sales-service'; Write-Host '[SALES] Iniciando en :8084...' -ForegroundColor Green; mvn spring-boot:run -q"
}

# ============================================
# Report Service
# ============================================
function Start-Report {
    $env:REPORT_DB_URL = $REPORT_DB_URL
    $env:REPORT_DB_USERNAME = $REPORT_DB_USER
    $env:REPORT_DB_PASSWORD = $REPORT_DB_PASS
    Start-Process -WindowStyle Normal -FilePath "pwsh" -ArgumentList "-NoExit", "-Command", "cd '$ROOT/report-service'; Write-Host '[REPORT] Iniciando en :8085...' -ForegroundColor Green; mvn spring-boot:run -q"
}

# ============================================
# Purchase Service
# ============================================
function Start-Purchase {
    $env:PURCHASE_DB_URL = $PURCHASE_DB_URL
    $env:PURCHASE_DB_USERNAME = $PURCHASE_DB_USER
    $env:PURCHASE_DB_PASSWORD = $PURCHASE_DB_PASS
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
Write-Host "Elige qué levantar:" -ForegroundColor Yellow
Write-Host "  1) BACKEND COMPLETO (los 7 servicios)" -ForegroundColor White
Write-Host "  2) FRONTEND solo" -ForegroundColor White
Write-Host "  3) AMBOS (backend + frontend)" -ForegroundColor White
Write-Host "  4) Servicio específico" -ForegroundColor White

if ($Frontend) { $choice = 2 }
elseif ($Backend) { $choice = 1 }
else { $choice = Read-Host "Opción (1-4)" }

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
        Write-Host "✅ 7 servicios iniciados. Espera ~30 seg a que cada uno termine de compilar." -ForegroundColor Green
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
        Write-Host "✅ Todo iniciado" -ForegroundColor Green
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
