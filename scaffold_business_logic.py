import os

def write_file(path, content):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8") as f:
        f.write(content)

base = r"C:\Users\Santiago\Desktop\axisERP-platform"

# INVENTORY
base_inv = os.path.join(base, r"inventory-service\src\main\java\com\axiserp\inventory")
write_file(os.path.join(base_inv, r"domain\model\InventoryMovement.java"), """package com.axiserp.inventory.domain.model;
import java.util.UUID;
import lombok.*;
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class InventoryMovement {
    private UUID id;
    private UUID productId;
    private int quantity;
}""")
write_file(os.path.join(base_inv, r"ports\output\InventoryRepositoryPort.java"), """package com.axiserp.inventory.ports.output;
import java.util.UUID;
public interface InventoryRepositoryPort {
    void registerEntry(UUID productId, int quantity, String refType, UUID refId, String notes, UUID userId);
    void registerExit(UUID productId, int quantity, String refType, UUID refId, String notes, UUID userId);
}""")
write_file(os.path.join(base_inv, r"ports\input\RegisterMovementUseCase.java"), """package com.axiserp.inventory.ports.input;
import java.util.UUID;
public interface RegisterMovementUseCase {
    void registerEntry(UUID productId, int quantity, String refType, UUID refId, String notes);
    void registerExit(UUID productId, int quantity, String refType, UUID refId, String notes);
}""")
write_file(os.path.join(base_inv, r"application\usecase\ManageInventoryUseCase.java"), """package com.axiserp.inventory.application.usecase;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.security.core.context.SecurityContextHolder;
import com.axiserp.inventory.ports.input.RegisterMovementUseCase;
import com.axiserp.inventory.ports.output.InventoryRepositoryPort;
import lombok.RequiredArgsConstructor;
@Service
@RequiredArgsConstructor
public class ManageInventoryUseCase implements RegisterMovementUseCase {
    private final InventoryRepositoryPort inventoryRepository;
    @Override
    public void registerEntry(UUID productId, int quantity, String refType, UUID refId, String notes) {
        UUID userId = UUID.fromString((String) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        inventoryRepository.registerEntry(productId, quantity, refType, refId, notes, userId);
    }
    @Override
    public void registerExit(UUID productId, int quantity, String refType, UUID refId, String notes) {
        UUID userId = UUID.fromString((String) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        inventoryRepository.registerExit(productId, quantity, refType, refId, notes, userId);
    }
}""")
write_file(os.path.join(base_inv, r"infrastructure\adapters\out\persistence\entity\InventoryMovementEntity.java"), """package com.axiserp.inventory.infrastructure.adapters.out.persistence.entity;
import java.util.UUID;
import jakarta.persistence.*;
import lombok.*;
@Entity @Table(name = "inventory_movements")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class InventoryMovementEntity {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(name = "product_id") private UUID productId;
    private int quantity;
}""")
write_file(os.path.join(base_inv, r"infrastructure\adapters\out\persistence\repository\JpaMovementRepository.java"), """package com.axiserp.inventory.infrastructure.adapters.out.persistence.repository;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.axiserp.inventory.infrastructure.adapters.out.persistence.entity.InventoryMovementEntity;
public interface JpaMovementRepository extends JpaRepository<InventoryMovementEntity, UUID> {
    @Query(value = "SELECT fn_register_stock_entry(CAST(:p_product_id AS UUID), :p_quantity, :p_ref_type, CAST(:p_ref_id AS UUID), :p_notes, CAST(:p_user_id AS UUID))", nativeQuery = true)
    void registerStockEntry(@Param("p_product_id") String productId, @Param("p_quantity") int quantity, @Param("p_ref_type") String refType, @Param("p_ref_id") String refId, @Param("p_notes") String notes, @Param("p_user_id") String userId);
    @Query(value = "SELECT fn_register_stock_exit(CAST(:p_product_id AS UUID), :p_quantity, :p_ref_type, CAST(:p_ref_id AS UUID), :p_notes, CAST(:p_user_id AS UUID))", nativeQuery = true)
    void registerStockExit(@Param("p_product_id") String productId, @Param("p_quantity") int quantity, @Param("p_ref_type") String refType, @Param("p_ref_id") String refId, @Param("p_notes") String notes, @Param("p_user_id") String userId);
}""")
write_file(os.path.join(base_inv, r"infrastructure\adapters\out\persistence\adapter\InventoryRepositoryAdapter.java"), """package com.axiserp.inventory.infrastructure.adapters.out.persistence.adapter;
import java.util.UUID;
import org.springframework.stereotype.Component;
import com.axiserp.inventory.ports.output.InventoryRepositoryPort;
import com.axiserp.inventory.infrastructure.adapters.out.persistence.repository.JpaMovementRepository;
import lombok.RequiredArgsConstructor;
@Component
@RequiredArgsConstructor
public class InventoryRepositoryAdapter implements InventoryRepositoryPort {
    private final JpaMovementRepository movementRepository;
    @Override
    public void registerEntry(UUID productId, int quantity, String refType, UUID refId, String notes, UUID userId) {
        movementRepository.registerStockEntry(productId.toString(), quantity, refType, refId != null ? refId.toString() : null, notes, userId.toString());
    }
    @Override
    public void registerExit(UUID productId, int quantity, String refType, UUID refId, String notes, UUID userId) {
        movementRepository.registerStockExit(productId.toString(), quantity, refType, refId != null ? refId.toString() : null, notes, userId.toString());
    }
}""")
write_file(os.path.join(base_inv, r"infrastructure\adapters\in\web\controller\InventoryController.java"), """package com.axiserp.inventory.infrastructure.adapters.in.web.controller;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.axiserp.inventory.ports.input.RegisterMovementUseCase;
import lombok.RequiredArgsConstructor;
@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
public class InventoryController {
    private final RegisterMovementUseCase useCase;
    @PostMapping("/entry")
    public ResponseEntity<Void> entry(@RequestParam UUID productId, @RequestParam int quantity, @RequestParam String notes) {
        useCase.registerEntry(productId, quantity, "MANUAL", null, notes);
        return ResponseEntity.ok().build();
    }
}""")

# PURCHASE
base_pur = os.path.join(base, r"purchase-service\src\main\java\com\axiserp\purchase")
write_file(os.path.join(base_pur, r"domain\model\Purchase.java"), """package com.axiserp.purchase.domain.model;
import java.util.UUID;
import lombok.*;
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Purchase {
    private UUID id;
    private UUID supplierId;
    private double totalAmount;
}""")
write_file(os.path.join(base_pur, r"ports\output\PurchaseRepositoryPort.java"), """package com.axiserp.purchase.ports.output;
import com.axiserp.purchase.domain.model.Purchase;
public interface PurchaseRepositoryPort {
    Purchase save(Purchase purchase);
}""")
write_file(os.path.join(base_pur, r"ports\input\ManagePurchaseUseCase.java"), """package com.axiserp.purchase.ports.input;
import com.axiserp.purchase.domain.model.Purchase;
public interface ManagePurchaseUseCase {
    Purchase createPurchase(Purchase purchase);
}""")
write_file(os.path.join(base_pur, r"application\usecase\PurchaseUseCaseImpl.java"), """package com.axiserp.purchase.application.usecase;
import org.springframework.stereotype.Service;
import com.axiserp.purchase.domain.model.Purchase;
import com.axiserp.purchase.ports.input.ManagePurchaseUseCase;
import com.axiserp.purchase.ports.output.PurchaseRepositoryPort;
import lombok.RequiredArgsConstructor;
@Service
@RequiredArgsConstructor
public class PurchaseUseCaseImpl implements ManagePurchaseUseCase {
    private final PurchaseRepositoryPort repository;
    @Override
    public Purchase createPurchase(Purchase purchase) {
        return repository.save(purchase);
    }
}""")
write_file(os.path.join(base_pur, r"infrastructure\adapters\out\persistence\entity\PurchaseEntity.java"), """package com.axiserp.purchase.infrastructure.adapters.out.persistence.entity;
import java.util.UUID;
import jakarta.persistence.*;
import lombok.*;
@Entity @Table(name = "purchases")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PurchaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(name = "supplier_id") private UUID supplierId;
    @Column(name = "total_amount") private double totalAmount;
}""")
write_file(os.path.join(base_pur, r"infrastructure\adapters\out\persistence\repository\JpaPurchaseRepository.java"), """package com.axiserp.purchase.infrastructure.adapters.out.persistence.repository;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import com.axiserp.purchase.infrastructure.adapters.out.persistence.entity.PurchaseEntity;
public interface JpaPurchaseRepository extends JpaRepository<PurchaseEntity, UUID> {}""")
write_file(os.path.join(base_pur, r"infrastructure\adapters\out\persistence\adapter\PurchaseRepositoryAdapter.java"), """package com.axiserp.purchase.infrastructure.adapters.out.persistence.adapter;
import org.springframework.stereotype.Component;
import com.axiserp.purchase.domain.model.Purchase;
import com.axiserp.purchase.ports.output.PurchaseRepositoryPort;
import com.axiserp.purchase.infrastructure.adapters.out.persistence.entity.PurchaseEntity;
import com.axiserp.purchase.infrastructure.adapters.out.persistence.repository.JpaPurchaseRepository;
import lombok.RequiredArgsConstructor;
@Component
@RequiredArgsConstructor
public class PurchaseRepositoryAdapter implements PurchaseRepositoryPort {
    private final JpaPurchaseRepository jpaRepository;
    @Override
    public Purchase save(Purchase purchase) {
        PurchaseEntity entity = PurchaseEntity.builder()
            .supplierId(purchase.getSupplierId())
            .totalAmount(purchase.getTotalAmount())
            .build();
        entity = jpaRepository.save(entity);
        purchase.setId(entity.getId());
        return purchase;
    }
}""")
write_file(os.path.join(base_pur, r"infrastructure\adapters\in\web\controller\PurchaseController.java"), """package com.axiserp.purchase.infrastructure.adapters.in.web.controller;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.axiserp.purchase.domain.model.Purchase;
import com.axiserp.purchase.ports.input.ManagePurchaseUseCase;
import lombok.RequiredArgsConstructor;
@RestController
@RequestMapping("/api/v1/purchases")
@RequiredArgsConstructor
public class PurchaseController {
    private final ManagePurchaseUseCase useCase;
    @PostMapping
    public ResponseEntity<Purchase> createPurchase(@RequestBody Purchase purchase) {
        return ResponseEntity.ok(useCase.createPurchase(purchase));
    }
}""")

# SALES
base_sal = os.path.join(base, r"sales-service\src\main\java\com\axiserp\sales")
write_file(os.path.join(base_sal, r"domain\model\Sale.java"), """package com.axiserp.sales.domain.model;
import java.util.UUID;
import lombok.*;
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Sale {
    private UUID id;
    private UUID customerId;
    private double totalAmount;
}""")
write_file(os.path.join(base_sal, r"ports\output\SaleRepositoryPort.java"), """package com.axiserp.sales.ports.output;
import com.axiserp.sales.domain.model.Sale;
public interface SaleRepositoryPort {
    Sale save(Sale sale);
}""")
write_file(os.path.join(base_sal, r"ports\input\ManageSaleUseCase.java"), """package com.axiserp.sales.ports.input;
import com.axiserp.sales.domain.model.Sale;
public interface ManageSaleUseCase {
    Sale createSale(Sale sale);
}""")
write_file(os.path.join(base_sal, r"application\usecase\SaleUseCaseImpl.java"), """package com.axiserp.sales.application.usecase;
import org.springframework.stereotype.Service;
import com.axiserp.sales.domain.model.Sale;
import com.axiserp.sales.ports.input.ManageSaleUseCase;
import com.axiserp.sales.ports.output.SaleRepositoryPort;
import lombok.RequiredArgsConstructor;
@Service
@RequiredArgsConstructor
public class SaleUseCaseImpl implements ManageSaleUseCase {
    private final SaleRepositoryPort repository;
    @Override
    public Sale createSale(Sale sale) {
        return repository.save(sale);
    }
}""")
write_file(os.path.join(base_sal, r"infrastructure\adapters\out\persistence\entity\SaleEntity.java"), """package com.axiserp.sales.infrastructure.adapters.out.persistence.entity;
import java.util.UUID;
import jakarta.persistence.*;
import lombok.*;
@Entity @Table(name = "sales")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SaleEntity {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(name = "customer_id") private UUID customerId;
    @Column(name = "total_amount") private double totalAmount;
}""")
write_file(os.path.join(base_sal, r"infrastructure\adapters\out\persistence\repository\JpaSaleRepository.java"), """package com.axiserp.sales.infrastructure.adapters.out.persistence.repository;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import com.axiserp.sales.infrastructure.adapters.out.persistence.entity.SaleEntity;
public interface JpaSaleRepository extends JpaRepository<SaleEntity, UUID> {}""")
write_file(os.path.join(base_sal, r"infrastructure\adapters\out\persistence\adapter\SaleRepositoryAdapter.java"), """package com.axiserp.sales.infrastructure.adapters.out.persistence.adapter;
import org.springframework.stereotype.Component;
import com.axiserp.sales.domain.model.Sale;
import com.axiserp.sales.ports.output.SaleRepositoryPort;
import com.axiserp.sales.infrastructure.adapters.out.persistence.entity.SaleEntity;
import com.axiserp.sales.infrastructure.adapters.out.persistence.repository.JpaSaleRepository;
import lombok.RequiredArgsConstructor;
@Component
@RequiredArgsConstructor
public class SaleRepositoryAdapter implements SaleRepositoryPort {
    private final JpaSaleRepository jpaRepository;
    @Override
    public Sale save(Sale sale) {
        SaleEntity entity = SaleEntity.builder()
            .customerId(sale.getCustomerId())
            .totalAmount(sale.getTotalAmount())
            .build();
        entity = jpaRepository.save(entity);
        sale.setId(entity.getId());
        return sale;
    }
}""")
write_file(os.path.join(base_sal, r"infrastructure\adapters\in\web\controller\SaleController.java"), """package com.axiserp.sales.infrastructure.adapters.in.web.controller;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.axiserp.sales.domain.model.Sale;
import com.axiserp.sales.ports.input.ManageSaleUseCase;
import lombok.RequiredArgsConstructor;
@RestController
@RequestMapping("/api/v1/sales")
@RequiredArgsConstructor
public class SaleController {
    private final ManageSaleUseCase useCase;
    @PostMapping
    public ResponseEntity<Sale> createSale(@RequestBody Sale sale) {
        return ResponseEntity.ok(useCase.createSale(sale));
    }
}""")

# REPORT
base_rep = os.path.join(base, r"report-service\src\main\java\com\axiserp\report")
write_file(os.path.join(base_rep, r"domain\model\Report.java"), """package com.axiserp.report.domain.model;
import java.util.UUID;
import lombok.*;
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Report {
    private UUID id;
    private String name;
}""")
write_file(os.path.join(base_rep, r"ports\output\ReportRepositoryPort.java"), """package com.axiserp.report.ports.output;
import com.axiserp.report.domain.model.Report;
public interface ReportRepositoryPort {
    Report generateReport(Report report);
}""")
write_file(os.path.join(base_rep, r"ports\input\GenerateReportUseCase.java"), """package com.axiserp.report.ports.input;
import com.axiserp.report.domain.model.Report;
public interface GenerateReportUseCase {
    Report generateReport(Report report);
}""")
write_file(os.path.join(base_rep, r"application\usecase\ReportUseCaseImpl.java"), """package com.axiserp.report.application.usecase;
import org.springframework.stereotype.Service;
import com.axiserp.report.domain.model.Report;
import com.axiserp.report.ports.input.GenerateReportUseCase;
import com.axiserp.report.ports.output.ReportRepositoryPort;
import lombok.RequiredArgsConstructor;
@Service
@RequiredArgsConstructor
public class ReportUseCaseImpl implements GenerateReportUseCase {
    private final ReportRepositoryPort repository;
    @Override
    public Report generateReport(Report report) {
        return repository.generateReport(report);
    }
}""")
write_file(os.path.join(base_rep, r"infrastructure\adapters\out\persistence\entity\ReportEntity.java"), """package com.axiserp.report.infrastructure.adapters.out.persistence.entity;
import java.util.UUID;
import jakarta.persistence.*;
import lombok.*;
@Entity @Table(name = "report_cache")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ReportEntity {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(name = "cache_key") private String name;
    @Column(name = "cache_data") private String data;
}""")
write_file(os.path.join(base_rep, r"infrastructure\adapters\out\persistence\repository\JpaReportRepository.java"), """package com.axiserp.report.infrastructure.adapters.out.persistence.repository;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import com.axiserp.report.infrastructure.adapters.out.persistence.entity.ReportEntity;
public interface JpaReportRepository extends JpaRepository<ReportEntity, UUID> {}""")
write_file(os.path.join(base_rep, r"infrastructure\adapters\out\persistence\adapter\ReportRepositoryAdapter.java"), """package com.axiserp.report.infrastructure.adapters.out.persistence.adapter;
import org.springframework.stereotype.Component;
import com.axiserp.report.domain.model.Report;
import com.axiserp.report.ports.output.ReportRepositoryPort;
import com.axiserp.report.infrastructure.adapters.out.persistence.entity.ReportEntity;
import com.axiserp.report.infrastructure.adapters.out.persistence.repository.JpaReportRepository;
import lombok.RequiredArgsConstructor;
@Component
@RequiredArgsConstructor
public class ReportRepositoryAdapter implements ReportRepositoryPort {
    private final JpaReportRepository jpaRepository;
    @Override
    public Report generateReport(Report report) {
        ReportEntity entity = ReportEntity.builder()
            .name(report.getName())
            .data("{}")
            .build();
        entity = jpaRepository.save(entity);
        report.setId(entity.getId());
        return report;
    }
}""")
write_file(os.path.join(base_rep, r"infrastructure\adapters\in\web\controller\ReportController.java"), """package com.axiserp.report.infrastructure.adapters.in.web.controller;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.axiserp.report.domain.model.Report;
import com.axiserp.report.ports.input.GenerateReportUseCase;
import lombok.RequiredArgsConstructor;
@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
public class ReportController {
    private final GenerateReportUseCase useCase;
    @PostMapping
    public ResponseEntity<Report> generateReport(@RequestBody Report report) {
        return ResponseEntity.ok(useCase.generateReport(report));
    }
}""")

print("Done generating all services.")
