package com.axiserp.sales.infrastructure.adapters.out.persistence.adapter;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.axiserp.sales.domain.model.Invoice;
import com.axiserp.sales.infrastructure.adapters.out.persistence.entity.InvoiceEntity;
import com.axiserp.sales.infrastructure.adapters.out.persistence.repository.JpaInvoiceRepository;
import com.axiserp.sales.ports.output.InvoiceRepositoryPort;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class InvoiceRepositoryAdapter implements InvoiceRepositoryPort {

    private final JpaInvoiceRepository jpaInvoiceRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Optional<Invoice> findById(UUID id) {
        return jpaInvoiceRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<Invoice> findBySaleId(UUID saleId) {
        return jpaInvoiceRepository.findBySaleId(saleId).map(this::toDomain);
    }

    @Override
    public Invoice save(Invoice invoice) {
        // Generate sequential invoice number via DB sequence
        Long nextInvoiceNumber = (Long) entityManager
                .createNativeQuery("SELECT nextval('invoice_number_seq')")
                .getSingleResult();

        InvoiceEntity entity = toEntity(invoice);
        entity.setInvoiceNumber(nextInvoiceNumber);
        InvoiceEntity saved = jpaInvoiceRepository.save(entity);
        return toDomain(saved);
    }

    private Invoice toDomain(InvoiceEntity entity) {
        return Invoice.builder()
                .id(entity.getId())
                .saleId(entity.getSaleId())
                .invoiceNumber(entity.getInvoiceNumber())
                .customerSnapshot(entity.getCustomerSnapshot())
                .itemsSnapshot(entity.getItemsSnapshot())
                .subtotal(entity.getSubtotal())
                .discount(entity.getDiscount())
                .tax(entity.getTax())
                .total(entity.getTotal())
                .issuedAt(entity.getIssuedAt())
                .build();
    }

    private InvoiceEntity toEntity(Invoice domain) {
        return InvoiceEntity.builder()
                .id(domain.getId())
                .saleId(domain.getSaleId())
                .customerSnapshot(domain.getCustomerSnapshot())
                .itemsSnapshot(domain.getItemsSnapshot())
                .subtotal(domain.getSubtotal())
                .discount(domain.getDiscount())
                .tax(domain.getTax())
                .total(domain.getTotal())
                .issuedAt(domain.getIssuedAt())
                .build();
    }
}
