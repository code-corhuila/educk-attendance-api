package com.corhuila.edutrack.attendance.application.service;

import com.corhuila.edutrack.attendance.domain.model.AttendanceEvent;
import com.corhuila.edutrack.attendance.domain.model.AttendanceStatus;
import com.corhuila.edutrack.attendance.domain.model.StudentAbsentEvent;
import com.corhuila.edutrack.attendance.domain.port.in.GetAttendanceUseCase;
import com.corhuila.edutrack.attendance.domain.port.in.RegisterAttendanceUseCase;
import com.corhuila.edutrack.attendance.domain.port.out.AttendanceEventPublisherPort;
import com.corhuila.edutrack.attendance.domain.port.out.AttendanceRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

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
 * - DIP: Depends on abstractions (AttendanceRepositoryPort, AttendanceEventPublisherPort).
 * - Constructor Injection: Allows testing with mocks without Spring context.
 */
@Service
public class AttendanceService implements RegisterAttendanceUseCase, GetAttendanceUseCase {

    private static final Logger log = LoggerFactory.getLogger(AttendanceService.class);

    private final AttendanceRepositoryPort repositoryPort;
    private final AttendanceEventPublisherPort eventPublisherPort;

    public AttendanceService(AttendanceRepositoryPort repositoryPort, AttendanceEventPublisherPort eventPublisherPort) {
        this.repositoryPort = repositoryPort;
        this.eventPublisherPort = eventPublisherPort;
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

        // HU-005: If the registered status is ABSENT, publish the domain event
        // StudentAbsent to the output port.
        // NOTE: According to ADR-007, dual-writes should be prevented using Transactional Outbox.
        // If the publisher port throws an exception, the @Transactional boundary will rollback the DB save.
        if (saved.getStatus() == AttendanceStatus.ABSENT) {
            publishStudentAbsentEvent(saved);
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
     * Builds and publishes the StudentAbsent domain event through the output port.
     */
    private void publishStudentAbsentEvent(AttendanceEvent saved) {
        StudentAbsentEvent.StudentAbsentPayload payload = new StudentAbsentEvent.StudentAbsentPayload(
            saved.getId().toString(),
            saved.getStudentId().toString(),
            saved.getSchoolId() != null ? saved.getSchoolId().toString() : "school-default",
            saved.getDate(),
            saved.getStatus().name(),
            saved.getSequenceNum()
        );
        StudentAbsentEvent domainEvent = new StudentAbsentEvent(saved.getId().toString(), payload);
        
        try {
            eventPublisherPort.publishStudentAbsent(domainEvent);
        } catch (Exception e) {
            log.error("Failed to publish StudentAbsent event, transaction will be rolled back: {}", e.getMessage());
            throw new IllegalStateException("Failed to publish attendance event", e);
        }
    }
}
