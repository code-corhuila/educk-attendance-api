package com.corhuila.edutrack.attendance.domain.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidad de dominio: un registro de asistencia ya validado (fecha no futura,
 * estado perteneciente a AttendanceStatus). No depende de frameworks.
 */
public class AttendanceEvent {
    private final UUID id;
    private final UUID studentId;
    private final UUID schoolId;
    private final LocalDate date;
    private final AttendanceStatus status; // PRESENT, ABSENT, LATE, JUSTIFIED (invariante de dominio)
    private final UUID teacherId;
    private final Long sequenceNum;
    private final LocalDateTime recordedAt;

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

    public UUID getId() { return id; }
    public UUID getStudentId() { return studentId; }
    public UUID getSchoolId() { return schoolId; }
    public LocalDate getDate() { return date; }
    public AttendanceStatus getStatus() { return status; }
    public UUID getTeacherId() { return teacherId; }
    public Long getSequenceNum() { return sequenceNum; }
    public LocalDateTime getRecordedAt() { return recordedAt; }
}
