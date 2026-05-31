# Script para auditar bases de datos en Neon
# Extrae credenciales del .env y valida cada BD

# Leer .env
$env_file = "C:\Users\Santiago\Desktop\axisERP-platform\.env"
$env_content = Get-Content $env_file

# Extraer URLs de BD
$auth_db_url = ($env_content | Select-String "AUTH_DB_URL=") -replace "AUTH_DB_URL=" -replace ".*user=" -replace "&.*" | ForEach-Object {$_.Trim()}
$catalog_db_url = ($env_content | Select-String "CATALOG_DB_URL=") | ForEach-Object {$_.Trim()}
$inventory_db_url = ($env_content | Select-String "INVENTORY_DB_URL=") | ForEach-Object {$_.Trim()}
$sales_db_url = ($env_content | Select-String "SALES_DB_URL=") | ForEach-Object {$_.Trim()}
$purchase_db_url = ($env_content | Select-String "PURCHASE_DB_URL=") | ForEach-Object {$_.Trim()}
$report_db_url = ($env_content | Select-String "REPORT_DB_URL=") | ForEach-Object {$_.Trim()}

Write-Host "=========================================="
Write-Host "AUDITORIA NEON DATABASE - AxisERP"
Write-Host "=========================================="
Write-Host ""
Write-Host "URLs encontradas en .env:"
Write-Host "AUTH:      $(if ($auth_db_url) { 'OK' } else { 'FALTA' })"
Write-Host "CATALOG:   $(if ($catalog_db_url) { 'OK' } else { 'FALTA' })"
Write-Host "INVENTORY: $(if ($inventory_db_url) { 'OK' } else { 'FALTA' })"
Write-Host "SALES:     $(if ($sales_db_url) { 'OK' } else { 'FALTA' })"
Write-Host "PURCHASE:  $(if ($purchase_db_url) { 'OK' } else { 'FALTA' })"
Write-Host "REPORT:    $(if ($report_db_url) { 'OK' } else { 'FALTA' })"
Write-Host ""

# Para cada BD, conectar y listar tablas
$databases = @(
    @{name="AUTH"; url=$auth_db_url},
    @{name="CATALOG"; url=$catalog_db_url},
    @{name="INVENTORY"; url=$inventory_db_url},
    @{name="SALES"; url=$sales_db_url},
    @{name="PURCHASE"; url=$purchase_db_url},
    @{name="REPORT"; url=$report_db_url}
)

foreach ($db in $databases) {
    if ($db.url) {
        Write-Host "========== $($db.name) =========="
        Write-Host $db.url
        Write-Host ""
    }
}
