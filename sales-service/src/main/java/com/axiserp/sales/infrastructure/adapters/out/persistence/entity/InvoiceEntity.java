package com.axiserp.sales.infrastructure.adapters.out.persistence.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "invoices")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "sale_id", nullable = false, unique = true)
    private UUID saleId;

    /**
     * Sequential invoice number. Assigned before persist via nextval query.
     * The sequence 'invoice_number_seq' is created at startup by Hibernate
     * if ddl-auto=create/update. If not, it must exist in the DB.
     */
    @Column(name = "invoice_number", nullable = false, unique = true, updatable = false)
    private Long invoiceNumber;

    @Column(name = "customer_snapshot", nullable = false, columnDefinition = "TEXT")
    private String customerSnapshot;

    @Column(name = "items_snapshot", nullable = false, columnDefinition = "TEXT")
    private String itemsSnapshot;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal discount;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal tax;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal total;

    @Column(name = "issued_at", nullable = false)
    private LocalDateTime issuedAt;

    @PrePersist
    protected void onCreate() {
        if (this.issuedAt == null) {
            this.issuedAt = LocalDateTime.now();
        }
    }
}
