package com.codingshuttle.projects.lovable_clone.controller;

import com.codingshuttle.projects.lovable_clone.dto.chat.ChatRequest;
import com.codingshuttle.projects.lovable_clone.service.AIGenerationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequiredArgsConstructor
public class ChatController {

    private final AIGenerationService aiGenerationService;

    @PostMapping(value = "/api/chat/stream",produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> streamChat(@RequestBody ChatRequest request) {
        return aiGenerationService.streamResponse(request.message(), request.projectId())
                .map(data -> ServerSentEvent.<String>builder()
                        .data(data)
                        .build());
    }
}
