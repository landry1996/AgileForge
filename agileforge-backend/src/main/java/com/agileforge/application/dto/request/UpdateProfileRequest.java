package com.agileforge.application.dto.request;

import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
        @Size(min = 1, max = 100) String firstName,
        @Size(min = 1, max = 100) String lastName,
        @Size(max = 200) String displayName,
        @Size(max = 500) String avatarUrl,
        @Size(max = 20) String phone,
        @Size(max = 50) String timezone,
        @Size(max = 10) String locale
) {}
