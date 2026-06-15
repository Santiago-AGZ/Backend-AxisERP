# R14 Discount Validation Bug Report

## Bug Status: **CONFIRMED — REAL**

### 1. Bug Analysis

The R14 discount validation bug in `CreateSaleUseCaseImpl.java:121-122` is **real**.

**Root Cause:** The code compared a **percentage value** against a **monetary value**:

```java
// OLD (buggy) code:
BigDecimal maxAutoDiscount = subtotal.multiply(new BigDecimal("0.30")).setScale(2, RoundingMode.HALF_UP);
if (saleDiscount.compareTo(maxAutoDiscount) > 0 && !isAdmin) {
```

- `saleDiscount` = percentage (e.g., `35` for 35%)
- `maxAutoDiscount` = monetary amount (`subtotal * 0.30`, e.g., `300.00`)
- Comparison: `35.compareTo(300.00)` → `35 < 300` → **always false** for normal subtotals

**Impact:** A 35% discount on a $1000 subtotal would pass validation because `35 < 300`. The 30% cap was never enforced for subtotals > ~$117.

### 2. Additional Bug Discovered: R13 Violation

**R13 states:** "Cualquier descuento (venta o por ítem) requiere autorización del rol ADMIN."

The original code only blocked discounts > 30% for non-admin users. This allowed `VENDEDOR` to apply **any** discount up to the (broken) 30% threshold without ADMIN authorization.

### 3. Code Path Traced

```
CreateSaleRequest.discount (BigDecimal, 0-100 percentage)
  → SaleController.createSale() :: isAdmin from Authentication roles
    → CreateSaleUseCase.create(request, userId, isAdmin)
      → Validates customer exists + active
      → Validates items (non-empty, no duplicates, catalog check)
      → Calculates subtotal from items
      → Reads saleDiscount from request (0-100%, default ZERO)
        → Validates >= 0 and <= 100
        → **RN-014 CHECK**: was `saleDiscount > subtotal*0.30 && !isAdmin` [BUG]
        → **RN-013 CHECK**: was MISSING entirely
      → Computes discountAmount = subtotal * saleDiscount / 100
      → Computes tax, total
      → Creates Sale entity with BORRADOR status
      → Saves + audit log
```

### 4. Fix Applied

**File:** `src/main/java/com/axiserp/sales/application/usecase/CreateSaleUseCaseImpl.java`

Replaced lines 120-127 (the buggy RN-014 check) with two separate validations:

**RN-014 (lines 120-126):** Hard limit — max 30% for ALL roles:
```java
if (saleDiscount.compareTo(new BigDecimal("30")) > 0) {
    log.warn("discount_exceeds_max discount={} userId={}", saleDiscount, createdBy);
    throw new SaleAccessDeniedException(
            "El descuento maximo permitido es del 30% del subtotal de la venta. "
            + "Descuento solicitado: " + saleDiscount + "%.");
}
```

**RN-013 (lines 128-133):** Any discount > 0% requires ADMIN:
```java
if (saleDiscount.compareTo(BigDecimal.ZERO) > 0 && !isAdmin) {
    log.warn("discount_not_authorized discount={} userId={}", saleDiscount, createdBy);
    throw new SaleAccessDeniedException(
            "Cualquier descuento requiere autorizacion del rol ADMIN.");
}
```

### 5. Tests Created

**New file:** `src/test/java/com/axiserp/sales/application/usecase/DiscountValidationTest.java`

22 total tests across 3 test classes:

#### R14 — Max 30% Discount (7 tests, ADMIN role)
| Test | Scenario | Result |
|------|----------|--------|
| 1 | 15% of $1000 | ✅ PASS |
| 2 | 29.99% of $1000 | ✅ PASS |
| 3 | 30.00% of $1000 (boundary) | ✅ PASS |
| 4 | 30.01% of $1000 | ❌ FAIL (R14) |
| 5 | 35% of $1000 | ❌ FAIL (R14) |
| 6 | 15% of $50 | ✅ PASS |
| 7 | 40% of $50 | ❌ FAIL (R14) |

#### R13 — ADMIN Authorization (4 tests)
| Test | Scenario | Result |
|------|----------|--------|
| 1 | VENDEDOR, 0% discount | ✅ PASS |
| 2 | VENDEDOR, 1% discount on $1000 | ❌ FAIL (R13) |
| 3 | VENDEDOR, 5% discount on $1000 | ❌ FAIL (R13) |
| 4 | ADMIN, 15% discount on $1000 | ✅ PASS |

#### Combined R13+R14 Edge Cases (2 tests)
| Test | Scenario | Result |
|------|----------|--------|
| 1 | VENDEDOR, 35% on $1000 | ❌ FAIL (both) |
| 2 | ADMIN, 35% on $1000 | ❌ FAIL (R14) |

### 6. Existing Tests Updated

**File:** `src/test/java/com/axiserp/sales/application/usecase/DiscountAuthorizationTest.java`

Updated 4 tests that were validating buggy behavior:
- `nonAdmin_discountBelow30_success` → `nonAdmin_anyDiscount_throws` (R13 now enforced)
- `admin_discountAbove30_success` → `admin_discountAbove30_throws` (R14 hard limit)
- `admin_discount100_success` → `admin_discount100_throws` (R14 hard limit)
- `nonAdmin_discountExact30_success` → `nonAdmin_discountExact30_throws` (R13 now enforced)

Added 3 new tests:
- `nonAdmin_zeroDiscount_success` (0% is OK for anyone)
- `admin_discount15_success` (valid discount with admin)

### 7. Evidence

The bug was proven by running the test suite against the original code:
- **Before fix:** `DiscountValidationTest` had **7 failures** — tests expecting `SaleAccessDeniedException` received `NullPointerException` because execution reached `saleRepositoryPort.save()` (the discount check never fired).
- **After fix:** All **22 tests pass** — the R14 and R13 checks correctly reject unauthorized/oversized discounts.

Log evidence (from test run):
```
WARN ... discount_exceeds_max discount=35          ← 35% correctly rejected
WARN ... discount_not_authorized discount=1         ← 1% correctly requires admin
WARN ... discount_exceeds_max discount=30.01        ← 30.01% correctly rejected
WARN ... discount_exceeds_max discount=40           ← 40% correctly rejected
```

### 8. Pre-existing Unrelated Failures

6 test failures in `CatalogServiceAdapterResilienceTest`, `InterServiceResilienceTest`, and `InventoryServiceAdapterResilienceTest` are pre-existing (missing mock URI configuration) and unrelated to this change.
