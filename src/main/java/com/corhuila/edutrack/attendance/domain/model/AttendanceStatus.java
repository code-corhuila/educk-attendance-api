package com.corhuila.edutrack.attendance.domain.model;

import com.corhuila.edutrack.attendance.domain.exception.InvalidAttendanceException;

import java.util.Locale;

/**
 * Domain enum that restricts the valid states of an attendance record.
 * It is the single source of truth for existing states (domain invariant),
 * preventing primitive obsession by avoiding the use of raw Strings.
 */
public enum AttendanceStatus {
    PRESENT,
    ABSENT,
    LATE,
    JUSTIFIED;

    /**
     * Converts a raw string value (from HTTP, DB, messaging, etc.) into a
     * valid domain AttendanceStatus.
     * Maps legacy values (TARDY -> LATE, EXCUSED -> JUSTIFIED) to preserve
     * read-path compatibility for existing records in the database.
     *
     * @param rawStatus value received from an input port/adapter
     * @return the corresponding AttendanceStatus
     * @throws InvalidAttendanceException if rawStatus is null, blank, or invalid
     */
    public static AttendanceStatus fromString(String rawStatus) {
        if (rawStatus == null || rawStatus.isBlank()) {
            throw new InvalidAttendanceException(
                "Attendance status cannot be null or empty. Allowed values: " + allowedValues());
        }
        
        String normalized = rawStatus.trim().toUpperCase(Locale.ROOT);
        
        // Handle legacy status values for backward compatibility
        if ("TARDY".equals(normalized)) {
            return LATE;
        }
        if ("EXCUSED".equals(normalized)) {
            return JUSTIFIED;
        }

        try {
            return AttendanceStatus.valueOf(normalized);
        } catch (IllegalArgumentException notAValidEnumConstant) {
            throw new InvalidAttendanceException(
                "Invalid status: " + rawStatus + ". Allowed values: " + allowedValues());
        }
    }

    private static String allowedValues() {
        StringBuilder sb = new StringBuilder();
        AttendanceStatus[] values = values();
        for (int i = 0; i < values.length; i++) {
            sb.append(values[i].name());
            if (i < values.length - 1) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }
}
