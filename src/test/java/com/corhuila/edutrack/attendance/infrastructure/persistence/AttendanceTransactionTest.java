package com.corhuila.edutrack.attendance.infrastructure.persistence;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.MountableFile;
import com.corhuila.edutrack.attendance.application.service.AttendanceService;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

@SpringBootTest
@Testcontainers
class AttendanceTransactionTest {
    @Container static PostgreSQLContainer<?> database = new PostgreSQLContainer<>("postgres:16-alpine")
            .withPassword(UUID.randomUUID().toString())
            .withCopyFileToContainer(script("01_ddl/V1__init_attendance.sql"), "/docker-entrypoint-initdb.d/01.sql")
            .withCopyFileToContainer(script("01_ddl/V3__outbox_events_schema.sql"), "/docker-entrypoint-initdb.d/03.sql")
            .withCopyFileToContainer(script("04_alter/V4__align_attendance_runtime.sql"), "/docker-entrypoint-initdb.d/04.sql");

    static MountableFile script(String file) {
        String root = System.getProperty("attendance.db.path");
        if (root == null) throw new IllegalStateException("Set -Dattendance.db.path to educk-attendance-db");
        return MountableFile.forHostPath(Path.of(root, file));
    }

    @DynamicPropertySource static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", database::getJdbcUrl);
        registry.add("spring.datasource.username", database::getUsername);
        registry.add("spring.datasource.password", database::getPassword);
        registry.add("jwt.secret", () -> UUID.randomUUID().toString() + UUID.randomUUID());
        registry.add("spring.rabbitmq.port", () -> 1);
    }

    @Autowired AttendanceService service;
    @Autowired JdbcTemplate jdbc;
    @SpyBean JpaAttendanceOutboxAdapter outbox;

    @Test void persistsWithoutBrokerAndRollsBackWhenOutboxFails() {
        UUID student = UUID.randomUUID(), school = UUID.randomUUID(), teacher = UUID.randomUUID();
        service.registerAttendance(student, school, LocalDate.now(), "ABSENT", teacher, 3L);
        assertEquals(1, service.getAttendanceByStudent(student).size());
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM outbox_events WHERE status = 'PENDING'", Integer.class));
        assertEquals(4L, jdbc.queryForObject("SELECT MAX(lamport_timestamp) FROM outbox_events", Long.class));

        service.registerAttendance(student, school, LocalDate.now(), "JUSTIFIED", teacher, 4L);
        assertEquals(2, service.getAttendanceByStudent(student).size());
        JpaAttendanceOutboxAdapter target = org.springframework.test.util.AopTestUtils.getUltimateTargetObject(outbox);
        doThrow(new IllegalStateException("Injected outbox failure")).when(target).append(any());
        assertThrows(IllegalStateException.class, () -> service.registerAttendance(
                student, school, LocalDate.now(), "ABSENT", teacher, 5L));
        assertEquals(2, service.getAttendanceByStudent(student).size());
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM outbox_events", Integer.class));
    }
}
