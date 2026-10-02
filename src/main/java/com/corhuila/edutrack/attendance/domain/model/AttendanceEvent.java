package com.corhuila.edutrack.attendance.domain.model;

import com.corhuila.edutrack.attendance.domain.exception.InvalidAttendanceDateException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain entity: a validated attendance record (non-future date, valid status).
 * Contains domain invariant enforcement and does not depend on frameworks.
 */
public class AttendanceEvent {
    private final UUID id;
    private final UUID studentId;
    private final UUID schoolId;
    private final LocalDate date;
    private final AttendanceStatus status; // PRESENT, ABSENT, LATE, JUSTIFIED (domain invariant)
    private final UUID teacherId;
    private final Long sequenceNum;
    private final LocalDateTime recordedAt;

    /**
     * Use the static factory method register() for creating new attendance events 
     * to ensure domain invariants are validated. This constructor is kept public 
     * mainly for reconstruction from the persistence layer.
     */
    public AttendanceEvent(UUID id, UUID studentId, UUID schoolId, LocalDate date, AttendanceStatus status, UUID teacherId, Long sequenceNum, LocalDateTime recordedAt) {
        this.id = id;
        this.studentId = studentId;
        this.schoolId = schoolId;
        this.date = date;
        this.status = status;
        this.teacherId = teacherId;
        this.sequenceNum = sequenceNum;
        this.recordedAt = recordedAt;
    }

    /**
     * Creates a new AttendanceEvent, enforcing domain invariants.
     */
    public static AttendanceEvent register(UUID id, UUID studentId, UUID schoolId, LocalDate date, AttendanceStatus status, UUID teacherId, Long sequenceNum) {
        if (date != null && date.isAfter(LocalDate.now())) {
            throw new InvalidAttendanceDateException("Attendance date cannot be in the future: " + date);
        }
        
        LocalDate finalDate = date != null ? date : LocalDate.now();
        Long finalSequence = sequenceNum != null ? sequenceNum : 1L;
        
        return new AttendanceEvent(
            id != null ? id : UUID.randomUUID(),
            studentId,
            schoolId,
            finalDate,
            status,
            teacherId,
            finalSequence,
            LocalDateTime.now()
        );
    }

    public UUID getId() { return id; }
    public UUID getStudentId() { return studentId; }
    public UUID getSchoolId() { return schoolId; }
    public LocalDate getDate() { return date; }
    public AttendanceStatus getStatus() { return status; }
    public UUID getTeacherId() { return teacherId; }
    public Long getSequenceNum() { return sequenceNum; }
    public LocalDateTime getRecordedAt() { return recordedAt; }
}
