package com.example.ui.screens.patient

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterAltOff
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.domain.model.ClinicalReviewStatus
import com.example.domain.model.EyeSide
import com.example.domain.model.MeasurementEnvelope
import com.example.domain.model.QualityStatus
import com.example.domain.model.TransportStatus
import com.example.i18n.LocalAirTonStrings
import com.example.ui.components.AirTonCard
import com.example.ui.components.EmptyOrOfflineStateCard
import com.example.ui.components.EyeBadge
import com.example.ui.components.EyeTrendChart
import com.example.ui.components.IndependentStateBadgeRow
import com.example.ui.components.PrototypeDisclosureBanner
import com.example.ui.components.ProvenanceKeyValueRow
import com.example.ui.theme.LocalAirTonColors
import com.example.ui.theme.MonoMetadataStyle
import com.example.ui.theme.NumericDisplayStyle
import com.example.viewmodel.AirTonUiState
import com.example.viewmodel.HistoryFilterState
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PatientHistoryScreen(
    state: AirTonUiState,
    onUpdateFilter: ((HistoryFilterState) -> HistoryFilterState) -> Unit,
    onClearFilters: () -> Unit,
    onOpenDetail: (String) -> Unit
) {
    val filter = state.historyFilter
    val allSubjectRecords = state.activePatientEnvelopes

    val filtered = remember(allSubjectRecords, filter) {
        allSubjectRecords.filter { env ->
            val m = env.measurement
            val matchesEye = filter.eyeFilter == null || m.eye == filter.eyeFilter
            val matchesQuality = filter.qualityFilter == null || m.qualityStatus == filter.qualityFilter
            val matchesTransport = filter.transportFilter == null || env.transportStatus == filter.transportFilter
            val matchesReview = filter.reviewFilter == null || env.reviewStatus == filter.reviewFilter
            val matchesDate = when (filter.dateRangeDays) {
                3 -> m.capturedAt >= "2026-11-18"
                7 -> m.capturedAt >= "2026-11-14"
                else -> true
            }
            matchesEye && matchesQuality && matchesTransport && matchesReview && matchesDate
        }
    }

    val hasActiveFilters = filter != HistoryFilterState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("patient_history_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { PrototypeDisclosureBanner(compact = true) }

        item {
            AirTonCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Separate OD / OS Trend", style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = "${filtered.size} record(s)",
                        style = MonoMetadataStyle,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                EyeTrendChart(
                    envelopes = filtered,
                    compact = false,
                    onSelectMeasurement = onOpenDetail
                )
            }
        }

        item {
            AirTonCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Filters", style = MaterialTheme.typography.titleSmall)
                    if (hasActiveFilters) {
                        TextButton(onClick = onClearFilters) {
                            Text("Clear")
                        }
                    }
                }
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    EyeSide.entries.forEach { eye ->
                        FilterChip(
                            selected = filter.eyeFilter == eye,
                            onClick = {
                                onUpdateFilter { it.copy(eyeFilter = if (it.eyeFilter == eye) null else eye) }
                            },
                            label = { Text(eye.code) },
                            modifier = Modifier.testTag("filter_eye_${eye.code.lowercase()}")
                        )
                    }
                    FilterChip(
                        selected = filter.qualityFilter == QualityStatus.PASS,
                        onClick = {
                            onUpdateFilter {
                                it.copy(qualityFilter = if (it.qualityFilter == QualityStatus.PASS) null else QualityStatus.PASS)
                            }
                        },
                        label = { Text("Accepted") }
                    )
                    FilterChip(
                        selected = filter.qualityFilter == QualityStatus.REJECT,
                        onClick = {
                            onUpdateFilter {
                                it.copy(qualityFilter = if (it.qualityFilter == QualityStatus.REJECT) null else QualityStatus.REJECT)
                            }
                        },
                        label = { Text("Rejected") },
                        modifier = Modifier.testTag("filter_rejected_chip")
                    )
                    FilterChip(
                        selected = filter.transportFilter == TransportStatus.LOCAL_ONLY,
                        onClick = {
                            onUpdateFilter {
                                it.copy(
                                    transportFilter = if (it.transportFilter == TransportStatus.LOCAL_ONLY) null else TransportStatus.LOCAL_ONLY
                                )
                            }
                        },
                        label = { Text("Local-only") }
                    )
                    FilterChip(
                        selected = filter.reviewFilter == ClinicalReviewStatus.REVIEWED,
                        onClick = {
                            onUpdateFilter {
                                it.copy(
                                    reviewFilter = if (it.reviewFilter == ClinicalReviewStatus.REVIEWED) null else ClinicalReviewStatus.REVIEWED
                                )
                            }
                        },
                        label = { Text("Reviewed") }
                    )
                    FilterChip(
                        selected = filter.dateRangeDays == 3,
                        onClick = {
                            onUpdateFilter { it.copy(dateRangeDays = if (it.dateRangeDays == 3) null else 3) }
                        },
                        label = { Text("Last 3 days") }
                    )
                }
            }
        }

        if (filtered.isEmpty()) {
            item {
                EmptyOrOfflineStateCard(
                    icon = if (hasActiveFilters) Icons.Filled.FilterAltOff else Icons.Filled.History,
                    title = if (hasActiveFilters) "No matching records" else "No synthetic history yet",
                    description = if (hasActiveFilters) {
                        "Try clearing one or more filters to view all eye-specific records."
                    } else {
                        "Start a guided simulated session to create your first deterministic demonstration record."
                    },
                    actionLabel = if (hasActiveFilters) "Clear Filters" else null,
                    onAction = if (hasActiveFilters) onClearFilters else null
                )
            }
        } else {
            items(filtered, key = { it.measurement.measurementId }) { env ->
                MeasurementHistoryRowCard(
                    envelope = env,
                    onClick = { onOpenDetail(env.measurement.measurementId) }
                )
            }
        }
    }
}

@Composable
fun MeasurementHistoryRowCard(
    envelope: MeasurementEnvelope,
    onClick: () -> Unit,
    subjectLabel: String? = null
) {
    val m = envelope.measurement
    val semantic = LocalAirTonColors.current

    AirTonCard(
        modifier = Modifier.testTag("measurement_row_${m.measurementId}"),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                EyeBadge(m.eye)
                Column {
                    if (subjectLabel != null) {
                        Text(subjectLabel, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    }
                    Text(m.capturedAt, style = MonoMetadataStyle, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Text(
                text = if (m.qualityStatus == QualityStatus.PASS && m.iopEstimateMmhg != null) {
                    String.format(Locale.US, "%.1f mmHg", m.iopEstimateMmhg)
                } else {
                    "No value"
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (m.qualityStatus == QualityStatus.PASS) {
                    MaterialTheme.colorScheme.primary
                } else semantic.statusErrorContent
            )
        }

        if (m.qualityReasons.isNotEmpty()) {
            Text(
                text = "Rejection reason(s): ${m.qualityReasons.joinToString(", ") { it.labelEn }}",
                style = MaterialTheme.typography.bodySmall,
                color = semantic.statusErrorContent
            )
        }

        IndependentStateBadgeRow(envelope)
    }
}

@Composable
fun MeasurementDetailScreen(
    envelope: MeasurementEnvelope?,
    patientName: String?
) {
    val strings = LocalAirTonStrings.current
    val semantic = LocalAirTonColors.current

    if (envelope == null) {
        EmptyOrOfflineStateCard(
            icon = Icons.Filled.History,
            title = "Record not found",
            description = "The requested synthetic measurement ID could not be loaded from local storage."
        )
        return
    }

    val m = envelope.measurement

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("measurement_detail_screen"),
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
                    EyeBadge(m.eye)
                    Text("SYNTHETIC v${m.schemaVersion}", style = MonoMetadataStyle, color = MaterialTheme.colorScheme.primary)
                }
                if (m.qualityStatus == QualityStatus.PASS && m.iopEstimateMmhg != null) {
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = String.format(Locale.US, "%.1f", m.iopEstimateMmhg),
                            style = NumericDisplayStyle,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "mmHg (Synthetic demo estimate)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    Text(
                        text = "Rejected Acquisition — iop_estimate_mmhg: null",
                        style = MaterialTheme.typography.titleMedium,
                        color = semantic.statusErrorContent,
                        fontWeight = FontWeight.Bold
                    )
                    m.qualityReasons.forEach { r ->
                        Text("• ${r.wireValue} — ${r.labelEn}", style = MaterialTheme.typography.bodySmall, color = semantic.statusErrorContent)
                    }
                }
                Text(
                    text = strings.nonClinicalResultDisclaimer,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                HorizontalDivider()
                IndependentStateBadgeRow(envelope)
            }
        }

        // Complete 16-field v1.0 Contract Provenance Card
        item {
            AirTonCard {
                Text("v1.0 Contract Provenance (Immutable)", style = MaterialTheme.typography.titleMedium)
                patientName?.let { ProvenanceKeyValueRow("Demo Subject", it) }
                ProvenanceKeyValueRow("measurement_id", m.measurementId, mono = true)
                ProvenanceKeyValueRow("session_id", m.sessionId, mono = true)
                ProvenanceKeyValueRow("subject_id", m.subjectId, mono = true)
                ProvenanceKeyValueRow("device_id", m.deviceId, mono = true)
                ProvenanceKeyValueRow("eye", m.eye.labelEn, mono = true)
                ProvenanceKeyValueRow("captured_at", m.capturedAt, mono = true)
                ProvenanceKeyValueRow("synthetic", m.synthetic.toString(), mono = true)
                ProvenanceKeyValueRow("quality_status", m.qualityStatus.wireValue, mono = true)
                ProvenanceKeyValueRow(
                    "quality_reasons",
                    m.qualityReasons.joinToString(", ") { it.wireValue }.ifEmpty { "[]" },
                    mono = true
                )
                ProvenanceKeyValueRow(
                    "iop_estimate_mmhg",
                    m.iopEstimateMmhg?.let { String.format(Locale.US, "%.1f", it) } ?: "null",
                    mono = true
                )
                ProvenanceKeyValueRow("calibration_id", m.calibrationId, mono = true)
                ProvenanceKeyValueRow("model_id", m.modelId, mono = true)
                ProvenanceKeyValueRow("firmware_version", m.firmwareVersion, mono = true)
                ProvenanceKeyValueRow("clock_status", m.clockStatus.wireValue, mono = true)
                ProvenanceKeyValueRow("raw_sha256", m.rawSha256, mono = true)
            }
        }

        // Independent Transport & Sync State Card
        item {
            AirTonCard {
                Text("Transport & Local Persistence State", style = MaterialTheme.typography.titleMedium)
                ProvenanceKeyValueRow("Local SQLite", "Saved immutably on device")
                ProvenanceKeyValueRow("Sync State", envelope.transportStatus.labelEn)
                ProvenanceKeyValueRow("Last Sync Attempt", envelope.lastSyncAttemptAt ?: "Not attempted", mono = true)
                ProvenanceKeyValueRow("Simulated Delivery", envelope.deliveredAt ?: "Not delivered", mono = true)
                envelope.syncFailureReason?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall, color = semantic.statusErrorContent)
                }
            }
        }

        // Append-Only Clinician Review Events
        item {
            AirTonCard {
                Text("Clinician Review Trail (Append-Only)", style = MaterialTheme.typography.titleMedium)
                if (envelope.reviewEvents.isEmpty()) {
                    Text(
                        text = "No clinician review events logged yet. Delivery alone does not mean a clinician has reviewed this record.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    envelope.reviewEvents.forEach { ev ->
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(ev.actionType.labelEn, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                            Text("${ev.authorName} · ${ev.createdAt}", style = MonoMetadataStyle, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            ev.note?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                        }
                        HorizontalDivider()
                    }
                }
            }
        }

        // Quarantined Conflicts (if any)
        if (envelope.conflicts.isNotEmpty()) {
            item {
                AirTonCard {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Filled.ReportProblem, contentDescription = null, tint = semantic.statusErrorContent)
                        Text("Quarantined Conflicting Replays", style = MaterialTheme.typography.titleMedium, color = semantic.statusErrorContent)
                    }
                    envelope.conflicts.forEach { conf ->
                        Text(conf.summary, style = MaterialTheme.typography.bodySmall)
                        ProvenanceKeyValueRow("Conflict ID", conf.conflictId, mono = true)
                        ProvenanceKeyValueRow("Detected At", conf.detectedAt, mono = true)
                        ProvenanceKeyValueRow("Incoming Hash", conf.incomingPayloadSha256.take(24) + "…", mono = true)
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}
