package com.example.ui.screens.clinic

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.i18n.LocalAirTonStrings
import com.example.ui.components.AirTonCard
import com.example.ui.components.PrototypeDisclosureBanner
import com.example.ui.components.ProvenanceKeyValueRow
import com.example.ui.components.StatusPill
import com.example.ui.theme.LocalAirTonColors
import com.example.ui.theme.MonoMetadataStyle
import com.example.viewmodel.AirTonUiState

@Composable
fun ClinicOverviewScreen(
    state: AirTonUiState,
    onOpenAssignments: () -> Unit,
    onOpenDevices: () -> Unit,
    onOpenOpsQueue: () -> Unit
) {
    val clinic = state.clinic
    val serviceNeededCount = state.devices.count { it.needsService }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("clinic_overview_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { PrototypeDisclosureBanner(compact = false) }

        item {
            AirTonCard {
                Text(clinic?.name ?: "Demo Eye Care Clinic", style = MaterialTheme.typography.headlineSmall)
                Text(
                    text = "Operational & device-service visibility only. Clinic administrators cannot alter clinical observations or act as a clinician.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                HorizontalDivider()
                ProvenanceKeyValueRow("Location", clinic?.locationLabel ?: "Demo")
                ProvenanceKeyValueRow("Clinicians", "${state.clinicians.size} fictional staff")
                ProvenanceKeyValueRow("Enrolled Patients", "${state.patients.size} demo profiles")
                ProvenanceKeyValueRow("Service History Logs", "${state.serviceEvents.size} append-only entries")
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AirTonCard(modifier = Modifier.weight(1f), onClick = onOpenDevices) {
                    Text("Device Fleet", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${state.devices.size}", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
                    Text("$serviceNeededCount needs service", style = MaterialTheme.typography.bodySmall)
                }
                AirTonCard(modifier = Modifier.weight(1f), onClick = onOpenOpsQueue) {
                    Text("Ops Queue", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${state.unsyncedCount + state.conflicts.size}", style = MaterialTheme.typography.headlineMedium, color = LocalAirTonColors.current.demoBannerContent)
                    Text("Sync / conflict items", style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(onClick = onOpenAssignments, modifier = Modifier.weight(1f)) {
                    Text("Assignments")
                }
                OutlinedButton(onClick = onOpenDevices, modifier = Modifier.weight(1f)) {
                    Text("Device Inventory")
                }
            }
        }
    }
}

@Composable
fun ClinicAssignmentsScreen(
    state: AirTonUiState,
    onAssignDevice: (deviceId: String, subjectId: String?) -> Unit
) {
    val strings = LocalAirTonStrings.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("clinic_assignments_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { PrototypeDisclosureBanner(compact = true) }
        item {
            Text(
                text = "Simulated enrolment, training orientation, and device loaner allocation.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        items(state.patients, key = { it.subjectId }) { patient ->
            val clinician = state.clinicians.firstOrNull { it.clinicianId == patient.assignedClinicianId }
            val spareDevice = state.devices.firstOrNull { it.assignedSubjectId == null && !it.needsService }

            AirTonCard {
                Text(patient.fullName, style = MaterialTheme.typography.titleMedium)
                ProvenanceKeyValueRow("Subject ID", patient.subjectId, mono = true)
                ProvenanceKeyValueRow("Assigned Clinician", clinician?.fullName ?: strings.notAvailable)
                ProvenanceKeyValueRow("Enrolment Status", patient.enrolmentStatus)
                ProvenanceKeyValueRow("Training Status", patient.trainingStatus)
                ProvenanceKeyValueRow("Allocated Device", patient.assignedDeviceId ?: "None assigned", mono = true)

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (patient.assignedDeviceId != null) {
                        OutlinedButton(
                            onClick = { onAssignDevice(patient.assignedDeviceId, null) },
                            modifier = Modifier.testTag("return_device_${patient.subjectId}")
                        ) {
                            Icon(Icons.Filled.SwapHoriz, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.size(4.dp))
                            Text("Return to Pool")
                        }
                    } else if (spareDevice != null) {
                        Button(
                            onClick = { onAssignDevice(spareDevice.deviceId, patient.subjectId) },
                            modifier = Modifier.testTag("allocate_device_${patient.subjectId}")
                        ) {
                            Icon(Icons.Filled.Devices, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.size(6.dp))
                            Text("Allocate ${spareDevice.deviceId}")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ClinicDevicesScreen(
    state: AirTonUiState,
    onLogServiceEvent: (deviceId: String, eventType: String, summary: String, calRef: String?, clearServiceFlag: Boolean) -> Unit
) {
    val strings = LocalAirTonStrings.current
    val semantic = LocalAirTonColors.current
    var serviceTargetDeviceId by remember { mutableStateOf<String?>(null) }

    if (serviceTargetDeviceId != null) {
        val targetId = serviceTargetDeviceId!!
        var eventType by remember { mutableStateOf("Bench inspection (Demo)") }
        var summary by remember { mutableStateOf("") }
        var calRef by remember { mutableStateOf("") }
        var clearFlag by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { serviceTargetDeviceId = null },
            title = { Text("Log Service Event ($targetId)") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Append-only demo service log. Does not certify hardware calibration.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("Bench inspection (Demo)", "Calibration check (Demo)", "Repair (Demo)").forEach { type ->
                            FilterChip(
                                selected = eventType == type,
                                onClick = { eventType = type },
                                label = { Text(type.substringBefore(" ")) }
                            )
                        }
                    }
                    OutlinedTextField(
                        value = summary,
                        onValueChange = { summary = it },
                        label = { Text("Service summary") },
                        minLines = 2
                    )
                    OutlinedTextField(
                        value = calRef,
                        onValueChange = { calRef = it },
                        label = { Text("Demo calibration ref (optional)") }
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = clearFlag, onCheckedChange = { clearFlag = it })
                        Text("Clear 'Needs service' operational flag", style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                Button(
                    enabled = summary.isNotBlank(),
                    onClick = {
                        onLogServiceEvent(targetId, eventType, summary, calRef.ifBlank { null }, clearFlag)
                        serviceTargetDeviceId = null
                    }
                ) {
                    Text("Append Event")
                }
            },
            dismissButton = {
                TextButton(onClick = { serviceTargetDeviceId = null }) { Text(strings.cancel) }
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("clinic_devices_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { PrototypeDisclosureBanner(compact = true) }
        item { Text("Mock Device Inventory (${state.devices.size})", style = MaterialTheme.typography.titleMedium) }

        items(state.devices, key = { it.deviceId }) { dev ->
            AirTonCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(dev.serialNumber, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text("${dev.deviceId} · ${dev.modelName}", style = MonoMetadataStyle, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    StatusPill(
                        text = if (dev.needsService) "Service Needed" else dev.assignmentState,
                        icon = if (dev.needsService) Icons.Filled.Build else Icons.Filled.Devices,
                        backgroundColor = if (dev.needsService) semantic.statusErrorBg else semantic.statusNeutralBg,
                        contentColor = if (dev.needsService) semantic.statusErrorContent else semantic.statusNeutralContent
                    )
                }
                HorizontalDivider()
                ProvenanceKeyValueRow("Firmware", dev.firmwareVersion ?: strings.notAvailable, mono = true)
                ProvenanceKeyValueRow("Calibration ID", dev.calibrationId ?: strings.notAvailable, mono = true)
                ProvenanceKeyValueRow("Calibration State", dev.calibrationStatus)
                ProvenanceKeyValueRow("Assigned Subject", dev.assignedSubjectId ?: "Unassigned", mono = true)
                ProvenanceKeyValueRow("Release Status", dev.releaseStatus)
                ProvenanceKeyValueRow("Last Service", dev.lastServiceAt ?: strings.notAvailable, mono = true)

                OutlinedButton(
                    onClick = { serviceTargetDeviceId = dev.deviceId },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.size(6.dp))
                    Text("Append Service Event")
                }
            }
        }

        item {
            Spacer(Modifier.size(4.dp))
            Text("Append-Only Service History (${state.serviceEvents.size})", style = MaterialTheme.typography.titleMedium)
        }

        items(state.serviceEvents, key = { it.serviceEventId }) { ev ->
            AirTonCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(ev.eventType, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Text(ev.deviceId, style = MonoMetadataStyle, color = MaterialTheme.colorScheme.primary)
                }
                Text("${ev.actorName} · ${ev.performedAt}", style = MonoMetadataStyle, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(ev.summary, style = MaterialTheme.typography.bodySmall)
                ProvenanceKeyValueRow("Calibration Ref", ev.calibrationRef ?: strings.notAvailable, mono = true)
            }
        }
    }
}

@Composable
fun ClinicOpsQueueScreen(
    state: AirTonUiState
) {
    val semantic = LocalAirTonColors.current
    val serviceNeededDevices = state.devices.filter { it.needsService }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("clinic_ops_queue_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { PrototypeDisclosureBanner(compact = true) }

        item {
            AirTonCard {
                Text("Operational Workflow Counts (Demo)", style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "Tracks device-service and data-transport status only. Commercial revenue, MRR, and clinical risk metrics are intentionally excluded.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                HorizontalDivider()
                ProvenanceKeyValueRow("Unsynced records (local/pending/failed)", "${state.unsyncedCount}", mono = true)
                ProvenanceKeyValueRow("Delivered awaiting clinician review", "${state.clinicianReviewQueue.size}", mono = true)
                ProvenanceKeyValueRow("Quarantined payload conflicts", "${state.conflicts.size}", mono = true)
                ProvenanceKeyValueRow("Devices flagged for service", "${serviceNeededDevices.size}", mono = true)
            }
        }

        if (state.conflicts.isNotEmpty()) {
            item { Text("Quarantined Conflicts", style = MaterialTheme.typography.titleMedium) }
            items(state.conflicts, key = { it.conflictId }) { conf ->
                AirTonCard {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Filled.ReportProblem, contentDescription = null, tint = semantic.statusErrorContent)
                        Text("${conf.conflictId} (${conf.measurementId})", style = MonoMetadataStyle, color = semantic.statusErrorContent)
                    }
                    Text(conf.summary, style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        if (serviceNeededDevices.isNotEmpty()) {
            item { Text("Devices Awaiting Service", style = MaterialTheme.typography.titleMedium) }
            items(serviceNeededDevices, key = { it.deviceId }) { dev ->
                AirTonCard {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Filled.Build, contentDescription = null, tint = semantic.statusErrorContent)
                        Text("${dev.serialNumber} (${dev.deviceId})", style = MaterialTheme.typography.titleSmall)
                    }
                    Text(dev.calibrationStatus, style = MaterialTheme.typography.bodySmall, color = semantic.statusErrorContent)
                }
            }
        }

        item {
            AirTonCard {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Filled.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text("Architecture Note", style = MaterialTheme.typography.titleSmall)
                }
                Text(
                    text = "This compact mobile view covers essential clinic/service workflows while keeping repositories cleanly separated for a dedicated web-based administrative console later.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
