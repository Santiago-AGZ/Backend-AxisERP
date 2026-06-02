package com.axiserp.catalog.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductBarcode {

    private UUID id;
    private UUID productId;
    private String barcode;
    private BarcodeType barcodeType;
    private LocalDateTime createdAt;

    public enum BarcodeType {
        EAN13, EAN8, UPC_A, UPC_E, CODE128, CODE39, QR
    }
}
