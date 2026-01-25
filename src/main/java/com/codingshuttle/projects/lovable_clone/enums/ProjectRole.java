package com.codingshuttle.projects.lovable_clone.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Set;

@RequiredArgsConstructor
@Getter
public enum ProjectRole {
    EDITOR(
            ProjectPermission.VIEW,
            ProjectPermission.EDIT,
            ProjectPermission.DELETE,
            ProjectPermission.VIEW_MEMBERS
    ),
    VIEWER(Set.of(
            ProjectPermission.VIEW,
            ProjectPermission.VIEW_MEMBERS
    )),
    OWNER(Set.of(
            ProjectPermission.MANAGE_MEMBERS,
            ProjectPermission.DELETE,
            ProjectPermission.VIEW,
            ProjectPermission.EDIT,
            ProjectPermission.VIEW_MEMBERS
    ));

    ProjectRole(ProjectPermission... permissions) {
        this.permissions = Set.of(permissions);
    }

    private final Set<ProjectPermission> permissions;
}
