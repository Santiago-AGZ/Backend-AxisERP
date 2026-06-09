package com.axiserp.purchase.application.usecase;

import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.axiserp.purchase.application.dto.mapper.PurchaseMapper;
import com.axiserp.purchase.application.dto.response.PurchaseResponse;
import com.axiserp.purchase.domain.exception.PurchaseNotFoundException;
import com.axiserp.purchase.domain.model.Purchase;
import com.axiserp.purchase.ports.input.GetPurchaseUseCase;
import com.axiserp.purchase.ports.output.PurchaseRepositoryPort;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetPurchaseUseCaseImpl implements GetPurchaseUseCase {

    private static final Logger log = LoggerFactory.getLogger(GetPurchaseUseCaseImpl.class);

    private final PurchaseRepositoryPort purchaseRepositoryPort;

    @Override
    public PurchaseResponse execute(UUID id) {
        Purchase purchase = purchaseRepositoryPort.findById(id)
                .orElseThrow(() -> new PurchaseNotFoundException(id));
        log.info("purchase_get id={}", id);
        return PurchaseMapper.toResponse(purchase);
    }
}
