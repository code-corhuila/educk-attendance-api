package com.corhuila.edutrack.attendance.domain.port.out;

import com.corhuila.edutrack.attendance.domain.model.StudentAbsentEvent;

/**
 * Output port that records domain events in the transactional outbox (ADR-007).
 * Implementations MUST join the caller's transaction and MUST NOT talk to the broker.
 */
public interface AttendanceOutboxPort {

    /** Appends the event to the outbox as PENDING (payload already carries the Lamport timestamp). */
    void append(StudentAbsentEvent event);
}