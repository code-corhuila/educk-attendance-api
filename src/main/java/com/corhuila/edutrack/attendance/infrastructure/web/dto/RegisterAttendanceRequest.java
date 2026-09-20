package com.corhuila.edutrack.attendance.infrastructure.web.dto;

import java.time.LocalDate;
import java.util.UUID;

public class RegisterAttendanceRequest {
    private UUID studentId;
    private UUID schoolId;
    private LocalDate date;
    private String status;
    private UUID teacherId;
    private Long sequenceNum;

    public RegisterAttendanceRequest() {}

    public RegisterAttendanceRequest(UUID studentId, UUID schoolId, LocalDate date, String status, UUID teacherId, Long sequenceNum) {
        this.studentId = studentId;
        this.schoolId = schoolId;
        this.date = date;
        this.status = status;
        this.teacherId = teacherId;
        this.sequenceNum = sequenceNum;
    }

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
}
