package com.axiserp.catalog.application.shared;

import java.util.List;

public record PageResult<T>(List<T> content, long totalElements) {}
