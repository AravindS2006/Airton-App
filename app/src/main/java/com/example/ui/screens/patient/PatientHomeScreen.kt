package com.example.ui.screens.patient

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.BluetoothDisabled
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.domain.model.EyeSide
import com.example.domain.model.MeasurementEnvelope
import com.example.domain.model.QualityStatus
import com.example.domain.model.TransportStatus
import com.example.i18n.LocalAirTonStrings
import com.example.ui.components.AirTonCard
import com.example.ui.components.EyeBadge
import com.example.ui.components.EyeTrendChart
import com.example.ui.components.IndependentStateBadgeRow
import com.example.ui.components.PrototypeDisclosureBanner
import com.example.ui.components.SectionHeaderRow
import com.example.ui.components.StatusPill
import com.example.ui.theme.DeepNavyInk
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
    val latestOd = envelopes.firstOrNull { it.measurement.eye == EyeSide.OD }
    val latestOs = envelopes.firstOrNull { it.measurement.eye == EyeSide.OS }
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
        item { PrototypeDisclosureBanner(compact = false) }

        // Greeting & Fictional Subject Switcher
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Welcome back, ${patient?.fullName?.substringBefore(" ") ?: "Guest"}",
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

        // Clinical Hero Device Card — Deep Navy Precision Surface
        item {
            Surface(
                shape = MaterialTheme.shapes.large,
                color = DeepNavyInk,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
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
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color.White.copy(alpha = 0.12f),
                                modifier = Modifier.size(38.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.BluetoothDisabled,
                                    contentDescription = "No physical AirTon device connected",
                                    tint = Color(0xFF99F6E4),
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = strings.noDeviceConnected,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White,
                                    modifier = Modifier.testTag("device_not_connected_label")
                                )
                                Text(
                                    text = "Software simulation ready · Zero hardware active",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.78f)
                                )
                            }
                        }
                    }

                    Button(
                        onClick = onStartSimulatedSession,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2DD4BF),
                            contentColor = DeepNavyInk
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("try_device_simulation_button")
                    ) {
                        Icon(Icons.Filled.PlayArrow, contentDescription = null)
                        Spacer(Modifier.size(8.dp))
                        Text(strings.tryDeviceSimulation, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Separate OD & OS Latest Glance Tiles (Never Averaged)
        item {
            SectionHeaderRow(
                title = "Latest Eye-Specific Records",
                subtitle = "OD and OS are always tracked separately"
            )
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                EyeGlanceCard(
                    eye = EyeSide.OD,
                    envelope = latestOd,
                    modifier = Modifier.weight(1f),
                    onClick = latestOd?.let { { onOpenMeasurementDetail(it.measurement.measurementId) } }
                )
                EyeGlanceCard(
                    eye = EyeSide.OS,
                    envelope = latestOs,
                    modifier = Modifier.weight(1f),
                    onClick = latestOs?.let { { onOpenMeasurementDetail(it.measurement.measurementId) } }
                )
            }
        }

        // Care-Plan Card — Truthful Null-State Handling
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

        // Latest Synthetic Session Full 3-State Summary
        if (latest != null) {
            item {
                val m = latest.measurement
                AirTonCard(onClick = { onOpenMeasurementDetail(m.measurementId) }) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Most Recent Session Activity", style = MaterialTheme.typography.titleMedium)
                        EyeBadge(m.eye)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(m.capturedAt, style = MonoMetadataStyle, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("ID: ${m.measurementId}", style = MonoMetadataStyle, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text(
                            text = if (m.qualityStatus == QualityStatus.PASS && m.iopEstimateMmhg != null) {
                                String.format(Locale.US, "%.1f mmHg", m.iopEstimateMmhg)
                            } else {
                                "No value (Rejected)"
                            },
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (m.qualityStatus == QualityStatus.PASS) MaterialTheme.colorScheme.primary else semantic.statusErrorContent
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

        // Next Step Operational Card
        item {
            AirTonCard {
                Text("Operational Next Step (Demo)", style = MaterialTheme.typography.titleMedium)
                val nextCopy = if (unsyncedForPatient > 0) {
                    "$unsyncedForPatient local/pending synthetic record(s) are stored on this device. Open Sync Status to test offline retry or delivery acknowledgement."
                } else {
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

        // Compact OD / OS Trend Preview
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
                    TextButton(onClick = onOpenHistory) { Text("Full Chart") }
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

@Composable
private fun EyeGlanceCard(
    eye: EyeSide,
    envelope: MeasurementEnvelope?,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)?
) {
    val semantic = LocalAirTonColors.current
    val accent = if (eye == EyeSide.OD) semantic.odSeries else semantic.osSeries

    AirTonCard(
        modifier = modifier,
        borderColor = accent.copy(alpha = 0.30f),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            EyeBadge(eye)
            Text(
                text = if (eye == EyeSide.OD) "Right" else "Left",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (envelope == null) {
            Text("—", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("No record yet", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            val m = envelope.measurement
            if (m.qualityStatus == QualityStatus.PASS && m.iopEstimateMmhg != null) {
                Text(
                    text = String.format(Locale.US, "%.1f", m.iopEstimateMmhg),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = accent
                )
                Text("mmHg (Synthetic)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                Text(
                    text = "No value",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = semantic.statusErrorContent
                )
                Text(
                    text = m.qualityReasons.firstOrNull()?.labelEn ?: "Rejected",
                    style = MaterialTheme.typography.labelSmall,
                    color = semantic.statusErrorContent,
                    maxLines = 1
                )
            }
            Text(
                text = envelope.transportStatus.labelEn,
                style = MonoMetadataStyle,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
