package com.agileforge.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "documents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DocumentEntity extends BaseEntity {

    @Column(name = "project_id", nullable = false)
    private UUID projectId;

    @Column(name = "parent_id")
    private UUID parentId;

    @Column(nullable = false, length = 300)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "doc_type", nullable = false, length = 30)
    private String docType;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(nullable = false)
    private int position;

    @Column(name = "author_id", nullable = false)
    private UUID authorId;

    @Column(name = "last_edited_by")
    private UUID lastEditedBy;

    @Column(nullable = false)
    private int version;
}
