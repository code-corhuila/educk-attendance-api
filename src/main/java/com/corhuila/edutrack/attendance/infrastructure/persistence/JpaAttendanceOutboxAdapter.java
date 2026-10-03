package com.corhuila.edutrack.attendance.infrastructure.persistence;

import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.corhuila.edutrack.attendance.domain.model.StudentAbsentEvent;
import com.corhuila.edutrack.attendance.domain.port.out.AttendanceOutboxPort;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Outbox adapter (ADR-007): writes domain events to the outbox_events table.
 * Propagation.MANDATORY forces the insert into the caller's transaction (same ACID
 * unit as the attendance record) and fails fast otherwise. Never touches RabbitMQ.
 */
@Component
public class JpaAttendanceOutboxAdapter implements AttendanceOutboxPort {

    static final String AGGREGATE_TYPE = "Attendance";

    private final SpringDataAttendanceOutboxRepository repository;
    private final ObjectMapper objectMapper;

    public JpaAttendanceOutboxAdapter(SpringDataAttendanceOutboxRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void append(StudentAbsentEvent event) {
        StudentAbsentEvent.StudentAbsentPayload payload = event.getPayload();
        repository.save(new AttendanceOutboxEntity(
            UUID.fromString(event.getEventId()),
            UUID.fromString(event.getAggregateId()),
            AGGREGATE_TYPE,
            event.getEventType(),
            toJson(payload),
            payload.getLamportTimestamp(),
            Instant.now()
        ));
    }

    private String toJson(StudentAbsentEvent.StudentAbsentPayload payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            // Unchecked: rolls back the surrounding attendance transaction.
            throw new IllegalStateException("Cannot serialize outbox payload", e);
        }
    }
}
