package com.corhuila.edutrack.attendance.domain.exception;

/**
 * Se lanza cuando se intenta registrar una asistencia con una fecha de sesión
 * futura, violando el invariante de dominio "no se puede pasar asistencia de
 * un día que aún no ha ocurrido".
 */
public class InvalidAttendanceDateException extends RuntimeException {
    public InvalidAttendanceDateException(String message) {
        super(message);
    }
}
