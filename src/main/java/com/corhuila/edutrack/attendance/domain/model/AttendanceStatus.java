package com.corhuila.edutrack.attendance.domain.model;

import com.corhuila.edutrack.attendance.domain.exception.InvalidAttendanceException;

import java.util.Locale;

/**
 * Enum de dominio que restringe los estados válidos de un registro de asistencia.
 * Es el único punto de verdad sobre qué estados existen (invariante de dominio),
 * evitando el "primitive obsession" de manejar el estado como String suelto.
 */
public enum AttendanceStatus {
    PRESENT,
    ABSENT,
    LATE,
    JUSTIFIED;

    /**
     * Convierte un valor crudo (proveniente de HTTP, BD, mensajería, etc.) en un
     * AttendanceStatus válido del dominio.
     *
     * @param rawStatus valor recibido desde un puerto de entrada/adaptador
     * @return el AttendanceStatus correspondiente
     * @throws InvalidAttendanceException si rawStatus es nulo, vacío o no coincide
     *                                     con PRESENT, ABSENT, LATE o JUSTIFIED
     */
    public static AttendanceStatus fromString(String rawStatus) {
        if (rawStatus == null || rawStatus.isBlank()) {
            throw new InvalidAttendanceException(
                "El estado de asistencia no puede ser nulo ni vacío. Valores permitidos: "
                    + allowedValues());
        }
        try {
            return AttendanceStatus.valueOf(rawStatus.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException notAValidEnumConstant) {
            throw new InvalidAttendanceException(
                "Estado inválido: " + rawStatus + ". Valores permitidos: " + allowedValues());
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
