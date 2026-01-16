package com.codingshuttle.projects.lovable_clone.service.impl;

import com.codingshuttle.projects.lovable_clone.dto.project.ProjectRequest;
import com.codingshuttle.projects.lovable_clone.dto.project.ProjectResponse;
import com.codingshuttle.projects.lovable_clone.dto.project.ProjectSummaryResponse;
import com.codingshuttle.projects.lovable_clone.entity.Project;
import com.codingshuttle.projects.lovable_clone.entity.User;
import com.codingshuttle.projects.lovable_clone.mapper.ProjectMapper;
import com.codingshuttle.projects.lovable_clone.repository.ProjectRepository;
import com.codingshuttle.projects.lovable_clone.repository.UserRepository;
import com.codingshuttle.projects.lovable_clone.service.ProjectService;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Transactional
public class ProjectServiceImpl implements ProjectService {

    ProjectRepository projectRepository;
    UserRepository userRepository;
    ProjectMapper projectMapper;

    @Override
    public ProjectResponse createProject(ProjectRequest request, Long userId) {
        User owner = userRepository.findById(userId).orElseThrow();

        Project project = Project.builder()
                .name(request.name())
                .owner(owner)
                .isPublic(false)
                .build();

        project = projectRepository.save(project);
        return projectMapper.toProjectResponse(project);

    }

    @Override
    public List<ProjectSummaryResponse> getUserProjects(Long userId) {
        //One way of getting projects accessible by user is to get all projects where user is owner or projects which are public
        /*
                return projectRepository.findAllAccessibleByUser(userId)
                .stream()
                .map(projectMapper::toProjectSummaryResponse)
                .toList();
        */

        var projects = projectRepository.findAllAccessibleByUser(userId);
        return projectMapper.toProjectSummaryResponseList(projects);
    }

    @Override
    public ProjectResponse getUserProjectById(Long id, Long userId) {
        Project project = getAccessibleProjectByIdAndUserId(id, userId);
        return projectMapper.toProjectResponse(project);
    }

    @Override
    public ProjectResponse updateProject(Long id, ProjectRequest request, Long userId) {
        Project project = getAccessibleProjectByIdAndUserId(id, userId);

        project.setName(request.name());
        project = projectRepository.save(project);

        return projectMapper.toProjectResponse(project);

    }

    @Override
    public void softDelete(Long id, Long userId) {
        Project project = getAccessibleProjectByIdAndUserId(id, userId);

        if(!project.getOwner().getId().equals(userId))
            throw new RuntimeException(("You are not authorized to delete this project"));
        project.setDeletedAt(java.time.Instant.now());
        projectRepository.save(project);
    }

    /// Internal method to get project by id and userId
    private Project getAccessibleProjectByIdAndUserId(Long projectId, Long userId) {
        return projectRepository.findAccessibleProjectById(projectId, userId)
                .orElseThrow();
    }
}
