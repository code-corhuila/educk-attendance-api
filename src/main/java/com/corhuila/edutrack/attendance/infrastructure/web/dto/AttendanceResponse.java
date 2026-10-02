package com.corhuila.edutrack.attendance.infrastructure.web.dto;

import com.corhuila.edutrack.attendance.domain.model.AttendanceEvent;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class AttendanceResponse {
    private UUID id;
    private UUID studentId;
    private UUID schoolId;
    private LocalDate date;
    private String status;
    private UUID teacherId;
    private Long sequenceNum;
    private LocalDateTime recordedAt;

    public AttendanceResponse(UUID id, UUID studentId, UUID schoolId, LocalDate date, String status, UUID teacherId, Long sequenceNum, LocalDateTime recordedAt) {
        this.id = id;
        this.studentId = studentId;
        this.schoolId = schoolId;
        this.date = date;
        this.status = status;
        this.teacherId = teacherId;
        this.sequenceNum = sequenceNum;
        this.recordedAt = recordedAt;
    }

    public static AttendanceResponse fromDomain(AttendanceEvent event) {
        return new AttendanceResponse(
            event.getId(),
            event.getStudentId(),
            event.getSchoolId(),
            event.getDate(),
            event.getStatus().name(), // output DTO exposes status as String
            event.getTeacherId(),
            event.getSequenceNum(),
            event.getRecordedAt()
        );
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
