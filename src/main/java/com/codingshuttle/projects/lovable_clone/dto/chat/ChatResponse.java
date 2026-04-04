package com.codingshuttle.projects.lovable_clone.dto.chat;

import com.codingshuttle.projects.lovable_clone.entity.ChatEvent;
import com.codingshuttle.projects.lovable_clone.entity.ChatSession;
import com.codingshuttle.projects.lovable_clone.enums.MessageRole;

import java.time.Instant;
import java.util.List;

public record ChatResponse(
    Long id,
    ChatSession chatSession,
    List<ChatEvent> events,
    String content,
    MessageRole role,
    Integer tokensUsed,
    Instant createdAt

) {
}
