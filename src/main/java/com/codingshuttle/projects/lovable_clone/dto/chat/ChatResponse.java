package com.codingshuttle.projects.lovable_clone.dto.chat;

import com.codingshuttle.projects.lovable_clone.enums.MessageRole;

import java.time.Instant;
import java.util.List;

public record ChatResponse(
    Long id,
    List<ChatEventResponse> events,
    String content,
    MessageRole role,
    Integer tokensUsed,
    Instant createdAt

) {
}
