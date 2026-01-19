package com.codingshuttle.projects.lovable_clone.error;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class BadRequestException extends RuntimeException {
    String message;
}
