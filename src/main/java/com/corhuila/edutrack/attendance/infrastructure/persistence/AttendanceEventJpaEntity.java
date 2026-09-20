package com.corhuila.edutrack.attendance.infrastructure.persistence;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "attendance_events")
public class AttendanceEventJpaEntity {

    @Id
    private UUID id;

    @Column(name = "student_id", nullable = false)
    private UUID studentId;

    @Column(name = "school_id")
    private UUID schoolId;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "teacher_id")
    private UUID teacherId;

    @Column(name = "sequence_num", nullable = false)
    private Long sequenceNum;

    @Column(name = "recorded_at", nullable = false)
    private LocalDateTime recordedAt;

    public AttendanceEventJpaEntity() {}

    public AttendanceEventJpaEntity(UUID id, UUID studentId, UUID schoolId, LocalDate date, String status, UUID teacherId, Long sequenceNum, LocalDateTime recordedAt) {
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
    public void setId(UUID id) { this.id = id; }
    public UUID getStudentId() { return studentId; }
    public void setStudentId(UUID studentId) { this.studentId = studentId; }
    public UUID getSchoolId() { return schoolId; }
    public void setSchoolId(UUID schoolId) { this.schoolId = schoolId; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public UUID getTeacherId() { return teacherId; }
    public void setTeacherId(UUID teacherId) { this.teacherId = teacherId; }
    public Long getSequenceNum() { return sequenceNum; }
    public void setSequenceNum(Long sequenceNum) { this.sequenceNum = sequenceNum; }
    public LocalDateTime getRecordedAt() { return recordedAt; }
    public void setRecordedAt(LocalDateTime recordedAt) { this.recordedAt = recordedAt; }
}
