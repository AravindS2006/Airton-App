package com.example.ui.screens.clinician

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.domain.model.ClinicalReviewStatus
import com.example.domain.model.EyeSide
import com.example.domain.model.MeasurementEnvelope
import com.example.domain.model.PatientProfile
import com.example.domain.model.QualityStatus
import com.example.domain.model.ReviewActionType
import com.example.domain.model.TransportStatus
import com.example.i18n.LocalAirTonStrings
import com.example.ui.components.AirTonCard
import com.example.ui.components.EmptyOrOfflineStateCard
import com.example.ui.components.EyeBadge
import com.example.ui.components.EyeTrendChart
import com.example.ui.components.IndependentStateBadgeRow
import com.example.ui.components.PrototypeDisclosureBanner
import com.example.ui.components.ProvenanceKeyValueRow
import com.example.ui.screens.patient.MeasurementHistoryRowCard
import com.example.ui.theme.LocalAirTonColors
import com.example.ui.theme.MonoMetadataStyle
import com.example.viewmodel.AirTonUiState
import java.util.Locale

@Composable
fun ClinicianOverviewScreen(
    state: AirTonUiState,
    onOpenQueue: () -> Unit,
    onOpenPatients: () -> Unit,
    onOpenRecordReview: (String) -> Unit
) {
    val clinician = state.activeClinician
    val queue = state.clinicianReviewQueue
    val recentDelivered = remember(state.allEnvelopes) {
        state.allEnvelopes.filter { it.transportStatus == TransportStatus.DELIVERED }.take(5)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("clinician_overview_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { PrototypeDisclosureBanner(compact = false) }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(clinician?.fullName ?: "Treating Clinician (Demo)", style = MaterialTheme.typography.headlineMedium)
                Text(
                    text = "Traceable synthetic review workspace · Not a diagnostic decision-support engine.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AirTonCard(modifier = Modifier.weight(1f), onClick = onOpenPatients) {
                    Text("Assigned Roster", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${state.patients.size}", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
                    Text("Demo patients", style = MaterialTheme.typography.bodySmall)
                }
                AirTonCard(modifier = Modifier.weight(1f), onClick = onOpenQueue) {
                    Text("Awaiting Review", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${queue.size}", style = MaterialTheme.typography.headlineMedium, color = LocalAirTonColors.current.demoBannerContent)
                    Text("Delivered only", style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        item {
            AirTonCard {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Filled.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text("Delivery & Offline Caveat", style = MaterialTheme.typography.titleSmall)
                }
                Text(
                    text = "${state.unsyncedCount} synthetic record(s) are currently local-only, pending, or failed on patient devices and are not yet delivered to this review queue. Administrative receipt is distinct from clinician review.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            Text("Recent Delivered Sessions", style = MaterialTheme.typography.titleMedium)
        }

        items(recentDelivered, key = { it.measurement.measurementId }) { env ->
            val patientName = state.patients.firstOrNull { it.subjectId == env.measurement.subjectId }?.fullName
            MeasurementHistoryRowCard(
                envelope = env,
                subjectLabel = patientName,
                onClick = { onOpenRecordReview(env.measurement.measurementId) }
            )
        }
    }
}

@Composable
fun ClinicianPatientRosterScreen(
    state: AirTonUiState,
    onOpenPatientDetail: (String) -> Unit
) {
    val strings = LocalAirTonStrings.current
    var query by remember { mutableStateOf("") }
    var filterUnconfiguredOnly by remember { mutableStateOf(false) }

    val filteredPatients = remember(state.patients, state.carePlans, query, filterUnconfiguredOnly) {
        state.patients.filter { p ->
            val plan = state.carePlans.firstOrNull { it.subjectId == p.subjectId }
            val matchesQuery = query.isBlank() ||
                p.fullName.contains(query, ignoreCase = true) ||
                p.subjectId.contains(query, ignoreCase = true)
            val matchesPlan = !filterUnconfiguredOnly || plan?.isScheduleConfigured != true
            matchesQuery && matchesPlan
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("clinician_roster_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { PrototypeDisclosureBanner(compact = true) }

        item {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("roster_search_input"),
                label = { Text("Search demo patients by name or ID") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                singleLine = true
            )
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = !filterUnconfiguredOnly,
                    onClick = { filterUnconfiguredOnly = false },
                    label = { Text("All (${state.patients.size})") }
                )
                FilterChip(
                    selected = filterUnconfiguredOnly,
                    onClick = { filterUnconfiguredOnly = true },
                    label = { Text("Plan Not Configured") }
                )
            }
        }

        if (filteredPatients.isEmpty()) {
            item {
                EmptyOrOfflineStateCard(
                    icon = Icons.Filled.People,
                    title = "No matching demo patients",
                    description = "Adjust your search query or clear the plan filter."
                )
            }
        } else {
            items(filteredPatients, key = { it.subjectId }) { patient ->
                val plan = state.carePlans.firstOrNull { it.subjectId == patient.subjectId }
                val patientRecords = state.allEnvelopes.filter { it.measurement.subjectId == patient.subjectId }
                val awaiting = patientRecords.count {
                    it.transportStatus == TransportStatus.DELIVERED &&
                        it.reviewStatus == ClinicalReviewStatus.AWAITING_REVIEW
                }
                AirTonCard(
                    modifier = Modifier.testTag("roster_patient_${patient.subjectId}"),
                    onClick = { onOpenPatientDetail(patient.subjectId) }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(patient.fullName, style = MaterialTheme.typography.titleMedium)
                            Text(patient.subjectId, style = MonoMetadataStyle, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        if (awaiting > 0) {
                            Text(
                                text = "$awaiting to review",
                                style = MaterialTheme.typography.labelMedium,
                                color = LocalAirTonColors.current.demoBannerContent,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    ProvenanceKeyValueRow(
                        "Care Plan",
                        plan?.scheduleSummary ?: strings.notConfigured
                    )
                    ProvenanceKeyValueRow(
                        "Synthetic Records",
                        "${patientRecords.size} total (${patientRecords.count { it.measurement.qualityStatus == QualityStatus.REJECT }} rejected)"
                    )
                }
            }
        }
    }
}

@Composable
fun ClinicianPatientDetailScreen(
    patient: PatientProfile?,
    state: AirTonUiState,
    onEditCarePlan: (String) -> Unit,
    onOpenRecordReview: (String) -> Unit
) {
    val strings = LocalAirTonStrings.current
    if (patient == null) {
        EmptyOrOfflineStateCard(
            icon = Icons.Filled.People,
            title = "Patient not found",
            description = "Unable to locate this demo subject."
        )
        return
    }
    val plan = state.carePlans.firstOrNull { it.subjectId == patient.subjectId }
    val envelopes = state.allEnvelopes.filter { it.measurement.subjectId == patient.subjectId }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("clinician_patient_detail"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { PrototypeDisclosureBanner(compact = true) }

        item {
            AirTonCard {
                Text(patient.fullName, style = MaterialTheme.typography.headlineSmall)
                ProvenanceKeyValueRow("Subject ID", patient.subjectId, mono = true)
                ProvenanceKeyValueRow("Assigned Device", patient.assignedDeviceId ?: strings.notAvailable, mono = true)
                ProvenanceKeyValueRow("Care Plan Schedule", plan?.scheduleSummary ?: strings.notConfigured)
                ProvenanceKeyValueRow("Retry Policy", plan?.maxRetriesPerEye?.toString() ?: strings.notConfigured)
                OutlinedButton(
                    onClick = { onEditCarePlan(patient.subjectId) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_care_plan_button")
                ) {
                    Icon(Icons.Filled.EditNote, contentDescription = null)
                    Spacer(Modifier.size(8.dp))
                    Text("Edit Illustrative Care Plan")
                }
            }
        }

        item {
            AirTonCard {
                Text("Separate OD / OS History", style = MaterialTheme.typography.titleMedium)
                EyeTrendChart(
                    envelopes = envelopes,
                    compact = false,
                    onSelectMeasurement = onOpenRecordReview
                )
            }
        }

        item {
            Text("All Acquisitions Including Failures (${envelopes.size})", style = MaterialTheme.typography.titleMedium)
        }

        if (envelopes.isEmpty()) {
            item {
                EmptyOrOfflineStateCard(
                    icon = Icons.Filled.Info,
                    title = "No acquisitions recorded",
                    description = "This demo patient has not recorded any simulated sessions yet."
                )
            }
        } else {
            items(envelopes, key = { it.measurement.measurementId }) { env ->
                MeasurementHistoryRowCard(
                    envelope = env,
                    onClick = { onOpenRecordReview(env.measurement.measurementId) }
                )
            }
        }
    }
}

@Composable
fun ClinicianReviewQueueScreen(
    state: AirTonUiState,
    onOpenRecordReview: (String) -> Unit
) {
    val queue = state.clinicianReviewQueue

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("clinician_review_queue_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { PrototypeDisclosureBanner(compact = true) }
        item {
            Text(
                text = "Showing strictly Delivered records whose clinical review state is Awaiting Review (${queue.size}). Local-only and failed records do not appear here.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (queue.isEmpty()) {
            item {
                EmptyOrOfflineStateCard(
                    icon = Icons.Filled.CheckCircle,
                    title = "Review queue clear",
                    description = "All currently delivered synthetic records have a logged clinician review event."
                )
            }
        } else {
            items(queue, key = { it.measurement.measurementId }) { env ->
                val name = state.patients.firstOrNull { it.subjectId == env.measurement.subjectId }?.fullName
                MeasurementHistoryRowCard(
                    envelope = env,
                    subjectLabel = name,
                    onClick = { onOpenRecordReview(env.measurement.measurementId) }
                )
            }
        }
    }
}

@Composable
fun ClinicianRecordReviewScreen(
    envelope: MeasurementEnvelope?,
    state: AirTonUiState,
    onSubmitAction: (String, ReviewActionType, String?) -> Unit
) {
    val semantic = LocalAirTonColors.current
    if (envelope == null) {
        EmptyOrOfflineStateCard(
            icon = Icons.Filled.Info,
            title = "Record unavailable",
            description = "Could not load this record for clinician review."
        )
        return
    }

    val m = envelope.measurement
    val patientName = state.patients.firstOrNull { it.subjectId == m.subjectId }?.fullName
    val clinicianName = state.activeClinician?.fullName ?: "Demo Clinician"
    var noteDraft by remember(m.measurementId) { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("clinician_record_review_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { PrototypeDisclosureBanner(compact = false) }

        item {
            AirTonCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(patientName ?: m.subjectId, style = MaterialTheme.typography.titleLarge)
                        Text(m.capturedAt, style = MonoMetadataStyle, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    EyeBadge(m.eye)
                }
                Text(
                    text = if (m.qualityStatus == QualityStatus.PASS && m.iopEstimateMmhg != null) {
                        String.format(Locale.US, "%.1f mmHg (Synthetic demo estimate)", m.iopEstimateMmhg)
                    } else {
                        "Rejected acquisition (iop_estimate_mmhg: null) — ${m.qualityReasons.joinToString(", ") { it.labelEn }}"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    color = if (m.qualityStatus == QualityStatus.PASS) MaterialTheme.colorScheme.primary else semantic.statusErrorContent
                )
                IndependentStateBadgeRow(envelope)
                HorizontalDivider()
                ProvenanceKeyValueRow("measurement_id", m.measurementId, mono = true)
                ProvenanceKeyValueRow("device_id / fw", "${m.deviceId} (${m.firmwareVersion})", mono = true)
                ProvenanceKeyValueRow("calibration_id", m.calibrationId, mono = true)
                ProvenanceKeyValueRow("clock_status", m.clockStatus.wireValue, mono = true)
                ProvenanceKeyValueRow("raw_sha256", m.rawSha256.take(28) + "…", mono = true)
            }
        }

        // Append-Only Clinician Review Controls
        item {
            AirTonCard {
                Text("Append-Only Review Actions", style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "Acting as $clinicianName. Every action appends a new timestamped record and never overwrites prior observations or review events.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = noteDraft,
                    onValueChange = { noteDraft = it },
                    label = { Text("Optional clinician demo note / amendment") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("clinician_note_input"),
                    minLines = 2
                )

                Button(
                    onClick = {
                        onSubmitAction(m.measurementId, ReviewActionType.ACKNOWLEDGED_REVIEW, noteDraft.ifBlank { null })
                        noteDraft = ""
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("acknowledge_review_button")
                ) {
                    Icon(Icons.Filled.CheckCircle, contentDescription = null)
                    Spacer(Modifier.size(8.dp))
                    Text("Acknowledge Reviewed")
                }

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = {
                            onSubmitAction(
                                m.measurementId,
                                ReviewActionType.FOLLOW_UP_REQUESTED,
                                noteDraft.ifBlank { "Follow-up or reacquisition requested in demo." }
                            )
                            noteDraft = ""
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("request_followup_button")
                    ) {
                        Icon(Icons.Filled.Flag, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.size(4.dp))
                        Text("Follow-Up")
                    }
                    OutlinedButton(
                        onClick = {
                            if (noteDraft.isNotBlank()) {
                                onSubmitAction(m.measurementId, ReviewActionType.NOTE_APPENDED, noteDraft)
                                noteDraft = ""
                            }
                        },
                        enabled = noteDraft.isNotBlank(),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("append_note_button")
                    ) {
                        Icon(Icons.Filled.EditNote, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.size(4.dp))
                        Text("Append Note")
                    }
                }
            }
        }

        // Immutable Audit Trail of Review Events
        item {
            AirTonCard {
                Text("Append-Only Review History (${envelope.reviewEvents.size})", style = MaterialTheme.typography.titleMedium)
                if (envelope.reviewEvents.isEmpty()) {
                    Text(
                        text = "No review events recorded yet.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    envelope.reviewEvents.forEach { ev ->
                        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text(ev.actionType.labelEn, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                            Text("${ev.authorName} · ${ev.createdAt}", style = MonoMetadataStyle, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            ev.note?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                        }
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

@Composable
fun ClinicianCarePlanEditorScreen(
    initialSubjectId: String?,
    state: AirTonUiState,
    onSavePlan: (
        subjectId: String,
        eyes: List<EyeSide>,
        scheduleSummary: String?,
        repetitions: Int?,
        maxRetries: Int?,
        instructions: String?
    ) -> Unit
) {
    val strings = LocalAirTonStrings.current
    var selectedSubjectId by remember(initialSubjectId) {
        mutableStateOf(initialSubjectId ?: state.patients.firstOrNull()?.subjectId.orEmpty())
    }
    val existingPlan = state.carePlans.firstOrNull { it.subjectId == selectedSubjectId }

    var includeOd by remember(selectedSubjectId, existingPlan) {
        mutableStateOf(existingPlan?.eyesPrescribed?.contains(EyeSide.OD) == true)
    }
    var includeOs by remember(selectedSubjectId, existingPlan) {
        mutableStateOf(existingPlan?.eyesPrescribed?.contains(EyeSide.OS) == true)
    }
    var scheduleText by remember(selectedSubjectId, existingPlan) {
        mutableStateOf(existingPlan?.scheduleSummary.orEmpty())
    }
    var repetitionsText by remember(selectedSubjectId, existingPlan) {
        mutableStateOf(existingPlan?.repetitionsPerSession?.toString().orEmpty())
    }
    var maxRetriesText by remember(selectedSubjectId, existingPlan) {
        mutableStateOf(existingPlan?.maxRetriesPerEye?.toString().orEmpty())
    }
    var instructionsText by remember(selectedSubjectId, existingPlan) {
        mutableStateOf(existingPlan?.clinicianInstructions.orEmpty())
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("clinician_care_plan_editor"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { PrototypeDisclosureBanner(compact = false) }

        item {
            AirTonCard {
                Text("Illustrative Care-Plan Editor (Clinician Only)", style = MaterialTheme.typography.titleLarge)
                Text(
                    text = "Leave any field empty to preserve 'Not configured'. This demo editor does not transmit real instructions or configure physical hardware.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text("Select Demo Patient", style = MaterialTheme.typography.titleSmall)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    state.patients.forEach { p ->
                        FilterChip(
                            selected = p.subjectId == selectedSubjectId,
                            onClick = { selectedSubjectId = p.subjectId },
                            label = { Text(p.fullName.substringBefore(" ")) }
                        )
                    }
                }

                Text("Prescribed Eyes", style = MaterialTheme.typography.titleSmall)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    FilterChip(
                        selected = includeOd,
                        onClick = { includeOd = !includeOd },
                        label = { Text("Right Eye (OD)") }
                    )
                    FilterChip(
                        selected = includeOs,
                        onClick = { includeOs = !includeOs },
                        label = { Text("Left Eye (OS)") }
                    )
                }

                OutlinedTextField(
                    value = scheduleText,
                    onValueChange = { scheduleText = it },
                    label = { Text("Schedule summary (blank = Not configured)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = repetitionsText,
                        onValueChange = { repetitionsText = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Repetitions") },
                        placeholder = { Text(strings.notConfigured) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = maxRetriesText,
                        onValueChange = { maxRetriesText = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Max Retries") },
                        placeholder = { Text(strings.notConfigured) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = instructionsText,
                    onValueChange = { instructionsText = it },
                    label = { Text("Patient-visible read-only demo instructions") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                HorizontalDivider()
                Text("Safety Limits Preserved as Unconfigured", style = MaterialTheme.typography.titleSmall)
                ProvenanceKeyValueRow("Clinical Alert Thresholds", strings.notConfigured)
                ProvenanceKeyValueRow("Calibration Interval", strings.notConfigured)
                ProvenanceKeyValueRow("Escalation Policy", strings.notConfigured)

                Button(
                    onClick = {
                        val eyes = buildList {
                            if (includeOd) add(EyeSide.OD)
                            if (includeOs) add(EyeSide.OS)
                        }
                        onSavePlan(
                            selectedSubjectId,
                            eyes,
                            scheduleText.ifBlank { null },
                            repetitionsText.toIntOrNull(),
                            maxRetriesText.toIntOrNull(),
                            instructionsText.ifBlank { null }
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("save_care_plan_button")
                ) {
                    Text("Save Illustrative Demo Plan")
                }
            }
        }
    }
}
