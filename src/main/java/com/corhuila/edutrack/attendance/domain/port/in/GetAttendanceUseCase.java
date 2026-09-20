package com.corhuila.edutrack.attendance.domain.port.in;

import com.corhuila.edutrack.attendance.domain.model.AttendanceEvent;
import java.util.List;
import java.util.UUID;

public interface GetAttendanceUseCase {
    List<AttendanceEvent> getAttendanceByStudent(UUID studentId);
    List<AttendanceEvent> getAllAttendance();
}
