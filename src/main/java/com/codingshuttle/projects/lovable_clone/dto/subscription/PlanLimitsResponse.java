package com.codingshuttle.projects.lovable_clone.dto.subscription;

public record PlanLimitsResponse(
        String planName,
        int maxTokensPerDay,
        int maxProjects,
        boolean unlimitedAi
) {
}
