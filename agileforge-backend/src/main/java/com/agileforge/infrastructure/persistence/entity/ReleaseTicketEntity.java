package com.agileforge.infrastructure.persistence.entity;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "release_tickets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReleaseTicketEntity {

    @EmbeddedId
    private ReleaseTicketId id;
}
