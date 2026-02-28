package com.codingshuttle.projects.lovable_clone.entity;

import com.codingshuttle.projects.lovable_clone.enums.MessageRole;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name="chat_messages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY,optional = false)
    @JoinColumns({
            @JoinColumn(name = "project_id", referencedColumnName = "project_id",nullable = false),
            @JoinColumn(name= "user_id", referencedColumnName = "user_id",nullable = false)
    })
    ChatSession chatSession;

    @Column(columnDefinition = "text",nullable = false)
    String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    MessageRole role;

    Integer tokensUsed = 0;

    @CreationTimestamp
    Instant createdAt;
}
