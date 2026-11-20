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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.BluetoothDisabled
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.domain.model.EyeSide
import com.example.domain.model.QualityStatus
import com.example.domain.model.TransportStatus
import com.example.i18n.LocalAirTonStrings
import com.example.ui.components.AirTonCard
import com.example.ui.components.EyeBadge
import com.example.ui.components.EyeTrendChart
import com.example.ui.components.IndependentStateBadgeRow
import com.example.ui.components.PrototypeDisclosureBanner
import com.example.ui.components.StatusPill
import com.example.ui.theme.LocalAirTonColors
import com.example.ui.theme.MonoMetadataStyle
import com.example.viewmodel.AirTonUiState
import java.util.Locale

@Composable
fun PatientHomeScreen(
    state: AirTonUiState,
    onSelectPatient: (String) -> Unit,
    onStartSimulatedSession: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenMeasurementDetail: (String) -> Unit,
    onOpenSync: () -> Unit,
    onOpenCarePlan: () -> Unit
) {
    val strings = LocalAirTonStrings.current
    val semantic = LocalAirTonColors.current
    val patient = state.activePatient
    val carePlan = state.activePatientCarePlan
    val envelopes = state.activePatientEnvelopes
    val latest = envelopes.firstOrNull()
    val unsyncedForPatient = envelopes.count {
        it.transportStatus == TransportStatus.LOCAL_ONLY ||
            it.transportStatus == TransportStatus.PENDING ||
            it.transportStatus == TransportStatus.FAILED
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("patient_home_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            PrototypeDisclosureBanner(compact = false)
        }

        // Greeting & discreet profile identity + quick fictional patient selector
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Hello, ${patient?.fullName?.substringBefore(" ") ?: "Guest"}",
                    style = MaterialTheme.typography.headlineMedium
                )
                Text(
                    text = buildString {
                        append("Demo Subject: ${patient?.subjectId ?: "—"} ")
                        patient?.caregiverName?.let { append("· Caregiver: ${it.substringBefore(" (")}") }
                    },
                    style = MonoMetadataStyle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    state.patients.forEach { p ->
                        FilterChip(
                            selected = p.subjectId == patient?.subjectId,
                            onClick = { onSelectPatient(p.subjectId) },
                            label = { Text(p.fullName.substringBefore(" ")) },
                            modifier = Modifier.testTag("home_patient_chip_${p.subjectId}")
                        )
                    }
                }
            }
        }

        // Device Connection Card — truthfully states no physical device connected
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
                            imageVector = Icons.Filled.BluetoothDisabled,
                            contentDescription = "No physical AirTon device connected",
                            tint = semantic.statusNeutralContent
                        )
                        Column {
                            Text(
                                text = strings.noDeviceConnected,
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.testTag("device_not_connected_label")
                            )
                            Text(
                                text = "Hardware BLE protocol is not active in this prototype.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                Button(
                    onClick = onStartSimulatedSession,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("try_device_simulation_button")
                ) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = null)
                    Spacer(Modifier.size(8.dp))
                    Text(strings.tryDeviceSimulation)
                }
            }
        }

        // Care-Plan Card — truthful null-state handling
        item {
            AirTonCard(onClick = onOpenCarePlan) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Filled.CalendarMonth, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text("Clinician Monitoring Plan", style = MaterialTheme.typography.titleMedium)
                    }
                    StatusPill(
                        text = if (carePlan?.isScheduleConfigured == true) "Read-only Demo" else strings.notConfigured,
                        icon = Icons.Filled.CalendarMonth,
                        backgroundColor = semantic.statusNeutralBg,
                        contentColor = semantic.statusNeutralContent
                    )
                }
                if (carePlan != null && carePlan.isScheduleConfigured) {
                    Text(
                        text = carePlan.scheduleSummary ?: strings.notConfigured,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Prescribed eyes: ${carePlan.eyesPrescribed.joinToString(" & ") { it.code }.ifEmpty { strings.notConfigured }} · Retry limit: ${carePlan.maxRetriesPerEye?.toString() ?: strings.notConfigured}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text(
                        text = "Your monitoring plan has not been configured in this demo.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.testTag("unconfigured_care_plan_notice")
                    )
                }
            }
        }

        // Latest Synthetic Session Summary
        item {
            AirTonCard(
                onClick = latest?.let { { onOpenMeasurementDetail(it.measurement.measurementId) } }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Latest Synthetic Record", style = MaterialTheme.typography.titleMedium)
                    if (latest != null) {
                        EyeBadge(latest.measurement.eye)
                    }
                }
                if (latest == null) {
                    Text(
                        text = "No synthetic measurements recorded yet for this demo profile.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    val m = latest.measurement
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = m.capturedAt,
                                style = MonoMetadataStyle,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "ID: ${m.measurementId}",
                                style = MonoMetadataStyle,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = if (m.qualityStatus == QualityStatus.PASS && m.iopEstimateMmhg != null) {
                                String.format(Locale.US, "%.1f mmHg", m.iopEstimateMmhg)
                            } else {
                                "No value (Rejected)"
                            },
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (m.qualityStatus == QualityStatus.PASS) {
                                MaterialTheme.colorScheme.primary
                            } else semantic.statusErrorContent
                        )
                    }
                    if (m.qualityReasons.isNotEmpty()) {
                        Text(
                            text = "Rejection reason: ${m.qualityReasons.joinToString(", ") { it.labelEn }}",
                            style = MaterialTheme.typography.bodySmall,
                            color = semantic.statusErrorContent
                        )
                    }
                    IndependentStateBadgeRow(latest)
                    Text(
                        text = strings.nonClinicalResultDisclaimer,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Next Step Card (non-clinical operational guidance)
        item {
            AirTonCard {
                Text("Suggested Next Step (Demo)", style = MaterialTheme.typography.titleMedium)
                val nextCopy = when {
                    unsyncedForPatient > 0 ->
                        "$unsyncedForPatient local/pending synthetic record(s) are stored on this device. Open Sync Status to test offline retry or delivery acknowledgement."
                    else ->
                        "Run a guided simulated acquisition or inspect separate OD & OS history and record provenance."
                }
                Text(
                    text = nextCopy,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = onOpenHistory,
                        modifier = Modifier.weight(1f).testTag("home_open_history_button")
                    ) {
                        Text("History")
                        Spacer(Modifier.size(4.dp))
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                    OutlinedButton(
                        onClick = onOpenSync,
                        modifier = Modifier.weight(1f).testTag("home_open_sync_button")
                    ) {
                        Icon(Icons.Filled.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.size(4.dp))
                        Text("Sync ($unsyncedForPatient)")
                    }
                }
            }
        }

        // Compact OD / OS Trend Preview (no clinical thresholds or normal bands)
        item {
            AirTonCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("OD & OS Synthetic Trend Preview", style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = "Separate eye series · No diagnostic bands",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    TextButton(onClick = onOpenHistory) {
                        Text("Full Chart")
                    }
                }
                HorizontalDivider()
                EyeTrendChart(
                    envelopes = envelopes,
                    compact = true,
                    onSelectMeasurement = onOpenMeasurementDetail
                )
            }
        }
    }
}
