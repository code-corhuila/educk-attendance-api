package com.corhuila.edutrack.attendance.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

@ExtendWith(MockitoExtension.class)
class JpaLamportClockAdapterTest {

    @Mock
    private EntityManager entityManager;

    @Mock
    private Query query;

    @Test
    void tick_locksFirstThenReturnsMaxOfLocalAndReceivedPlusOne() {
        when(entityManager.createNativeQuery(anyString())).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.getSingleResult()).thenReturn("", 6L); // lock result, then next timestamp

        long result = new JpaLamportClockAdapter(entityManager).tick(5L);

        assertThat(result).isEqualTo(6L);
        ArgumentCaptor<String> sql = ArgumentCaptor.forClass(String.class);
        verify(entityManager, times(2)).createNativeQuery(sql.capture());
        assertThat(sql.getAllValues().get(0)).contains("pg_advisory_xact_lock");
        assertThat(sql.getAllValues().get(1)).contains("GREATEST(COALESCE(MAX(lamport_timestamp), 0), :received) + 1");
        verify(query).setParameter("received", 5L);
    }
}
