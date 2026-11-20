package com.example.ui.screens.patient

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.ContactPhone
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
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
import androidx.compose.ui.unit.dp
import com.example.domain.model.ClinicalReviewStatus
import com.example.domain.model.TransportStatus
import com.example.i18n.LocalAirTonStrings
import com.example.ui.components.AirTonCard
import com.example.ui.components.PrototypeDisclosureBanner
import com.example.ui.components.ProvenanceKeyValueRow
import com.example.ui.components.SimulatedContactBottomSheet
import com.example.ui.theme.LocalAirTonColors
import com.example.ui.theme.MonoMetadataStyle
import com.example.viewmodel.AirTonUiState

@Composable
fun PatientCarePlanScreen(
    state: AirTonUiState
) {
    val strings = LocalAirTonStrings.current
    val patient = state.activePatient
    val carePlan = state.activePatientCarePlan
    val clinician = state.clinicians.firstOrNull { it.clinicianId == patient?.assignedClinicianId }
    val clinic = state.clinic
    var showContactSheet by remember { mutableStateOf(false) }

    if (showContactSheet) {
        SimulatedContactBottomSheet(
            clinic = clinic,
            clinician = clinician,
            onDismiss = { showContactSheet = false }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("patient_care_plan_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { PrototypeDisclosureBanner(compact = false) }

        item {
            AirTonCard {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(Icons.Filled.LocalHospital, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Column {
                        Text("Assigned Demo Care Team", style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = "Fictional clinic fixture · Read-only",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                HorizontalDivider()
                ProvenanceKeyValueRow("Treating Clinician", clinician?.fullName ?: strings.notAvailable)
                ProvenanceKeyValueRow("Specialty", clinician?.roleTitle ?: strings.notAvailable)
                ProvenanceKeyValueRow("Clinic", clinic?.name ?: strings.notAvailable)
                ProvenanceKeyValueRow("Demo Hours", clinic?.demoHoursLabel ?: strings.notAvailable)

                OutlinedButton(
                    onClick = { showContactSheet = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("open_simulated_contact_sheet")
                ) {
                    Icon(Icons.Filled.ContactPhone, contentDescription = null)
                    Spacer(Modifier.size(8.dp))
                    Text("View Simulated Contact Sheet")
                }
            }
        }

        item {
            AirTonCard {
                Text("Clinician-Authored Monitoring Plan (Read-Only)", style = MaterialTheme.typography.titleMedium)
                if (carePlan == null || !carePlan.isScheduleConfigured) {
                    Text(
                        text = "Your monitoring plan has not been configured in this demo. Unset schedule, repetitions, retry limits, and thresholds remain blank rather than invented.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    ProvenanceKeyValueRow("Author", carePlan.authorName ?: strings.notAvailable)
                    ProvenanceKeyValueRow("Updated At", carePlan.updatedAt ?: strings.notAvailable, mono = true)
                    ProvenanceKeyValueRow(
                        "Prescribed Eyes",
                        carePlan.eyesPrescribed.joinToString(" & ") { it.labelEn }.ifEmpty { strings.notConfigured }
                    )
                    ProvenanceKeyValueRow("Schedule", carePlan.scheduleSummary ?: strings.notConfigured)
                }
                HorizontalDivider()
                Text("Unresolved Protocol Parameters", style = MaterialTheme.typography.titleSmall)
                ProvenanceKeyValueRow("Repetitions / Session", carePlan?.repetitionsPerSession?.toString() ?: strings.notConfigured)
                ProvenanceKeyValueRow("Max Retries / Eye", carePlan?.maxRetriesPerEye?.toString() ?: strings.notConfigured)
                ProvenanceKeyValueRow("Clinical Alert Thresholds", carePlan?.clinicalAlertThresholdNote ?: strings.notConfigured)
                ProvenanceKeyValueRow("Calibration Interval", carePlan?.calibrationIntervalNote ?: strings.notConfigured)
                ProvenanceKeyValueRow("Escalation Policy", carePlan?.escalationPolicyNote ?: strings.notConfigured)

                if (!carePlan?.clinicianInstructions.isNullOrBlank()) {
                    HorizontalDivider()
                    Text("Clinician Instructions (Read-Only)", style = MaterialTheme.typography.titleSmall)
                    Text(
                        text = carePlan?.clinicianInstructions.orEmpty(),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
fun PatientSyncStatusScreen(
    state: AirTonUiState,
    onToggleOffline: (Boolean) -> Unit,
    onRetrySync: () -> Unit,
    onTriggerDuplicateRetry: (String) -> Unit,
    onTriggerConflictReplay: (String) -> Unit,
    onOpenMeasurementDetail: (String) -> Unit
) {
    val semantic = LocalAirTonColors.current
    val envelopes = state.activePatientEnvelopes
    val localOnlyCount = envelopes.count { it.transportStatus == TransportStatus.LOCAL_ONLY }
    val pendingOrFailedCount = envelopes.count {
        it.transportStatus == TransportStatus.PENDING || it.transportStatus == TransportStatus.FAILED
    }
    val deliveredCount = envelopes.count { it.transportStatus == TransportStatus.DELIVERED }
    val reviewedCount = envelopes.count {
        it.reviewStatus == ClinicalReviewStatus.REVIEWED ||
            it.reviewStatus == ClinicalReviewStatus.FOLLOW_UP_REQUESTED
    }
    val sampleRecord = envelopes.firstOrNull() ?: state.allEnvelopes.firstOrNull()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("patient_sync_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { PrototypeDisclosureBanner(compact = true) }

        // Simulated Offline Toggle
        item {
            AirTonCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = if (state.preferences.simulatedOfflineMode) Icons.Filled.CloudOff else Icons.Filled.CloudDone,
                            contentDescription = null,
                            tint = if (state.preferences.simulatedOfflineMode) semantic.statusErrorContent else semantic.statusOkContent
                        )
                        Column {
                            Text(
                                text = if (state.preferences.simulatedOfflineMode) "Simulated Offline Mode" else "Simulated Transport Ready",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "Local demo simulation only — no data leaves this device.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Switch(
                        checked = state.preferences.simulatedOfflineMode,
                        onCheckedChange = onToggleOffline,
                        modifier = Modifier.testTag("simulated_offline_switch")
                    )
                }

                Button(
                    onClick = onRetrySync,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("retry_pending_sync_button")
                ) {
                    Icon(Icons.Filled.Sync, contentDescription = null)
                    Spacer(Modifier.size(8.dp))
                    Text("Retry Pending Sync (${state.unsyncedCount} total)")
                }
            }
        }

        // 4 Independent Milestones Card
        item {
            AirTonCard {
                Text("Four Independent State Milestones", style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "Saving locally does not imply delivery, and delivery does not imply a clinician has reviewed the record.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                ProvenanceKeyValueRow("1. Local-only on device", "$localOnlyCount record(s)", mono = true)
                ProvenanceKeyValueRow("2. Pending / Failed retry", "$pendingOrFailedCount record(s)", mono = true)
                ProvenanceKeyValueRow("3. Simulated Delivered", "$deliveredCount record(s)", mono = true)
                ProvenanceKeyValueRow("4. Clinician Reviewed", "$reviewedCount record(s)", mono = true)
            }
        }

        // Idempotent Duplicate & Conflicting Replay Sandbox
        if (sampleRecord != null) {
            item {
                AirTonCard {
                    Text("Idempotency & Conflict Sandbox", style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = "Test v1.0 ingest safety against ${sampleRecord.measurement.measurementId}: identical retries are idempotent no-ops, while altered replays are quarantined without overwriting the original record.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedButton(
                        onClick = { onTriggerDuplicateRetry(sampleRecord.measurement.measurementId) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("simulate_duplicate_retry_button")
                    ) {
                        Icon(Icons.Filled.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.size(8.dp))
                        Text("Simulate Idempotent Duplicate Retry")
                    }
                    OutlinedButton(
                        onClick = { onTriggerConflictReplay(sampleRecord.measurement.measurementId) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("simulate_conflict_replay_button")
                    ) {
                        Icon(Icons.Filled.ReportProblem, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.size(8.dp))
                        Text("Simulate Conflicting Replay (Quarantine)")
                    }
                }
            }
        }

        if (state.conflicts.isNotEmpty()) {
            item {
                Text("Quarantined Conflicts (${state.conflicts.size})", style = MaterialTheme.typography.titleMedium)
            }
            items(state.conflicts, key = { it.conflictId }) { conf ->
                AirTonCard(onClick = { onOpenMeasurementDetail(conf.measurementId) }) {
                    Text(
                        text = "${conf.conflictId} · ${conf.measurementId}",
                        style = MonoMetadataStyle,
                        fontWeight = FontWeight.SemiBold,
                        color = semantic.statusErrorContent
                    )
                    Text(conf.summary, style = MaterialTheme.typography.bodySmall)
                    Text("Detected: ${conf.detectedAt}", style = MonoMetadataStyle, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
