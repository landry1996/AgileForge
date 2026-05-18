package com.agileforge.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "board_columns")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BoardColumnEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "project_id", nullable = false)
    private UUID projectId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "mapped_status", nullable = false, length = 30)
    private String mappedStatus;

    @Column(nullable = false)
    private int position;

    @Column(name = "wip_limit")
    private Integer wipLimit;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
