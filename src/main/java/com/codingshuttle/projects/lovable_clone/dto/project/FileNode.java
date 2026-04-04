package com.codingshuttle.projects.lovable_clone.dto.project;

import java.time.Instant;

public record FileNode(
        String path
) {

    @Override
    public String toString() {
        return path;
    }
}
