package com.axiserp.catalog.application.dto.request;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCategoryRequest {

    private String name;

    private String description;

    private UUID parentId;
}
