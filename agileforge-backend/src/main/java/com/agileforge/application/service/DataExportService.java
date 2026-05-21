package com.agileforge.application.service;

import com.agileforge.domain.model.AuditEvent;
import com.agileforge.domain.model.Ticket;
import com.agileforge.domain.model.User;
import com.agileforge.domain.port.out.AuditEventRepositoryPort;
import com.agileforge.domain.port.out.TicketRepositoryPort;
import com.agileforge.domain.port.out.UserRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.*;

@Service
@Transactional(readOnly = true)
public class DataExportService {

    private final AuditEventRepositoryPort auditRepository;
    private final TicketRepositoryPort ticketRepository;
    private final UserRepositoryPort userRepository;

    public DataExportService(AuditEventRepositoryPort auditRepository,
                             TicketRepositoryPort ticketRepository,
                             UserRepositoryPort userRepository) {
        this.auditRepository = auditRepository;
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
    }

    public String exportAuditCsv(UUID orgId, String fromDate, String toDate) {
        List<AuditEvent> events = auditRepository.findByOrganizationId(orgId, 0, 10000);

        Instant from = fromDate != null ? LocalDate.parse(fromDate).atStartOfDay(ZoneOffset.UTC).toInstant() : Instant.MIN;
        Instant to = toDate != null ? LocalDate.parse(toDate).plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant() : Instant.MAX;

        List<AuditEvent> filtered = events.stream()
                .filter(e -> e.getCreatedAt() != null)
                .filter(e -> e.getCreatedAt().isAfter(from) && e.getCreatedAt().isBefore(to))
                .toList();

        StringBuilder csv = new StringBuilder();
        csv.append("Timestamp,Action,EntityType,EntityId,UserId,Severity,IPAddress\n");
        for (AuditEvent e : filtered) {
            csv.append(String.format("%s,%s,%s,%s,%s,%s,%s\n",
                    e.getCreatedAt(), e.getAction(), e.getEntityType(),
                    e.getEntityId(), e.getUserId(),
                    e.getSeverity() != null ? e.getSeverity().name() : "",
                    e.getIpAddress() != null ? e.getIpAddress() : ""));
        }
        return csv.toString();
    }

    public String exportProjectTicketsCsv(UUID projectId) {
        List<Ticket> tickets = ticketRepository.findByProjectId(projectId);

        StringBuilder csv = new StringBuilder();
        csv.append("Key,Title,Type,Status,Priority,AssigneeId,StoryPoints,CreatedAt\n");
        for (Ticket t : tickets) {
            csv.append(String.format("%s,\"%s\",%s,%s,%s,%s,%s,%s\n",
                    t.getFullKey() != null ? t.getFullKey() : "",
                    escapeCsv(t.getTitle()),
                    t.getType() != null ? t.getType().name() : "",
                    t.getStatus() != null ? t.getStatus().name() : "",
                    t.getPriority() != null ? t.getPriority().name() : "",
                    t.getAssigneeId() != null ? t.getAssigneeId() : "",
                    t.getStoryPoints() != null ? t.getStoryPoints() : "",
                    t.getCreatedAt()));
        }
        return csv.toString();
    }

    public Map<String, Object> exportUserData(UUID userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return Map.of("error", "User not found");
        }

        User user = userOpt.get();
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", user.getId());
        data.put("email", user.getEmail());
        data.put("firstName", user.getFirstName());
        data.put("lastName", user.getLastName());
        data.put("createdAt", user.getCreatedAt());

        List<Ticket> assignedTickets = ticketRepository.findByAssigneeId(userId);
        List<Map<String, Object>> ticketData = assignedTickets.stream().map(t -> {
            Map<String, Object> td = new LinkedHashMap<>();
            td.put("id", t.getId());
            td.put("title", t.getTitle());
            td.put("type", t.getType() != null ? t.getType().name() : null);
            td.put("status", t.getStatus() != null ? t.getStatus().name() : null);
            td.put("createdAt", t.getCreatedAt());
            return td;
        }).toList();
        data.put("assignedTickets", ticketData);

        List<AuditEvent> auditEvents = auditRepository.findByUserId(userId, 0, 1000);
        List<Map<String, Object>> auditData = auditEvents.stream().map(e -> {
            Map<String, Object> ad = new LinkedHashMap<>();
            ad.put("action", e.getAction() != null ? e.getAction().name() : null);
            ad.put("entityType", e.getEntityType());
            ad.put("createdAt", e.getCreatedAt());
            return ad;
        }).toList();
        data.put("auditEvents", auditData);

        data.put("exportedAt", Instant.now());
        return data;
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        return value.replace("\"", "\"\"");
    }
}
