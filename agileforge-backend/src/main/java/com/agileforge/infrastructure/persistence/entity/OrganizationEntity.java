package com.agileforge.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "organizations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationEntity extends BaseEntity {

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false, unique = true, length = 100)
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "logo_url", length = 500)
    private String logoUrl;

    @Column(length = 500)
    private String website;

    @Column(nullable = false, length = 50)
    private String plan;

    @Column(name = "max_users")
    private int maxUsers;

    @Column(name = "max_projects")
    private int maxProjects;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;
}
