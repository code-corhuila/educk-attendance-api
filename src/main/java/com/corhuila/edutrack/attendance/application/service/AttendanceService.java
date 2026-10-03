package com.corhuila.edutrack.attendance.application.service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.corhuila.edutrack.attendance.domain.model.AttendanceEvent;
import com.corhuila.edutrack.attendance.domain.model.AttendanceStatus;
import com.corhuila.edutrack.attendance.domain.model.StudentAbsentEvent;
import com.corhuila.edutrack.attendance.domain.port.in.GetAttendanceUseCase;
import com.corhuila.edutrack.attendance.domain.port.in.RegisterAttendanceUseCase;
import com.corhuila.edutrack.attendance.domain.port.out.AttendanceOutboxPort;
import com.corhuila.edutrack.attendance.domain.port.out.AttendanceRepositoryPort;
import com.corhuila.edutrack.attendance.domain.port.out.LamportClockPort;

/**
 * Application service that orchestrates attendance registration and retrieval.
 *
 * Architectural Rule (Hexagonal / Ports & Adapters): This class ONLY depends
 * on domain types and output ports (interfaces). It knows nothing about JPA,
 * RabbitMQ, or Spring MVC. The @Service/@Transactional annotations are the
 * only Spring coupling, strictly for bean assembly and transaction boundaries,
 * not for business logic.
 *
 * SOLID:
 * - SRP: Only orchestrates the attendance use case (delegates invariants to domain,
 *   persistence and event publishing to its ports).
 * - DIP: Depends on abstractions (repository, outbox and Lamport clock ports).
 * - Constructor Injection: Allows testing with mocks without Spring context.
 */
@Service
public class AttendanceService implements RegisterAttendanceUseCase, GetAttendanceUseCase {

    private final AttendanceRepositoryPort repositoryPort;
    private final AttendanceOutboxPort outboxPort;
    private final LamportClockPort lamportClockPort;

    public AttendanceService(AttendanceRepositoryPort repositoryPort, AttendanceOutboxPort outboxPort,
                             LamportClockPort lamportClockPort) {
        this.repositoryPort = repositoryPort;
        this.outboxPort = outboxPort;
        this.lamportClockPort = lamportClockPort;
    }

    @Override
    @Transactional
    public AttendanceEvent registerAttendance(UUID studentId, UUID schoolId, LocalDate date, String status, UUID teacherId, Long sequenceNum) {
        // Domain invariant: The status must belong to AttendanceStatus (PRESENT, ABSENT, LATE, JUSTIFIED).
        AttendanceStatus attendanceStatus = AttendanceStatus.fromString(status);

        // Domain invariant: Date cannot be future. Handled by the entity's static factory.
        AttendanceEvent event = AttendanceEvent.register(
            UUID.randomUUID(),
            studentId,
            schoolId,
            date,
            attendanceStatus,
            teacherId,
            sequenceNum
        );

        AttendanceEvent saved = repositoryPort.save(event);

        // HU-005 + ADR-007: ABSENT records a StudentAbsent event in the outbox.
        // If the outbox write fails, the @Transactional boundary also rolls back the attendance save.
        if (saved.getStatus() == AttendanceStatus.ABSENT) {
            recordStudentAbsentInOutbox(saved);
        }

        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttendanceEvent> getAttendanceByStudent(UUID studentId) {
        return repositoryPort.findByStudentId(studentId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttendanceEvent> getAllAttendance() {
        return repositoryPort.findAll();
    }

    /**
     * Stamps the StudentAbsent event with a Lamport timestamp and appends it to the outbox.
     */
    private void recordStudentAbsentInOutbox(AttendanceEvent saved) {
        long lamportTimestamp = lamportClockPort.tick(saved.getSequenceNum());
        StudentAbsentEvent.StudentAbsentPayload payload = new StudentAbsentEvent.StudentAbsentPayload(
            saved.getId().toString(),
            saved.getStudentId().toString(),
            saved.getSchoolId() != null ? saved.getSchoolId().toString() : "school-default",
            saved.getDate(),
            saved.getStatus().name(),
            saved.getSequenceNum(),
            lamportTimestamp
        );
        StudentAbsentEvent domainEvent = new StudentAbsentEvent(saved.getId().toString(), payload);

        outboxPort.append(domainEvent); // same ACID transaction, no broker call (ADR-007)
    }
}