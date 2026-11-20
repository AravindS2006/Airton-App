package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.persistence.AirTonDatabase
import com.example.data.preferences.AppPreferencesState
import com.example.data.preferences.AppPreferencesStore
import com.example.data.repositories.RoomAirTonStore
import com.example.domain.model.AppLanguage
import com.example.domain.model.AppNotification
import com.example.domain.model.CarePlan
import com.example.domain.model.ClinicProfile
import com.example.domain.model.ClinicalReviewStatus
import com.example.domain.model.ClinicianProfile
import com.example.domain.model.ConflictRecord
import com.example.domain.model.DemoRole
import com.example.domain.model.DeviceRecord
import com.example.domain.model.EyeSide
import com.example.domain.model.IngestOutcome
import com.example.domain.model.MeasurementEnvelope
import com.example.domain.model.PatientProfile
import com.example.domain.model.QualityStatus
import com.example.domain.model.ReviewActionType
import com.example.domain.model.ServiceEvent
import com.example.domain.model.SimulationScenario
import com.example.domain.model.SyntheticMeasurementV1
import com.example.domain.model.TextScalePreset
import com.example.domain.model.ThemePreference
import com.example.domain.model.TransportStatus
import com.example.domain.ports.DeviceGateway
import com.example.services.SimulatedDeviceGateway
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.hypot

enum class GuidedSessionStep(val stepNumber: Int, val label: String) {
    DISCLOSURE(1, "Disclosure"),
    SELECT_EYE(2, "Select Eye"),
    READINESS(3, "Readiness"),
    ALIGNMENT(4, "Positioning"),
    ACQUIRING(5, "Acquiring"),
    QUALITY_RESULT(6, "Result"),
    SAVED_SUMMARY(7, "Saved")
}

data class GuidedSessionUiState(
    val step: GuidedSessionStep = GuidedSessionStep.DISCLOSURE,
    val selectedEye: EyeSide = EyeSide.OD,
    val selectedScenario: SimulationScenario = SimulationScenario.READY_ACCEPTED,
    val reticleOffsetX: Float = 0.28f, // Normalized -1f..1f from optical center
    val reticleOffsetY: Float = -0.22f,
    val isAcquiring: Boolean = false,
    val latestSimulatedRecord: SyntheticMeasurementV1? = null,
    val latestOutcomeMessage: String? = null,
    val latestSavedMeasurementId: String? = null,
    val latestSavedTransportStatus: TransportStatus? = null,
    val attemptCountInSession: Int = 0
) {
    val alignmentDistance: Float
        get() = hypot(reticleOffsetX, reticleOffsetY).coerceAtMost(1f)

    val alignmentScore: Float
        get() = (1f - alignmentDistance).coerceIn(0f, 1f)

    val isWithinTargetZone: Boolean
        get() = alignmentDistance <= 0.22f
}

data class HistoryFilterState(
    val eyeFilter: EyeSide? = null, // null = both (shown separately)
    val qualityFilter: QualityStatus? = null,
    val transportFilter: TransportStatus? = null,
    val reviewFilter: ClinicalReviewStatus? = null,
    val dateRangeDays: Int? = null // null = all dates, 3 or 7
)

data class AirTonUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val preferences: AppPreferencesState = AppPreferencesState(),
    val clinic: ClinicProfile? = null,
    val clinicians: List<ClinicianProfile> = emptyList(),
    val patients: List<PatientProfile> = emptyList(),
    val carePlans: List<CarePlan> = emptyList(),
    val allEnvelopes: List<MeasurementEnvelope> = emptyList(),
    val devices: List<DeviceRecord> = emptyList(),
    val serviceEvents: List<ServiceEvent> = emptyList(),
    val conflicts: List<ConflictRecord> = emptyList(),
    val notifications: List<AppNotification> = emptyList(),
    val historyFilter: HistoryFilterState = HistoryFilterState(),
    val guidedSession: GuidedSessionUiState = GuidedSessionUiState(),
    val hardwareBannerLabel: String = "No AirTon device connected (Software simulation adapter active)"
) {
    val activePatient: PatientProfile?
        get() = patients.firstOrNull { it.subjectId == preferences.activePatientId } ?: patients.firstOrNull()

    val activeClinician: ClinicianProfile?
        get() = clinicians.firstOrNull { it.clinicianId == preferences.activeClinicianId } ?: clinicians.firstOrNull()

    val activePatientCarePlan: CarePlan?
        get() = activePatient?.let { p -> carePlans.firstOrNull { it.subjectId == p.subjectId } }

    val activePatientEnvelopes: List<MeasurementEnvelope>
        get() = activePatient?.let { p ->
            allEnvelopes.filter { it.measurement.subjectId == p.subjectId }
        } ?: emptyList()

    val unreadNotificationCount: Int
        get() = notifications.count { !it.isRead }

    val clinicianReviewQueue: List<MeasurementEnvelope>
        get() = allEnvelopes.filter {
            it.transportStatus == TransportStatus.DELIVERED &&
                it.reviewStatus == ClinicalReviewStatus.AWAITING_REVIEW
        }

    val unsyncedCount: Int
        get() = allEnvelopes.count {
            it.transportStatus == TransportStatus.LOCAL_ONLY ||
                it.transportStatus == TransportStatus.PENDING ||
                it.transportStatus == TransportStatus.FAILED
        }
}

class AirTonViewModel(
    application: Application,
    private val store: RoomAirTonStore,
    private val prefsStore: AppPreferencesStore,
    private val deviceGateway: DeviceGateway
) : AndroidViewModel(application) {

    constructor(application: Application) : this(
        application = application,
        store = RoomAirTonStore(AirTonDatabase.getInstance(application)),
        prefsStore = AppPreferencesStore(application),
        deviceGateway = SimulatedDeviceGateway()
    )

    private val _historyFilter = MutableStateFlow(HistoryFilterState())
    private val _guidedSession = MutableStateFlow(GuidedSessionUiState())
    private val _isLoading = MutableStateFlow(true)
    private val _errorMessage = MutableStateFlow<String?>(null)
    private val _snackbarEvents = MutableSharedFlow<String>(extraBufferCapacity = 8)
    val snackbarEvents: SharedFlow<String> = _snackbarEvents.asSharedFlow()

    private var simSequence = 1

    private val coreDataFlow = combine(
        store.observeClinic(),
        store.observeClinicians(),
        store.observePatients(),
        store.observeAllCarePlans(),
        store.observeAllEnvelopes()
    ) { clinic, clinicians, patients, plans, envelopes ->
        CoreBundle(clinic, clinicians, patients, plans, envelopes)
    }

    private val opsDataFlow = combine(
        store.observeDevices(),
        store.observeServiceEvents(),
        store.observeConflicts(),
        store.observeNotifications()
    ) { devices, serviceEvents, conflicts, notifications ->
        OpsBundle(devices, serviceEvents, conflicts, notifications)
    }

    val uiState: StateFlow<AirTonUiState> = combine(
        prefsStore.preferencesFlow,
        coreDataFlow,
        opsDataFlow,
        _historyFilter,
        _guidedSession
    ) { prefs, core, ops, filter, guided ->
        AirTonUiState(
            isLoading = _isLoading.value && core.patients.isEmpty(),
            errorMessage = _errorMessage.value,
            preferences = prefs,
            clinic = core.clinic,
            clinicians = core.clinicians,
            patients = core.patients,
            carePlans = core.plans,
            allEnvelopes = core.envelopes,
            devices = ops.devices,
            serviceEvents = ops.serviceEvents,
            conflicts = ops.conflicts,
            notifications = ops.notifications,
            historyFilter = filter,
            guidedSession = guided,
            hardwareBannerLabel = deviceGateway.deviceSummaryLabel()
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AirTonUiState()
    )

    init {
        viewModelScope.launch {
            try {
                store.ensureSeeded()
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Local demo database error: ${e.message ?: "Unable to initialize fixtures."}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // --- Onboarding & Preferences ---
    fun completeDisclosureAndEnterRole(role: DemoRole) {
        viewModelScope.launch {
            prefsStore.acknowledgeDisclosureAndSelectRole(role)
        }
    }

    fun switchDemoRole(role: DemoRole) {
        viewModelScope.launch {
            prefsStore.setSelectedRole(role)
            _snackbarEvents.tryEmit("Switched demo persona to ${role.name.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() }} (preview only).")
        }
    }

    fun selectActivePatient(subjectId: String) {
        viewModelScope.launch { prefsStore.setActivePatientId(subjectId) }
    }

    fun selectActiveClinician(clinicianId: String) {
        viewModelScope.launch { prefsStore.setActiveClinicianId(clinicianId) }
    }

    fun setLanguage(language: AppLanguage) {
        viewModelScope.launch { prefsStore.setLanguage(language) }
    }

    fun setThemePreference(theme: ThemePreference) {
        viewModelScope.launch { prefsStore.setThemePreference(theme) }
    }

    fun setTextScalePreset(preset: TextScalePreset) {
        viewModelScope.launch { prefsStore.setTextScalePreset(preset) }
    }

    fun setSimulatedOfflineMode(offline: Boolean) {
        viewModelScope.launch {
            prefsStore.setSimulatedOfflineMode(offline)
            _snackbarEvents.tryEmit(
                if (offline) "Simulated offline mode enabled. New records stay local-only."
                else "Simulated network restored. You may retry pending uploads."
            )
        }
    }

    fun setLocalRemindersEnabled(enabled: Boolean) {
        viewModelScope.launch { prefsStore.setLocalRemindersEnabled(enabled) }
    }

    fun setReducedMotionEnabled(enabled: Boolean) {
        viewModelScope.launch { prefsStore.setReducedMotionEnabled(enabled) }
    }

    fun resetDemoData() {
        viewModelScope.launch {
            try {
                store.resetDemoDatabase()
                prefsStore.resetOperationalPreferences()
                _historyFilter.value = HistoryFilterState()
                _guidedSession.value = GuidedSessionUiState()
                _errorMessage.value = null
                _snackbarEvents.tryEmit("Local demo database restored to deterministic baseline.")
            } catch (e: Exception) {
                _errorMessage.value = "Unable to reset local demo data: ${e.message}"
            }
        }
    }

    // --- Guided Simulated Measurement State Machine ---
    fun resetGuidedSession(initialEye: EyeSide = EyeSide.OD) {
        _guidedSession.value = GuidedSessionUiState(selectedEye = initialEye)
    }

    fun setGuidedStep(step: GuidedSessionStep) {
        _guidedSession.update { it.copy(step = step) }
    }

    fun selectGuidedEye(eye: EyeSide) {
        _guidedSession.update { it.copy(selectedEye = eye) }
    }

    fun selectSimulationScenario(scenario: SimulationScenario) {
        _guidedSession.update { it.copy(selectedScenario = scenario) }
    }

    fun updateAlignmentReticle(offsetX: Float, offsetY: Float) {
        _guidedSession.update {
            it.copy(
                reticleOffsetX = offsetX.coerceIn(-0.85f, 0.85f),
                reticleOffsetY = offsetY.coerceIn(-0.85f, 0.85f)
            )
        }
    }

    fun snapReticleToCenter() {
        _guidedSession.update { it.copy(reticleOffsetX = 0.02f, reticleOffsetY = -0.02f) }
    }

    fun runSimulatedAcquisition() {
        val current = _guidedSession.value
        val state = uiState.value
        val patient = state.activePatient ?: return
        val deviceId = patient.assignedDeviceId ?: "demo-dev-sim"
        val isOffline = state.preferences.simulatedOfflineMode

        viewModelScope.launch {
            _guidedSession.update {
                it.copy(
                    step = GuidedSessionStep.ACQUIRING,
                    isAcquiring = true,
                    latestSimulatedRecord = null,
                    latestOutcomeMessage = null
                )
            }
            val waitMs = if (state.preferences.reducedMotionEnabled) 250L else 850L
            delay(waitMs)

            val seq = simSequence++
            val sessionId = store.nextDemoSessionId()
            val measurementId = store.nextDemoMeasurementId()

            val result = deviceGateway.performSimulatedAcquisition(
                subjectId = patient.subjectId,
                deviceId = deviceId,
                sessionId = sessionId,
                measurementId = measurementId,
                eye = current.selectedEye,
                scenario = current.selectedScenario,
                alignmentScore = current.alignmentScore,
                sequenceIndex = seq
            )

            var savedId: String? = null
            var savedTransport: TransportStatus? = null

            if (result.measurement != null) {
                val initialTransport = if (isOffline) TransportStatus.LOCAL_ONLY else TransportStatus.PENDING
                when (val ingest = store.ingestMeasurement(result.measurement, initialTransport)) {
                    is IngestOutcome.Inserted -> {
                        savedId = ingest.measurementId
                        savedTransport = initialTransport
                    }
                    is IngestOutcome.ValidationRejected -> {
                        _errorMessage.value = ingest.errors.joinToString("; ")
                    }
                    else -> Unit
                }
            }

            _guidedSession.update {
                it.copy(
                    step = GuidedSessionStep.QUALITY_RESULT,
                    isAcquiring = false,
                    latestSimulatedRecord = result.measurement,
                    latestOutcomeMessage = result.statusMessage,
                    latestSavedMeasurementId = savedId,
                    latestSavedTransportStatus = savedTransport,
                    attemptCountInSession = it.attemptCountInSession + 1
                )
            }
        }
    }

    // --- History Filtering ---
    fun updateHistoryFilter(transform: (HistoryFilterState) -> HistoryFilterState) {
        _historyFilter.update(transform)
    }

    fun clearHistoryFilters() {
        _historyFilter.value = HistoryFilterState()
    }

    // --- Sync & Conflict Sandbox ---
    fun retryPendingSync() {
        val offline = uiState.value.preferences.simulatedOfflineMode
        viewModelScope.launch {
            val res = store.retryPendingUploads(isOfflineSimulated = offline)
            val msg = when {
                res.attemptedCount == 0 -> "No unsynced demo records waiting."
                offline -> "Offline mode active: ${res.failedOfflineCount} record(s) marked failed while preserving immutable IDs."
                else -> "${res.deliveredCount} record(s) marked delivered in simulation. Still awaiting clinician review."
            }
            _snackbarEvents.tryEmit(msg)
        }
    }

    fun triggerDuplicateRetryDemo(measurementId: String) {
        viewModelScope.launch {
            when (val outcome = store.simulateDuplicateIdempotentRetry(measurementId)) {
                is IngestOutcome.DuplicateIdempotent ->
                    _snackbarEvents.tryEmit("Idempotent duplicate confirmed for ${outcome.measurementId}: no duplicate row created.")
                else ->
                    _snackbarEvents.tryEmit("Duplicate retry check completed for $measurementId.")
            }
        }
    }

    fun triggerConflictingReplayDemo(measurementId: String) {
        viewModelScope.launch {
            when (val outcome = store.simulateConflictingReplay(measurementId)) {
                is IngestOutcome.ConflictQuarantined ->
                    _snackbarEvents.tryEmit("Conflicting replay quarantined (${outcome.conflict.conflictId}). Original record unchanged.")
                else ->
                    _snackbarEvents.tryEmit("Conflict replay simulation finished for $measurementId.")
            }
        }
    }

    // --- Clinician Review Actions (Append-Only) ---
    fun submitClinicianReviewAction(
        measurementId: String,
        actionType: ReviewActionType,
        note: String?
    ) {
        val clinician = uiState.value.activeClinician ?: return
        viewModelScope.launch {
            store.appendReviewEvent(
                measurementId = measurementId,
                actionType = actionType,
                authorClinicianId = clinician.clinicianId,
                authorName = clinician.fullName,
                note = note
            )
            _snackbarEvents.tryEmit("${actionType.labelEn} recorded by ${clinician.fullName}.")
        }
    }

    // --- Clinician Care-Plan Editor ---
    fun saveClinicianCarePlan(
        subjectId: String,
        eyesPrescribed: List<EyeSide>,
        scheduleSummary: String?,
        repetitionsPerSession: Int?,
        maxRetriesPerEye: Int?,
        clinicianInstructions: String?
    ) {
        val clinician = uiState.value.activeClinician ?: return
        val existing = uiState.value.carePlans.firstOrNull { it.subjectId == subjectId }
        val updated = CarePlan(
            planId = existing?.planId ?: "demo-plan-$subjectId",
            subjectId = subjectId,
            authorClinicianId = clinician.clinicianId,
            authorName = clinician.fullName,
            updatedAt = existing?.updatedAt,
            eyesPrescribed = eyesPrescribed,
            scheduleSummary = scheduleSummary?.trim()?.takeIf { it.isNotEmpty() },
            repetitionsPerSession = repetitionsPerSession,
            maxRetriesPerEye = maxRetriesPerEye,
            // Clinical alert thresholds, calibration intervals, and escalation remain strictly null unless explicitly configured
            clinicalAlertThresholdNote = existing?.clinicalAlertThresholdNote,
            calibrationIntervalNote = existing?.calibrationIntervalNote,
            escalationPolicyNote = existing?.escalationPolicyNote,
            clinicianInstructions = clinicianInstructions?.trim()?.takeIf { it.isNotEmpty() }
        )
        viewModelScope.launch {
            store.saveClinicianCarePlan(updated)
            _snackbarEvents.tryEmit("Illustrative demo care plan saved by ${clinician.fullName}.")
        }
    }

    // --- Clinic / Service Operations ---
    fun assignDeviceToPatient(deviceId: String, subjectId: String?, actorName: String = "R. Anitha (Clinic Admin · Demo)") {
        viewModelScope.launch {
            val state = uiState.value
            val currentDevice = state.devices.firstOrNull { it.deviceId == deviceId } ?: return@launch
            val previousOwner = currentDevice.assignedSubjectId

            // Detach previous owner if reassigned
            if (previousOwner != null && previousOwner != subjectId) {
                val prevPatient = state.patients.firstOrNull { it.subjectId == previousOwner }
                if (prevPatient != null) {
                    store.updatePatientAssignment(
                        subjectId = prevPatient.subjectId,
                        assignedDeviceId = null,
                        enrolmentStatus = "Device returned (Demo)",
                        trainingStatus = prevPatient.trainingStatus
                    )
                }
            }

            if (subjectId != null) {
                val target = state.patients.firstOrNull { it.subjectId == subjectId }
                if (target != null) {
                    store.updatePatientAssignment(
                        subjectId = target.subjectId,
                        assignedDeviceId = deviceId,
                        enrolmentStatus = "Enrolled (Demo)",
                        trainingStatus = "Home orientation completed (Demo)"
                    )
                }
                store.updateDeviceAssignment(
                    deviceId = deviceId,
                    assignedSubjectId = subjectId,
                    assignmentState = "Assigned to patient",
                    actorName = actorName
                )
                _snackbarEvents.tryEmit("Simulated assignment updated: $deviceId → $subjectId.")
            } else {
                store.updateDeviceAssignment(
                    deviceId = deviceId,
                    assignedSubjectId = null,
                    assignmentState = "Unassigned spare",
                    actorName = actorName
                )
                _snackbarEvents.tryEmit("Simulated device $deviceId returned to clinic pool.")
            }
        }
    }

    fun logDeviceServiceEvent(
        deviceId: String,
        eventType: String,
        summary: String,
        calibrationRef: String?,
        clearNeedsService: Boolean
    ) {
        viewModelScope.launch {
            store.appendServiceEvent(
                deviceId = deviceId,
                eventType = eventType,
                actorName = "S. Pradeep (Biomedical Tech · Demo)",
                summary = summary,
                calibrationRef = calibrationRef,
                clearNeedsService = clearNeedsService
            )
            _snackbarEvents.tryEmit("Append-only service event logged for $deviceId.")
        }
    }

    fun markNotificationRead(notificationId: String) {
        viewModelScope.launch { store.markNotificationRead(notificationId) }
    }

    fun dismissError() {
        _errorMessage.value = null
    }

    private data class CoreBundle(
        val clinic: ClinicProfile?,
        val clinicians: List<ClinicianProfile>,
        val patients: List<PatientProfile>,
        val plans: List<CarePlan>,
        val envelopes: List<MeasurementEnvelope>
    )

    private data class OpsBundle(
        val devices: List<DeviceRecord>,
        val serviceEvents: List<ServiceEvent>,
        val conflicts: List<ConflictRecord>,
        val notifications: List<AppNotification>
    )
}
