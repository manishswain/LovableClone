package com.codingshuttle.projects.lovable_clone.service.impl;

import com.codingshuttle.projects.lovable_clone.dto.project.ProjectRequest;
import com.codingshuttle.projects.lovable_clone.dto.project.ProjectResponse;
import com.codingshuttle.projects.lovable_clone.dto.project.ProjectSummaryResponse;
import com.codingshuttle.projects.lovable_clone.entity.Project;
import com.codingshuttle.projects.lovable_clone.entity.ProjectMember;
import com.codingshuttle.projects.lovable_clone.entity.ProjectMemberId;
import com.codingshuttle.projects.lovable_clone.entity.User;
import com.codingshuttle.projects.lovable_clone.enums.ProjectRole;
import com.codingshuttle.projects.lovable_clone.error.BadRequestException;
import com.codingshuttle.projects.lovable_clone.error.ResourceNotFoundException;
import com.codingshuttle.projects.lovable_clone.mapper.ProjectMapper;
import com.codingshuttle.projects.lovable_clone.repository.ProjectMemberRepository;
import com.codingshuttle.projects.lovable_clone.repository.ProjectRepository;
import com.codingshuttle.projects.lovable_clone.repository.UserRepository;
import com.codingshuttle.projects.lovable_clone.security.AuthUtil;
import com.codingshuttle.projects.lovable_clone.service.ProjectService;
import com.codingshuttle.projects.lovable_clone.service.SubscriptionService;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Transactional
public class ProjectServiceImpl implements ProjectService {

    ProjectRepository projectRepository;
    UserRepository userRepository;
    ProjectMapper projectMapper;
    ProjectMemberRepository projectMemberRepository;
    AuthUtil authUtil;
    SubscriptionService subscriptionService;

    @Override
    public ProjectResponse createProject(ProjectRequest request) {
        Long userId = authUtil.getCurrentUserId();

        if(!subscriptionService.canCreateNewProject()){
            throw new BadRequestException("User cannot create new project. Please upgrade your subscription plan.");
        }
        /*User owner = userRepository.findById(userId).orElseThrow(
                () -> new ResourceNotFoundException("User", userId.toString())
        );*/

        User owner = userRepository.getReferenceById(userId);



        Project project = Project.builder()
                .name(request.name())
                .isPublic(false)
                .build();

        project = projectRepository.save(project);

        ProjectMemberId projectMemberId = new ProjectMemberId(project.getId(), owner.getId());

        ProjectMember projectMember = ProjectMember.builder()
                .id(projectMemberId)
                .project(project)
                .user(owner)
                .projectRole(ProjectRole.OWNER)
                .invitedAt(Instant.now())
                .acceptedAt(Instant.now())
                .build();

        projectMemberRepository.save(projectMember);

        return projectMapper.toProjectResponse(project);

    }

    @Override
    public List<ProjectSummaryResponse> getUserProjects() {
        Long userId = authUtil.getCurrentUserId();
        var projects = projectRepository.findAllAccessibleByUser(userId);
        return projectMapper.toProjectSummaryResponseList(projects);
    }

    @Override
    @PreAuthorize("@security.canViewProject(#id)")
    public ProjectResponse getUserProjectById(Long id) {
        Long userId = authUtil.getCurrentUserId();
        Project project = getAccessibleProjectByIdAndUserId(id, userId);
        return projectMapper.toProjectResponse(project);
    }

    @Override
    @PreAuthorize("@security.canEditProject(#id)")
    public ProjectResponse updateProject(Long id, ProjectRequest request) {
        Long userId = authUtil.getCurrentUserId();
        Project project = getAccessibleProjectByIdAndUserId(id, userId);

        project.setName(request.name());
        project = projectRepository.save(project);

        return projectMapper.toProjectResponse(project);

    }

    @Override
    @PreAuthorize("@security.canDeleteProject(#id)")
    public void softDelete(Long id) {
        Long userId = authUtil.getCurrentUserId();
        Project project = getAccessibleProjectByIdAndUserId(id, userId);

        project.setDeletedAt(java.time.Instant.now());
        projectRepository.save(project);
    }

    /// Internal method to get project by id and userId
    private Project getAccessibleProjectByIdAndUserId(Long projectId, Long userId) {
        return projectRepository.findAccessibleProjectById(projectId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", projectId.toString()));
    }
}
