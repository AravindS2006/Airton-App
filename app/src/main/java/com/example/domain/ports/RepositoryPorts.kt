package com.example.domain.ports

import com.example.domain.model.AppNotification
import com.example.domain.model.CarePlan
import com.example.domain.model.ClinicProfile
import com.example.domain.model.ClinicianProfile
import com.example.domain.model.ConflictRecord
import com.example.domain.model.DeviceRecord
import com.example.domain.model.EyeSide
import com.example.domain.model.IngestOutcome
import com.example.domain.model.MeasurementEnvelope
import com.example.domain.model.PatientProfile
import com.example.domain.model.ReviewActionType
import com.example.domain.model.ReviewEvent
import com.example.domain.model.ServiceEvent
import com.example.domain.model.SimulationScenario
import com.example.domain.model.SyntheticMeasurementV1
import com.example.domain.model.TransportStatus
import kotlinx.coroutines.flow.Flow

/**
 * Clean architecture ports/adapters keeping UI decoupled from local persistence and future integrations.
 * No real hardware, BLE, auth, or cloud transport is implemented in this prototype.
 */

interface MeasurementRepository {
    fun observeAllEnvelopes(): Flow<List<MeasurementEnvelope>>
    fun observeEnvelopesForSubject(subjectId: String): Flow<List<MeasurementEnvelope>>
    fun observeEnvelope(measurementId: String): Flow<MeasurementEnvelope?>
    suspend fun ingestMeasurement(
        record: SyntheticMeasurementV1,
        initialTransportStatus: TransportStatus
    ): IngestOutcome
}

interface SessionRepository {
    fun nextDemoSessionId(): String
    fun nextDemoMeasurementId(): String
}

interface CarePlanRepository {
    fun observeAllCarePlans(): Flow<List<CarePlan>>
    fun observeCarePlanForSubject(subjectId: String): Flow<CarePlan?>
    suspend fun saveClinicianCarePlan(plan: CarePlan)
}

interface PatientRepository {
    fun observePatients(): Flow<List<PatientProfile>>
    fun observePatient(subjectId: String): Flow<PatientProfile?>
    suspend fun updatePatientAssignment(
        subjectId: String,
        assignedDeviceId: String?,
        enrolmentStatus: String,
        trainingStatus: String
    )
}

interface ClinicianRepository {
    fun observeClinicians(): Flow<List<ClinicianProfile>>
    fun observeClinic(): Flow<ClinicProfile?>
}

interface ReviewRepository {
    fun observeAllReviewEvents(): Flow<List<ReviewEvent>>
    suspend fun appendReviewEvent(
        measurementId: String,
        actionType: ReviewActionType,
        authorClinicianId: String,
        authorName: String,
        note: String?
    ): ReviewEvent
}

interface SyncRepository {
    fun observeConflicts(): Flow<List<ConflictRecord>>
    suspend fun retryPendingUploads(isOfflineSimulated: Boolean): SyncBatchResult
    suspend fun simulateDuplicateIdempotentRetry(measurementId: String): IngestOutcome
    suspend fun simulateConflictingReplay(measurementId: String): IngestOutcome
}

data class SyncBatchResult(
    val attemptedCount: Int,
    val deliveredCount: Int,
    val failedOfflineCount: Int
)

enum class HardwareConnectionState {
    NOT_CONNECTED_SIMULATION_ONLY
}

data class SimulatedAcquisitionResult(
    val scenario: SimulationScenario,
    val measurement: SyntheticMeasurementV1?,
    val statusMessage: String
)

/**
 * Future hardware BLE / USB integration seam.
 * In this prototype, ONLY a deterministic simulated adapter exists.
 */
interface DeviceGateway {
    val connectionState: HardwareConnectionState
    val isSimulatedOnly: Boolean
    fun deviceSummaryLabel(): String
    fun performSimulatedAcquisition(
        subjectId: String,
        deviceId: String,
        sessionId: String,
        measurementId: String,
        eye: EyeSide,
        scenario: SimulationScenario,
        alignmentScore: Float,
        sequenceIndex: Int
    ): SimulatedAcquisitionResult
}

interface ClinicOperationsRepository {
    fun observeDevices(): Flow<List<DeviceRecord>>
    fun observeServiceEvents(): Flow<List<ServiceEvent>>
    fun observeNotifications(): Flow<List<AppNotification>>
    suspend fun updateDeviceAssignment(
        deviceId: String,
        assignedSubjectId: String?,
        assignmentState: String,
        actorName: String
    )
    suspend fun appendServiceEvent(
        deviceId: String,
        eventType: String,
        actorName: String,
        summary: String,
        calibrationRef: String?,
        clearNeedsService: Boolean
    ): ServiceEvent
    suspend fun markNotificationRead(notificationId: String)
    suspend fun addDemoNotification(notification: AppNotification)
}

interface DemoModeController {
    suspend fun ensureSeeded()
    suspend fun resetDemoDatabase()
}
