package com.example.ui.screens.patient

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BluetoothDisabled
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.domain.model.EyeSide
import com.example.domain.model.QualityStatus
import com.example.domain.model.SimulationScenario
import com.example.i18n.LocalAirTonStrings
import com.example.ui.components.AirTonCard
import com.example.ui.components.AlignmentGuideCanvas
import com.example.ui.components.EyeBadge
import com.example.ui.components.PrototypeDisclosureBanner
import com.example.ui.components.ProvenanceKeyValueRow
import com.example.ui.components.QualityBadge
import com.example.ui.components.TransportBadge
import com.example.ui.theme.LocalAirTonColors
import com.example.ui.theme.MonoMetadataStyle
import com.example.ui.theme.NumericDisplayStyle
import com.example.viewmodel.AirTonUiState
import com.example.viewmodel.GuidedSessionStep
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GuidedMeasurementScreen(
    state: AirTonUiState,
    onStepChange: (GuidedSessionStep) -> Unit,
    onSelectEye: (EyeSide) -> Unit,
    onSelectScenario: (SimulationScenario) -> Unit,
    onAlignmentChange: (Float, Float) -> Unit,
    onSnapCenter: () -> Unit,
    onRunAcquisition: () -> Unit,
    onResetSession: () -> Unit,
    onInspectSavedRecord: (String) -> Unit,
    onExitToHome: () -> Unit
) {
    val strings = LocalAirTonStrings.current
    val semantic = LocalAirTonColors.current
    val session = state.guidedSession
    val carePlan = state.activePatientCarePlan

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("guided_measurement_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { PrototypeDisclosureBanner(compact = false) }

        // Segmented 7-Step Clinical Progress Bar
        item {
            AirTonCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Step ${session.step.stepNumber} of 7 · ${session.step.label}",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = strings.simulationNoHardwareNotice,
                            style = MaterialTheme.typography.bodySmall,
                            color = semantic.demoBannerContent,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.testTag("sim_no_hardware_text")
                        )
                    }
                    TextButton(
                        onClick = {
                            onResetSession()
                            onExitToHome()
                        },
                        modifier = Modifier.testTag("guided_cancel_button")
                    ) {
                        Icon(Icons.Filled.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.size(4.dp))
                        Text("Exit")
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    GuidedSessionStep.entries.forEach { step ->
                        val active = step.stepNumber <= session.step.stepNumber
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(6.dp)
                                .clip(RoundedCornerShape(999.dp))
                                .background(
                                    if (active) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                        )
                    }
                }
            }
        }

        when (session.step) {
            GuidedSessionStep.DISCLOSURE -> {
                item {
                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        border = BorderStroke(1.dp, semantic.cardBorder),
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Image(
                                painter = painterResource(id = R.drawable.img_concept_optics_1795199062846),
                                contentDescription = "Conceptual non-contact optical alignment diagram",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(140.dp)
                            )
                            Column(
                                modifier = Modifier.padding(18.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text("1. Review Simulation Disclosure", style = MaterialTheme.typography.titleLarge)
                                Text(
                                    text = "This walkthrough demonstrates how AirTon guides positioning, checks quality, records eye-specific provenance, and separates local storage from sync and clinician review.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "No physical sensor, air puff, camera, or Bluetooth connection is operating. This workflow never measures your real eye.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Button(
                                    onClick = { onStepChange(GuidedSessionStep.SELECT_EYE) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .testTag("step1_continue_button")
                                ) {
                                    Text("Continue to Eye Selection")
                                }
                            }
                        }
                    }
                }
            }

            GuidedSessionStep.SELECT_EYE -> {
                item {
                    AirTonCard {
                        Text("2. Confirm Eye & Demo Scenario", style = MaterialTheme.typography.titleLarge)
                        Text(
                            text = "Right Eye (OD) and Left Eye (OS) are always recorded separately.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            EyeSide.entries.forEach { eye ->
                                FilterChip(
                                    selected = session.selectedEye == eye,
                                    onClick = { onSelectEye(eye) },
                                    label = { Text(eye.labelEn) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("select_eye_${eye.code.lowercase()}")
                                )
                            }
                        }

                        HorizontalDivider()

                        Text("Pre-Capture Simulation Scenario", style = MaterialTheme.typography.titleSmall)
                        Text(
                            text = "Choose any deterministic software outcome to test accepted captures, null-IOP rejections, and hardware states.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            SimulationScenario.entries.forEach { sc ->
                                FilterChip(
                                    selected = session.selectedScenario == sc,
                                    onClick = { onSelectScenario(sc) },
                                    label = { Text(sc.shortTag) },
                                    modifier = Modifier.testTag("scenario_chip_${sc.id}")
                                )
                            }
                        }
                        Text(
                            text = "Selected: ${session.selectedScenario.labelEn}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Button(
                            onClick = { onStepChange(GuidedSessionStep.READINESS) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("step2_continue_button")
                        ) {
                            Text("Check Simulated Readiness")
                        }
                    }
                }
            }

            GuidedSessionStep.READINESS -> {
                item {
                    AirTonCard {
                        Text("3. Simulated Device Readiness", style = MaterialTheme.typography.titleLarge)
                        ProvenanceKeyValueRow("Physical Hardware", "Not connected (Simulation adapter)")
                        ProvenanceKeyValueRow("Target Eye", session.selectedEye.labelEn)
                        ProvenanceKeyValueRow("Demo Scenario", session.selectedScenario.labelEn)
                        ProvenanceKeyValueRow(
                            "Retry Policy Limit",
                            carePlan?.maxRetriesPerEye?.toString() ?: strings.notConfigured
                        )
                        ProvenanceKeyValueRow(
                            "Simulated Network",
                            if (state.preferences.simulatedOfflineMode) "Offline (saves Local-only)" else "Online (queues Pending sync)"
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedButton(
                                onClick = { onStepChange(GuidedSessionStep.SELECT_EYE) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Back")
                            }
                            Button(
                                onClick = { onStepChange(GuidedSessionStep.ALIGNMENT) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("step3_continue_button")
                            ) {
                                Text("Positioning Guide")
                            }
                        }
                    }
                }
            }

            GuidedSessionStep.ALIGNMENT -> {
                item {
                    AlignmentGuideCanvas(
                        eye = session.selectedEye,
                        offsetX = session.reticleOffsetX,
                        offsetY = session.reticleOffsetY,
                        alignmentScore = session.alignmentScore,
                        isAligned = session.isWithinTargetZone,
                        onOffsetChange = onAlignmentChange,
                        onCenterAssist = onSnapCenter
                    )
                }
                item {
                    AirTonCard {
                        Text("4. Ready for Simulated Capture", style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = "If you selected 'Pass (Synthetic)' and leave the reticle off-center (<65%), the simulator will truthfully reject the capture for alignment with null IOP.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedButton(
                                onClick = { onStepChange(GuidedSessionStep.READINESS) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Back")
                            }
                            Button(
                                onClick = onRunAcquisition,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("run_simulated_capture_button")
                            ) {
                                Icon(Icons.Filled.PlayArrow, contentDescription = null)
                                Spacer(Modifier.size(6.dp))
                                Text("Simulate Capture")
                            }
                        }
                    }
                }
            }

            GuidedSessionStep.ACQUIRING -> {
                item {
                    val pulse = if (state.preferences.reducedMotionEnabled) {
                        1f
                    } else {
                        val inf = rememberInfiniteTransition(label = "acquiring_pulse")
                        val p by inf.animateFloat(
                            initialValue = 0.9f,
                            targetValue = 1.12f,
                            animationSpec = infiniteRepeatable(tween(650), RepeatMode.Reverse),
                            label = "pulse_val"
                        )
                        p
                    }
                    AirTonCard {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .scale(pulse)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Science,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                            Spacer(Modifier.height(14.dp))
                            Text("Running Software Simulation…", style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "Generating deterministic v1.0 contract payload for ${session.selectedEye.labelEn}.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            GuidedSessionStep.QUALITY_RESULT -> {
                item {
                    val record = session.latestSimulatedRecord
                    AirTonCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("6. Quality Evaluation (Synthetic)", style = MaterialTheme.typography.titleLarge)
                            EyeBadge(session.selectedEye)
                        }

                        if (record == null) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(Icons.Filled.BluetoothDisabled, contentDescription = null, tint = semantic.statusErrorContent)
                                Text(
                                    text = session.latestOutcomeMessage ?: "Simulated device disconnected.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.testTag("result_disconnected_text")
                                )
                            }
                        } else if (record.qualityStatus == QualityStatus.PASS && record.iopEstimateMmhg != null) {
                            QualityBadge(QualityStatus.PASS)
                            Row(
                                verticalAlignment = Alignment.Bottom,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = String.format(Locale.US, "%.1f", record.iopEstimateMmhg),
                                    style = NumericDisplayStyle,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.testTag("result_synthetic_iop_value")
                                )
                                Text(
                                    text = "mmHg (Synthetic demo only)",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = strings.nonClinicalResultDisclaimer,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            QualityBadge(QualityStatus.REJECT)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Filled.ErrorOutline, contentDescription = null, tint = semantic.statusErrorContent)
                                Text(
                                    text = "Acquisition rejected — No IOP value recorded (null)",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = semantic.statusErrorContent,
                                    modifier = Modifier.testTag("result_rejected_no_value")
                                )
                            }
                            record.qualityReasons.forEach { reason ->
                                Text(
                                    text = "• ${reason.labelEn}: ${reason.guidanceEn}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = "Configured retry limit: ${carePlan?.maxRetriesPerEye?.toString() ?: strings.notConfigured}. You may end safely at any time.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        HorizontalDivider()

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedButton(
                                onClick = { onStepChange(GuidedSessionStep.SELECT_EYE) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Filled.Replay, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.size(4.dp))
                                Text("Try Another")
                            }
                            if (record != null) {
                                Button(
                                    onClick = { onStepChange(GuidedSessionStep.SAVED_SUMMARY) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("step6_saved_summary_button")
                                ) {
                                    Text("Saved Summary")
                                }
                            } else {
                                Button(
                                    onClick = {
                                        onResetSession()
                                        onExitToHome()
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Finish Safely")
                                }
                            }
                        }
                    }
                }
            }

            GuidedSessionStep.SAVED_SUMMARY -> {
                item {
                    val record = session.latestSimulatedRecord
                    val savedTransport = session.latestSavedTransportStatus
                    AirTonCard {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = semantic.statusOkContent)
                            Text("7. Immutable Local Record Saved", style = MaterialTheme.typography.titleLarge)
                        }
                        if (record != null) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                QualityBadge(record.qualityStatus)
                                savedTransport?.let { TransportBadge(it) }
                            }
                            ProvenanceKeyValueRow("Measurement ID", record.measurementId, mono = true)
                            ProvenanceKeyValueRow("Session ID", record.sessionId, mono = true)
                            ProvenanceKeyValueRow("Eye", record.eye.labelEn)
                            ProvenanceKeyValueRow("Captured At", record.capturedAt, mono = true)
                            ProvenanceKeyValueRow("Clock Status", record.clockStatus.wireValue, mono = true)
                            Text(
                                text = "Local persistence does not imply cloud delivery or clinician review. Open Sync Status to simulate transport delivery.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Button(
                                onClick = { onInspectSavedRecord(record.measurementId) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("inspect_saved_provenance_button")
                            ) {
                                Icon(Icons.Filled.Science, contentDescription = null)
                                Spacer(Modifier.size(8.dp))
                                Text("Inspect Full Provenance")
                            }
                        }
                        OutlinedButton(
                            onClick = {
                                onResetSession()
                                onExitToHome()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Return to Patient Home")
                        }
                    }
                }
            }
        }
    }
}
