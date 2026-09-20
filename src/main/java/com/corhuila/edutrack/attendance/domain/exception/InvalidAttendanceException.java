package com.corhuila.edutrack.attendance.domain.exception;

public class InvalidAttendanceException extends RuntimeException {
    public InvalidAttendanceException(String message) {
        super(message);
    }
}
