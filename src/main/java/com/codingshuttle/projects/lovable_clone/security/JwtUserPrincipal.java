package com.codingshuttle.projects.lovable_clone.security;

import lombok.AllArgsConstructor;
import org.springframework.security.core.GrantedAuthority;

import java.util.List;

public record JwtUserPrincipal (
    Long userId,
    String username,
    List<GrantedAuthority> authorities) {
}
