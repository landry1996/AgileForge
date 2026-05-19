package com.agileforge.infrastructure.web.controller;

import com.agileforge.application.dto.request.CreateTicketLinkRequest;
import com.agileforge.application.dto.response.TicketLinkResponse;
import com.agileforge.application.service.TicketLinkService;
import com.agileforge.domain.model.TicketLink;
import com.agileforge.domain.port.out.UserRepositoryPort;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/tickets/{ticketId}/links")
@Tag(name = "Ticket Links", description = "Ticket dependency/link management endpoints")
public class TicketLinkController {

    private final TicketLinkService ticketLinkService;
    private final UserRepositoryPort userRepository;

    public TicketLinkController(TicketLinkService ticketLinkService, UserRepositoryPort userRepository) {
        this.ticketLinkService = ticketLinkService;
        this.userRepository = userRepository;
    }

    @PostMapping
    @Operation(summary = "Create a link between two tickets")
    public ResponseEntity<TicketLinkResponse> createLink(@PathVariable UUID ticketId,
                                                         @Valid @RequestBody CreateTicketLinkRequest request,
                                                         Authentication auth) {
        String userEmail = auth.getName();
        TicketLink link = ticketLinkService.createLink(ticketId, request.targetTicketId(),
                request.linkType(), userEmail);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(link));
    }

    @GetMapping
    @Operation(summary = "Get all links for a ticket")
    public ResponseEntity<List<TicketLinkResponse>> getLinks(@PathVariable UUID ticketId) {
        List<TicketLinkResponse> links = ticketLinkService.getLinksForTicket(ticketId).stream()
                .map(this::toResponse).toList();
        return ResponseEntity.ok(links);
    }

    @DeleteMapping("/{linkId}")
    @Operation(summary = "Delete a ticket link")
    public ResponseEntity<Void> deleteLink(@PathVariable UUID ticketId,
                                           @PathVariable UUID linkId) {
        ticketLinkService.deleteLink(linkId);
        return ResponseEntity.noContent().build();
    }

    private TicketLinkResponse toResponse(TicketLink link) {
        String sourceKey = ticketLinkService.getTicketKey(link.getSourceTicketId());
        String targetKey = ticketLinkService.getTicketKey(link.getTargetTicketId());
        return new TicketLinkResponse(
                link.getId(),
                link.getSourceTicketId(),
                link.getTargetTicketId(),
                link.getLinkType().name(),
                sourceKey,
                targetKey,
                link.getCreatedAt()
        );
    }
}
