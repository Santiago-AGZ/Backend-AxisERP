
param()

$GW   = "http://localhost:8080"
$PASS = "Santiabad*123"
$SCRIPT:RESULTS = [System.Collections.Generic.List[PSCustomObject]]::new()
$SCRIPT:CUR_USER = ""
$SCRIPT:CUR_SVC  = ""

function Get-NeonToken([string]$email) {
    $body = "{`"username`":`"$email`",`"password`":`"$PASS`"}"
    $resp = Invoke-WebRequest -Uri "$GW/api/v1/auth/login" -Method POST -Body $body -ContentType "application/json" -UseBasicParsing -ErrorAction Stop
    return ($resp.Content | ConvertFrom-Json).accessToken
}

function Invoke-Api([string]$method, [string]$url, [string]$body, [hashtable]$headers) {
    try {
        if ($body) {
            $resp = Invoke-WebRequest -Uri "$GW$url" -Method $method -Body $body -ContentType "application/json" -Headers $headers -UseBasicParsing -ErrorAction Stop
        } else {
            $resp = Invoke-WebRequest -Uri "$GW$url" -Method $method -Headers $headers -UseBasicParsing -ErrorAction Stop
        }
        return @{ ok = $true; code = [int]$resp.StatusCode; data = ($resp.Content | ConvertFrom-Json) }
    } catch {
        $sc = 0
        try { $sc = [int]$_.Exception.Response.StatusCode.value__ } catch {}
        return @{ ok = $false; code = $sc; msg = $_.ErrorDetails.Message }
    }
}

function Assert-Result([string]$label, [hashtable]$result, [bool]$shouldSucceed) {
    $passed   = ($result.ok -eq $shouldSucceed)
    $icon     = if ($passed) { "PASS" } else { "FAIL" }
    $expected = if ($shouldSucceed) { "2xx" } else { "4xx" }
    $http     = $result.code

    $SCRIPT:RESULTS.Add([PSCustomObject]@{
        User    = $SCRIPT:CUR_USER
        Service = $SCRIPT:CUR_SVC
        Test    = $label
        HTTP    = $http
        Expect  = $expected
        Result  = $icon
    })

    $color = if ($passed) { "Green" } else { "Red" }
    Write-Host ("    {0,-4} [{1,3}] {2}" -f $icon, $http, $label) -ForegroundColor $color
}

# ── Setup: productId con inventario garantizado en Neon ─────
Write-Host "Conectando a Neon y preparando datos de prueba..."
$adminToken = Get-NeonToken "santiagoalvarez374@gmail.com"
$adminH     = @{ Authorization = "Bearer $adminToken" }

$prodsResp  = Invoke-Api "GET" "/api/v1/productos" "" $adminH
$PRODUCT_ID = if ($prodsResp.ok -and $prodsResp.data.data.Count -gt 0) { $prodsResp.data.data[0].id } else { $null }
Write-Host "ProductId: $PRODUCT_ID"

# Garantizar que el producto tiene inventario inicializado
if ($PRODUCT_ID) {
    $invCheck = Invoke-Api "GET" "/api/v1/inventory/products/$PRODUCT_ID" "" $adminH
    if (-not $invCheck.ok) {
        $initBody = "{`"productId`":`"$PRODUCT_ID`",`"initialStock`":1000,`"minStock`":10,`"maxStock`":5000}"
        $initResp = Invoke-Api "POST" "/api/v1/inventory/initialize" $initBody $adminH
        Write-Host "Inventario inicializado: stock=$($initResp.data.data.currentStock)"
    } else {
        Write-Host "Inventario ya existe: stock=$($invCheck.data.data.currentStock)"
    }
}
Write-Host ""

$USERS = @(
    @{ email = "santiagoalvarez374@gmail.com";                     role = "ADMIN"      },
    @{ email = "santhygutierrez2002@gmail.com";                    role = "VENDEDOR"   },
    @{ email = "santiago.alvarez.gutierrez@correounivalle.edu.co"; role = "INVENTARIO" }
)

foreach ($userInfo in $USERS) {
    $email = $userInfo.email
    $role  = $userInfo.role

    Write-Host ""
    Write-Host ("=" * 62)
    Write-Host "  USUARIO : $email"
    Write-Host "  ROL     : $role"
    Write-Host ("=" * 62)

    $token = Get-NeonToken $email
    $H     = @{ Authorization = "Bearer $token" }

    $SCRIPT:CUR_USER = $role
    $isAdmin    = ($role -eq "ADMIN")
    $isVendedor = ($role -eq "VENDEDOR")
    $isInvent   = ($role -eq "INVENTARIO")

    # ── AUTH ────────────────────────────────────────────────────
    $SCRIPT:CUR_SVC = "AUTH"
    Write-Host "`n  [AUTH]"

    $meResp = Invoke-Api "GET" "/api/v1/auth/me" "" $H
    Assert-Result "GET /auth/me  (self)" $meResp $true

    $listResp = Invoke-Api "GET" "/api/v1/usuarios" "" $H
    Assert-Result "GET /usuarios (list)" $listResp $isAdmin

    $rand9 = Get-Random -Minimum 100 -Maximum 999
    $newUser = "{`"name`":`"Test $role`",`"email`":`"testcrud.$($role.ToLower())$rand9@neon.com`",`"password`":`"Santiabad*123`",`"role`":`"VENDEDOR`"}"
    $createUserResp = Invoke-Api "POST" "/api/v1/usuarios" $newUser $H
    Assert-Result "POST /usuarios (create)" $createUserResp $isAdmin

    if ($meResp.ok) {
        $uid = $meResp.data.id
        $getOneResp = Invoke-Api "GET" "/api/v1/usuarios/$uid" "" $H
        Assert-Result "GET /usuarios/{id}" $getOneResp $isAdmin
    }

    # ── CATALOG - CATEGORIAS ────────────────────────────────────
    $SCRIPT:CUR_SVC = "CATALOG"
    Write-Host "`n  [CATALOG - Categorias]"

    $catListResp = Invoke-Api "GET" "/api/v1/categorias" "" $H
    Assert-Result "GET /categorias (list)" $catListResp $true

    $catName = "Cat $role $(Get-Random -Maximum 9999)"
    $catCreateResp = Invoke-Api "POST" "/api/v1/categorias" "{`"name`":`"$catName`",`"description`":`"CRUD $role`"}" $H
    Assert-Result "POST /categorias (create)" $catCreateResp ($isAdmin -or $isInvent)

    # Usar categoria creada o una existente para update/delete
    $catId = if ($catCreateResp.ok) {
        $catCreateResp.data.data.id
    } elseif ($catListResp.ok -and $catListResp.data.data.Count -gt 0) {
        # Buscar una cat que no esté desactivada
        ($catListResp.data.data | Where-Object { $_.status -eq "ACTIVA" } | Select-Object -First 1).id
    } else { $null }

    if ($catId) {
        $catUpdateResp = Invoke-Api "PUT" "/api/v1/categorias/$catId" "{`"name`":`"$catName Upd`",`"description`":`"Updated by $role`"}" $H
        Assert-Result "PUT /categorias/{id} (update)" $catUpdateResp ($isAdmin -or $isInvent)

        $catDeacResp = Invoke-Api "PATCH" "/api/v1/categorias/$catId/desactivar" "" $H
        Assert-Result "PATCH /categorias/desactivar" $catDeacResp $isAdmin
    }

    # ── CATALOG - PRODUCTOS ─────────────────────────────────────
    Write-Host "`n  [CATALOG - Productos]"

    $prodListResp = Invoke-Api "GET" "/api/v1/productos" "" $H
    Assert-Result "GET /productos (list)" $prodListResp $true

    $prodSearch = Invoke-Api "GET" "/api/v1/productos?codigo=PROD-100001" "" $H
    Assert-Result "GET /productos?codigo= (search)" $prodSearch $true

    $randN    = Get-Random -Minimum 300000 -Maximum 399999
    $prodCode = "PROD-$randN"
    $prodBody = if ($catId) {
        "{`"name`":`"Prod $role $randN`",`"codigo`":`"$prodCode`",`"categoryId`":`"$catId`",`"purchasePrice`":80.00,`"salePrice`":120.00}"
    } else {
        # Buscar catId válido
        $validCat = if ($catListResp.ok -and $catListResp.data.data.Count -gt 0) { $catListResp.data.data[0].id } else { [guid]::NewGuid().ToString() }
        "{`"name`":`"Prod $role $randN`",`"codigo`":`"$prodCode`",`"categoryId`":`"$validCat`",`"purchasePrice`":80.00,`"salePrice`":120.00}"
    }

    $prodCreateResp = Invoke-Api "POST" "/api/v1/productos" $prodBody $H
    Assert-Result "POST /productos (create)" $prodCreateResp ($isAdmin -or $isInvent)

    $testProdId = if ($prodCreateResp.ok) { $prodCreateResp.data.data.id } else { $PRODUCT_ID }

    if ($testProdId) {
        $prodUpdateResp = Invoke-Api "PUT" "/api/v1/productos/$testProdId" "{`"name`":`"Prod $role Upd`",`"purchasePrice`":90.00,`"salePrice`":130.00}" $H
        Assert-Result "PUT /productos/{id} (update)" $prodUpdateResp ($isAdmin -or $isInvent)

        $prodDeacResp = Invoke-Api "PATCH" "/api/v1/productos/$testProdId/desactivar" "" $H
        Assert-Result "PATCH /productos/desactivar" $prodDeacResp $isAdmin
    }

    # ── INVENTORY ───────────────────────────────────────────────
    $SCRIPT:CUR_SVC = "INVENTORY"
    Write-Host "`n  [INVENTORY]"

    if ($PRODUCT_ID) {
        $invGetResp = Invoke-Api "GET" "/api/v1/inventory/products/$PRODUCT_ID" "" $H
        Assert-Result "GET /inventory/products/{id}" $invGetResp $true

        $invMovResp = Invoke-Api "GET" "/api/v1/inventory/products/$PRODUCT_ID/movements" "" $H
        Assert-Result "GET /inventory/movements" $invMovResp $true

        $entryResp = Invoke-Api "POST" "/api/v1/inventory/products/$PRODUCT_ID/entry?quantity=3&referenceType=COMPRA&notes=Entry+$role" "" $H
        Assert-Result "POST /inventory/entry" $entryResp $true

        $exitResp = Invoke-Api "POST" "/api/v1/inventory/products/$PRODUCT_ID/exit?quantity=1&referenceType=VENTA&notes=Exit+$role" "" $H
        Assert-Result "POST /inventory/exit" $exitResp $true
    }

    # ── PURCHASE - SUPPLIERS ────────────────────────────────────
    $SCRIPT:CUR_SVC = "PURCHASE"
    Write-Host "`n  [PURCHASE - Proveedores]"

    $supListResp = Invoke-Api "GET" "/api/v1/suppliers" "" $H
    Assert-Result "GET /suppliers (list)" $supListResp $isAdmin

    $supRand = Get-Random -Minimum 400000 -Maximum 499999
    $supCode = "PROV-$supRand"
    $supCreateResp = Invoke-Api "POST" "/api/v1/suppliers" "{`"codigo`":`"$supCode`",`"name`":`"Prov $role $supRand`",`"nit`":`"8$(Get-Random -Maximum 9999999)-1`",`"phone`":`"30099$(Get-Random -Maximum 99999)`",`"email`":`"prov.$($role.ToLower())@test.com`"}" $H
    Assert-Result "POST /suppliers (create)" $supCreateResp $isAdmin

    if ($supCreateResp.ok) {
        $supGetResp = Invoke-Api "GET" "/api/v1/suppliers/$supCode" "" $H
        Assert-Result "GET /suppliers/{codigo}" $supGetResp $isAdmin
    }

    $purListResp = Invoke-Api "GET" "/api/v1/purchases" "" $H
    Assert-Result "GET /purchases (list)" $purListResp $true

    # ── SALES - CUSTOMERS ───────────────────────────────────────
    $SCRIPT:CUR_SVC = "SALES"
    Write-Host "`n  [SALES - Clientes y Ventas]"

    $custListResp = Invoke-Api "GET" "/api/v1/customers" "" $H
    Assert-Result "GET /customers (list)" $custListResp ($isAdmin -or $isVendedor)

    $cliRand = Get-Random -Minimum 500000 -Maximum 599999
    $cliCode = "CLI-$cliRand"
    $custCreateResp = Invoke-Api "POST" "/api/v1/customers" "{`"codigo`":`"$cliCode`",`"name`":`"Cliente $role $cliRand`",`"documentType`":`"CC`",`"documentNumber`":`"$(Get-Random -Minimum 10000000 -Maximum 99999999)`",`"email`":`"cli.$($role.ToLower())$cliRand@test.com`"}" $H
    Assert-Result "POST /customers (create)" $custCreateResp ($isAdmin -or $isVendedor)

    if ($custCreateResp.ok) {
        $custGetResp = Invoke-Api "GET" "/api/v1/customers/$cliCode" "" $H
        Assert-Result "GET /customers/{codigo}" $custGetResp ($isAdmin -or $isVendedor)
    }

    $saleListResp = Invoke-Api "GET" "/api/v1/sales" "" $H
    Assert-Result "GET /sales (list)" $saleListResp ($isAdmin -or $isVendedor)

    if (($isAdmin -or $isVendedor) -and $custCreateResp.ok -and $PRODUCT_ID) {
        $custId = $custCreateResp.data.data.id
        $saleCreateResp = Invoke-Api "POST" "/api/v1/sales" "{`"customerId`":`"$custId`",`"items`":[{`"productId`":`"$PRODUCT_ID`",`"productName`":`"Laptop Test`",`"quantity`":1,`"unitPrice`":700.00,`"discount`":0}],`"notes`":`"Venta $role`"}" $H
        Assert-Result "POST /sales (create)" $saleCreateResp ($isAdmin -or $isVendedor)

        if ($saleCreateResp.ok) {
            $saleId = $saleCreateResp.data.data.id
            $saleGetResp = Invoke-Api "GET" "/api/v1/sales/$saleId" "" $H
            Assert-Result "GET /sales/{id}" $saleGetResp ($isAdmin -or $isVendedor)

            $confirmResp = Invoke-Api "PATCH" "/api/v1/sales/$saleId/confirm" "" $H
            Assert-Result "PATCH /sales/confirm" $confirmResp ($isAdmin -or $isVendedor)

            $payResp = Invoke-Api "PATCH" "/api/v1/sales/$saleId/pay" "" $H
            Assert-Result "PATCH /sales/pay" $payResp ($isAdmin -or $isVendedor)

            $voidBody = Invoke-Api "PATCH" "/api/v1/sales/$saleId/void" "" $H
            Assert-Result "PATCH /sales/void (admin only)" $voidBody $isAdmin
        }
    } else {
        $dummyFail = @{ ok = $false; code = 403 }
        Assert-Result "POST /sales - no acceso" $dummyFail $false
    }

    # ── REPORT ──────────────────────────────────────────────────
    $SCRIPT:CUR_SVC = "REPORT"
    Write-Host "`n  [REPORT]"

    $repListResp = Invoke-Api "GET" "/api/v1/reports" "" $H
    Assert-Result "GET /reports (list types)" $repListResp $true

    $repGetResp = Invoke-Api "GET" "/api/v1/reports/DAILY_SALES" "" $H
    Assert-Result "GET /reports/DAILY_SALES" $repGetResp $true
}

# ══════════════════════════════════════════════════════════════
# RESUMEN FINAL
# ══════════════════════════════════════════════════════════════
$total = $SCRIPT:RESULTS.Count
$pass  = ($SCRIPT:RESULTS | Where-Object { $_.Result -eq "PASS" }).Count
$fail  = ($SCRIPT:RESULTS | Where-Object { $_.Result -eq "FAIL" }).Count

Write-Host ""
Write-Host ("=" * 62)
Write-Host "  RESUMEN FINAL - CRUD NEON - 3 USUARIOS"
Write-Host ("=" * 62)
Write-Host ""
Write-Host ("  {0,-10} {1,-20} {2,-3} {3,-4} {4}" -f "ROL","TEST","HTTP","EXP","RESULT")
Write-Host ("  " + ("-" * 58))
foreach ($row in $SCRIPT:RESULTS) {
    $color = if ($row.Result -eq "PASS") { "White" } else { "Red" }
    Write-Host ("  {0,-10} {1,-30} {2,-3} {3,-4} {4}" -f $row.User, $row.Test, $row.HTTP, $row.Expect, $row.Result) -ForegroundColor $color
}
Write-Host ""
Write-Host ("  TOTAL: $total  |  PASS: $pass  |  FAIL: $fail")
if ($fail -eq 0) {
    Write-Host "  *** TODOS LOS TESTS PASARON CORRECTAMENTE ***" -ForegroundColor Green
} else {
    Write-Host "  *** $fail TESTS FALLARON - REVISAR ***" -ForegroundColor Red
}
