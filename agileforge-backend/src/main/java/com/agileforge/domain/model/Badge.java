package com.agileforge.domain.model;

import java.time.Instant;
import java.util.UUID;

public class Badge {

    private UUID id;
    private String name;
    private String description;
    private String icon;
    private String category;
    private String criteriaType;
    private int criteriaThreshold;
    private int points;
    private BadgeRarity rarity;
    private Instant createdAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getCriteriaType() { return criteriaType; }
    public void setCriteriaType(String criteriaType) { this.criteriaType = criteriaType; }
    public int getCriteriaThreshold() { return criteriaThreshold; }
    public void setCriteriaThreshold(int criteriaThreshold) { this.criteriaThreshold = criteriaThreshold; }
    public int getPoints() { return points; }
    public void setPoints(int points) { this.points = points; }
    public BadgeRarity getRarity() { return rarity; }
    public void setRarity(BadgeRarity rarity) { this.rarity = rarity; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
