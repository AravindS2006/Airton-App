package com.example.domain.model

/**
 * Demo-only persona switch. Never represents real authentication or authorization.
 */
enum class DemoRole {
    PATIENT,
    CLINICIAN,
    CLINIC_SERVICE
}

enum class EyeSide(val code: String, val labelEn: String) {
    OD("OD", "Right Eye (OD)"),
    OS("OS", "Left Eye (OS)")
}

enum class QualityStatus(val wireValue: String) {
    PASS("pass"),
    REJECT("reject")
}

/**
 * Supported v1.0 synthetic rejection reasons matching schemas/measurement.schema.json.
 */
enum class QualityReason(val wireValue: String, val labelEn: String, val guidanceEn: String) {
    ALIGNMENT(
        "alignment",
        "Alignment out of target",
        "Center the guide ring smoothly before simulated acquisition."
    ),
    BLINK(
        "blink",
        "Blink detected during capture",
        "Pause comfortably and try another simulated capture when ready."
    ),
    SIGNAL_MISSING(
        "signal_missing",
        "Optical signal missing",
        "No usable simulated reflection was captured during the demo window."
    ),
    CALIBRATION_EXPIRED(
        "calibration_expired",
        "Calibration expired",
        "Device calibration status requires clinic service verification before use."
    ),
    CLOCK_UNTRUSTED(
        "clock_untrusted",
        "Device clock untrusted",
        "Record rejected because the simulated device timestamp could not be verified."
    );

    companion object {
        fun fromWire(value: String): QualityReason? = entries.firstOrNull { it.wireValue == value }
    }
}

enum class ClockStatus(val wireValue: String) {
    TRUSTED("trusted"),
    UNTRUSTED("untrusted");

    companion object {
        fun fromWire(value: String): ClockStatus? = entries.firstOrNull { it.wireValue == value }
    }
}

/**
 * Transport status is strictly independent from Acquisition Quality and Clinical Review Status.
 */
enum class TransportStatus(val labelEn: String) {
    LOCAL_ONLY("Local only"),
    PENDING("Pending sync"),
    DELIVERED("Delivered"),
    FAILED("Sync failed"),
    QUARANTINED("Conflict quarantined")
}

/**
 * Clinical review status is strictly independent from Transport Status and Acquisition Quality.
 */
enum class ClinicalReviewStatus(val labelEn: String) {
    NOT_APPLICABLE("Not applicable in demo"),
    AWAITING_REVIEW("Awaiting clinician review"),
    REVIEWED("Reviewed by clinician"),
    FOLLOW_UP_REQUESTED("Follow-up requested")
}

enum class ReviewActionType(val labelEn: String) {
    ACKNOWLEDGED_REVIEW("Review acknowledged"),
    FOLLOW_UP_REQUESTED("Follow-up / reacquisition requested"),
    NOTE_APPENDED("Clinician note appended")
}

enum class AppLanguage(val code: String, val nativeLabel: String) {
    ENGLISH("en", "English"),
    TAMIL("ta", "தமிழ் (UI Labels + Review Tags)")
}

enum class ThemePreference(val labelEn: String) {
    LIGHT("Light (Recommended)"),
    DARK("Dark"),
    SYSTEM("Follow system")
}

enum class TextScalePreset(val labelEn: String, val multiplier: Float) {
    STANDARD("Standard (100%)", 1.0f),
    LARGE("Large (115%)", 1.15f),
    EXTRA_LARGE("Extra Large (130%)", 1.30f)
}

enum class SimulationScenario(
    val id: String,
    val labelEn: String,
    val shortTag: String,
    val producesRecord: Boolean,
    val expectedQuality: QualityStatus?,
    val rejectionReason: QualityReason?,
    val simulatedIopMmhg: Double?,
    val clockStatus: ClockStatus = ClockStatus.TRUSTED
) {
    READY_ACCEPTED(
        id = "ready_accepted",
        labelEn = "Accepted simulated capture",
        shortTag = "Pass (Synthetic)",
        producesRecord = true,
        expectedQuality = QualityStatus.PASS,
        rejectionReason = null,
        simulatedIopMmhg = 17.4,
        clockStatus = ClockStatus.TRUSTED
    ),
    DISCONNECTED(
        id = "disconnected",
        labelEn = "Simulated device disconnected",
        shortTag = "Disconnected",
        producesRecord = false,
        expectedQuality = null,
        rejectionReason = null,
        simulatedIopMmhg = null
    ),
    CALIBRATION_UNAVAILABLE(
        id = "calibration_unavailable",
        labelEn = "Calibration expired / unavailable",
        shortTag = "Cal. Expired",
        producesRecord = true,
        expectedQuality = QualityStatus.REJECT,
        rejectionReason = QualityReason.CALIBRATION_EXPIRED,
        simulatedIopMmhg = null
    ),
    ALIGNMENT_REJECTED(
        id = "alignment_rejected",
        labelEn = "Alignment rejected (no IOP value)",
        shortTag = "Alignment Reject",
        producesRecord = true,
        expectedQuality = QualityStatus.REJECT,
        rejectionReason = QualityReason.ALIGNMENT,
        simulatedIopMmhg = null
    ),
    BLINK_REJECTED(
        id = "blink_rejected",
        labelEn = "Blink detected (no IOP value)",
        shortTag = "Blink Reject",
        producesRecord = true,
        expectedQuality = QualityStatus.REJECT,
        rejectionReason = QualityReason.BLINK,
        simulatedIopMmhg = null
    ),
    SIGNAL_MISSING(
        id = "signal_missing",
        labelEn = "Optical signal missing (no IOP value)",
        shortTag = "No Signal",
        producesRecord = true,
        expectedQuality = QualityStatus.REJECT,
        rejectionReason = QualityReason.SIGNAL_MISSING,
        simulatedIopMmhg = null
    ),
    CLOCK_UNTRUSTED(
        id = "clock_untrusted",
        labelEn = "Clock untrusted (no IOP value)",
        shortTag = "Untrusted Clock",
        producesRecord = true,
        expectedQuality = QualityStatus.REJECT,
        rejectionReason = QualityReason.CLOCK_UNTRUSTED,
        simulatedIopMmhg = null,
        clockStatus = ClockStatus.UNTRUSTED
    )
}

/**
 * Strictly typed v1.0 synthetic measurement contract matching schemas/measurement.schema.json.
 * Immutable once recorded.
 */
data class SyntheticMeasurementV1(
    val schemaVersion: String = SCHEMA_VERSION_V1,
    val measurementId: String,
    val sessionId: String,
    val subjectId: String,
    val deviceId: String,
    val eye: EyeSide,
    val capturedAt: String, // ISO-8601 with explicit timezone offset
    val synthetic: Boolean = true,
    val qualityStatus: QualityStatus,
    val qualityReasons: List<QualityReason>,
    val iopEstimateMmhg: Double?, // Strictly null when qualityStatus == REJECT
    val calibrationId: String,
    val modelId: String,
    val firmwareVersion: String,
    val rawSha256: String,
    val clockStatus: ClockStatus
) {
    companion object {
        const val SCHEMA_VERSION_V1 = "1.0"
    }
}

/**
 * Combined UI read-model keeping the immutable measurement strictly separated from
 * transport state, conflict quarantine state, and append-only clinical review events.
 */
data class MeasurementEnvelope(
    val measurement: SyntheticMeasurementV1,
    val transportStatus: TransportStatus,
    val lastSyncAttemptAt: String?,
    val deliveredAt: String?,
    val syncFailureReason: String?,
    val reviewStatus: ClinicalReviewStatus,
    val reviewEvents: List<ReviewEvent>,
    val conflicts: List<ConflictRecord>
)

data class ReviewEvent(
    val eventId: String,
    val measurementId: String,
    val actionType: ReviewActionType,
    val authorClinicianId: String,
    val authorName: String,
    val note: String?,
    val createdAt: String
)

data class ConflictRecord(
    val conflictId: String,
    val measurementId: String,
    val incomingPayloadSha256: String,
    val existingPayloadSha256: String,
    val detectedAt: String,
    val summary: String
)

data class ClinicProfile(
    val clinicId: String,
    val name: String,
    val locationLabel: String,
    val demoPhoneLabel: String,
    val demoHoursLabel: String
)

data class ClinicianProfile(
    val clinicianId: String,
    val fullName: String,
    val roleTitle: String,
    val clinicId: String,
    val demoContactLabel: String
)

data class PatientProfile(
    val subjectId: String,
    val fullName: String,
    val caregiverName: String?,
    val assignedClinicianId: String,
    val clinicId: String,
    val assignedDeviceId: String?,
    val enrolmentStatus: String,
    val trainingStatus: String
)

/**
 * Clinician-authored illustrative care plan.
 * Unresolved clinical parameters MUST remain null and render as "Not configured".
 */
data class CarePlan(
    val planId: String,
    val subjectId: String,
    val authorClinicianId: String?,
    val authorName: String?,
    val updatedAt: String?,
    val eyesPrescribed: List<EyeSide>,
    val scheduleSummary: String?, // null -> "Not configured"
    val repetitionsPerSession: Int?, // null -> "Not configured"
    val maxRetriesPerEye: Int?, // null -> "Not configured"
    val clinicalAlertThresholdNote: String?, // Always null by default ("Not configured")
    val calibrationIntervalNote: String?, // Always null by default ("Not configured")
    val escalationPolicyNote: String?, // Always null by default ("Not configured")
    val clinicianInstructions: String? // Read-only for patients
) {
    val isScheduleConfigured: Boolean
        get() = !scheduleSummary.isNullOrBlank()
}

data class DeviceRecord(
    val deviceId: String,
    val serialNumber: String,
    val modelName: String,
    val firmwareVersion: String?, // null -> "Not available"
    val calibrationId: String?, // null -> "Not available"
    val calibrationStatus: String,
    val assignmentState: String,
    val assignedSubjectId: String?,
    val releaseStatus: String,
    val lastServiceAt: String?, // null -> "Not available"
    val needsService: Boolean
)

data class ServiceEvent(
    val serviceEventId: String,
    val deviceId: String,
    val eventType: String, // Inspection, Calibration check, Repair, Reassignment
    val actorName: String,
    val performedAt: String,
    val summary: String,
    val calibrationRef: String? // null -> "Not available"
)

enum class NotificationCategory(val labelEn: String) {
    DEMO_REMINDER("Demo Reminder"),
    SYNC_EVENT("Sync Update"),
    CLINICIAN_REVIEW("Clinician Review")
}

data class AppNotification(
    val notificationId: String,
    val category: NotificationCategory,
    val title: String,
    val body: String,
    val createdAt: String,
    val relatedMeasurementId: String?,
    val isRead: Boolean
)

sealed interface IngestOutcome {
    data class Inserted(val measurementId: String) : IngestOutcome
    data class DuplicateIdempotent(val measurementId: String) : IngestOutcome
    data class ConflictQuarantined(val conflict: ConflictRecord) : IngestOutcome
    data class ValidationRejected(val errors: List<String>) : IngestOutcome
}
