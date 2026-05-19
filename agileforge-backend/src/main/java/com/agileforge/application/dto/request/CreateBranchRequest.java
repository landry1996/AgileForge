package com.agileforge.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateBranchRequest(
        UUID ticketId,

        @NotBlank(message = "Branch name is required")
        @Size(max = 255, message = "Branch name must not exceed 255 characters")
        String branchName
) {}
