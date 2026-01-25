package com.codingshuttle.projects.lovable_clone.security;

import com.codingshuttle.projects.lovable_clone.enums.ProjectPermission;
import com.codingshuttle.projects.lovable_clone.repository.ProjectMemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;

@Component("security")
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = lombok.AccessLevel.PRIVATE)
public class SecurityExpressions {

    ProjectMemberRepository projectMemberRepository;
    AuthUtil authUtil;

    private Boolean hasPermission(Long projectId, ProjectPermission permission) {
        Long userId = authUtil.getCurrentUserId();
        return projectMemberRepository.findRoleByProjectIdAndUserId(projectId, userId)
                .map(role -> role.getPermissions().contains(permission))
                .orElse(false);
    }

    public Boolean canViewProject(Long projectId) {
        return hasPermission(projectId, ProjectPermission.VIEW);

    }

    public Boolean canEditProject(Long projectId) {
        return hasPermission(projectId, ProjectPermission.EDIT);
    }

    public Boolean canDeleteProject(Long projectId) {
        return hasPermission(projectId, ProjectPermission.DELETE);
    }

    public Boolean canViewMembers(Long projectId) {
        return hasPermission(projectId, ProjectPermission.VIEW_MEMBERS);
    }

    public Boolean canManageMembers(Long projectId) {
        return hasPermission(projectId, ProjectPermission.MANAGE_MEMBERS);
    }
}
