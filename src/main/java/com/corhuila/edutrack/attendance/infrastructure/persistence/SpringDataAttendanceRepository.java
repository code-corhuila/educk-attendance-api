package com.corhuila.edutrack.attendance.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SpringDataAttendanceRepository extends JpaRepository<AttendanceEventJpaEntity, UUID> {
    List<AttendanceEventJpaEntity> findByStudentId(UUID studentId);
}
