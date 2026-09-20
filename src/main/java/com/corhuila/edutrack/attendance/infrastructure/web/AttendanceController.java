package com.corhuila.edutrack.attendance.infrastructure.web;

import com.corhuila.edutrack.attendance.domain.model.AttendanceEvent;
import com.corhuila.edutrack.attendance.domain.port.in.GetAttendanceUseCase;
import com.corhuila.edutrack.attendance.domain.port.in.RegisterAttendanceUseCase;
import com.corhuila.edutrack.attendance.infrastructure.web.dto.AttendanceResponse;
import com.corhuila.edutrack.attendance.infrastructure.web.dto.RegisterAttendanceRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/attendance")
@CrossOrigin(origins = "*")
public class AttendanceController {

    private final RegisterAttendanceUseCase registerAttendanceUseCase;
    private final GetAttendanceUseCase getAttendanceUseCase;

    public AttendanceController(RegisterAttendanceUseCase registerAttendanceUseCase, GetAttendanceUseCase getAttendanceUseCase) {
        this.registerAttendanceUseCase = registerAttendanceUseCase;
        this.getAttendanceUseCase = getAttendanceUseCase;
    }

    @PostMapping
    public ResponseEntity<AttendanceResponse> registerAttendance(@RequestBody RegisterAttendanceRequest request) {
        AttendanceEvent created = registerAttendanceUseCase.registerAttendance(
            request.getStudentId(),
            request.getSchoolId(),
            request.getDate(),
            request.getStatus(),
            request.getTeacherId(),
            request.getSequenceNum()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(AttendanceResponse.fromDomain(created));
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<AttendanceResponse>> getAttendanceByStudent(@PathVariable UUID studentId) {
        List<AttendanceEvent> events = getAttendanceUseCase.getAttendanceByStudent(studentId);
        return ResponseEntity.ok(events.stream().map(AttendanceResponse::fromDomain).collect(Collectors.toList()));
    }

    @GetMapping
    public ResponseEntity<List<AttendanceResponse>> getAllAttendance() {
        List<AttendanceEvent> events = getAttendanceUseCase.getAllAttendance();
        return ResponseEntity.ok(events.stream().map(AttendanceResponse::fromDomain).collect(Collectors.toList()));
    }

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("OK - Attendance Service (HU-005)");
    }
}
