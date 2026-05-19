package com.agileforge.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "ticket_labels")
@IdClass(TicketLabelId.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TicketLabelEntity {

    @Id
    @Column(name = "ticket_id", nullable = false)
    private UUID ticketId;

    @Id
    @Column(name = "label_id", nullable = false)
    private UUID labelId;
}
