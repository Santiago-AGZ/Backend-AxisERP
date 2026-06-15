package com.axiserp.sales.application.usecase;

import java.time.LocalDateTime;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.axiserp.sales.application.dto.SaleResponseMapper;
import com.axiserp.sales.application.dto.response.SaleResponse;
import com.axiserp.sales.domain.exception.SaleNotFoundException;
import com.axiserp.sales.domain.exception.SaleNotModifiableException;
import com.axiserp.sales.domain.model.Sale;
import com.axiserp.sales.domain.model.SaleStatus;
import com.axiserp.sales.ports.input.PaySaleUseCase;
import com.axiserp.sales.ports.output.SaleRepositoryPort;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaySaleUseCaseImpl implements PaySaleUseCase {

    private static final Logger log = LoggerFactory.getLogger(PaySaleUseCaseImpl.class);

    private final SaleRepositoryPort saleRepositoryPort;

    @Override
    @Transactional
    public SaleResponse pay(UUID saleId) {
        Sale sale = saleRepositoryPort.findById(saleId)
                .orElseThrow(() -> new SaleNotFoundException(saleId));

        GetSaleUseCaseImpl.checkOwnership(sale);

        if (sale.getStatus() != SaleStatus.CONFIRMADA) {
            throw new SaleNotModifiableException("La venta solo puede pagarse si esta CONFIRMADA. Estado actual: " + sale.getStatus());
        }

        sale.setStatus(SaleStatus.PAGADA);
        sale.setUpdatedAt(LocalDateTime.now());

        Sale saved = saleRepositoryPort.save(sale);
        log.info("sale_paid id={}", saved.getId());

        return SaleResponseMapper.toResponse(saved);
    }
}
