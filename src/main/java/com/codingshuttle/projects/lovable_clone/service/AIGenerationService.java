package com.codingshuttle.projects.lovable_clone.service;

import aj.org.objectweb.asm.commons.Remapper;
import com.codingshuttle.projects.lovable_clone.dto.chat.StreamResponse;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public interface AIGenerationService {

    Flux<StreamResponse> streamResponse(String message, Long projectId);
}
