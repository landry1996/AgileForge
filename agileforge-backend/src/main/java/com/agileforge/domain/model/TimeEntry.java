package com.agileforge.domain.model;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public class TimeEntry {

    private UUID id;
    private UUID ticketId;
    private UUID userId;
    private double hours;
    private String description;
    private LocalDate workDate;
    private Instant createdAt;

    public TimeEntry() {}

    public TimeEntry(UUID ticketId, UUID userId, double hours, String description, LocalDate workDate) {
        this.ticketId = ticketId;
        this.userId = userId;
        this.hours = hours;
        this.description = description;
        this.workDate = workDate;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getTicketId() { return ticketId; }
    public void setTicketId(UUID ticketId) { this.ticketId = ticketId; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public double getHours() { return hours; }
    public void setHours(double hours) { this.hours = hours; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDate getWorkDate() { return workDate; }
    public void setWorkDate(LocalDate workDate) { this.workDate = workDate; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
