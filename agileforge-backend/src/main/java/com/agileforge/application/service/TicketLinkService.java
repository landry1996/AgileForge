package com.agileforge.application.service;

import com.agileforge.domain.exception.BusinessException;
import com.agileforge.domain.exception.EntityNotFoundException;
import com.agileforge.domain.model.Ticket;
import com.agileforge.domain.model.TicketLink;
import com.agileforge.domain.model.TicketLinkType;
import com.agileforge.domain.port.out.TicketLinkRepositoryPort;
import com.agileforge.domain.port.out.TicketRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional
public class TicketLinkService {

    private static final Logger log = LoggerFactory.getLogger(TicketLinkService.class);

    private final TicketLinkRepositoryPort ticketLinkRepository;
    private final TicketRepositoryPort ticketRepository;

    public TicketLinkService(TicketLinkRepositoryPort ticketLinkRepository,
                             TicketRepositoryPort ticketRepository) {
        this.ticketLinkRepository = ticketLinkRepository;
        this.ticketRepository = ticketRepository;
    }

    public TicketLink createLink(UUID sourceTicketId, UUID targetTicketId, String linkTypeStr, String createdBy) {
        // Validate both tickets exist
        Ticket sourceTicket = ticketRepository.findById(sourceTicketId)
                .orElseThrow(() -> new EntityNotFoundException("Ticket", sourceTicketId));
        Ticket targetTicket = ticketRepository.findById(targetTicketId)
                .orElseThrow(() -> new EntityNotFoundException("Ticket", targetTicketId));

        if (sourceTicketId.equals(targetTicketId)) {
            throw new BusinessException("A ticket cannot be linked to itself");
        }

        TicketLinkType linkType = TicketLinkType.valueOf(linkTypeStr.toUpperCase());

        // Check if link already exists
        if (ticketLinkRepository.existsLink(sourceTicketId, targetTicketId, linkType)) {
            throw new BusinessException("This link already exists");
        }

        // Check for circular dependencies if BLOCKS type
        if (linkType == TicketLinkType.BLOCKS) {
            detectBlockingCycle(sourceTicketId, targetTicketId);
        }

        // Create the primary link
        TicketLink link = new TicketLink(sourceTicketId, targetTicketId, linkType, createdBy);
        TicketLink saved = ticketLinkRepository.save(link);

        // Create inverse link
        TicketLinkType inverseType = linkType.getInverse();
        if (!ticketLinkRepository.existsLink(targetTicketId, sourceTicketId, inverseType)) {
            TicketLink inverseLink = new TicketLink(targetTicketId, sourceTicketId, inverseType, createdBy);
            ticketLinkRepository.save(inverseLink);
        }

        log.info("Ticket link created: {} --[{}]--> {} by {}",
                sourceTicket.getFullKey(), linkType, targetTicket.getFullKey(), createdBy);
        return saved;
    }

    @Transactional(readOnly = true)
    public List<TicketLink> getLinksForTicket(UUID ticketId) {
        // Verify ticket exists
        ticketRepository.findById(ticketId)
                .orElseThrow(() -> new EntityNotFoundException("Ticket", ticketId));
        return ticketLinkRepository.findAllByTicketId(ticketId);
    }

    public void deleteLink(UUID linkId) {
        TicketLink link = ticketLinkRepository.findById(linkId)
                .orElseThrow(() -> new EntityNotFoundException("TicketLink", linkId));

        TicketLinkType inverseType = link.getLinkType().getInverse();

        // Delete the primary link
        ticketLinkRepository.deleteById(linkId);

        // Delete the inverse link
        ticketLinkRepository.delete(link.getTargetTicketId(), link.getSourceTicketId(), inverseType);

        log.info("Ticket link deleted: {} --[{}]--> {}",
                link.getSourceTicketId(), link.getLinkType(), link.getTargetTicketId());
    }

    @Transactional(readOnly = true)
    public String getTicketKey(UUID ticketId) {
        return ticketRepository.findById(ticketId)
                .map(Ticket::getFullKey)
                .orElse(null);
    }

    private void detectBlockingCycle(UUID sourceTicketId, UUID targetTicketId) {
        // If we're saying sourceTicket BLOCKS targetTicket, check if targetTicket
        // already (transitively) blocks sourceTicket - that would create a cycle.
        Set<UUID> visited = new HashSet<>();
        Queue<UUID> queue = new LinkedList<>();
        queue.add(targetTicketId);

        while (!queue.isEmpty()) {
            UUID current = queue.poll();
            if (current.equals(sourceTicketId)) {
                throw new BusinessException(
                        "Cannot create this blocking link: it would create a circular dependency");
            }
            if (visited.contains(current)) {
                continue;
            }
            visited.add(current);

            // Find all tickets that 'current' BLOCKS
            List<TicketLink> outgoingBlocks = ticketLinkRepository.findBySourceTicketId(current).stream()
                    .filter(link -> link.getLinkType() == TicketLinkType.BLOCKS)
                    .toList();

            for (TicketLink link : outgoingBlocks) {
                if (!visited.contains(link.getTargetTicketId())) {
                    queue.add(link.getTargetTicketId());
                }
            }
        }
    }
}
