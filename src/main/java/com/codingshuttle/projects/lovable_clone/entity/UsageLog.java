package com.codingshuttle.projects.lovable_clone.entity;

import java.time.Instant;

public class UsageLog {

    Long id;
    User user;
    Project project;

    String action;

    Integer tokensUsed;
    Integer durationMs;

    String metaData; // JSON of {model_used, prompt_used},

    Instant createdAt;
}
