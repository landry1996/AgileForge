package com.agileforge.infrastructure.persistence.adapter;

import com.agileforge.domain.model.*;
import com.agileforge.domain.port.out.SearchPort;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class SearchAdapter implements SearchPort {

    private final EntityManager entityManager;

    public SearchAdapter(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public SearchResult<Ticket> searchTickets(UUID projectId, String query, String status,
                                              String type, String priority, UUID assigneeId,
                                              int page, int size) {
        StringBuilder sql = new StringBuilder("SELECT * FROM tickets WHERE is_deleted = false");
        StringBuilder countSql = new StringBuilder("SELECT COUNT(*) FROM tickets WHERE is_deleted = false");
        List<Object> params = new ArrayList<>();
        int paramIndex = 1;

        if (projectId != null) {
            String clause = " AND project_id = ?" + paramIndex;
            sql.append(clause);
            countSql.append(clause);
            params.add(projectId);
            paramIndex++;
        }

        if (query != null && !query.isBlank()) {
            String clause = " AND search_vector @@ plainto_tsquery('english', ?" + paramIndex + ")";
            sql.append(clause);
            countSql.append(clause);
            params.add(query.trim());
            paramIndex++;
        }

        if (status != null && !status.isBlank()) {
            String clause = " AND UPPER(status) = ?" + paramIndex;
            sql.append(clause);
            countSql.append(clause);
            params.add(status.toUpperCase());
            paramIndex++;
        }

        if (type != null && !type.isBlank()) {
            String clause = " AND UPPER(type) = ?" + paramIndex;
            sql.append(clause);
            countSql.append(clause);
            params.add(type.toUpperCase());
            paramIndex++;
        }

        if (priority != null && !priority.isBlank()) {
            String clause = " AND UPPER(priority) = ?" + paramIndex;
            sql.append(clause);
            countSql.append(clause);
            params.add(priority.toUpperCase());
            paramIndex++;
        }

        if (assigneeId != null) {
            String clause = " AND assignee_id = ?" + paramIndex;
            sql.append(clause);
            countSql.append(clause);
            params.add(assigneeId);
            paramIndex++;
        }

        if (query != null && !query.isBlank()) {
            sql.append(" ORDER BY ts_rank(search_vector, plainto_tsquery('english', ?").append(paramIndex).append(")) DESC");
            params.add(query.trim());
            paramIndex++;
        } else {
            sql.append(" ORDER BY created_at DESC");
        }

        sql.append(" LIMIT ").append(size).append(" OFFSET ").append(page * size);

        Query nativeQuery = entityManager.createNativeQuery(sql.toString());
        Query countQuery = entityManager.createNativeQuery(countSql.toString());

        for (int i = 0; i < params.size(); i++) {
            Object param = params.get(i);
            if (i < params.size() - (query != null && !query.isBlank() ? 1 : 0)) {
                nativeQuery.setParameter(i + 1, param);
            }
            countQuery.setParameter(i + 1, param);
        }

        // Re-set all params for nativeQuery (including ranking param)
        for (int i = 0; i < params.size(); i++) {
            nativeQuery.setParameter(i + 1, params.get(i));
        }

        @SuppressWarnings("unchecked")
        List<Object[]> rows = nativeQuery.getResultList();
        long totalCount = ((Number) countQuery.getSingleResult()).longValue();

        List<Ticket> tickets = rows.stream().map(this::mapRowToTicket).toList();
        return new SearchResult<>(tickets, totalCount, page, size);
    }

    private Ticket mapRowToTicket(Object[] row) {
        Ticket t = new Ticket();
        t.setId((UUID) row[0]);
        t.setProjectId(row[3] != null ? (UUID) row[3] : null);
        t.setKey(row[4] != null ? (String) row[4] : null);
        t.setNumber(row[5] != null ? ((Number) row[5]).longValue() : 0);
        t.setTitle(row[6] != null ? (String) row[6] : null);
        t.setDescription(row[7] != null ? (String) row[7] : null);
        t.setType(row[8] != null ? TicketType.valueOf(((String) row[8]).toUpperCase()) : null);
        t.setStatus(row[9] != null ? TicketStatus.valueOf(((String) row[9]).toUpperCase()) : null);
        t.setPriority(row[10] != null ? TicketPriority.valueOf(((String) row[10]).toUpperCase()) : null);
        t.setAssigneeId(row[11] != null ? (UUID) row[11] : null);
        t.setReporterId(row[12] != null ? (UUID) row[12] : null);
        t.setEpicId(row[13] != null ? (UUID) row[13] : null);
        t.setParentId(row[14] != null ? (UUID) row[14] : null);
        t.setSprintId(row[15] != null ? (UUID) row[15] : null);
        t.setStoryPoints(row[16] != null ? ((Number) row[16]).intValue() : null);
        t.setEstimatedHours(row[17] != null ? ((Number) row[17]).doubleValue() : null);
        t.setLoggedHours(row[18] != null ? ((Number) row[18]).doubleValue() : null);
        t.setDueDate(row[19] != null ? ((java.sql.Date) row[19]).toLocalDate() : null);
        t.setEnvironment(row[20] != null ? (String) row[20] : null);
        t.setComponent(row[21] != null ? (String) row[21] : null);
        t.setLabels(row[22] != null ? (String) row[22] : null);
        t.setAffectedVersion(row[23] != null ? (String) row[23] : null);
        t.setFixVersion(row[24] != null ? (String) row[24] : null);
        t.setQualityScore(row[25] != null ? ((Number) row[25]).intValue() : 0);
        return t;
    }
}
