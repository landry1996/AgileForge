package com.agileforge.domain.model;

import java.util.UUID;

public class BoardColumn {

    private UUID id;
    private UUID projectId;
    private String name;
    private TicketStatus mappedStatus;
    private int position;
    private Integer wipLimit;

    public BoardColumn() {}

    public BoardColumn(UUID projectId, String name, TicketStatus mappedStatus, int position) {
        this.projectId = projectId;
        this.name = name;
        this.mappedStatus = mappedStatus;
        this.position = position;
    }

    public boolean isWipExceeded(int currentCount) {
        return wipLimit != null && currentCount >= wipLimit;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getProjectId() { return projectId; }
    public void setProjectId(UUID projectId) { this.projectId = projectId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public TicketStatus getMappedStatus() { return mappedStatus; }
    public void setMappedStatus(TicketStatus mappedStatus) { this.mappedStatus = mappedStatus; }

    public int getPosition() { return position; }
    public void setPosition(int position) { this.position = position; }

    public Integer getWipLimit() { return wipLimit; }
    public void setWipLimit(Integer wipLimit) { this.wipLimit = wipLimit; }
}
