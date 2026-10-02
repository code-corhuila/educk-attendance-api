package com.corhuila.edutrack.attendance.infrastructure.persistence;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import org.mockito.junit.jupiter.MockitoExtension;

import com.corhuila.edutrack.attendance.domain.model.StudentAbsentEvent;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/** Unit tests for outbox insertion: repository mocked, real ObjectMapper, no Spring context. */
@ExtendWith(MockitoExtension.class)
class JpaAttendanceOutboxAdapterTest {

    @Mock
    private SpringDataAttendanceOutboxRepository repository;

    private final ObjectMapper objectMapper = new ObjectMapper()
        .registerModule(new JavaTimeModule())
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @Test
    void append_insertsPendingRowWithLamportTimestampInPayload() throws Exception {
        UUID attendanceId = UUID.randomUUID();
        UUID studentId = UUID.randomUUID();
        StudentAbsentEvent.StudentAbsentPayload payload = new StudentAbsentEvent.StudentAbsentPayload(
            attendanceId.toString(), studentId.toString(), UUID.randomUUID().toString(),
            LocalDate.of(2026, 9, 30), "ABSENT", 3L, 42L);
        StudentAbsentEvent event = new StudentAbsentEvent(attendanceId.toString(), payload);

        new JpaAttendanceOutboxAdapter(repository, objectMapper).append(event);

        ArgumentCaptor<AttendanceOutboxEntity> captor = ArgumentCaptor.forClass(AttendanceOutboxEntity.class);
        verify(repository).save(captor.capture());
        AttendanceOutboxEntity row = captor.getValue();
        assertThat(row.getId()).isEqualTo(UUID.fromString(event.getEventId()));
        assertThat(row.getAggregateId()).isEqualTo(attendanceId);
        assertThat(row.getAggregateType()).isEqualTo("Attendance");
        assertThat(row.getEventType()).isEqualTo("StudentAbsent");
        assertThat(row.getLamportTimestamp()).isEqualTo(42L);
        assertThat(row.getStatus()).isEqualTo("PENDING");
        assertThat(row.getProcessedAt()).isNull();

        JsonNode json = objectMapper.readTree(row.getPayload());
        assertThat(json.get("lamportTimestamp").asLong()).isEqualTo(42L);
        assertThat(json.get("studentId").asText()).isEqualTo(studentId.toString());
        assertThat(json.get("status").asText()).isEqualTo("ABSENT");
    }
}
