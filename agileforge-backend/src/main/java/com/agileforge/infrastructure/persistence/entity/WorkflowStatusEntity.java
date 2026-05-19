package com.agileforge.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "workflow_statuses", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"workflow_id", "name"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowStatusEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "workflow_id", nullable = false)
    private UUID workflowId;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, length = 20)
    private String category;

    @Column(nullable = false)
    private int position;

    @Column(length = 7)
    private String color;
}
