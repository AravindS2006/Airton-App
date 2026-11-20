package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhoneDisabled
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.domain.model.ClinicProfile
import com.example.domain.model.ClinicalReviewStatus
import com.example.domain.model.ClinicianProfile
import com.example.domain.model.DemoRole
import com.example.domain.model.EyeSide
import com.example.domain.model.MeasurementEnvelope
import com.example.domain.model.QualityStatus
import com.example.domain.model.TransportStatus
import com.example.i18n.LocalAirTonStrings
import com.example.ui.theme.LocalAirTonColors
import com.example.ui.theme.MonoMetadataStyle

/**
 * Persistent high-visibility disclosure banner using both icon and text (never color alone).
 */
@Composable
fun PrototypeDisclosureBanner(
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    val strings = LocalAirTonStrings.current
    val semantic = LocalAirTonColors.current

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("prototype_disclosure_banner"),
        shape = RoundedCornerShape(12.dp),
        color = semantic.demoBannerBg,
        border = BorderStroke(1.dp, semantic.demoBannerBorder.copy(alpha = 0.55f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = if (compact) 7.dp else 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Science,
                contentDescription = "Simulation prototype indicator",
                tint = semantic.demoBannerContent,
                modifier = Modifier.size(18.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = strings.prototypeBannerTitle,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = semantic.demoBannerContent
                )
                if (!compact) {
                    Text(
                        text = strings.prototypeBannerSubtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = semantic.demoBannerContent.copy(alpha = 0.9f)
                    )
                }
            }
        }
    }
}

@Composable
fun AirTonCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val semantic = LocalAirTonColors.current
    val baseModifier = if (onClick != null) {
        modifier.clickable(onClick = onClick)
    } else modifier

    Card(
        modifier = baseModifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, semantic.cardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            content = content
        )
    }
}

@Composable
fun StatusPill(
    text: String,
    icon: ImageVector,
    backgroundColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.semantics(mergeDescendants = true) {
            contentDescription = text
        },
        shape = RoundedCornerShape(999.dp),
        color = backgroundColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(13.dp)
            )
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = contentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun EyeBadge(eye: EyeSide, modifier: Modifier = Modifier) {
    val semantic = LocalAirTonColors.current
    val tint = if (eye == EyeSide.OD) semantic.odSeries else semantic.osSeries
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = tint.copy(alpha = 0.14f),
        border = BorderStroke(1.dp, tint.copy(alpha = 0.35f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Visibility,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(13.dp)
            )
            Text(
                text = eye.code,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = tint
            )
        }
    }
}

@Composable
fun QualityBadge(status: QualityStatus, modifier: Modifier = Modifier) {
    val semantic = LocalAirTonColors.current
    if (status == QualityStatus.PASS) {
        StatusPill(
            text = "Quality: Accepted",
            icon = Icons.Filled.CheckCircle,
            backgroundColor = semantic.statusOkBg,
            contentColor = semantic.statusOkContent,
            modifier = modifier
        )
    } else {
        StatusPill(
            text = "Quality: Rejected",
            icon = Icons.Filled.ErrorOutline,
            backgroundColor = semantic.statusErrorBg,
            contentColor = semantic.statusErrorContent,
            modifier = modifier
        )
    }
}

@Composable
fun TransportBadge(status: TransportStatus, modifier: Modifier = Modifier) {
    val semantic = LocalAirTonColors.current
    val (icon, bg, fg) = when (status) {
        TransportStatus.LOCAL_ONLY -> Triple(Icons.Filled.CloudOff, semantic.statusNeutralBg, semantic.statusNeutralContent)
        TransportStatus.PENDING -> Triple(Icons.Filled.CloudQueue, semantic.demoBannerBg, semantic.demoBannerContent)
        TransportStatus.DELIVERED -> Triple(Icons.Filled.CloudDone, semantic.statusOkBg, semantic.statusOkContent)
        TransportStatus.FAILED -> Triple(Icons.Filled.WarningAmber, semantic.statusErrorBg, semantic.statusErrorContent)
        TransportStatus.QUARANTINED -> Triple(Icons.Filled.ReportProblem, semantic.statusErrorBg, semantic.statusErrorContent)
    }
    StatusPill(
        text = "Sync: ${status.labelEn}",
        icon = icon,
        backgroundColor = bg,
        contentColor = fg,
        modifier = modifier
    )
}

@Composable
fun ReviewBadge(status: ClinicalReviewStatus, modifier: Modifier = Modifier) {
    val semantic = LocalAirTonColors.current
    val (icon, bg, fg) = when (status) {
        ClinicalReviewStatus.NOT_APPLICABLE -> Triple(Icons.Filled.Schedule, semantic.statusNeutralBg, semantic.statusNeutralContent)
        ClinicalReviewStatus.AWAITING_REVIEW -> Triple(Icons.Filled.Schedule, semantic.demoBannerBg, semantic.demoBannerContent)
        ClinicalReviewStatus.REVIEWED -> Triple(Icons.Filled.CheckCircle, semantic.statusOkBg, semantic.statusOkContent)
        ClinicalReviewStatus.FOLLOW_UP_REQUESTED -> Triple(Icons.Filled.Flag, semantic.demoBannerBg, semantic.demoBannerContent)
    }
    StatusPill(
        text = "Review: ${status.labelEn}",
        icon = icon,
        backgroundColor = bg,
        contentColor = fg,
        modifier = modifier
    )
}

/**
 * Displays all 3 decoupled AirTon state dimensions separately, never collapsing them into one badge.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun IndependentStateBadgeRow(
    envelope: MeasurementEnvelope,
    modifier: Modifier = Modifier
) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        QualityBadge(envelope.measurement.qualityStatus)
        TransportBadge(envelope.transportStatus)
        ReviewBadge(envelope.reviewStatus)
    }
}

@Composable
fun ProvenanceKeyValueRow(
    label: String,
    value: String,
    mono: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(0.42f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = value,
            style = if (mono) MonoMetadataStyle else MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(0.58f)
        )
    }
}

@Composable
fun EmptyOrOfflineStateCard(
    icon: ImageVector,
    title: String,
    description: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    AirTonCard {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(text = title, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        if (actionLabel != null && onAction != null) {
            TextButton(onClick = onAction) {
                Text(actionLabel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AirTonTopBar(
    title: String,
    currentRole: DemoRole,
    unreadInboxCount: Int,
    showBack: Boolean = false,
    onBack: (() -> Unit)? = null,
    onOpenRoleSwitcher: () -> Unit,
    onOpenInbox: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val strings = LocalAirTonStrings.current
    val roleShort = when (currentRole) {
        DemoRole.PATIENT -> strings.rolePatient.substringBefore(" /")
        DemoRole.CLINICIAN -> strings.roleClinician
        DemoRole.CLINIC_SERVICE -> strings.roleClinicService.substringBefore(" /")
    }

    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            titleContentColor = MaterialTheme.colorScheme.onBackground
        ),
        navigationIcon = {
            if (showBack && onBack != null) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("top_bar_back_button")
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = strings.back)
                }
            }
        },
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        actions = {
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .clickable(onClick = onOpenRoleSwitcher)
                    .testTag("role_switcher_chip"),
                shape = RoundedCornerShape(999.dp),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.SwapHoriz,
                        contentDescription = strings.switchPersona,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = roleShort,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        maxLines = 1
                    )
                }
            }

            IconButton(
                onClick = onOpenInbox,
                modifier = Modifier.testTag("open_inbox_button")
            ) {
                BadgedBox(
                    badge = {
                        if (unreadInboxCount > 0) {
                            Badge { Text(unreadInboxCount.toString()) }
                        }
                    }
                ) {
                    Icon(Icons.Filled.Notifications, contentDescription = strings.inboxTitle)
                }
            }

            IconButton(
                onClick = onOpenSettings,
                modifier = Modifier.testTag("open_settings_button")
            ) {
                Icon(Icons.Filled.Settings, contentDescription = strings.settingsTitle)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoleSwitcherBottomSheet(
    currentRole: DemoRole,
    onSelectRole: (DemoRole) -> Unit,
    onDismiss: () -> Unit
) {
    val strings = LocalAirTonStrings.current
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Demo Role Switcher",
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = strings.rolePreviewNotice,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            RoleOptionRow(
                icon = Icons.Filled.Person,
                title = strings.rolePatient,
                subtitle = "Guided simulation, OD/OS history, read-only care plan, sync status",
                selected = currentRole == DemoRole.PATIENT,
                testTag = "switch_role_patient",
                onClick = { onSelectRole(DemoRole.PATIENT) }
            )
            RoleOptionRow(
                icon = Icons.Filled.MedicalServices,
                title = strings.roleClinician,
                subtitle = "Assigned roster, review queue, append-only notes, illustrative care plans",
                selected = currentRole == DemoRole.CLINICIAN,
                testTag = "switch_role_clinician",
                onClick = { onSelectRole(DemoRole.CLINICIAN) }
            )
            RoleOptionRow(
                icon = Icons.Filled.Build,
                title = strings.roleClinicService,
                subtitle = "Patient enrolment, device fleet inventory, service logs, ops queue",
                selected = currentRole == DemoRole.CLINIC_SERVICE,
                testTag = "switch_role_clinic",
                onClick = { onSelectRole(DemoRole.CLINIC_SERVICE) }
            )
        }
    }
}

@Composable
private fun RoleOptionRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    selected: Boolean,
    testTag: String,
    onClick: () -> Unit
) {
    val borderColor = if (selected) MaterialTheme.colorScheme.primary else LocalAirTonColors.current.cardBorder
    val bgColor = if (selected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
    else MaterialTheme.colorScheme.surface

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(bgColor)
            .border(1.5.dp, borderColor, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(14.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (selected) {
            Icon(
                Icons.Filled.CheckCircle,
                contentDescription = "Selected role",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimulatedContactBottomSheet(
    clinic: ClinicProfile?,
    clinician: ClinicianProfile?,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(Icons.Filled.PhoneDisabled, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text("Simulated Care-Team Contact Sheet", style = MaterialTheme.typography.titleLarge)
            }
            PrototypeDisclosureBanner(compact = true)
            Text(
                text = "Demo contact actions do not place phone calls, send SMS, or contact a real clinic or physician.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            HorizontalDivider()
            if (clinician != null) {
                ProvenanceKeyValueRow("Assigned Clinician", clinician.fullName)
                ProvenanceKeyValueRow("Role", clinician.roleTitle)
                ProvenanceKeyValueRow("Demo Desk", clinician.demoContactLabel)
            }
            if (clinic != null) {
                ProvenanceKeyValueRow("Clinic", clinic.name)
                ProvenanceKeyValueRow("Hours", clinic.demoHoursLabel)
                ProvenanceKeyValueRow("Demo Number", clinic.demoPhoneLabel)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Filled.Info, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                Text(
                    text = "For real eye discomfort or vision changes, contact your actual treating eye care provider directly.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
