package com.agileforge.application.service;

import com.agileforge.domain.exception.BusinessException;
import com.agileforge.domain.exception.EntityNotFoundException;
import com.agileforge.domain.exception.ForbiddenException;
import com.agileforge.domain.model.Organization;
import com.agileforge.domain.model.Project;
import com.agileforge.domain.model.ProjectMember;
import com.agileforge.domain.port.out.OrganizationMemberRepositoryPort;
import com.agileforge.domain.port.out.OrganizationRepositoryPort;
import com.agileforge.domain.port.out.ProjectMemberRepositoryPort;
import com.agileforge.domain.port.out.ProjectRepositoryPort;
import com.agileforge.infrastructure.persistence.entity.RoleEntity;
import com.agileforge.infrastructure.persistence.repository.JpaRoleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ProjectService {

    private static final Logger log = LoggerFactory.getLogger(ProjectService.class);

    private final ProjectRepositoryPort projectRepository;
    private final ProjectMemberRepositoryPort projectMemberRepository;
    private final OrganizationRepositoryPort organizationRepository;
    private final OrganizationMemberRepositoryPort orgMemberRepository;
    private final JpaRoleRepository roleRepository;

    public ProjectService(ProjectRepositoryPort projectRepository,
                          ProjectMemberRepositoryPort projectMemberRepository,
                          OrganizationRepositoryPort organizationRepository,
                          OrganizationMemberRepositoryPort orgMemberRepository,
                          JpaRoleRepository roleRepository) {
        this.projectRepository = projectRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.organizationRepository = organizationRepository;
        this.orgMemberRepository = orgMemberRepository;
        this.roleRepository = roleRepository;
    }

    public Project create(UUID organizationId, String name, String key, String description,
                          String type, String visibility, String startDate, String endDate, UUID creatorId) {

        verifyOrgMembership(organizationId, creatorId);

        Organization org = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new EntityNotFoundException("Organization", organizationId));

        int currentProjects = organizationRepository.countProjectsByOrganizationId(organizationId);
        if (!org.canAddProject(currentProjects)) {
            throw new BusinessException("Organization has reached maximum projects limit (" + org.getMaxProjects() + ")");
        }

        if (projectRepository.existsByOrganizationIdAndKey(organizationId, key)) {
            throw new BusinessException("Project key already exists in this organization: " + key);
        }

        Project.ProjectType projectType = type != null ? Project.ProjectType.valueOf(type.toUpperCase()) : Project.ProjectType.SOFTWARE;
        Project project = new Project(organizationId, name, key, description, projectType);

        if (visibility != null) {
            project.setVisibility(Project.ProjectVisibility.valueOf(visibility.toUpperCase()));
        }
        if (startDate != null) project.setStartDate(LocalDate.parse(startDate));
        if (endDate != null) project.setEndDate(LocalDate.parse(endDate));
        project.setLeadId(creatorId);

        Project saved = projectRepository.save(project);

        RoleEntity pmRole = roleRepository.findByCode("PROJECT_MANAGER")
                .orElseThrow(() -> new BusinessException("Default role PROJECT_MANAGER not found"));

        ProjectMember member = new ProjectMember(saved.getId(), creatorId, pmRole.getId());
        projectMemberRepository.save(member);

        log.info("Project created: {} ({}) in org {}", name, key, organizationId);
        return saved;
    }

    @Transactional(readOnly = true)
    public Project getById(UUID id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Project", id));
    }

    @Transactional(readOnly = true)
    public List<Project> getByOrganizationId(UUID organizationId) {
        return projectRepository.findByOrganizationId(organizationId);
    }

    @Transactional(readOnly = true)
    public List<Project> getByUserId(UUID userId) {
        return projectRepository.findByUserId(userId);
    }

    public Project update(UUID id, String name, String description, String type,
                          String visibility, String status, String startDate, String endDate,
                          String logoUrl, UUID requesterId) {

        Project project = getById(id);
        verifyProjectMembership(id, requesterId);

        if (name != null) project.setName(name);
        if (description != null) project.setDescription(description);
        if (type != null) project.setType(Project.ProjectType.valueOf(type.toUpperCase()));
        if (visibility != null) project.setVisibility(Project.ProjectVisibility.valueOf(visibility.toUpperCase()));
        if (status != null) project.setStatus(Project.ProjectStatus.valueOf(status.toUpperCase()));
        if (startDate != null) project.setStartDate(LocalDate.parse(startDate));
        if (endDate != null) project.setEndDate(LocalDate.parse(endDate));
        if (logoUrl != null) project.setLogoUrl(logoUrl);

        return projectRepository.save(project);
    }

    public void addMember(UUID projectId, UUID userId, String roleCode, UUID requesterId) {
        verifyProjectMembership(projectId, requesterId);

        if (projectMemberRepository.existsByProjectIdAndUserId(projectId, userId)) {
            throw new BusinessException("User is already a member of this project");
        }

        RoleEntity role = roleRepository.findByCode(roleCode)
                .orElseThrow(() -> new EntityNotFoundException("Role not found: " + roleCode));

        ProjectMember member = new ProjectMember(projectId, userId, role.getId());
        projectMemberRepository.save(member);

        log.info("Member added to project {}: user={}, role={}", projectId, userId, roleCode);
    }

    public void removeMember(UUID projectId, UUID userId, UUID requesterId) {
        verifyProjectMembership(projectId, requesterId);

        ProjectMember member = projectMemberRepository.findByProjectIdAndUserId(projectId, userId)
                .orElseThrow(() -> new EntityNotFoundException("Member not found in project"));

        projectMemberRepository.delete(member.getId());
        log.info("Member removed from project {}: user={}", projectId, userId);
    }

    @Transactional(readOnly = true)
    public List<ProjectMember> getMembers(UUID projectId) {
        return projectMemberRepository.findByProjectId(projectId);
    }

    private void verifyOrgMembership(UUID organizationId, UUID userId) {
        if (!orgMemberRepository.existsByOrganizationIdAndUserId(organizationId, userId)) {
            throw new ForbiddenException("You are not a member of this organization");
        }
    }

    private void verifyProjectMembership(UUID projectId, UUID userId) {
        if (!projectMemberRepository.existsByProjectIdAndUserId(projectId, userId)) {
            throw new ForbiddenException("You are not a member of this project");
        }
    }
}
