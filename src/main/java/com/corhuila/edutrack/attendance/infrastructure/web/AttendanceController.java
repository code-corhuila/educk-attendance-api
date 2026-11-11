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
    private final java.util.Map<String, AttendanceResponse> idempotencyCache = new java.util.concurrent.ConcurrentHashMap<>();

    public AttendanceController(RegisterAttendanceUseCase registerAttendanceUseCase, GetAttendanceUseCase getAttendanceUseCase) {
        this.registerAttendanceUseCase = registerAttendanceUseCase;
        this.getAttendanceUseCase = getAttendanceUseCase;
    }

    @PostMapping
    public ResponseEntity<AttendanceResponse> registerAttendance(
            @RequestBody RegisterAttendanceRequest request,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            AttendanceResponse cached = idempotencyCache.get(idempotencyKey.trim());
            if (cached != null) {
                return ResponseEntity.status(HttpStatus.CREATED).body(cached);
            }
        }
        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        UUID verifiedTeacherId = (auth != null && auth.getName() != null && !"anonymousUser".equals(auth.getName()))
                ? UUID.fromString(auth.getName())
                : request.getTeacherId();
        AttendanceEvent created = registerAttendanceUseCase.registerAttendance(
            request.getStudentId(),
            request.getSchoolId(),
            request.getDate(),
            request.getStatus(),
            verifiedTeacherId,
            request.getSequenceNum()
        );
        AttendanceResponse response = AttendanceResponse.fromDomain(created);
        if (idempotencyKey != null && !idempotencyKey.isBlank() && idempotencyCache.size() < 1024) {
            idempotencyCache.putIfAbsent(idempotencyKey.trim(), response);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
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
