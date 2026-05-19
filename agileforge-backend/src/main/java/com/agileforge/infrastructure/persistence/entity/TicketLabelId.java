package com.agileforge.infrastructure.persistence.entity;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public class TicketLabelId implements Serializable {

    private UUID ticketId;
    private UUID labelId;

    public TicketLabelId() {}

    public TicketLabelId(UUID ticketId, UUID labelId) {
        this.ticketId = ticketId;
        this.labelId = labelId;
    }

    public UUID getTicketId() { return ticketId; }
    public void setTicketId(UUID ticketId) { this.ticketId = ticketId; }

    public UUID getLabelId() { return labelId; }
    public void setLabelId(UUID labelId) { this.labelId = labelId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TicketLabelId that = (TicketLabelId) o;
        return Objects.equals(ticketId, that.ticketId) && Objects.equals(labelId, that.labelId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ticketId, labelId);
    }
}
