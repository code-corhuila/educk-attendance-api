package com.corhuila.edutrack.attendance.domain.port.out;

import com.corhuila.edutrack.attendance.domain.model.StudentAbsentEvent;

public interface AttendanceEventPublisherPort {
    void publishStudentAbsent(StudentAbsentEvent event);
}
