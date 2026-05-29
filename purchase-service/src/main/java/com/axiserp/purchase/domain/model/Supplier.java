package com.axiserp.purchase.domain.model;

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
public class Supplier {

    private UUID id;
    private String name;
    private String nit;
    private String phone;
    private String email;
    private String address;
    private SupplierStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public boolean isActive() {
        return this.status == SupplierStatus.ACTIVO;
    }

    public boolean isDeleted() {
        return this.status == SupplierStatus.ELIMINADO;
    }
}
