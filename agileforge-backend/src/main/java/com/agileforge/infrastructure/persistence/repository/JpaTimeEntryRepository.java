package com.agileforge.infrastructure.persistence.repository;

import com.agileforge.infrastructure.persistence.entity.TimeEntryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface JpaTimeEntryRepository extends JpaRepository<TimeEntryEntity, UUID> {

    List<TimeEntryEntity> findByTicketIdOrderByWorkDateDesc(UUID ticketId);

    List<TimeEntryEntity> findByUserIdOrderByWorkDateDesc(UUID userId);

    List<TimeEntryEntity> findByUserIdAndWorkDateBetweenOrderByWorkDateDesc(UUID userId, LocalDate from, LocalDate to);

    @Query("SELECT COALESCE(SUM(e.hours), 0) FROM TimeEntryEntity e WHERE e.ticketId = :ticketId")
    double sumHoursByTicketId(@Param("ticketId") UUID ticketId);

    @Query("SELECT COALESCE(SUM(e.hours), 0) FROM TimeEntryEntity e WHERE e.userId = :userId AND e.workDate BETWEEN :from AND :to")
    double sumHoursByUserIdAndWorkDateBetween(@Param("userId") UUID userId, @Param("from") LocalDate from, @Param("to") LocalDate to);
}
