package com.codingshuttle.projects.lovable_clone.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class ChatSessionId implements Serializable {
    Long projectId;
    Long userId;
}
