package com.example.data.fixtures

import com.example.domain.contract.MeasurementValidator
import com.example.domain.model.AppNotification
import com.example.domain.model.CarePlan
import com.example.domain.model.ClinicProfile
import com.example.domain.model.ClinicianProfile
import com.example.domain.model.ClockStatus
import com.example.domain.model.ConflictRecord
import com.example.domain.model.DeviceRecord
import com.example.domain.model.EyeSide
import com.example.domain.model.NotificationCategory
import com.example.domain.model.PatientProfile
import com.example.domain.model.QualityReason
import com.example.domain.model.QualityStatus
import com.example.domain.model.ReviewActionType
import com.example.domain.model.ReviewEvent
import com.example.domain.model.ServiceEvent
import com.example.domain.model.SyntheticMeasurementV1
import com.example.domain.model.TransportStatus

/**
 * Curated, deterministic fictional fixtures with fixed timestamps (+05:30).
 * Never generates random readings at render time.
 * All names, IDs, values, and contacts are strictly fictional demonstration data.
 */
object DeterministicFixtures {

    const val DEMO_CLINIC_ID = "demo-clinic-01"
    const val CLINICIAN_KAVITHA_ID = "demo-clin-01"
    const val CLINICIAN_NAVEEN_ID = "demo-clin-02"

    const val PATIENT_MEERA_ID = "demo-subj-meera"
    const val PATIENT_ARJUN_ID = "demo-subj-arjun"
    const val PATIENT_LAKSHMI_ID = "demo-subj-lakshmi"

    val clinic = ClinicProfile(
        clinicId = DEMO_CLINIC_ID,
        name = "Kaveri Demo Eye Care Clinic",
        locationLabel = "Chennai (Fictional Demo Clinic)",
        demoPhoneLabel = "Demo Desk · 000-0100-DEMO (Not a real phone)",
        demoHoursLabel = "Mon–Sat · 09:00–17:00 IST (Simulated schedule)"
    )

    val clinicians = listOf(
        ClinicianProfile(
            clinicianId = CLINICIAN_KAVITHA_ID,
            fullName = "Dr. Kavitha Raman",
            roleTitle = "Treating Ophthalmologist (Demo)",
            clinicId = DEMO_CLINIC_ID,
            demoContactLabel = "Simulated Care Desk · Extension 401"
        ),
        ClinicianProfile(
            clinicianId = CLINICIAN_NAVEEN_ID,
            fullName = "Dr. Naveen Balaji",
            roleTitle = "Glaucoma Follow-Up Specialist (Demo)",
            clinicId = DEMO_CLINIC_ID,
            demoContactLabel = "Simulated Care Desk · Extension 408"
        )
    )

    val patients = listOf(
        PatientProfile(
            subjectId = PATIENT_MEERA_ID,
            fullName = "Meera Subramanian",
            caregiverName = "Karthik Subramanian (Trained Caregiver)",
            assignedClinicianId = CLINICIAN_KAVITHA_ID,
            clinicId = DEMO_CLINIC_ID,
            assignedDeviceId = "demo-dev-01",
            enrolmentStatus = "Enrolled (Demo)",
            trainingStatus = "Home orientation completed (Demo)"
        ),
        PatientProfile(
            subjectId = PATIENT_ARJUN_ID,
            fullName = "Arjun Krishnan",
            caregiverName = null,
            assignedClinicianId = CLINICIAN_KAVITHA_ID,
            clinicId = DEMO_CLINIC_ID,
            assignedDeviceId = "demo-dev-02",
            enrolmentStatus = "Enrolled (Demo)",
            trainingStatus = "Home orientation completed (Demo)"
        ),
        PatientProfile(
            subjectId = PATIENT_LAKSHMI_ID,
            fullName = "Lakshmi Narayanan",
            caregiverName = "Divya Narayanan (Caregiver)",
            assignedClinicianId = CLINICIAN_NAVEEN_ID,
            clinicId = DEMO_CLINIC_ID,
            assignedDeviceId = null,
            enrolmentStatus = "Pending device allocation (Demo)",
            trainingStatus = "Orientation scheduled (Demo)"
        )
    )

    /**
     * Unresolved safety/clinical fields are deliberately null matching config/home-monitoring.json.
     * Patient Lakshmi has no configured schedule at all to demonstrate truthful null-state UI.
     */
    val carePlans = listOf(
        CarePlan(
            planId = "demo-plan-meera",
            subjectId = PATIENT_MEERA_ID,
            authorClinicianId = CLINICIAN_KAVITHA_ID,
            authorName = "Dr. Kavitha Raman",
            updatedAt = "2026-11-12T10:15:00+05:30",
            eyesPrescribed = listOf(EyeSide.OD, EyeSide.OS),
            scheduleSummary = "Morning & evening check-in (Illustrative demo plan)",
            repetitionsPerSession = null,
            maxRetriesPerEye = null,
            clinicalAlertThresholdNote = null,
            calibrationIntervalNote = null,
            escalationPolicyNote = null,
            clinicianInstructions = "Demo clinician note: Follow comfortable seated posture during home sessions. Contact the clinic directly by phone during office hours if you have eye discomfort. Do not adjust prescribed drops based on this prototype."
        ),
        CarePlan(
            planId = "demo-plan-arjun",
            subjectId = PATIENT_ARJUN_ID,
            authorClinicianId = CLINICIAN_KAVITHA_ID,
            authorName = "Dr. Kavitha Raman",
            updatedAt = "2026-11-14T16:40:00+05:30",
            eyesPrescribed = listOf(EyeSide.OD, EyeSide.OS),
            scheduleSummary = "Once daily morning capture (Illustrative demo plan)",
            repetitionsPerSession = null,
            maxRetriesPerEye = null,
            clinicalAlertThresholdNote = null,
            calibrationIntervalNote = null,
            escalationPolicyNote = null,
            clinicianInstructions = "Demo clinician note: Record both right (OD) and left (OS) eyes separately before midday when convenient. Sync when home Wi-Fi is available."
        ),
        CarePlan(
            planId = "demo-plan-lakshmi",
            subjectId = PATIENT_LAKSHMI_ID,
            authorClinicianId = null,
            authorName = null,
            updatedAt = null,
            eyesPrescribed = emptyList(),
            scheduleSummary = null,
            repetitionsPerSession = null,
            maxRetriesPerEye = null,
            clinicalAlertThresholdNote = null,
            calibrationIntervalNote = null,
            escalationPolicyNote = null,
            clinicianInstructions = null
        )
    )

    val devices = listOf(
        DeviceRecord(
            deviceId = "demo-dev-01",
            serialNumber = "ATN-SIM-2026-001",
            modelName = "AirTon Concept Unit (Simulated)",
            firmwareVersion = "0.4.2-sim",
            calibrationId = "demo-cal-2026-11-a",
            calibrationStatus = "Demo calibration active (Unverified prototype)",
            assignmentState = "Assigned to patient",
            assignedSubjectId = PATIENT_MEERA_ID,
            releaseStatus = "Research prototype — Not clinically cleared",
            lastServiceAt = "2026-11-05T11:20:00+05:30",
            needsService = false
        ),
        DeviceRecord(
            deviceId = "demo-dev-02",
            serialNumber = "ATN-SIM-2026-002",
            modelName = "AirTon Concept Unit (Simulated)",
            firmwareVersion = "0.4.2-sim",
            calibrationId = "demo-cal-2026-11-b",
            calibrationStatus = "Demo calibration active (Unverified prototype)",
            assignmentState = "Assigned to patient",
            assignedSubjectId = PATIENT_ARJUN_ID,
            releaseStatus = "Research prototype — Not clinically cleared",
            lastServiceAt = "2026-11-08T14:05:00+05:30",
            needsService = false
        ),
        DeviceRecord(
            deviceId = "demo-dev-03",
            serialNumber = "ATN-SIM-2026-003",
            modelName = "AirTon Concept Unit (Simulated)",
            firmwareVersion = "0.4.1-sim",
            calibrationId = "demo-cal-2026-08-exp",
            calibrationStatus = "Calibration expired — service required",
            assignmentState = "Returned for service",
            assignedSubjectId = null,
            releaseStatus = "Quarantined in clinic depot (Demo)",
            lastServiceAt = "2026-08-19T09:30:00+05:30",
            needsService = true
        ),
        DeviceRecord(
            deviceId = "demo-dev-04",
            serialNumber = "ATN-SIM-2026-004",
            modelName = "AirTon Concept Unit (Simulated)",
            firmwareVersion = null, // Intentionally null -> "Not available"
            calibrationId = null,   // Intentionally null -> "Not available"
            calibrationStatus = "Not available",
            assignmentState = "Unassigned spare",
            assignedSubjectId = null,
            releaseStatus = "Not provisioned",
            lastServiceAt = null,   // Intentionally null -> "Not available"
            needsService = false
        )
    )

    data class SeedMeasurementSpec(
        val record: SyntheticMeasurementV1,
        val transportStatus: TransportStatus,
        val lastSyncAttemptAt: String?,
        val deliveredAt: String?,
        val syncFailureReason: String?
    )

    private fun seedRecord(
        measurementId: String,
        sessionId: String,
        subjectId: String,
        deviceId: String,
        eye: EyeSide,
        capturedAt: String,
        qualityStatus: QualityStatus,
        qualityReasons: List<QualityReason>,
        iopEstimateMmhg: Double?,
        calibrationId: String = "demo-cal-2026-11-a",
        firmwareVersion: String = "0.4.2-sim",
        clockStatus: ClockStatus = ClockStatus.TRUSTED
    ): SyntheticMeasurementV1 {
        val rawHash = MeasurementValidator.sha256Hex("airton-seed|$measurementId|$sessionId|$capturedAt|${eye.code}")
        val record = SyntheticMeasurementV1(
            schemaVersion = SyntheticMeasurementV1.SCHEMA_VERSION_V1,
            measurementId = measurementId,
            sessionId = sessionId,
            subjectId = subjectId,
            deviceId = deviceId,
            eye = eye,
            capturedAt = capturedAt,
            synthetic = true,
            qualityStatus = qualityStatus,
            qualityReasons = qualityReasons,
            iopEstimateMmhg = iopEstimateMmhg,
            calibrationId = calibrationId,
            modelId = "demo-sim-model-v0",
            firmwareVersion = firmwareVersion,
            rawSha256 = rawHash,
            clockStatus = clockStatus
        )
        require(MeasurementValidator.validate(record) is MeasurementValidator.ValidationResult.Valid) {
            "Seed fixture $measurementId violated v1.0 synthetic contract!"
        }
        return record
    }

    /**
     * 14 deterministic OD/OS synthetic measurements spanning dates, accepted & all 5 rejection reasons,
     * and all independent transport & clinical-review states.
     */
    val measurements: List<SeedMeasurementSpec> = listOf(
        // Session 1 (Meera, Nov 14) - OD & OS accepted, delivered & reviewed
        SeedMeasurementSpec(
            record = seedRecord(
                measurementId = "demo-meas-001",
                sessionId = "demo-sess-001",
                subjectId = PATIENT_MEERA_ID,
                deviceId = "demo-dev-01",
                eye = EyeSide.OD,
                capturedAt = "2026-11-14T08:12:00+05:30",
                qualityStatus = QualityStatus.PASS,
                qualityReasons = emptyList(),
                iopEstimateMmhg = 16.4
            ),
            transportStatus = TransportStatus.DELIVERED,
            lastSyncAttemptAt = "2026-11-14T08:13:10+05:30",
            deliveredAt = "2026-11-14T08:13:10+05:30",
            syncFailureReason = null
        ),
        SeedMeasurementSpec(
            record = seedRecord(
                measurementId = "demo-meas-002",
                sessionId = "demo-sess-001",
                subjectId = PATIENT_MEERA_ID,
                deviceId = "demo-dev-01",
                eye = EyeSide.OS,
                capturedAt = "2026-11-14T08:14:25+05:30",
                qualityStatus = QualityStatus.PASS,
                qualityReasons = emptyList(),
                iopEstimateMmhg = 15.8
            ),
            transportStatus = TransportStatus.DELIVERED,
            lastSyncAttemptAt = "2026-11-14T08:15:02+05:30",
            deliveredAt = "2026-11-14T08:15:02+05:30",
            syncFailureReason = null
        ),
        // Session 2 (Meera, Nov 16) - OD rejected (BLINK, null IOP), OS accepted, delivered & follow-up requested on OD
        SeedMeasurementSpec(
            record = seedRecord(
                measurementId = "demo-meas-003",
                sessionId = "demo-sess-002",
                subjectId = PATIENT_MEERA_ID,
                deviceId = "demo-dev-01",
                eye = EyeSide.OD,
                capturedAt = "2026-11-16T08:05:10+05:30",
                qualityStatus = QualityStatus.REJECT,
                qualityReasons = listOf(QualityReason.BLINK),
                iopEstimateMmhg = null
            ),
            transportStatus = TransportStatus.DELIVERED,
            lastSyncAttemptAt = "2026-11-16T08:08:00+05:30",
            deliveredAt = "2026-11-16T08:08:00+05:30",
            syncFailureReason = null
        ),
        SeedMeasurementSpec(
            record = seedRecord(
                measurementId = "demo-meas-004",
                sessionId = "demo-sess-002",
                subjectId = PATIENT_MEERA_ID,
                deviceId = "demo-dev-01",
                eye = EyeSide.OS,
                capturedAt = "2026-11-16T08:07:30+05:30",
                qualityStatus = QualityStatus.PASS,
                qualityReasons = emptyList(),
                iopEstimateMmhg = 16.9
            ),
            transportStatus = TransportStatus.DELIVERED,
            lastSyncAttemptAt = "2026-11-16T08:08:05+05:30",
            deliveredAt = "2026-11-16T08:08:05+05:30",
            syncFailureReason = null
        ),
        // Session 3 (Meera, Nov 18) - OD & OS accepted, delivered & awaiting review (in clinician queue)
        SeedMeasurementSpec(
            record = seedRecord(
                measurementId = "demo-meas-005",
                sessionId = "demo-sess-003",
                subjectId = PATIENT_MEERA_ID,
                deviceId = "demo-dev-01",
                eye = EyeSide.OD,
                capturedAt = "2026-11-18T19:22:00+05:30",
                qualityStatus = QualityStatus.PASS,
                qualityReasons = emptyList(),
                iopEstimateMmhg = 18.2
            ),
            transportStatus = TransportStatus.DELIVERED,
            lastSyncAttemptAt = "2026-11-18T19:23:12+05:30",
            deliveredAt = "2026-11-18T19:23:12+05:30",
            syncFailureReason = null
        ),
        SeedMeasurementSpec(
            record = seedRecord(
                measurementId = "demo-meas-006",
                sessionId = "demo-sess-003",
                subjectId = PATIENT_MEERA_ID,
                deviceId = "demo-dev-01",
                eye = EyeSide.OS,
                capturedAt = "2026-11-18T19:24:15+05:30",
                qualityStatus = QualityStatus.PASS,
                qualityReasons = emptyList(),
                iopEstimateMmhg = 17.5
            ),
            transportStatus = TransportStatus.DELIVERED,
            lastSyncAttemptAt = "2026-11-18T19:25:01+05:30",
            deliveredAt = "2026-11-18T19:25:01+05:30",
            syncFailureReason = null
        ),
        // Session 4 (Meera, Nov 19) - OD rejected (ALIGNMENT), OS accepted but LOCAL_ONLY (never synced yet)
        SeedMeasurementSpec(
            record = seedRecord(
                measurementId = "demo-meas-007",
                sessionId = "demo-sess-004",
                subjectId = PATIENT_MEERA_ID,
                deviceId = "demo-dev-01",
                eye = EyeSide.OD,
                capturedAt = "2026-11-19T08:18:40+05:30",
                qualityStatus = QualityStatus.REJECT,
                qualityReasons = listOf(QualityReason.ALIGNMENT),
                iopEstimateMmhg = null
            ),
            transportStatus = TransportStatus.PENDING,
            lastSyncAttemptAt = null,
            deliveredAt = null,
            syncFailureReason = null
        ),
        SeedMeasurementSpec(
            record = seedRecord(
                measurementId = "demo-meas-008",
                sessionId = "demo-sess-004",
                subjectId = PATIENT_MEERA_ID,
                deviceId = "demo-dev-01",
                eye = EyeSide.OS,
                capturedAt = "2026-11-19T08:20:05+05:30",
                qualityStatus = QualityStatus.PASS,
                qualityReasons = emptyList(),
                iopEstimateMmhg = 16.1
            ),
            transportStatus = TransportStatus.LOCAL_ONLY,
            lastSyncAttemptAt = null,
            deliveredAt = null,
            syncFailureReason = null
        ),
        // Session 5 (Meera, Nov 20 morning) - OD accepted, delivered & awaiting review
        SeedMeasurementSpec(
            record = seedRecord(
                measurementId = "demo-meas-009",
                sessionId = "demo-sess-005",
                subjectId = PATIENT_MEERA_ID,
                deviceId = "demo-dev-01",
                eye = EyeSide.OD,
                capturedAt = "2026-11-20T07:55:00+05:30",
                qualityStatus = QualityStatus.PASS,
                qualityReasons = emptyList(),
                iopEstimateMmhg = 17.1
            ),
            transportStatus = TransportStatus.DELIVERED,
            lastSyncAttemptAt = "2026-11-20T07:56:15+05:30",
            deliveredAt = "2026-11-20T07:56:15+05:30",
            syncFailureReason = null
        ),
        // Session 6 (Arjun, Nov 15) - OD accepted (reviewed with appended note), OS rejected (SIGNAL_MISSING)
        SeedMeasurementSpec(
            record = seedRecord(
                measurementId = "demo-meas-010",
                sessionId = "demo-sess-006",
                subjectId = PATIENT_ARJUN_ID,
                deviceId = "demo-dev-02",
                eye = EyeSide.OD,
                capturedAt = "2026-11-15T09:10:00+05:30",
                qualityStatus = QualityStatus.PASS,
                qualityReasons = emptyList(),
                iopEstimateMmhg = 21.3,
                calibrationId = "demo-cal-2026-11-b"
            ),
            transportStatus = TransportStatus.DELIVERED,
            lastSyncAttemptAt = "2026-11-15T09:12:00+05:30",
            deliveredAt = "2026-11-15T09:12:00+05:30",
            syncFailureReason = null
        ),
        // Session 7 (Arjun, Nov 17) - OD accepted with a quarantined conflicting replay record (original preserved!)
        SeedMeasurementSpec(
            record = seedRecord(
                measurementId = "demo-meas-011",
                sessionId = "demo-sess-007",
                subjectId = PATIENT_ARJUN_ID,
                deviceId = "demo-dev-02",
                eye = EyeSide.OD,
                capturedAt = "2026-11-17T09:04:00+05:30",
                qualityStatus = QualityStatus.PASS,
                qualityReasons = emptyList(),
                iopEstimateMmhg = 19.6,
                calibrationId = "demo-cal-2026-11-b"
            ),
            transportStatus = TransportStatus.QUARANTINED,
            lastSyncAttemptAt = "2026-11-17T09:14:20+05:30",
            deliveredAt = "2026-11-17T09:05:10+05:30",
            syncFailureReason = "Conflicting payload replay detected; original immutable record preserved."
        ),
        // Session 7 (Arjun, Nov 17) - OS rejected (SIGNAL_MISSING), delivered awaiting review
        SeedMeasurementSpec(
            record = seedRecord(
                measurementId = "demo-meas-012",
                sessionId = "demo-sess-007",
                subjectId = PATIENT_ARJUN_ID,
                deviceId = "demo-dev-02",
                eye = EyeSide.OS,
                capturedAt = "2026-11-17T09:06:30+05:30",
                qualityStatus = QualityStatus.REJECT,
                qualityReasons = listOf(QualityReason.SIGNAL_MISSING),
                iopEstimateMmhg = null,
                calibrationId = "demo-cal-2026-11-b"
            ),
            transportStatus = TransportStatus.DELIVERED,
            lastSyncAttemptAt = "2026-11-17T09:08:00+05:30",
            deliveredAt = "2026-11-17T09:08:00+05:30",
            syncFailureReason = null
        ),
        // Session 8 (Arjun, Nov 19) - OD rejected (CLOCK_UNTRUSTED), FAILED offline sync waiting for retry
        SeedMeasurementSpec(
            record = seedRecord(
                measurementId = "demo-meas-013",
                sessionId = "demo-sess-008",
                subjectId = PATIENT_ARJUN_ID,
                deviceId = "demo-dev-02",
                eye = EyeSide.OD,
                capturedAt = "2026-11-19T10:42:00+05:30",
                qualityStatus = QualityStatus.REJECT,
                qualityReasons = listOf(QualityReason.CLOCK_UNTRUSTED),
                iopEstimateMmhg = null,
                calibrationId = "demo-cal-2026-11-b",
                clockStatus = ClockStatus.UNTRUSTED
            ),
            transportStatus = TransportStatus.FAILED,
            lastSyncAttemptAt = "2026-11-19T10:43:15+05:30",
            deliveredAt = null,
            syncFailureReason = "Simulated network interrupted during upload; retry preserves demo-meas-013."
        ),
        // Session 9 (Arjun, Nov 19) - OS rejected (CALIBRATION_EXPIRED from returned unit demo-dev-03), delivered awaiting review
        SeedMeasurementSpec(
            record = seedRecord(
                measurementId = "demo-meas-014",
                sessionId = "demo-sess-009",
                subjectId = PATIENT_ARJUN_ID,
                deviceId = "demo-dev-03",
                eye = EyeSide.OS,
                capturedAt = "2026-11-19T16:15:00+05:30",
                qualityStatus = QualityStatus.REJECT,
                qualityReasons = listOf(QualityReason.CALIBRATION_EXPIRED),
                iopEstimateMmhg = null,
                calibrationId = "demo-cal-2026-08-exp",
                firmwareVersion = "0.4.1-sim"
            ),
            transportStatus = TransportStatus.DELIVERED,
            lastSyncAttemptAt = "2026-11-19T16:16:40+05:30",
            deliveredAt = "2026-11-19T16:16:40+05:30",
            syncFailureReason = null
        )
    )

    val reviewEvents: List<ReviewEvent> = listOf(
        ReviewEvent(
            eventId = "demo-rev-001",
            measurementId = "demo-meas-001",
            actionType = ReviewActionType.ACKNOWLEDGED_REVIEW,
            authorClinicianId = CLINICIAN_KAVITHA_ID,
            authorName = "Dr. Kavitha Raman",
            note = "Demo review acknowledgement recorded for OD baseline session.",
            createdAt = "2026-11-14T14:30:00+05:30"
        ),
        ReviewEvent(
            eventId = "demo-rev-002",
            measurementId = "demo-meas-002",
            actionType = ReviewActionType.ACKNOWLEDGED_REVIEW,
            authorClinicianId = CLINICIAN_KAVITHA_ID,
            authorName = "Dr. Kavitha Raman",
            note = "Demo review acknowledgement recorded for OS baseline session.",
            createdAt = "2026-11-14T14:31:00+05:30"
        ),
        ReviewEvent(
            eventId = "demo-rev-003",
            measurementId = "demo-meas-003",
            actionType = ReviewActionType.FOLLOW_UP_REQUESTED,
            authorClinicianId = CLINICIAN_KAVITHA_ID,
            authorName = "Dr. Kavitha Raman",
            note = "Demo follow-up: Blink rejected OD acquisition. Please review comfortable positioning at next scheduled check-in.",
            createdAt = "2026-11-16T11:45:00+05:30"
        ),
        ReviewEvent(
            eventId = "demo-rev-004",
            measurementId = "demo-meas-004",
            actionType = ReviewActionType.ACKNOWLEDGED_REVIEW,
            authorClinicianId = CLINICIAN_KAVITHA_ID,
            authorName = "Dr. Kavitha Raman",
            note = "OS record acknowledged in demo workflow.",
            createdAt = "2026-11-16T11:46:00+05:30"
        ),
        ReviewEvent(
            eventId = "demo-rev-005",
            measurementId = "demo-meas-010",
            actionType = ReviewActionType.ACKNOWLEDGED_REVIEW,
            authorClinicianId = CLINICIAN_KAVITHA_ID,
            authorName = "Dr. Kavitha Raman",
            note = "Initial demo acknowledgement logged.",
            createdAt = "2026-11-15T15:00:00+05:30"
        ),
        ReviewEvent(
            eventId = "demo-rev-006",
            measurementId = "demo-meas-010",
            actionType = ReviewActionType.NOTE_APPENDED,
            authorClinicianId = CLINICIAN_NAVEEN_ID,
            authorName = "Dr. Naveen Balaji",
            note = "Appended immutable amendment (Demo): Discuss home session timing during next scheduled in-clinic visit.",
            createdAt = "2026-11-16T09:20:00+05:30"
        )
    )

    val conflicts: List<ConflictRecord> = listOf(
        ConflictRecord(
            conflictId = "demo-conf-001",
            measurementId = "demo-meas-011",
            incomingPayloadSha256 = MeasurementValidator.sha256Hex("conflicting-replay|demo-meas-011|22.9"),
            existingPayloadSha256 = MeasurementValidator.canonicalPayloadSha256(
                measurements.first { it.record.measurementId == "demo-meas-011" }.record
            ),
            detectedAt = "2026-11-17T09:14:20+05:30",
            summary = "Seeded conflict example: Replay for demo-meas-011 carried a mismatched synthetic payload and was quarantined without overwriting the original record."
        )
    )

    val serviceEvents: List<ServiceEvent> = listOf(
        ServiceEvent(
            serviceEventId = "demo-srv-001",
            deviceId = "demo-dev-01",
            eventType = "Calibration verification (Demo)",
            actorName = "S. Pradeep (Biomedical Tech · Demo)",
            performedAt = "2026-11-05T11:20:00+05:30",
            summary = "Bench check logged in simulation database prior to home loaner assignment.",
            calibrationRef = "demo-cal-2026-11-a"
        ),
        ServiceEvent(
            serviceEventId = "demo-srv-002",
            deviceId = "demo-dev-02",
            eventType = "Allocation & orientation (Demo)",
            actorName = "R. Anitha (Clinic Coordinator · Demo)",
            performedAt = "2026-11-08T14:05:00+05:30",
            summary = "Assigned demo-dev-02 to Arjun Krishnan with simulated setup checklist.",
            calibrationRef = "demo-cal-2026-11-b"
        ),
        ServiceEvent(
            serviceEventId = "demo-srv-003",
            deviceId = "demo-dev-03",
            eventType = "Return & quarantine (Demo)",
            actorName = "S. Pradeep (Biomedical Tech · Demo)",
            performedAt = "2026-11-19T17:10:00+05:30",
            summary = "Calibration status expired; unit removed from active pool awaiting formal bench recalibration protocol.",
            calibrationRef = "demo-cal-2026-08-exp"
        ),
        ServiceEvent(
            serviceEventId = "demo-srv-004",
            deviceId = "demo-dev-04",
            eventType = "Intake inspection (Demo)",
            actorName = "S. Pradeep (Biomedical Tech · Demo)",
            performedAt = "2026-11-10T10:00:00+05:30",
            summary = "Physical shell checked; firmware and calibration ID remain unassigned.",
            calibrationRef = null
        )
    )

    val notifications: List<AppNotification> = listOf(
        AppNotification(
            notificationId = "demo-notif-001",
            category = NotificationCategory.DEMO_REMINDER,
            title = "Local demo reminder — simulation mode active",
            body = "This is a local prototype notification. No clinician is continuously monitoring this app.",
            createdAt = "2026-11-20T08:00:00+05:30",
            relatedMeasurementId = null,
            isRead = false
        ),
        AppNotification(
            notificationId = "demo-notif-002",
            category = NotificationCategory.CLINICIAN_REVIEW,
            title = "Demo review event recorded for demo-meas-003",
            body = "Dr. Kavitha Raman logged a simulated follow-up note on the blink-rejected OD session.",
            createdAt = "2026-11-16T11:45:00+05:30",
            relatedMeasurementId = "demo-meas-003",
            isRead = false
        ),
        AppNotification(
            notificationId = "demo-notif-003",
            category = NotificationCategory.SYNC_EVENT,
            title = "2 demo records awaiting sync",
            body = "Local-only and pending synthetic records remain stored on device until simulated sync runs.",
            createdAt = "2026-11-19T08:21:00+05:30",
            relatedMeasurementId = "demo-meas-008",
            isRead = false
        ),
        AppNotification(
            notificationId = "demo-notif-004",
            category = NotificationCategory.SYNC_EVENT,
            title = "Conflicting replay quarantined (demo-meas-011)",
            body = "An altered replay for demo-meas-011 was quarantined to protect the immutable original record.",
            createdAt = "2026-11-17T09:14:20+05:30",
            relatedMeasurementId = "demo-meas-011",
            isRead = true
        ),
        AppNotification(
            notificationId = "demo-notif-005",
            category = NotificationCategory.CLINICIAN_REVIEW,
            title = "Immutable clinician note appended (demo-meas-010)",
            body = "Dr. Naveen Balaji appended a demo note without overwriting the earlier review event.",
            createdAt = "2026-11-16T09:20:00+05:30",
            relatedMeasurementId = "demo-meas-010",
            isRead = true
        )
    )
}
