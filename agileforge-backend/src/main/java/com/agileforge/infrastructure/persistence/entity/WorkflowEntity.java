package com.agileforge.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "workflows", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"project_id", "ticket_type"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowEntity extends BaseEntity {

    @Column(name = "project_id", nullable = false)
    private UUID projectId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "ticket_type", nullable = false, length = 30)
    private String ticketType;

    @Column(name = "is_default", nullable = false)
    private boolean isDefault = false;
}
