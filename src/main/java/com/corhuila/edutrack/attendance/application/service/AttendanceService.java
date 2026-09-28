package com.corhuila.edutrack.attendance.application.service;

import com.corhuila.edutrack.attendance.domain.exception.InvalidAttendanceDateException;
import com.corhuila.edutrack.attendance.domain.model.AttendanceEvent;
import com.corhuila.edutrack.attendance.domain.model.AttendanceStatus;
import com.corhuila.edutrack.attendance.domain.model.StudentAbsentEvent;
import com.corhuila.edutrack.attendance.domain.port.in.GetAttendanceUseCase;
import com.corhuila.edutrack.attendance.domain.port.in.RegisterAttendanceUseCase;
import com.corhuila.edutrack.attendance.domain.port.out.AttendanceEventPublisherPort;
import com.corhuila.edutrack.attendance.domain.port.out.AttendanceRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Servicio de aplicación (capa application/service) que orquesta el registro
 * y la consulta de asistencia.
 *
 * Regla arquitectónica (Hexagonal / Ports & Adapters): esta clase SOLO depende
 * de tipos de dominio y de puertos de salida (interfaces). No conoce JPA,
 * RabbitMQ ni Spring MVC; las anotaciones @Service/@Transactional son el único
 * acoplamiento a Spring y sirven exclusivamente para el ensamblado del bean y
 * la demarcación transaccional, no para la lógica de negocio en sí.
 *
 * SOLID:
 * - SRP: únicamente orquesta el caso de uso de asistencia (valida invariantes,
 *   delega persistencia y publicación de eventos a sus puertos).
 * - DIP: depende de abstracciones (AttendanceRepositoryPort,
 *   AttendanceEventPublisherPort), no de implementaciones concretas.
 * - Inyección por constructor (sin @Autowired en campos) para poder
 *   instanciar la clase en pruebas unitarias con mocks, sin contexto Spring.
 */
@Service
public class AttendanceService implements RegisterAttendanceUseCase, GetAttendanceUseCase {

    private final AttendanceRepositoryPort repositoryPort;
    private final AttendanceEventPublisherPort eventPublisherPort;

    public AttendanceService(AttendanceRepositoryPort repositoryPort, AttendanceEventPublisherPort eventPublisherPort) {
        this.repositoryPort = repositoryPort;
        this.eventPublisherPort = eventPublisherPort;
    }

    @Override
    @Transactional
    public AttendanceEvent registerAttendance(UUID studentId, UUID schoolId, LocalDate date, String status, UUID teacherId, Long sequenceNum) {
        LocalDate sessionDate = resolveSessionDate(date);
        validateSessionDateIsNotFuture(sessionDate);

        // Invariante de dominio: el estado debe pertenecer a AttendanceStatus
        // (PRESENT, ABSENT, LATE, JUSTIFIED). Lanza InvalidAttendanceException si no.
        AttendanceStatus attendanceStatus = AttendanceStatus.fromString(status);

        AttendanceEvent event = new AttendanceEvent(
            UUID.randomUUID(),
            studentId,
            schoolId,
            sessionDate,
            attendanceStatus,
            teacherId,
            sequenceNum != null ? sequenceNum : 1L,
            LocalDateTime.now()
        );

        AttendanceEvent saved = repositoryPort.save(event);

        // HU-005: si el estado registrado es ABSENT, se publica el evento de
        // dominio StudentAbsent hacia RabbitMQ a través del puerto de salida.
        if (saved.getStatus() == AttendanceStatus.ABSENT) {
            publishStudentAbsentEvent(saved);
        }

        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttendanceEvent> getAttendanceByStudent(UUID studentId) {
        return repositoryPort.findByStudentId(studentId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttendanceEvent> getAllAttendance() {
        return repositoryPort.findAll();
    }

    // Si no se envía fecha, se asume el día de hoy (nunca puede resultar futura).
    private LocalDate resolveSessionDate(LocalDate date) {
        return date != null ? date : LocalDate.now();
    }

    // Invariante de dominio: no se puede registrar asistencia de una fecha futura.
    private void validateSessionDateIsNotFuture(LocalDate sessionDate) {
        if (sessionDate.isAfter(LocalDate.now())) {
            throw new InvalidAttendanceDateException(
                "La fecha de asistencia no puede ser futura: " + sessionDate);
        }
    }

    // Construye y publica el evento de dominio StudentAbsent a través del puerto de salida.
    private void publishStudentAbsentEvent(AttendanceEvent saved) {
        StudentAbsentEvent.StudentAbsentPayload payload = new StudentAbsentEvent.StudentAbsentPayload(
            saved.getId().toString(),
            saved.getStudentId().toString(),
            saved.getSchoolId() != null ? saved.getSchoolId().toString() : "school-default",
            saved.getDate(),
            saved.getStatus().name(),
            saved.getSequenceNum()
        );
        StudentAbsentEvent domainEvent = new StudentAbsentEvent(saved.getId().toString(), payload);
        eventPublisherPort.publishStudentAbsent(domainEvent);
    }
    // Added comment for PR validation flow
}
