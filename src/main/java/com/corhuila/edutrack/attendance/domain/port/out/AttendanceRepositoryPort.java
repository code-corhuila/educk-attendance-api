package com.corhuila.edutrack.attendance.domain.port.out;

import com.corhuila.edutrack.attendance.domain.model.AttendanceEvent;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AttendanceRepositoryPort {
    AttendanceEvent save(AttendanceEvent event);
    Optional<AttendanceEvent> findById(UUID id);
    List<AttendanceEvent> findByStudentId(UUID studentId);
    List<AttendanceEvent> findAll();
}
