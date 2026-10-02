package com.corhuila.edutrack.attendance.domain.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class StudentAbsentEvent {
    private String eventId;
    private String eventType;
    private LocalDateTime occurredAt;
    private String correlationId;
    private String aggregateId;
    private StudentAbsentPayload payload;
    private int version;

    public StudentAbsentEvent() {
        this.eventId = UUID.randomUUID().toString();
        this.eventType = "StudentAbsent";
        this.occurredAt = LocalDateTime.now();
        this.correlationId = UUID.randomUUID().toString();
        this.version = 1;
    }

    public StudentAbsentEvent(String aggregateId, StudentAbsentPayload payload) {
        this();
        this.aggregateId = aggregateId;
        this.payload = payload;
    }

    public String getEventId() { return eventId; }
    public String getEventType() { return eventType; }
    public LocalDateTime getOccurredAt() { return occurredAt; }
    public String getCorrelationId() { return correlationId; }
    public String getAggregateId() { return aggregateId; }
    public StudentAbsentPayload getPayload() { return payload; }
    public int getVersion() { return version; }

    public static class StudentAbsentPayload {
        private String attendanceId;
        private String studentId;
        private String studentName;
        private String schoolId;
        private String schoolName;
        private LocalDate date;
        private String status;
        private String teacherId;
        private String teacherName;
        private Long sequenceNum;
        // Lamport logical timestamp: lets consumers order events causally (ADR-007).
        private Long lamportTimestamp; 
        private LocalDateTime recordedAt;

        public StudentAbsentPayload(String attendanceId, String studentId, String schoolId, LocalDate date, String status, Long sequenceNum, Long lamportTimestamp) {
            this.attendanceId = attendanceId;
            this.studentId = studentId;
            this.schoolId = schoolId;
            this.date = date;
            this.status = status;
            this.sequenceNum = sequenceNum;
            this.lamportTimestamp = lamportTimestamp;
            this.recordedAt = LocalDateTime.now();
        }

        public String getAttendanceId() { return attendanceId; }
        public String getStudentId() { return studentId; }
        public String getStudentName() { return studentName; }
        public void setStudentName(String studentName) { this.studentName = studentName; }
        public String getSchoolId() { return schoolId; }
        public String getSchoolName() { return schoolName; }
        public void setSchoolName(String schoolName) { this.schoolName = schoolName; }
        public LocalDate getDate() { return date; }
        public String getStatus() { return status; }
        public String getTeacherId() { return teacherId; }
        public void setTeacherId(String teacherId) { this.teacherId = teacherId; }
        public String getTeacherName() { return teacherName; }
        public void setTeacherName(String teacherName) { this.teacherName = teacherName; }
        public Long getSequenceNum() { return sequenceNum; }
        public Long getLamportTimestamp() { return lamportTimestamp; }
        public LocalDateTime getRecordedAt() { return recordedAt; }
    }
}
