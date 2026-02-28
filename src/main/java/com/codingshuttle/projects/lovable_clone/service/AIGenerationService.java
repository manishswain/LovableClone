package com.codingshuttle.projects.lovable_clone.service;

import aj.org.objectweb.asm.commons.Remapper;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public interface AIGenerationService {

    Flux<String> streamResponse(String message, Long projectId);
}
