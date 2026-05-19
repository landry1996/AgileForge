package com.agileforge.domain.model;

import java.time.Instant;
import java.util.UUID;

public class KeyResult {

    private UUID id;
    private UUID objectiveId;
    private String title;
    private double targetValue;
    private double currentValue;
    private String unit;
    private double startValue;
    private Instant createdAt;
    private Instant updatedAt;

    public KeyResult() {}

    public KeyResult(UUID objectiveId, String title, double targetValue, String unit, double startValue) {
        this.objectiveId = objectiveId;
        this.title = title;
        this.targetValue = targetValue;
        this.unit = unit;
        this.startValue = startValue;
        this.currentValue = startValue;
    }

    public int getProgress() {
        if (targetValue == startValue) {
            return 0;
        }
        return (int) ((currentValue - startValue) / (targetValue - startValue) * 100);
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getObjectiveId() { return objectiveId; }
    public void setObjectiveId(UUID objectiveId) { this.objectiveId = objectiveId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public double getTargetValue() { return targetValue; }
    public void setTargetValue(double targetValue) { this.targetValue = targetValue; }

    public double getCurrentValue() { return currentValue; }
    public void setCurrentValue(double currentValue) { this.currentValue = currentValue; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public double getStartValue() { return startValue; }
    public void setStartValue(double startValue) { this.startValue = startValue; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
