package com.axiserp.sales.application.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class PaginatedResponse<T> {
    private List<T> content;
    private long totalRecords;
}
