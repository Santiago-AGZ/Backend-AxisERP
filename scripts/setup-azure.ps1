# ============================================
# Azure Container Apps Setup Script
# Ejecutar UNA vez para crear la infraestructura
# ============================================

$ErrorActionPreference = "Stop"

# --- Config ---
$ResourceGroup = "axisERP-rg"
$Location = "eastus"
$EnvName = "axisERP-env"
$RegistryName = "axiserpreg"

# --- 1. Login ---
Write-Host "=== 1. Azure Login ===" -ForegroundColor Cyan
az login

# --- 2. Resource Group ---
Write-Host "=== 2. Crear Resource Group ===" -ForegroundColor Cyan
az group create --name $ResourceGroup --location $Location

# --- 3. Container Registry ---
Write-Host "=== 3. Crear Container Registry ===" -ForegroundColor Cyan
az acr create --resource-group $ResourceGroup --name $RegistryName --sku Basic --admin-enabled true

# --- 4. Container Apps Environment ---
Write-Host "=== 4. Crear Container Apps Environment ===" -ForegroundColor Cyan
az containerapp env create --name $EnvName --resource-group $ResourceGroup --location $Location

# --- 5. Container Apps (internos, sin ingress) ---
Write-Host "=== 5. Crear Container Apps ===" -ForegroundColor Cyan

# Auth Service
az containerapp create `
  --name auth-service `
  --resource-group $ResourceGroup `
  --environment $EnvName `
  --image mcr.microsoft.com/azuredocs/containerapps-helloworld:latest `
  --target-port 8081 `
  --ingress internal `
  --min-replicas 1 `
  --max-replicas 3 `
  --cpu 0.5 `
  --memory 1Gi

# Catalog Service
az containerapp create `
  --name catalog-service `
  --resource-group $ResourceGroup `
  --environment $EnvName `
  --image mcr.microsoft.com/azuredocs/containerapps-helloworld:latest `
  --target-port 8082 `
  --ingress internal `
  --min-replicas 1 `
  --max-replicas 3 `
  --cpu 0.5 `
  --memory 1Gi

# Inventory Service
az containerapp create `
  --name inventory-service `
  --resource-group $ResourceGroup `
  --environment $EnvName `
  --image mcr.microsoft.com/azuredocs/containerapps-helloworld:latest `
  --target-port 8083 `
  --ingress internal `
  --min-replicas 1 `
  --max-replicas 3 `
  --cpu 0.5 `
  --memory 1Gi

# Sales Service
az containerapp create `
  --name sales-service `
  --resource-group $ResourceGroup `
  --environment $EnvName `
  --image mcr.microsoft.com/azuredocs/containerapps-helloworld:latest `
  --target-port 8084 `
  --ingress internal `
  --min-replicas 1 `
  --max-replicas 3 `
  --cpu 0.5 `
  --memory 1Gi

# Purchase Service
az containerapp create `
  --name purchase-service `
  --resource-group $ResourceGroup `
  --environment $EnvName `
  --image mcr.microsoft.com/azuredocs/containerapps-helloworld:latest `
  --target-port 8086 `
  --ingress internal `
  --min-replicas 1 `
  --max-replicas 3 `
  --cpu 0.5 `
  --memory 1Gi

# Report Service
az containerapp create `
  --name report-service `
  --resource-group $ResourceGroup `
  --environment $EnvName `
  --image mcr.microsoft.com/azuredocs/containerapps-helloworld:latest `
  --target-port 8085 `
  --ingress internal `
  --min-replicas 1 `
  --max-replicas 3 `
  --cpu 0.5 `
  --memory 1Gi

# API Gateway (ingreso externo)
az containerapp create `
  --name api-gateway `
  --resource-group $ResourceGroup `
  --environment $EnvName `
  --image mcr.microsoft.com/azuredocs/containerapps-helloworld:latest `
  --target-port 8080 `
  --ingress external `
  --min-replicas 1 `
  --max-replicas 3 `
  --cpu 0.5 `
  --memory 1Gi

# --- 6. Mostrar URLs ---
Write-Host "=== 6. URLs ===" -ForegroundColor Cyan
$GatewayFqdn = az containerapp show --name api-gateway --resource-group $ResourceGroup --query properties.configuration.ingress.fqdn --output tsv
Write-Host "Gateway URL: https://$GatewayFqdn" -ForegroundColor Green

Write-Host "`n=== Setup completo ===" -ForegroundColor Green
Write-Host "Ahora configura los GitHub Secrets y ejecuta el pipeline deploy.yml"
