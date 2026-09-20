package com.corhuila.edutrack.attendance.domain.port.in;

import com.corhuila.edutrack.attendance.domain.model.AttendanceEvent;
import java.time.LocalDate;
import java.util.UUID;

public interface RegisterAttendanceUseCase {
    AttendanceEvent registerAttendance(UUID studentId, UUID schoolId, LocalDate date, String status, UUID teacherId, Long sequenceNum);
}
