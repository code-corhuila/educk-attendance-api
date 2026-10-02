package com.corhuila.edutrack.attendance.infrastructure.persistence;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.corhuila.edutrack.attendance.domain.port.out.LamportClockPort;

import jakarta.persistence.EntityManager;

/**
 * Lamport clock derived from the outbox itself (no extra table, no DDL).
 * A transaction-scoped advisory lock serializes concurrent roll calls, so reading
 * MAX(lamport_timestamp) is race-free; the lock is released on commit.
 * Retention jobs must always keep the newest outbox row, or the clock would restart.
 */
@Component
public class JpaLamportClockAdapter implements LamportClockPort {

    private static final long CLOCK_LOCK_KEY = 8_301L;
    // CAST avoids Hibernate failing on the void result of pg_advisory_xact_lock.
    private static final String LOCK_SQL = "SELECT CAST(pg_advisory_xact_lock(:key) AS TEXT)";
    // Lamport rule: new = max(local, received) + 1.
    private static final String NEXT_SQL =
        "SELECT GREATEST(COALESCE(MAX(lamport_timestamp), 0), :received) + 1 FROM outbox_events";

    private final EntityManager entityManager;

    public JpaLamportClockAdapter(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public long tick(long receivedTimestamp) {
        entityManager.createNativeQuery(LOCK_SQL).setParameter("key", CLOCK_LOCK_KEY).getSingleResult();
        Object next = entityManager.createNativeQuery(NEXT_SQL)
            .setParameter("received", receivedTimestamp).getSingleResult();
        return ((Number) next).longValue();
    }
}
