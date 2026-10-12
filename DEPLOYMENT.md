# Attendance runtime verification

Use the paired HU-005 database correction: V1 and V3 are under 01_ddl,
and V4 adds school_id and the persisted Lamport clock and allows JUSTIFIED.
All SQL remains in educk-attendance-db. Set DB_USER, DB_PASSWORD and JWT_SECRET;
use the same local HMAC signing key as Identity within the selected environment.
Each environment needs its own database instance and persistent volume.

Run Java 21: mvn verify -Dapi.version=1.44 -Dattendance.db.path=/path/to/educk-attendance-db

The PostgreSQL test verifies startup with schema validation, ABSENT persistence
and a PENDING outbox event while the broker is unreachable, Lamport timestamp,
JUSTIFIED persistence, and rollback of business data when outbox append fails.
This does not prove relay publication, notification delivery or full HU-005 acceptance.
The persistence key is the event UUID; it does not provide HTTP request deduplication.
