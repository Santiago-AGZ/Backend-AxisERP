package com.axiserp.purchase.application.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import com.axiserp.purchase.domain.model.SupplierStatus;

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
public class SupplierResponse {

    private UUID id;
    private String name;
    private String nit;
    private String phone;
    private String email;
    private String address;
    private SupplierStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
