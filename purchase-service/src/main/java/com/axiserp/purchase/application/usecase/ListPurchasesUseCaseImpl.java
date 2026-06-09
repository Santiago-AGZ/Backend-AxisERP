package com.axiserp.purchase.application.usecase;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.axiserp.purchase.application.dto.mapper.PurchaseMapper;
import com.axiserp.purchase.application.dto.response.PurchaseResponse;
import com.axiserp.purchase.domain.model.Purchase;
import com.axiserp.purchase.ports.input.ListPurchasesUseCase;
import com.axiserp.purchase.ports.output.PurchaseRepositoryPort;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ListPurchasesUseCaseImpl implements ListPurchasesUseCase {

    private static final Logger log = LoggerFactory.getLogger(ListPurchasesUseCaseImpl.class);

    private final PurchaseRepositoryPort purchaseRepositoryPort;

    @Override
    public List<PurchaseResponse> execute() {
        List<Purchase> purchases = purchaseRepositoryPort.findAll();
        log.info("purchases_list count={}", purchases.size());
        return purchases.stream().map(PurchaseMapper::toResponse).toList();
    }

    @Override
    public List<PurchaseResponse> execute(String search, String status, int page, int size) {
        List<Purchase> purchases = purchaseRepositoryPort.findAll(search, status, page, size);
        log.info("purchases_list search={} status={} count={}", search, status, purchases.size());
        return purchases.stream().map(PurchaseMapper::toResponse).toList();
    }

    @Override
    public long countAll() {
        return purchaseRepositoryPort.countAll();
    }

    @Override
    public long countByFilters(String search, String status) {
        return purchaseRepositoryPort.countByFilters(search, status);
    }
}
