package com.codingshuttle.projects.lovable_clone.dto.project;

import java.time.Instant;

public record ProjectSummaryResponse(
        Long id,
        String name,
        Instant createdAt,
        Instant updatedAt
) {
}
