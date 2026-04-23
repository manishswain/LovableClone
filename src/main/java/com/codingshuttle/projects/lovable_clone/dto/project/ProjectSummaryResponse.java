package com.codingshuttle.projects.lovable_clone.dto.project;

import com.codingshuttle.projects.lovable_clone.enums.ProjectRole;

import java.time.Instant;

public record ProjectSummaryResponse(
        Long id,
        String name,
        Instant createdAt,
        Instant updatedAt,
        ProjectRole role
) {
}
