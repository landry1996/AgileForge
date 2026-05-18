package com.agileforge.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "projects", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"organization_id", "key"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProjectEntity extends BaseEntity {

    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false, length = 10)
    private String key;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "logo_url", length = 500)
    private String logoUrl;

    @Column(nullable = false, length = 30)
    private String type;

    @Column(nullable = false, length = 20)
    private String visibility;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "lead_id")
    private UUID leadId;
}
