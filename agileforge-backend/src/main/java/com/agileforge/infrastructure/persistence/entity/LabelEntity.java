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

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "labels", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"project_id", "name"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LabelEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "project_id", nullable = false)
    private UUID projectId;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, length = 7)
    private String color;

    @Column(length = 200)
    private String description;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
