package com.corhuila.edutrack.attendance.domain.exception;

/**
 * Thrown when an attendance status is invalid.
 */
public class InvalidAttendanceException extends AttendanceDomainException {
    public InvalidAttendanceException(String message) {
        super(message);
    }
}
