package com.corhuila.edutrack.attendance.domain.model;

import com.corhuila.edutrack.attendance.domain.exception.InvalidAttendanceException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AttendanceStatusTest {

    @Test
    void fromString_acceptsExactEnumNames() {
        assertThat(AttendanceStatus.fromString("PRESENT")).isEqualTo(AttendanceStatus.PRESENT);
        assertThat(AttendanceStatus.fromString("ABSENT")).isEqualTo(AttendanceStatus.ABSENT);
        assertThat(AttendanceStatus.fromString("LATE")).isEqualTo(AttendanceStatus.LATE);
        assertThat(AttendanceStatus.fromString("JUSTIFIED")).isEqualTo(AttendanceStatus.JUSTIFIED);
    }

    @Test
    void fromString_isCaseInsensitiveAndTrimsWhitespace() {
        assertThat(AttendanceStatus.fromString(" present ")).isEqualTo(AttendanceStatus.PRESENT);
        assertThat(AttendanceStatus.fromString("late")).isEqualTo(AttendanceStatus.LATE);
    }

    @Test
    void fromString_acceptsLegacyValues() {
        // TARDY and EXCUSED are legacy values mapped to LATE and JUSTIFIED for backward compatibility.
        assertThat(AttendanceStatus.fromString("TARDY")).isEqualTo(AttendanceStatus.LATE);
        assertThat(AttendanceStatus.fromString("EXCUSED")).isEqualTo(AttendanceStatus.JUSTIFIED);
    }

    @Test
    void fromString_rejectsNullOrBlank() {
        assertThatThrownBy(() -> AttendanceStatus.fromString(null))
            .isInstanceOf(InvalidAttendanceException.class);
        assertThatThrownBy(() -> AttendanceStatus.fromString("  "))
            .isInstanceOf(InvalidAttendanceException.class);
    }
}
