package com.corhuila.edutrack.attendance.domain.exception;

/**
 * Thrown when an attendance date is invalid (e.g., in the future).
 */
public class InvalidAttendanceDateException extends AttendanceDomainException {
    public InvalidAttendanceDateException(String message) {
        super(message);
    }
}
