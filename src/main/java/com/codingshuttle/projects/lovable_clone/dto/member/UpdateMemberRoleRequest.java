package com.codingshuttle.projects.lovable_clone.dto.member;

import com.codingshuttle.projects.lovable_clone.enums.ProjectRole;
import jakarta.validation.constraints.NotNull;

public record UpdateMemberRoleRequest(@NotNull ProjectRole role) {
}
