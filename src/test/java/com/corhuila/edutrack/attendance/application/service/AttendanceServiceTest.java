package com.corhuila.edutrack.attendance.application.service;

import com.corhuila.edutrack.attendance.domain.exception.InvalidAttendanceDateException;
import com.corhuila.edutrack.attendance.domain.exception.InvalidAttendanceException;
import com.corhuila.edutrack.attendance.domain.model.AttendanceEvent;
import com.corhuila.edutrack.attendance.domain.model.AttendanceStatus;
import com.corhuila.edutrack.attendance.domain.model.StudentAbsentEvent;
import com.corhuila.edutrack.attendance.domain.port.out.AttendanceEventPublisherPort;
import com.corhuila.edutrack.attendance.domain.port.out.AttendanceRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Unit tests for AttendanceService. No Spring context is loaded:
 * the class is instantiated directly with mocked dependencies, which
 * confirms the application layer is not coupled to frameworks.
 */
@ExtendWith(MockitoExtension.class)
class AttendanceServiceTest {

    @Mock
    private AttendanceRepositoryPort repositoryPort;

    @Mock
    private AttendanceEventPublisherPort eventPublisherPort;

    private AttendanceService attendanceService;

    private UUID studentId;
    private UUID schoolId;
    private UUID teacherId;

    @BeforeEach
    void setUp() {
        attendanceService = new AttendanceService(repositoryPort, eventPublisherPort);
        studentId = UUID.randomUUID();
        schoolId = UUID.randomUUID();
        teacherId = UUID.randomUUID();
    }

    @Test
    void registerAttendance_withPresentStatus_savesAndDoesNotPublishEvent() {
        when(repositoryPort.save(any(AttendanceEvent.class))).thenAnswer(inv -> inv.getArgument(0));

        AttendanceEvent result = attendanceService.registerAttendance(
            studentId, schoolId, LocalDate.now(), "PRESENT", teacherId, 1L);

        assertThat(result.getStatus()).isEqualTo(AttendanceStatus.PRESENT);
        verify(repositoryPort, times(1)).save(any(AttendanceEvent.class));
        verifyNoInteractions(eventPublisherPort);
    }

    @Test
    void registerAttendance_withAbsentStatus_publishesStudentAbsentEvent() {
        when(repositoryPort.save(any(AttendanceEvent.class))).thenAnswer(inv -> inv.getArgument(0));

        AttendanceEvent result = attendanceService.registerAttendance(
            studentId, schoolId, LocalDate.now(), "absent", teacherId, 2L);

        assertThat(result.getStatus()).isEqualTo(AttendanceStatus.ABSENT);

        ArgumentCaptor<StudentAbsentEvent> captor = ArgumentCaptor.forClass(StudentAbsentEvent.class);
        verify(eventPublisherPort, times(1)).publishStudentAbsent(captor.capture());
        assertThat(captor.getValue().getPayload().getStudentId()).isEqualTo(studentId.toString());
        assertThat(captor.getValue().getPayload().getStatus()).isEqualTo("ABSENT");
    }

    @Test
    void registerAttendance_withNonAbsentStatus_neverPublishesEvent() {
        when(repositoryPort.save(any(AttendanceEvent.class))).thenAnswer(inv -> inv.getArgument(0));

        attendanceService.registerAttendance(studentId, schoolId, LocalDate.now(), "LATE", teacherId, 1L);
        attendanceService.registerAttendance(studentId, schoolId, LocalDate.now(), "JUSTIFIED", teacherId, 1L);

        verifyNoInteractions(eventPublisherPort);
    }

    @Test
    void registerAttendance_withFutureDate_throwsInvalidAttendanceDateExceptionAndSkipsPersistence() {
        LocalDate futureDate = LocalDate.now().plusDays(1);

        assertThatThrownBy(() -> attendanceService.registerAttendance(
            studentId, schoolId, futureDate, "PRESENT", teacherId, 1L))
            .isInstanceOf(InvalidAttendanceDateException.class);

        verifyNoInteractions(repositoryPort, eventPublisherPort);
    }

    @Test
    void registerAttendance_withTodayAsDate_isAllowed() {
        when(repositoryPort.save(any(AttendanceEvent.class))).thenAnswer(inv -> inv.getArgument(0));

        AttendanceEvent result = attendanceService.registerAttendance(
            studentId, schoolId, LocalDate.now(), "PRESENT", teacherId, 1L);

        assertThat(result.getDate()).isEqualTo(LocalDate.now());
    }

    @Test
    void registerAttendance_withInvalidStatus_throwsInvalidAttendanceExceptionAndSkipsPersistence() {
        assertThatThrownBy(() -> attendanceService.registerAttendance(
            studentId, schoolId, LocalDate.now(), "ON_VACATION", teacherId, 1L))
            .isInstanceOf(InvalidAttendanceException.class);

        verifyNoInteractions(repositoryPort, eventPublisherPort);
    }

    @Test
    void registerAttendance_withNullStatus_throwsInvalidAttendanceException() {
        assertThatThrownBy(() -> attendanceService.registerAttendance(
            studentId, schoolId, LocalDate.now(), null, teacherId, 1L))
            .isInstanceOf(InvalidAttendanceException.class);
    }

    @Test
    void registerAttendance_withNullDate_defaultsToToday() {
        when(repositoryPort.save(any(AttendanceEvent.class))).thenAnswer(inv -> inv.getArgument(0));

        AttendanceEvent result = attendanceService.registerAttendance(
            studentId, schoolId, null, "LATE", teacherId, 1L);

        assertThat(result.getDate()).isEqualTo(LocalDate.now());
        assertThat(result.getStatus()).isEqualTo(AttendanceStatus.LATE);
    }

    @Test
    void registerAttendance_withNullSequenceNum_defaultsToOne() {
        when(repositoryPort.save(any(AttendanceEvent.class))).thenAnswer(inv -> inv.getArgument(0));

        AttendanceEvent result = attendanceService.registerAttendance(
            studentId, schoolId, LocalDate.now(), "JUSTIFIED", teacherId, null);

        assertThat(result.getSequenceNum()).isEqualTo(1L);
    }

    @Test
    void getAttendanceByStudent_delegatesToRepositoryPort() {
        List<AttendanceEvent> expected = List.of(
            new AttendanceEvent(UUID.randomUUID(), studentId, schoolId, LocalDate.now(),
                AttendanceStatus.PRESENT, teacherId, 1L, LocalDateTime.now()));
        when(repositoryPort.findByStudentId(studentId)).thenReturn(expected);

        List<AttendanceEvent> result = attendanceService.getAttendanceByStudent(studentId);

        assertThat(result).isEqualTo(expected);
        verify(repositoryPort).findByStudentId(studentId);
    }

    @Test
    void getAllAttendance_delegatesToRepositoryPort() {
        when(repositoryPort.findAll()).thenReturn(List.of());

        attendanceService.getAllAttendance();

        verify(repositoryPort).findAll();
    }
}
