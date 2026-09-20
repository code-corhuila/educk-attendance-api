package com.corhuila.edutrack.attendance.application.service;

import com.corhuila.edutrack.attendance.domain.exception.InvalidAttendanceException;
import com.corhuila.edutrack.attendance.domain.model.AttendanceEvent;
import com.corhuila.edutrack.attendance.domain.model.StudentAbsentEvent;
import com.corhuila.edutrack.attendance.domain.port.in.GetAttendanceUseCase;
import com.corhuila.edutrack.attendance.domain.port.in.RegisterAttendanceUseCase;
import com.corhuila.edutrack.attendance.domain.port.out.AttendanceEventPublisherPort;
import com.corhuila.edutrack.attendance.domain.port.out.AttendanceRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class AttendanceService implements RegisterAttendanceUseCase, GetAttendanceUseCase {

    private static final Set<String> VALID_STATUSES = Set.of("PRESENT", "ABSENT", "TARDY", "EXCUSED");

    private final AttendanceRepositoryPort repositoryPort;
    private final AttendanceEventPublisherPort eventPublisherPort;

    public AttendanceService(AttendanceRepositoryPort repositoryPort, AttendanceEventPublisherPort eventPublisherPort) {
        this.repositoryPort = repositoryPort;
        this.eventPublisherPort = eventPublisherPort;
    }

    @Override
    @Transactional
    public AttendanceEvent registerAttendance(UUID studentId, UUID schoolId, LocalDate date, String status, UUID teacherId, Long sequenceNum) {
        if (status == null || !VALID_STATUSES.contains(status.toUpperCase())) {
            throw new InvalidAttendanceException("Invalid status: " + status + ". Allowed: " + VALID_STATUSES);
        }

        AttendanceEvent event = new AttendanceEvent(
            UUID.randomUUID(),
            studentId,
            schoolId,
            date != null ? date : LocalDate.now(),
            status.toUpperCase(),
            teacherId,
            sequenceNum != null ? sequenceNum : 1L,
            LocalDateTime.now()
        );

        AttendanceEvent saved = repositoryPort.save(event);

        // HU-005: If student is marked ABSENT, publish StudentAbsent domain event
        if ("ABSENT".equalsIgnoreCase(saved.getStatus())) {
            StudentAbsentEvent.StudentAbsentPayload payload = new StudentAbsentEvent.StudentAbsentPayload(
                saved.getId().toString(),
                saved.getStudentId().toString(),
                saved.getSchoolId() != null ? saved.getSchoolId().toString() : "school-default",
                saved.getDate(),
                "ABSENT",
                saved.getSequenceNum()
            );
            StudentAbsentEvent domainEvent = new StudentAbsentEvent(saved.getId().toString(), payload);
            eventPublisherPort.publishStudentAbsent(domainEvent);
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
}
