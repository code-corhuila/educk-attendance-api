package com.corhuila.edutrack.attendance.infrastructure.persistence;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpringDataAttendanceOutboxRepository extends JpaRepository<AttendanceOutboxEntity, UUID> {
}
