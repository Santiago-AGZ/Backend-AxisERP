package com.axiserp.sales.application.dto.response;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductSummary {

    private UUID id;
    private String name;
    private String codigo;
    private String status;
}