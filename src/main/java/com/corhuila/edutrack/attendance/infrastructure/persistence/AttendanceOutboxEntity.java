package com.corhuila.edutrack.attendance.infrastructure.persistence;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * JPA mapping of the transactional outbox table (ADR-007).
 * Rows are created as PENDING in the same transaction as the business data.
 * The table DDL is owned by the database repository (ADR-003).
 */
@Entity
@Table(name = "outbox_events")
public class AttendanceOutboxEntity {

    private static final String STATUS_PENDING = "PENDING";

    @Id
    private UUID id;

    @Column(name = "aggregate_type", nullable = false, length = 100)
    private String aggregateType;

    @Column(name = "aggregate_id", nullable = false)
    private UUID aggregateId;

    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;

    // JSON document stored in a JSONB column; includes the Lamport timestamp.
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload", nullable = false, columnDefinition = "jsonb")
    private String payload;

    @Column(name = "lamport_timestamp", nullable = false)
    private Long lamportTimestamp;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "processed_at")
    private Instant processedAt;

    /** Required by JPA. */
    protected AttendanceOutboxEntity() {
    }

    /** Creates a PENDING row; every mandatory value is validated. */
    public AttendanceOutboxEntity(UUID id, UUID aggregateId, String aggregateType, String eventType,
                                  String payload, Long lamportTimestamp, Instant createdAt) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.aggregateId = Objects.requireNonNull(aggregateId, "aggregateId must not be null");
        this.aggregateType = Objects.requireNonNull(aggregateType, "aggregateType must not be null");
        this.eventType = Objects.requireNonNull(eventType, "eventType must not be null");
        this.payload = Objects.requireNonNull(payload, "payload must not be null");
        this.lamportTimestamp = Objects.requireNonNull(lamportTimestamp, "lamportTimestamp must not be null");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt must not be null");
        this.status = STATUS_PENDING;
    }

    public UUID getId() { return id; }
    public String getAggregateType() { return aggregateType; }
    public UUID getAggregateId() { return aggregateId; }
    public String getEventType() { return eventType; }
    public String getPayload() { return payload; }
    public Long getLamportTimestamp() { return lamportTimestamp; }
    public String getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getProcessedAt() { return processedAt; }
}
