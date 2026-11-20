package com.example.ui.screens.shared

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.domain.model.AppLanguage
import com.example.domain.model.AppNotification
import com.example.domain.model.DemoRole
import com.example.domain.model.NotificationCategory
import com.example.domain.model.TextScalePreset
import com.example.domain.model.ThemePreference
import com.example.i18n.LocalAirTonStrings
import com.example.ui.components.AirTonCard
import com.example.ui.components.EmptyOrOfflineStateCard
import com.example.ui.components.PrototypeDisclosureBanner
import com.example.ui.components.ProvenanceKeyValueRow
import com.example.ui.components.StatusPill
import com.example.ui.theme.AirTonBlue
import com.example.ui.theme.ClinicalTeal
import com.example.ui.theme.DeepNavyInk
import com.example.ui.theme.LocalAirTonColors
import com.example.ui.theme.MonoMetadataStyle
import com.example.viewmodel.AirTonUiState
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    reducedMotion: Boolean,
    onFinished: () -> Unit
) {
    LaunchedEffect(Unit) {
        delay(if (reducedMotion) 200L else 900L)
        onFinished()
    }

    val pulse = if (reducedMotion) {
        1f
    } else {
        val transition = rememberInfiniteTransition(label = "splash_halo")
        val animated by transition.animateFloat(
            initialValue = 0.96f,
            targetValue = 1.05f,
            animationSpec = infiniteRepeatable(
                animation = tween(1200),
                repeatMode = RepeatMode.Reverse
            ),
            label = "splash_scale"
        )
        animated
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF0A1B30), DeepNavyInk, Color(0xFF123A66))
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = DeepNavyInk,
                border = BorderStroke(1.5.dp, Color(0xFF5EEAD4).copy(alpha = 0.4f)),
                shadowElevation = 12.dp,
                modifier = Modifier
                    .size(104.dp)
                    .scale(pulse)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.airton_icon_fg_1795199063632),
                    contentDescription = "AirTon optical emblem",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "AirTon",
                    style = MaterialTheme.typography.displaySmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Home Eye-Pressure Monitoring Companion",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.88f),
                    textAlign = TextAlign.Center
                )
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = Color.White.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = "PROTOTYPE · SIMULATION ONLY",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF99F6E4),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun WelcomeDisclosureScreen(
    onContinueToRoleSelect: () -> Unit
) {
    val strings = LocalAirTonStrings.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .widthIn(max = 620.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.size(48.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.airton_icon_fg_1795199063632),
                        contentDescription = "AirTon icon",
                        contentScale = ContentScale.Crop
                    )
                }
                Column {
                    Text(
                        text = "AirTon Mobile",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Home IOP Monitoring Companion · Prototype",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            PrototypeDisclosureBanner(compact = false)

            // Conceptual Optical Schematic Hero Card
            Surface(
                shape = MaterialTheme.shapes.medium,
                border = BorderStroke(1.dp, LocalAirTonColors.current.cardBorder),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Image(
                        painter = painterResource(id = R.drawable.img_concept_optics_1795199062846),
                        contentDescription = "Conceptual non-contact optical eye alignment schematic",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(152.dp)
                    )
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Intended Clinical Workflow",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Built for patients already under a treating doctor's care: captures eye-specific (OD / OS) records, preserves acquisition quality and provenance, and surfaces delivered sessions for clinician review.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            AirTonCard {
                Text(
                    text = "Non-Negotiable Prototype Boundaries",
                    style = MaterialTheme.typography.titleMedium
                )
                BoundaryBullet("No physical AirTon device, Bluetooth link, or air-puff stimulus is connected or operated.")
                BoundaryBullet("Does not diagnose, screen for, confirm, or rule out glaucoma or any ocular disease.")
                BoundaryBullet("Does not show normal/abnormal badges, clinical thresholds, or treatment advice.")
                BoundaryBullet("Uses local deterministic fictional fixtures only — never enter real patient data.")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = onContinueToRoleSelect,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .widthIn(max = 620.dp)
                .fillMaxWidth()
                .height(52.dp)
                .testTag("explore_demo_button")
        ) {
            Text(strings.exploreDemo, style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.size(8.dp))
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun BoundaryBullet(text: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = Icons.Filled.Shield,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .padding(top = 2.dp)
                .size(16.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun DemoRoleSelectorScreen(
    onSelectRole: (DemoRole) -> Unit
) {
    val strings = LocalAirTonStrings.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier.widthIn(max = 620.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            PrototypeDisclosureBanner(compact = true)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Select Demo Space",
                    style = MaterialTheme.typography.headlineMedium
                )
                Text(
                    text = strings.rolePreviewNotice,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            PersonaCard(
                icon = Icons.Filled.Person,
                accent = AirTonBlue,
                title = strings.rolePatient,
                tag = "Primary Home Workflow",
                description = "Calm home dashboard, 7-step guided simulated session with interactive alignment, separate OD/OS history, read-only care plan, and sync sandbox.",
                testTag = "select_role_patient",
                onClick = { onSelectRole(DemoRole.PATIENT) }
            )
            PersonaCard(
                icon = Icons.Filled.MedicalServices,
                accent = ClinicalTeal,
                title = strings.roleClinician,
                tag = "Care-Team Review",
                description = "Searchable assigned roster, separate OD/OS histories including rejected acquisitions, delivered-pending review queue, append-only notes, and null-safe care plans.",
                testTag = "select_role_clinician",
                onClick = { onSelectRole(DemoRole.CLINICIAN) }
            )
            PersonaCard(
                icon = Icons.Filled.Build,
                accent = MaterialTheme.colorScheme.onSurfaceVariant,
                title = strings.roleClinicService,
                tag = "Depot & Operations",
                description = "Clinic overview, patient enrolment and device allocation, hardware inventory with honest 'Not available' states, append-only service logs, and ops queue.",
                testTag = "select_role_clinic",
                onClick = { onSelectRole(DemoRole.CLINIC_SERVICE) }
            )
        }
    }
}

@Composable
private fun PersonaCard(
    icon: ImageVector,
    accent: Color,
    title: String,
    tag: String,
    description: String,
    testTag: String,
    onClick: () -> Unit
) {
    AirTonCard(
        modifier = Modifier.testTag(testTag),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(accent.copy(alpha = 0.13f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = accent)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "$tag · Demo preview",
                    style = MaterialTheme.typography.labelSmall,
                    color = accent
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
        }
        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun NotificationsInboxScreen(
    notifications: List<AppNotification>,
    onMarkRead: (String) -> Unit,
    onOpenMeasurement: (String) -> Unit
) {
    val semantic = LocalAirTonColors.current

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { PrototypeDisclosureBanner(compact = true) }
        item {
            Text(
                text = "Local demo reminders and workflow updates only. Not an urgent clinical alert service.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (notifications.isEmpty()) {
            item {
                EmptyOrOfflineStateCard(
                    icon = Icons.Filled.NotificationsNone,
                    title = "No demo notifications",
                    description = "Simulated sync acknowledgements and clinician review events will appear here."
                )
            }
        } else {
            items(notifications, key = { it.notificationId }) { notif ->
                val (bg, fg) = when (notif.category) {
                    NotificationCategory.DEMO_REMINDER -> semantic.statusNeutralBg to semantic.statusNeutralContent
                    NotificationCategory.SYNC_EVENT -> semantic.demoBannerBg to semantic.demoBannerContent
                    NotificationCategory.CLINICIAN_REVIEW -> semantic.statusOkBg to semantic.statusOkContent
                }
                AirTonCard(
                    onClick = {
                        onMarkRead(notif.notificationId)
                        notif.relatedMeasurementId?.let(onOpenMeasurement)
                    }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StatusPill(
                            text = notif.category.labelEn,
                            icon = Icons.Filled.Info,
                            backgroundColor = bg,
                            contentColor = fg
                        )
                        if (!notif.isRead) {
                            Box(
                                modifier = Modifier
                                    .size(9.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                        }
                    }
                    Text(
                        text = notif.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = if (notif.isRead) FontWeight.Medium else FontWeight.Bold
                    )
                    Text(
                        text = notif.body,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = notif.createdAt,
                        style = MonoMetadataStyle,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileSettingsScreen(
    state: AirTonUiState,
    onSelectRole: (DemoRole) -> Unit,
    onSelectPatient: (String) -> Unit,
    onSelectClinician: (String) -> Unit,
    onSelectLanguage: (AppLanguage) -> Unit,
    onSelectTheme: (ThemePreference) -> Unit,
    onSelectTextScale: (TextScalePreset) -> Unit,
    onToggleReminders: (Boolean) -> Unit,
    onToggleReducedMotion: (Boolean) -> Unit,
    onOpenAbout: () -> Unit,
    onResetDemoData: () -> Unit
) {
    val strings = LocalAirTonStrings.current
    var showResetDialog by remember { mutableStateOf(false) }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            icon = { Icon(Icons.Filled.WarningAmber, contentDescription = null) },
            title = { Text("Reset local demo data?") },
            text = {
                Text("This clears your local simulated measurements, appended clinician notes, care-plan edits, and service entries inside this app and restores the deterministic baseline fixtures. No files outside this app are affected.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showResetDialog = false
                        onResetDemoData()
                    },
                    modifier = Modifier.testTag("confirm_reset_demo_button")
                ) {
                    Text(strings.confirm)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text(strings.cancel)
                }
            }
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { PrototypeDisclosureBanner(compact = false) }

        item {
            AirTonCard {
                Text("Active Demo Persona", style = MaterialTheme.typography.titleMedium)
                Text(
                    text = strings.rolePreviewNotice,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    DemoRole.entries.forEach { role ->
                        val label = when (role) {
                            DemoRole.PATIENT -> "Patient"
                            DemoRole.CLINICIAN -> "Clinician"
                            DemoRole.CLINIC_SERVICE -> "Clinic"
                        }
                        FilterChip(
                            selected = state.preferences.selectedRole == role,
                            onClick = { onSelectRole(role) },
                            label = { Text(label) },
                            modifier = Modifier.testTag("settings_role_${role.name.lowercase()}")
                        )
                    }
                }
            }
        }

        item {
            AirTonCard {
                Text("Active Fictional Patient Profile", style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "Switch between seeded fictional patients (Lakshmi Narayanan has an intentionally unconfigured care plan).",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                state.patients.forEach { patient ->
                    val isSelected = state.preferences.activePatientId == patient.subjectId
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onSelectPatient(patient.subjectId) }
                            .padding(vertical = 8.dp, horizontal = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(patient.fullName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            Text(patient.subjectId, style = MonoMetadataStyle, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        if (isSelected) {
                            Icon(Icons.Filled.CheckCircle, contentDescription = "Selected patient", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
                HorizontalDivider()
                Text("Active Fictional Clinician Profile", style = MaterialTheme.typography.titleSmall)
                state.clinicians.forEach { doc ->
                    val isSelected = state.preferences.activeClinicianId == doc.clinicianId
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onSelectClinician(doc.clinicianId) }
                            .padding(vertical = 6.dp, horizontal = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(doc.fullName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                            Text(doc.roleTitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        if (isSelected) {
                            Icon(Icons.Filled.CheckCircle, contentDescription = "Selected clinician", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }

        item {
            AirTonCard {
                Text("Language & Accessibility", style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "Tamil mode localizes navigation/UI chrome and marks clinical-adjacent notices [Review Required].",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AppLanguage.entries.forEach { lang ->
                        FilterChip(
                            selected = state.preferences.language == lang,
                            onClick = { onSelectLanguage(lang) },
                            label = { Text(if (lang == AppLanguage.ENGLISH) "English" else "தமிழ் (Tamil)") },
                            modifier = Modifier.testTag("lang_chip_${lang.code}")
                        )
                    }
                }

                Text("Text Size", style = MaterialTheme.typography.titleSmall)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextScalePreset.entries.forEach { preset ->
                        FilterChip(
                            selected = state.preferences.textScalePreset == preset,
                            onClick = { onSelectTextScale(preset) },
                            label = { Text(preset.labelEn.substringBefore(" ")) }
                        )
                    }
                }

                Text("Theme", style = MaterialTheme.typography.titleSmall)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ThemePreference.entries.forEach { theme ->
                        FilterChip(
                            selected = state.preferences.themePreference == theme,
                            onClick = { onSelectTheme(theme) },
                            label = { Text(theme.name.lowercase().replaceFirstChar { it.uppercase() }) }
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Local Demo Reminders", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            "Strictly local demo notices — never clinical alerts.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = state.preferences.localDemoRemindersEnabled,
                        onCheckedChange = onToggleReminders
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Reduced Motion", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            "Shortens simulation and transition durations.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = state.preferences.reducedMotionEnabled,
                        onCheckedChange = onToggleReducedMotion
                    )
                }
            }
        }

        item {
            AirTonCard {
                Text("Safety, Version & Demo Reset", style = MaterialTheme.typography.titleMedium)
                ProvenanceKeyValueRow("App Version", "1.0.0-prototype (Android Native)", mono = true)
                ProvenanceKeyValueRow("Synthetic Contract", "v1.0 (Strict schema validation)", mono = true)
                ProvenanceKeyValueRow("Hardware Permissions", "None requested (Zero BLE/Camera/Location)")

                OutlinedButton(
                    onClick = onOpenAbout,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("open_about_button")
                ) {
                    Icon(Icons.Filled.Info, contentDescription = null)
                    Spacer(Modifier.size(8.dp))
                    Text(strings.aboutTitle)
                }

                Button(
                    onClick = { showResetDialog = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = LocalAirTonColors.current.statusErrorBg,
                        contentColor = LocalAirTonColors.current.statusErrorContent
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("reset_demo_data_button")
                ) {
                    Icon(Icons.Filled.RestartAlt, contentDescription = null)
                    Spacer(Modifier.size(8.dp))
                    Text(strings.resetDemoData)
                }
            }
        }
    }
}

@Composable
fun AboutLimitationsScreen() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { PrototypeDisclosureBanner(compact = false) }

        item {
            AirTonCard {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(shape = RoundedCornerShape(12.dp), modifier = Modifier.size(44.dp)) {
                        Image(
                            painter = painterResource(id = R.drawable.airton_icon_fg_1795199063632),
                            contentDescription = "AirTon emblem",
                            contentScale = ContentScale.Crop
                        )
                    }
                    Column {
                        Text("Intended Use (Proposed Future Product)", style = MaterialTheme.typography.titleMedium)
                        Text("Home IOP monitoring under clinician care", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Text(
                    text = "AirTon is intended to become a portable, affordable, non-contact home intraocular-pressure (IOP) monitoring companion for patients already under a treating doctor's care. Patients follow a clinician-established schedule while the system preserves eye-specific provenance and quality states.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            AirTonCard {
                Text("Current Prototype Boundaries", style = MaterialTheme.typography.titleMedium)
                BoundaryBullet("No verified physical AirTon pressure-estimation model or clinically cleared device exists in this build.")
                BoundaryBullet("No Bluetooth Low Energy (BLE) GATT contract, hardware interlock, pneumatic limit, or optical exposure limit is implemented.")
                BoundaryBullet("Monitoring schedules, repetitions, retry caps, clinical alert thresholds, and calibration intervals remain null / 'Not configured' unless explicitly entered in demo mode.")
                BoundaryBullet("Acquisition quality, transport delivery, and clinician review are strictly independent states.")
            }
        }

        item {
            AirTonCard {
                Text("Privacy & Data Handling Statement", style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "This prototype stores deterministic fictional records strictly on-device in a local SQLite (Room) database. No cloud backend, analytics SDK, tracking pixel, or third-party upload endpoint is connected. Do not enter real patient names or real medical readings.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            AirTonCard {
                Text("Integration Gates Required Before Real Use", style = MaterialTheme.typography.titleMedium)
                ProvenanceKeyValueRow("Hardware / BLE", "Approved GATT specification, safety interlocks, firmware signing")
                ProvenanceKeyValueRow("Pressure Model", "Calibrated physical IOP estimation & clinical validation")
                ProvenanceKeyValueRow("Cloud & Auth", "HIPAA/DISHA-compliant authenticated API, patient/clinician IAM")
                ProvenanceKeyValueRow("Regulatory", "Formal clinical trial evidence & regulatory release authorization")
            }
        }
    }
}
