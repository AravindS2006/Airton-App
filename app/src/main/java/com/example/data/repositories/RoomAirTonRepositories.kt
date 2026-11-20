package com.example.data.repositories

import androidx.room.withTransaction
import com.example.data.fixtures.DeterministicFixtures
import com.example.data.persistence.AirTonDao
import com.example.data.persistence.AirTonDatabase
import com.example.data.persistence.CarePlanEntity
import com.example.data.persistence.ClinicEntity
import com.example.data.persistence.ClinicianEntity
import com.example.data.persistence.ConflictRecordEntity
import com.example.data.persistence.DeviceEntity
import com.example.data.persistence.MeasurementEntity
import com.example.data.persistence.NotificationEntity
import com.example.data.persistence.PatientEntity
import com.example.data.persistence.ReviewEventEntity
import com.example.data.persistence.ServiceEventEntity
import com.example.data.persistence.TransportStateEntity
import com.example.domain.contract.MeasurementValidator
import com.example.domain.model.AppNotification
import com.example.domain.model.CarePlan
import com.example.domain.model.ClinicProfile
import com.example.domain.model.ClinicalReviewStatus
import com.example.domain.model.ClinicianProfile
import com.example.domain.model.ClockStatus
import com.example.domain.model.ConflictRecord
import com.example.domain.model.DeviceRecord
import com.example.domain.model.EyeSide
import com.example.domain.model.IngestOutcome
import com.example.domain.model.MeasurementEnvelope
import com.example.domain.model.NotificationCategory
import com.example.domain.model.PatientProfile
import com.example.domain.model.QualityReason
import com.example.domain.model.QualityStatus
import com.example.domain.model.ReviewActionType
import com.example.domain.model.ReviewEvent
import com.example.domain.model.ServiceEvent
import com.example.domain.model.SyntheticMeasurementV1
import com.example.domain.model.TransportStatus
import com.example.domain.ports.CarePlanRepository
import com.example.domain.ports.ClinicOperationsRepository
import com.example.domain.ports.ClinicianRepository
import com.example.domain.ports.DemoModeController
import com.example.domain.ports.MeasurementRepository
import com.example.domain.ports.PatientRepository
import com.example.domain.ports.ReviewRepository
import com.example.domain.ports.SessionRepository
import com.example.domain.ports.SyncBatchResult
import com.example.domain.ports.SyncRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.util.concurrent.atomic.AtomicInteger

/**
 * Room-backed local prototype repository implementing all AirTon clean-architecture ports.
 * Preserves immutable measurements, independent transport & review state machines,
 * conflict quarantine without overwrites, and idempotent fixture seeding.
 */
class RoomAirTonStore(
    private val db: AirTonDatabase
) : MeasurementRepository,
    SessionRepository,
    CarePlanRepository,
    PatientRepository,
    ClinicianRepository,
    ReviewRepository,
    SyncRepository,
    ClinicOperationsRepository,
    DemoModeController {

    private val dao: AirTonDao = db.airTonDao()
    private val sessionCounter = AtomicInteger(100)
    private val measurementCounter = AtomicInteger(100)
    private val eventCounter = AtomicInteger(100)

    override fun nextDemoSessionId(): String = "demo-sess-${sessionCounter.incrementAndGet()}"
    override fun nextDemoMeasurementId(): String = "demo-meas-${measurementCounter.incrementAndGet()}"

    private fun nextTimestampIso(): String {
        val idx = eventCounter.incrementAndGet()
        val minute = (20 + (idx % 39)).toString().padStart(2, '0')
        val second = ((idx * 13) % 60).toString().padStart(2, '0')
        return "2026-11-20T10:$minute:$second+05:30"
    }

    // --- Seeding & Reset ---
    override suspend fun ensureSeeded() {
        if (dao.countPatients() == 0) {
            seedBaselineTransactional()
        }
    }

    override suspend fun resetDemoDatabase() {
        db.withTransaction {
            dao.clearNotifications()
            dao.clearServiceEvents()
            dao.clearDevices()
            dao.clearConflicts()
            dao.clearReviewEvents()
            dao.clearTransportStates()
            dao.clearMeasurements()
            dao.clearCarePlans()
            dao.clearPatients()
            dao.clearClinicians()
            dao.clearClinics()
        }
        seedBaselineTransactional()
    }

    private suspend fun seedBaselineTransactional() {
        db.withTransaction {
            dao.insertClinic(DeterministicFixtures.clinic.toEntity())
            dao.insertClinicians(DeterministicFixtures.clinicians.map { it.toEntity() })
            dao.insertPatients(DeterministicFixtures.patients.map { it.toEntity() })
            dao.insertCarePlans(DeterministicFixtures.carePlans.map { it.toEntity() })
            dao.insertDevices(DeterministicFixtures.devices.map { it.toEntity() })
            dao.insertServiceEvents(DeterministicFixtures.serviceEvents.map { it.toEntity() })
            dao.insertNotifications(DeterministicFixtures.notifications.map { it.toEntity() })

            DeterministicFixtures.measurements.forEach { spec ->
                dao.insertMeasurementImmutable(spec.record.toEntity())
                dao.upsertTransportState(
                    TransportStateEntity(
                        measurementId = spec.record.measurementId,
                        transportStatus = spec.transportStatus.name,
                        lastSyncAttemptAt = spec.lastSyncAttemptAt,
                        deliveredAt = spec.deliveredAt,
                        syncFailureReason = spec.syncFailureReason
                    )
                )
            }
            dao.insertReviewEvents(DeterministicFixtures.reviewEvents.map { it.toEntity() })
            dao.insertConflicts(DeterministicFixtures.conflicts.map { it.toEntity() })
        }
    }

    // --- Measurement Repository ---
    override fun observeAllEnvelopes(): Flow<List<MeasurementEnvelope>> = combine(
        dao.observeMeasurements(),
        dao.observeTransportStates(),
        dao.observeReviewEvents(),
        dao.observeConflicts()
    ) { measurements, transports, reviews, conflicts ->
        val transportMap = transports.associateBy { it.measurementId }
        val reviewsMap = reviews.groupBy { it.measurementId }
        val conflictsMap = conflicts.groupBy { it.measurementId }
        measurements.map { m ->
            assembleEnvelope(
                m = m,
                transport = transportMap[m.measurementId],
                reviews = reviewsMap[m.measurementId].orEmpty(),
                conflicts = conflictsMap[m.measurementId].orEmpty()
            )
        }
    }

    override fun observeEnvelopesForSubject(subjectId: String): Flow<List<MeasurementEnvelope>> =
        observeAllEnvelopes().map { list -> list.filter { it.measurement.subjectId == subjectId } }

    override fun observeEnvelope(measurementId: String): Flow<MeasurementEnvelope?> =
        observeAllEnvelopes().map { list -> list.firstOrNull { it.measurement.measurementId == measurementId } }

    override suspend fun ingestMeasurement(
        record: SyntheticMeasurementV1,
        initialTransportStatus: TransportStatus
    ): IngestOutcome {
        when (val validation = MeasurementValidator.validate(record)) {
            is MeasurementValidator.ValidationResult.Invalid -> {
                return IngestOutcome.ValidationRejected(validation.errors)
            }
            is MeasurementValidator.ValidationResult.Valid -> Unit
        }

        val incomingHash = MeasurementValidator.canonicalPayloadSha256(record)
        val existing = dao.findMeasurementById(record.measurementId)

        if (existing != null) {
            return if (existing.canonicalPayloadSha256 == incomingHash) {
                IngestOutcome.DuplicateIdempotent(record.measurementId)
            } else {
                val conflict = ConflictRecord(
                    conflictId = "demo-conf-${eventCounter.incrementAndGet()}",
                    measurementId = record.measurementId,
                    incomingPayloadSha256 = incomingHash,
                    existingPayloadSha256 = existing.canonicalPayloadSha256,
                    detectedAt = nextTimestampIso(),
                    summary = "Conflicting replay detected for ${record.measurementId}; original immutable record preserved and replay quarantined."
                )
                db.withTransaction {
                    dao.insertConflict(conflict.toEntity())
                    val currentTransport = dao.findTransportState(record.measurementId)
                    dao.upsertTransportState(
                        TransportStateEntity(
                            measurementId = record.measurementId,
                            transportStatus = TransportStatus.QUARANTINED.name,
                            lastSyncAttemptAt = conflict.detectedAt,
                            deliveredAt = currentTransport?.deliveredAt,
                            syncFailureReason = conflict.summary
                        )
                    )
                    dao.insertNotification(
                        NotificationEntity(
                            notificationId = "demo-notif-${eventCounter.incrementAndGet()}",
                            category = NotificationCategory.SYNC_EVENT.name,
                            title = "Conflicting replay quarantined (${record.measurementId})",
                            body = conflict.summary,
                            createdAt = conflict.detectedAt,
                            relatedMeasurementId = record.measurementId,
                            isRead = false
                        )
                    )
                }
                IngestOutcome.ConflictQuarantined(conflict)
            }
        }

        val nowIso = nextTimestampIso()
        db.withTransaction {
            dao.insertMeasurementImmutable(record.toEntity())
            dao.upsertTransportState(
                TransportStateEntity(
                    measurementId = record.measurementId,
                    transportStatus = initialTransportStatus.name,
                    lastSyncAttemptAt = if (initialTransportStatus == TransportStatus.DELIVERED) nowIso else null,
                    deliveredAt = if (initialTransportStatus == TransportStatus.DELIVERED) nowIso else null,
                    syncFailureReason = null
                )
            )
        }
        return IngestOutcome.Inserted(record.measurementId)
    }

    // --- Sync Repository ---
    override fun observeConflicts(): Flow<List<ConflictRecord>> =
        dao.observeConflicts().map { list -> list.map { it.toDomain() } }

    override suspend fun retryPendingUploads(isOfflineSimulated: Boolean): SyncBatchResult {
        val unsynced = dao.findUnsyncedTransportStates()
        if (unsynced.isEmpty()) {
            return SyncBatchResult(attemptedCount = 0, deliveredCount = 0, failedOfflineCount = 0)
        }
        val nowIso = nextTimestampIso()
        var delivered = 0
        var failed = 0

        db.withTransaction {
            unsynced.forEach { state ->
                if (isOfflineSimulated) {
                    failed++
                    dao.upsertTransportState(
                        state.copy(
                            transportStatus = TransportStatus.FAILED.name,
                            lastSyncAttemptAt = nowIso,
                            syncFailureReason = "Simulated offline mode enabled; immutable ID ${state.measurementId} queued safely."
                        )
                    )
                } else {
                    delivered++
                    dao.upsertTransportState(
                        state.copy(
                            transportStatus = TransportStatus.DELIVERED.name,
                            lastSyncAttemptAt = nowIso,
                            deliveredAt = nowIso,
                            syncFailureReason = null
                        )
                    )
                }
            }
            if (delivered > 0) {
                dao.insertNotification(
                    NotificationEntity(
                        notificationId = "demo-notif-${eventCounter.incrementAndGet()}",
                        category = NotificationCategory.SYNC_EVENT.name,
                        title = "$delivered simulated record(s) marked delivered",
                        body = "Simulated transport acknowledged delivery. Records remain unreviewed until a clinician logs a review action.",
                        createdAt = nowIso,
                        relatedMeasurementId = unsynced.first().measurementId,
                        isRead = false
                    )
                )
            }
        }

        return SyncBatchResult(
            attemptedCount = unsynced.size,
            deliveredCount = delivered,
            failedOfflineCount = failed
        )
    }

    override suspend fun simulateDuplicateIdempotentRetry(measurementId: String): IngestOutcome {
        val existing = dao.findMeasurementById(measurementId)
            ?: return IngestOutcome.ValidationRejected(listOf("Measurement $measurementId not found."))
        return ingestMeasurement(existing.toDomain(), TransportStatus.DELIVERED)
    }

    override suspend fun simulateConflictingReplay(measurementId: String): IngestOutcome {
        val existing = dao.findMeasurementById(measurementId)?.toDomain()
            ?: return IngestOutcome.ValidationRejected(listOf("Measurement $measurementId not found."))

        // Create a schema-valid payload with the SAME measurementId but altered content
        val altered = if (existing.qualityStatus == QualityStatus.PASS && existing.iopEstimateMmhg != null) {
            existing.copy(
                iopEstimateMmhg = existing.iopEstimateMmhg + 3.5,
                rawSha256 = MeasurementValidator.sha256Hex("altered-replay|${existing.measurementId}|${eventCounter.incrementAndGet()}")
            )
        } else {
            existing.copy(
                calibrationId = "${existing.calibrationId}-tampered",
                rawSha256 = MeasurementValidator.sha256Hex("altered-replay|${existing.measurementId}|${eventCounter.incrementAndGet()}")
            )
        }
        return ingestMeasurement(altered, TransportStatus.PENDING)
    }

    // --- Review Repository (Append-Only) ---
    override fun observeAllReviewEvents(): Flow<List<ReviewEvent>> =
        dao.observeReviewEvents().map { list -> list.map { it.toDomain() } }

    override suspend fun appendReviewEvent(
        measurementId: String,
        actionType: ReviewActionType,
        authorClinicianId: String,
        authorName: String,
        note: String?
    ): ReviewEvent {
        val nowIso = nextTimestampIso()
        val event = ReviewEvent(
            eventId = "demo-rev-${eventCounter.incrementAndGet()}",
            measurementId = measurementId,
            actionType = actionType,
            authorClinicianId = authorClinicianId,
            authorName = authorName,
            note = note?.trim()?.takeIf { it.isNotEmpty() },
            createdAt = nowIso
        )
        db.withTransaction {
            dao.insertReviewEventAppendOnly(event.toEntity())
            dao.insertNotification(
                NotificationEntity(
                    notificationId = "demo-notif-${eventCounter.incrementAndGet()}",
                    category = NotificationCategory.CLINICIAN_REVIEW.name,
                    title = "${actionType.labelEn} ($measurementId)",
                    body = "$authorName logged an append-only demo review event.",
                    createdAt = nowIso,
                    relatedMeasurementId = measurementId,
                    isRead = false
                )
            )
        }
        return event
    }

    // --- Care Plans ---
    override fun observeAllCarePlans(): Flow<List<CarePlan>> =
        dao.observeCarePlans().map { list -> list.map { it.toDomain() } }

    override fun observeCarePlanForSubject(subjectId: String): Flow<CarePlan?> =
        dao.observeCarePlanForSubject(subjectId).map { it?.toDomain() }

    override suspend fun saveClinicianCarePlan(plan: CarePlan) {
        dao.upsertCarePlan(plan.copy(updatedAt = nextTimestampIso()).toEntity())
    }

    // --- Patients & Clinicians ---
    override fun observePatients(): Flow<List<PatientProfile>> =
        dao.observePatients().map { list -> list.map { it.toDomain() } }

    override fun observePatient(subjectId: String): Flow<PatientProfile?> =
        dao.observePatient(subjectId).map { it?.toDomain() }

    override suspend fun updatePatientAssignment(
        subjectId: String,
        assignedDeviceId: String?,
        enrolmentStatus: String,
        trainingStatus: String
    ) {
        dao.updatePatientAssignment(subjectId, assignedDeviceId, enrolmentStatus, trainingStatus)
    }

    override fun observeClinicians(): Flow<List<ClinicianProfile>> =
        dao.observeClinicians().map { list -> list.map { it.toDomain() } }

    override fun observeClinic(): Flow<ClinicProfile?> =
        dao.observeClinic().map { it?.toDomain() }

    // --- Clinic Operations ---
    override fun observeDevices(): Flow<List<DeviceRecord>> =
        dao.observeDevices().map { list -> list.map { it.toDomain() } }

    override fun observeServiceEvents(): Flow<List<ServiceEvent>> =
        dao.observeServiceEvents().map { list -> list.map { it.toDomain() } }

    override fun observeNotifications(): Flow<List<AppNotification>> =
        dao.observeNotifications().map { list -> list.map { it.toDomain() } }

    override suspend fun updateDeviceAssignment(
        deviceId: String,
        assignedSubjectId: String?,
        assignmentState: String,
        actorName: String
    ) {
        val current = dao.findDeviceById(deviceId) ?: return
        val nowIso = nextTimestampIso()
        db.withTransaction {
            dao.updateDevice(
                current.copy(
                    assignedSubjectId = assignedSubjectId,
                    assignmentState = assignmentState
                )
            )
            dao.insertServiceEventAppendOnly(
                ServiceEventEntity(
                    serviceEventId = "demo-srv-${eventCounter.incrementAndGet()}",
                    deviceId = deviceId,
                    eventType = "Assignment update (Demo)",
                    actorName = actorName,
                    performedAt = nowIso,
                    summary = "Device $deviceId updated to '$assignmentState'${assignedSubjectId?.let { " ($it)" } ?: ""}.",
                    calibrationRef = current.calibrationId
                )
            )
        }
    }

    override suspend fun appendServiceEvent(
        deviceId: String,
        eventType: String,
        actorName: String,
        summary: String,
        calibrationRef: String?,
        clearNeedsService: Boolean
    ): ServiceEvent {
        val nowIso = nextTimestampIso()
        val event = ServiceEvent(
            serviceEventId = "demo-srv-${eventCounter.incrementAndGet()}",
            deviceId = deviceId,
            eventType = eventType,
            actorName = actorName,
            performedAt = nowIso,
            summary = summary.trim(),
            calibrationRef = calibrationRef?.trim()?.takeIf { it.isNotEmpty() }
        )
        db.withTransaction {
            dao.insertServiceEventAppendOnly(event.toEntity())
            val dev = dao.findDeviceById(deviceId)
            if (dev != null) {
                dao.updateDevice(
                    dev.copy(
                        lastServiceAt = nowIso,
                        calibrationId = event.calibrationRef ?: dev.calibrationId,
                        calibrationStatus = if (clearNeedsService) {
                            "Demo service logged (Unverified prototype)"
                        } else dev.calibrationStatus,
                        needsService = if (clearNeedsService) false else dev.needsService
                    )
                )
            }
        }
        return event
    }

    override suspend fun markNotificationRead(notificationId: String) {
        dao.markNotificationRead(notificationId)
    }

    override suspend fun addDemoNotification(notification: AppNotification) {
        dao.insertNotification(notification.toEntity())
    }

    // --- Envelope Assembly & Mapping ---
    private fun assembleEnvelope(
        m: MeasurementEntity,
        transport: TransportStateEntity?,
        reviews: List<ReviewEventEntity>,
        conflicts: List<ConflictRecordEntity>
    ): MeasurementEnvelope {
        val transportStatus = transport?.transportStatus?.let { name ->
            TransportStatus.entries.firstOrNull { it.name == name }
        } ?: TransportStatus.LOCAL_ONLY

        val domainReviews = reviews.map { it.toDomain() }
        val derivedReviewStatus = deriveClinicalReviewStatus(transportStatus, domainReviews)

        return MeasurementEnvelope(
            measurement = m.toDomain(),
            transportStatus = transportStatus,
            lastSyncAttemptAt = transport?.lastSyncAttemptAt,
            deliveredAt = transport?.deliveredAt,
            syncFailureReason = transport?.syncFailureReason,
            reviewStatus = derivedReviewStatus,
            reviewEvents = domainReviews,
            conflicts = conflicts.map { it.toDomain() }
        )
    }

    private fun deriveClinicalReviewStatus(
        transportStatus: TransportStatus,
        events: List<ReviewEvent>
    ): ClinicalReviewStatus {
        val lastPrimary = events.lastOrNull {
            it.actionType == ReviewActionType.ACKNOWLEDGED_REVIEW ||
                it.actionType == ReviewActionType.FOLLOW_UP_REQUESTED
        }
        return when {
            lastPrimary?.actionType == ReviewActionType.FOLLOW_UP_REQUESTED ->
                ClinicalReviewStatus.FOLLOW_UP_REQUESTED
            lastPrimary?.actionType == ReviewActionType.ACKNOWLEDGED_REVIEW ->
                ClinicalReviewStatus.REVIEWED
            transportStatus == TransportStatus.DELIVERED ->
                ClinicalReviewStatus.AWAITING_REVIEW
            else ->
                ClinicalReviewStatus.NOT_APPLICABLE
        }
    }
}

// --- Entity <-> Domain Mappers ---

private fun ClinicProfile.toEntity() = ClinicEntity(clinicId, name, locationLabel, demoPhoneLabel, demoHoursLabel)
private fun ClinicEntity.toDomain() = ClinicProfile(clinicId, name, locationLabel, demoPhoneLabel, demoHoursLabel)

private fun ClinicianProfile.toEntity() = ClinicianEntity(clinicianId, fullName, roleTitle, clinicId, demoContactLabel)
private fun ClinicianEntity.toDomain() = ClinicianProfile(clinicianId, fullName, roleTitle, clinicId, demoContactLabel)

private fun PatientProfile.toEntity() = PatientEntity(
    subjectId, fullName, caregiverName, assignedClinicianId, clinicId, assignedDeviceId, enrolmentStatus, trainingStatus
)

private fun PatientEntity.toDomain() = PatientProfile(
    subjectId, fullName, caregiverName, assignedClinicianId, clinicId, assignedDeviceId, enrolmentStatus, trainingStatus
)

private fun CarePlan.toEntity() = CarePlanEntity(
    planId = planId,
    subjectId = subjectId,
    authorClinicianId = authorClinicianId,
    authorName = authorName,
    updatedAt = updatedAt,
    eyesPrescribedCsv = eyesPrescribed.joinToString(",") { it.code },
    scheduleSummary = scheduleSummary,
    repetitionsPerSession = repetitionsPerSession,
    maxRetriesPerEye = maxRetriesPerEye,
    clinicalAlertThresholdNote = clinicalAlertThresholdNote,
    calibrationIntervalNote = calibrationIntervalNote,
    escalationPolicyNote = escalationPolicyNote,
    clinicianInstructions = clinicianInstructions
)

private fun CarePlanEntity.toDomain() = CarePlan(
    planId = planId,
    subjectId = subjectId,
    authorClinicianId = authorClinicianId,
    authorName = authorName,
    updatedAt = updatedAt,
    eyesPrescribed = eyesPrescribedCsv.split(",").mapNotNull { code ->
        EyeSide.entries.firstOrNull { it.code == code.trim() }
    },
    scheduleSummary = scheduleSummary,
    repetitionsPerSession = repetitionsPerSession,
    maxRetriesPerEye = maxRetriesPerEye,
    clinicalAlertThresholdNote = clinicalAlertThresholdNote,
    calibrationIntervalNote = calibrationIntervalNote,
    escalationPolicyNote = escalationPolicyNote,
    clinicianInstructions = clinicianInstructions
)

private fun SyntheticMeasurementV1.toEntity() = MeasurementEntity(
    measurementId = measurementId,
    schemaVersion = schemaVersion,
    sessionId = sessionId,
    subjectId = subjectId,
    deviceId = deviceId,
    eyeCode = eye.code,
    capturedAt = capturedAt,
    synthetic = synthetic,
    qualityStatusWire = qualityStatus.wireValue,
    qualityReasonsCsv = qualityReasons.joinToString(",") { it.wireValue },
    iopEstimateMmhg = iopEstimateMmhg,
    calibrationId = calibrationId,
    modelId = modelId,
    firmwareVersion = firmwareVersion,
    rawSha256 = rawSha256,
    clockStatusWire = clockStatus.wireValue,
    canonicalPayloadSha256 = MeasurementValidator.canonicalPayloadSha256(this)
)

private fun MeasurementEntity.toDomain() = SyntheticMeasurementV1(
    schemaVersion = schemaVersion,
    measurementId = measurementId,
    sessionId = sessionId,
    subjectId = subjectId,
    deviceId = deviceId,
    eye = EyeSide.entries.firstOrNull { it.code == eyeCode } ?: EyeSide.OD,
    capturedAt = capturedAt,
    synthetic = synthetic,
    qualityStatus = if (qualityStatusWire == QualityStatus.PASS.wireValue) QualityStatus.PASS else QualityStatus.REJECT,
    qualityReasons = qualityReasonsCsv.split(",").mapNotNull { QualityReason.fromWire(it.trim()) },
    iopEstimateMmhg = iopEstimateMmhg,
    calibrationId = calibrationId,
    modelId = modelId,
    firmwareVersion = firmwareVersion,
    rawSha256 = rawSha256,
    clockStatus = ClockStatus.fromWire(clockStatusWire) ?: ClockStatus.UNTRUSTED
)

private fun ReviewEvent.toEntity() = ReviewEventEntity(
    eventId, measurementId, actionType.name, authorClinicianId, authorName, note, createdAt
)

private fun ReviewEventEntity.toDomain() = ReviewEvent(
    eventId = eventId,
    measurementId = measurementId,
    actionType = ReviewActionType.entries.firstOrNull { it.name == actionType } ?: ReviewActionType.NOTE_APPENDED,
    authorClinicianId = authorClinicianId,
    authorName = authorName,
    note = note,
    createdAt = createdAt
)

private fun ConflictRecord.toEntity() = ConflictRecordEntity(
    conflictId, measurementId, incomingPayloadSha256, existingPayloadSha256, detectedAt, summary
)

private fun ConflictRecordEntity.toDomain() = ConflictRecord(
    conflictId, measurementId, incomingPayloadSha256, existingPayloadSha256, detectedAt, summary
)

private fun DeviceRecord.toEntity() = DeviceEntity(
    deviceId, serialNumber, modelName, firmwareVersion, calibrationId,
    calibrationStatus, assignmentState, assignedSubjectId, releaseStatus, lastServiceAt, needsService
)

private fun DeviceEntity.toDomain() = DeviceRecord(
    deviceId, serialNumber, modelName, firmwareVersion, calibrationId,
    calibrationStatus, assignmentState, assignedSubjectId, releaseStatus, lastServiceAt, needsService
)

private fun ServiceEvent.toEntity() = ServiceEventEntity(
    serviceEventId, deviceId, eventType, actorName, performedAt, summary, calibrationRef
)

private fun ServiceEventEntity.toDomain() = ServiceEvent(
    serviceEventId, deviceId, eventType, actorName, performedAt, summary, calibrationRef
)

private fun AppNotification.toEntity() = NotificationEntity(
    notificationId, category.name, title, body, createdAt, relatedMeasurementId, isRead
)

private fun NotificationEntity.toDomain() = AppNotification(
    notificationId = notificationId,
    category = NotificationCategory.entries.firstOrNull { it.name == category } ?: NotificationCategory.DEMO_REMINDER,
    title = title,
    body = body,
    createdAt = createdAt,
    relatedMeasurementId = relatedMeasurementId,
    isRead = isRead
)
