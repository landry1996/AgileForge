package com.agileforge.infrastructure.persistence.adapter;

import com.agileforge.domain.model.OrganizationMember;
import com.agileforge.domain.port.out.OrganizationMemberRepositoryPort;
import com.agileforge.infrastructure.persistence.entity.OrganizationMemberEntity;
import com.agileforge.infrastructure.persistence.repository.JpaOrganizationMemberRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class OrganizationMemberRepositoryAdapter implements OrganizationMemberRepositoryPort {

    private final JpaOrganizationMemberRepository repository;

    public OrganizationMemberRepositoryAdapter(JpaOrganizationMemberRepository repository) {
        this.repository = repository;
    }

    @Override
    public OrganizationMember save(OrganizationMember member) {
        OrganizationMemberEntity entity = toEntity(member);
        OrganizationMemberEntity saved = repository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<OrganizationMember> findByOrganizationIdAndUserId(UUID organizationId, UUID userId) {
        return repository.findByOrganizationIdAndUserIdAndActiveTrue(organizationId, userId)
                .map(this::toDomain);
    }

    @Override
    public List<OrganizationMember> findByOrganizationId(UUID organizationId) {
        return repository.findByOrganizationIdAndActiveTrue(organizationId).stream()
                .map(this::toDomain).toList();
    }

    @Override
    public boolean existsByOrganizationIdAndUserId(UUID organizationId, UUID userId) {
        return repository.existsByOrganizationIdAndUserIdAndActiveTrue(organizationId, userId);
    }

    @Override
    public void delete(UUID id) {
        repository.findById(id).ifPresent(entity -> {
            entity.setActive(false);
            repository.save(entity);
        });
    }

    private OrganizationMemberEntity toEntity(OrganizationMember domain) {
        OrganizationMemberEntity entity = new OrganizationMemberEntity();
        entity.setId(domain.getId());
        entity.setOrganizationId(domain.getOrganizationId());
        entity.setUserId(domain.getUserId());
        entity.setRoleId(domain.getRoleId());
        entity.setJoinedAt(domain.getJoinedAt());
        entity.setActive(domain.isActive());
        return entity;
    }

    private OrganizationMember toDomain(OrganizationMemberEntity entity) {
        OrganizationMember domain = new OrganizationMember();
        domain.setId(entity.getId());
        domain.setOrganizationId(entity.getOrganizationId());
        domain.setUserId(entity.getUserId());
        domain.setRoleId(entity.getRoleId());
        domain.setJoinedAt(entity.getJoinedAt());
        domain.setActive(entity.isActive());
        return domain;
    }
}
