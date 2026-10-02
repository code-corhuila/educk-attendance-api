package com.corhuila.edutrack.attendance.domain.exception;

/**
 * Base exception for all attendance domain invariant violations.
 */
public class AttendanceDomainException extends RuntimeException {
    public AttendanceDomainException(String message) {
        super(message);
    }
}
