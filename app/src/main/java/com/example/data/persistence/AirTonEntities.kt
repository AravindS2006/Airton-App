package com.example.data.persistence

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "clinics")
data class ClinicEntity(
    @PrimaryKey val clinicId: String,
    val name: String,
    val locationLabel: String,
    val demoPhoneLabel: String,
    val demoHoursLabel: String
)

@Entity(tableName = "clinicians")
data class ClinicianEntity(
    @PrimaryKey val clinicianId: String,
    val fullName: String,
    val roleTitle: String,
    val clinicId: String,
    val demoContactLabel: String
)

@Entity(tableName = "patients")
data class PatientEntity(
    @PrimaryKey val subjectId: String,
    val fullName: String,
    val caregiverName: String?,
    val assignedClinicianId: String,
    val clinicId: String,
    val assignedDeviceId: String?,
    val enrolmentStatus: String,
    val trainingStatus: String
)

@Entity(tableName = "care_plans")
data class CarePlanEntity(
    @PrimaryKey val planId: String,
    val subjectId: String,
    val authorClinicianId: String?,
    val authorName: String?,
    val updatedAt: String?,
    val eyesPrescribedCsv: String, // e.g. "OD,OS" or ""
    val scheduleSummary: String?,
    val repetitionsPerSession: Int?,
    val maxRetriesPerEye: Int?,
    val clinicalAlertThresholdNote: String?,
    val calibrationIntervalNote: String?,
    val escalationPolicyNote: String?,
    val clinicianInstructions: String?
)

/**
 * Immutable v1.0 synthetic measurement table. Once inserted, measurement fields are NEVER updated.
 */
@Entity(tableName = "measurements")
data class MeasurementEntity(
    @PrimaryKey val measurementId: String,
    val schemaVersion: String,
    val sessionId: String,
    val subjectId: String,
    val deviceId: String,
    val eyeCode: String,
    val capturedAt: String,
    val synthetic: Boolean,
    val qualityStatusWire: String,
    val qualityReasonsCsv: String,
    val iopEstimateMmhg: Double?,
    val calibrationId: String,
    val modelId: String,
    val firmwareVersion: String,
    val rawSha256: String,
    val clockStatusWire: String,
    val canonicalPayloadSha256: String
)

/**
 * Decoupled transport/sync state tracked separately from the immutable measurement row.
 */
@Entity(tableName = "transport_states")
data class TransportStateEntity(
    @PrimaryKey val measurementId: String,
    val transportStatus: String,
    val lastSyncAttemptAt: String?,
    val deliveredAt: String?,
    val syncFailureReason: String?
)

/**
 * Append-only clinician review and amendment events.
 */
@Entity(tableName = "review_events")
data class ReviewEventEntity(
    @PrimaryKey val eventId: String,
    val measurementId: String,
    val actionType: String,
    val authorClinicianId: String,
    val authorName: String,
    val note: String?,
    val createdAt: String
)

/**
 * Conflicting replay quarantine audit table. Preserves evidence without mutating original measurements.
 */
@Entity(tableName = "conflict_records")
data class ConflictRecordEntity(
    @PrimaryKey val conflictId: String,
    val measurementId: String,
    val incomingPayloadSha256: String,
    val existingPayloadSha256: String,
    val detectedAt: String,
    val summary: String
)

@Entity(tableName = "devices")
data class DeviceEntity(
    @PrimaryKey val deviceId: String,
    val serialNumber: String,
    val modelName: String,
    val firmwareVersion: String?,
    val calibrationId: String?,
    val calibrationStatus: String,
    val assignmentState: String,
    val assignedSubjectId: String?,
    val releaseStatus: String,
    val lastServiceAt: String?,
    val needsService: Boolean
)

/**
 * Append-only clinic device service history table.
 */
@Entity(tableName = "service_events")
data class ServiceEventEntity(
    @PrimaryKey val serviceEventId: String,
    val deviceId: String,
    val eventType: String,
    val actorName: String,
    val performedAt: String,
    val summary: String,
    val calibrationRef: String?
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey val notificationId: String,
    val category: String,
    val title: String,
    val body: String,
    val createdAt: String,
    val relatedMeasurementId: String?,
    val isRead: Boolean
)
