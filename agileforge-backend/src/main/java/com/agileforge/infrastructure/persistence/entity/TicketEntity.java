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
@Table(name = "tickets", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"project_id", "number"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TicketEntity extends BaseEntity {

    @Column(name = "project_id", nullable = false)
    private UUID projectId;

    @Column(nullable = false, length = 10)
    private String key;

    @Column(nullable = false)
    private long number;

    @Column(nullable = false, length = 500)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 30)
    private String type;

    @Column(nullable = false, length = 30)
    private String status;

    @Column(nullable = false, length = 20)
    private String priority;

    @Column(name = "assignee_id")
    private UUID assigneeId;

    @Column(name = "reporter_id", nullable = false)
    private UUID reporterId;

    @Column(name = "epic_id")
    private UUID epicId;

    @Column(name = "parent_id")
    private UUID parentId;

    @Column(name = "sprint_id")
    private UUID sprintId;

    @Column(name = "story_points")
    private Integer storyPoints;

    @Column(name = "estimated_hours")
    private Double estimatedHours;

    @Column(name = "logged_hours")
    private Double loggedHours;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(length = 50)
    private String environment;

    @Column(length = 100)
    private String component;

    @Column(length = 500)
    private String labels;

    @Column(name = "affected_version", length = 50)
    private String affectedVersion;

    @Column(name = "fix_version", length = 50)
    private String fixVersion;

    @Column(name = "quality_score")
    private int qualityScore;
}
