package com.codingshuttle.projects.lovable_clone.service.impl;

import com.codingshuttle.projects.lovable_clone.service.AIGenerationService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class AIGenerationServiceImpl implements AIGenerationService {
    @Override
    public Flux<String> streamResponse(String message, Long projectId) {
        return Flux.just("AI response part 1 for project " + projectId + ": " + message,
                        "AI response part 2 for project " + projectId + ": " + message,
                        "AI response part 3 for project " + projectId + ": " + message)
                .delayElements(java.time.Duration.ofSeconds(2)); // Simulate delay for streaming
    }
}
