package com.corhuila.edutrack.attendance.infrastructure.persistence;

import com.corhuila.edutrack.attendance.domain.model.AttendanceEvent;
import com.corhuila.edutrack.attendance.domain.port.out.AttendanceRepositoryPort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class JpaAttendanceRepositoryAdapter implements AttendanceRepositoryPort {

    private final SpringDataAttendanceRepository repository;

    public JpaAttendanceRepositoryAdapter(SpringDataAttendanceRepository repository) {
        this.repository = repository;
    }

    @Override
    public AttendanceEvent save(AttendanceEvent event) {
        AttendanceEventJpaEntity entity = new AttendanceEventJpaEntity(
            event.getId(),
            event.getStudentId(),
            event.getSchoolId(),
            event.getDate(),
            event.getStatus(),
            event.getTeacherId(),
            event.getSequenceNum(),
            event.getRecordedAt()
        );
        AttendanceEventJpaEntity saved = repository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<AttendanceEvent> findById(UUID id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public List<AttendanceEvent> findByStudentId(UUID studentId) {
        return repository.findByStudentId(studentId).stream()
            .map(this::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public List<AttendanceEvent> findAll() {
        return repository.findAll().stream()
            .map(this::toDomain)
            .collect(Collectors.toList());
    }

    private AttendanceEvent toDomain(AttendanceEventJpaEntity entity) {
        return new AttendanceEvent(
            entity.getId(),
            entity.getStudentId(),
            entity.getSchoolId(),
            entity.getDate(),
            entity.getStatus(),
            entity.getTeacherId(),
            entity.getSequenceNum(),
            entity.getRecordedAt()
        );
    }
}
