package com.agileforge.application.dto.response;

import java.util.UUID;

public record BlockedTicketAlert(
        UUID ticketId,
        String ticketKey,
        String title,
        String reason,
        String currentStatus,
        int daysInStatus,
        String suggestion
) {}
