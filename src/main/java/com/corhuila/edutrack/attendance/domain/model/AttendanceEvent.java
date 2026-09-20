package com.corhuila.edutrack.attendance.domain.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class AttendanceEvent {
    private UUID id;
    private UUID studentId;
    private UUID schoolId;
    private LocalDate date;
    private String status; // PRESENT, ABSENT, TARDY, EXCUSED
    private UUID teacherId;
    private Long sequenceNum;
    private LocalDateTime recordedAt;

    public AttendanceEvent(UUID id, UUID studentId, UUID schoolId, LocalDate date, String status, UUID teacherId, Long sequenceNum, LocalDateTime recordedAt) {
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
    public String getStatus() { return status; }
    public UUID getTeacherId() { return teacherId; }
    public Long getSequenceNum() { return sequenceNum; }
    public LocalDateTime getRecordedAt() { return recordedAt; }
}
