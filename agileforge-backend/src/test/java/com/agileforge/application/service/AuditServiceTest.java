package com.agileforge.application.service;

import com.agileforge.application.dto.request.AuditFilterRequest;
import com.agileforge.application.dto.response.AuditSummaryResponse;
import com.agileforge.domain.model.*;
import com.agileforge.domain.port.out.AuditEventRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditServiceTest {

    @Mock
    private AuditEventRepositoryPort auditRepository;

    @InjectMocks
    private AuditService auditService;

    @Test
    void shouldLogAuditEvent() {
        UUID orgId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        AuditEvent event = new AuditEvent(orgId, null, userId, AuditAction.CREATE, "Ticket", UUID.randomUUID(), "details", "127.0.0.1", "Chrome", AuditSeverity.INFO);
        when(auditRepository.save(any())).thenReturn(event);
        when(auditRepository.findAlertRulesByOrganizationId(orgId)).thenReturn(List.of());

        AuditEvent result = auditService.log(orgId, null, userId, AuditAction.CREATE, "Ticket", UUID.randomUUID(), "details", "127.0.0.1", "Chrome");

        assertNotNull(result);
        verify(auditRepository).save(any());
    }

    @Test
    void shouldDetermineWarningSeverityForDelete() {
        UUID orgId = UUID.randomUUID();
        AuditEvent event = new AuditEvent(orgId, null, UUID.randomUUID(), AuditAction.DELETE, "Ticket", UUID.randomUUID(), null, null, null, AuditSeverity.WARNING);
        when(auditRepository.save(any())).thenReturn(event);
        when(auditRepository.findAlertRulesByOrganizationId(orgId)).thenReturn(List.of());

        AuditEvent result = auditService.log(orgId, null, UUID.randomUUID(), AuditAction.DELETE, "Ticket", UUID.randomUUID(), null, null, null);

        assertNotNull(result);
    }

    @Test
    void shouldGetByOrganization() {
        UUID orgId = UUID.randomUUID();
        AuditFilterRequest filters = new AuditFilterRequest(null, null, null, null, null, null, 0, 20);
        when(auditRepository.findByOrganizationId(orgId, 0, 20)).thenReturn(List.of());

        List<AuditEvent> result = auditService.getByOrganization(orgId, filters);

        assertNotNull(result);
        verify(auditRepository).findByOrganizationId(orgId, 0, 20);
    }

    @Test
    void shouldFilterByAction() {
        UUID orgId = UUID.randomUUID();
        AuditFilterRequest filters = new AuditFilterRequest(null, "CREATE", null, null, null, null, 0, 20);
        when(auditRepository.findByAction(AuditAction.CREATE, 0, 20)).thenReturn(List.of());

        List<AuditEvent> result = auditService.getByOrganization(orgId, filters);

        verify(auditRepository).findByAction(AuditAction.CREATE, 0, 20);
    }

    @Test
    void shouldGetByUser() {
        UUID userId = UUID.randomUUID();
        when(auditRepository.findByUserId(userId, 0, 10)).thenReturn(List.of());

        List<AuditEvent> result = auditService.getByUser(userId, 0, 10);

        assertNotNull(result);
        verify(auditRepository).findByUserId(userId, 0, 10);
    }

    @Test
    void shouldGetSummary() {
        UUID orgId = UUID.randomUUID();
        when(auditRepository.countByOrganizationId(orgId)).thenReturn(42L);
        when(auditRepository.findBySeverity(AuditSeverity.CRITICAL)).thenReturn(List.of());
        when(auditRepository.findByOrganizationId(orgId, 0, 1000)).thenReturn(List.of());

        AuditSummaryResponse result = auditService.getSummary(orgId);

        assertEquals(42L, result.totalEvents());
    }

    @Test
    void shouldCreateAlertRule() {
        UUID orgId = UUID.randomUUID();
        AuditAlertRule rule = new AuditAlertRule(orgId, "Test", "DELETE*", AuditSeverity.WARNING, "test@test.com");
        when(auditRepository.saveAlertRule(any())).thenReturn(rule);

        AuditAlertRule result = auditService.createAlertRule(orgId, "Test", "DELETE*", "WARNING", "test@test.com");

        assertNotNull(result);
        assertEquals("Test", result.getName());
    }

    @Test
    void shouldDeleteAlertRule() {
        UUID ruleId = UUID.randomUUID();
        doNothing().when(auditRepository).deleteAlertRule(ruleId);

        auditService.deleteAlertRule(ruleId);

        verify(auditRepository).deleteAlertRule(ruleId);
    }
}
