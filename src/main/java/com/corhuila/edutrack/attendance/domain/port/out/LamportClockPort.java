package com.corhuila.edutrack.attendance.domain.port.out;

/**
 * Output port for the service Lamport logical clock (causal ordering, ADR-007).
 * Lamport rule: new = max(local, received) + 1.
 */
public interface LamportClockPort {

    /** Returns the next logical timestamp, merging the one received from the caller. */
    long tick(long receivedTimestamp);
}
