package com.agileforge.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record CreatePortfolioRequest(
        @NotBlank @Size(max = 200) String name,
        String description,
        List<UUID> projectIds
) {}
