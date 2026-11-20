package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AssignmentInd
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.outlined.AssignmentInd
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Devices
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.FactCheck
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocalHospital
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.domain.model.DemoRole
import com.example.i18n.AirTonStrings
import com.example.i18n.LocalAirTonStrings
import com.example.i18n.stringsFor
import com.example.ui.components.AirTonTopBar
import com.example.ui.components.RoleSwitcherBottomSheet
import com.example.ui.screens.clinic.ClinicAssignmentsScreen
import com.example.ui.screens.clinic.ClinicDevicesScreen
import com.example.ui.screens.clinic.ClinicOpsQueueScreen
import com.example.ui.screens.clinic.ClinicOverviewScreen
import com.example.ui.screens.clinician.ClinicianCarePlanEditorScreen
import com.example.ui.screens.clinician.ClinicianOverviewScreen
import com.example.ui.screens.clinician.ClinicianPatientDetailScreen
import com.example.ui.screens.clinician.ClinicianPatientRosterScreen
import com.example.ui.screens.clinician.ClinicianRecordReviewScreen
import com.example.ui.screens.clinician.ClinicianReviewQueueScreen
import com.example.ui.screens.patient.GuidedMeasurementScreen
import com.example.ui.screens.patient.MeasurementDetailScreen
import com.example.ui.screens.patient.PatientCarePlanScreen
import com.example.ui.screens.patient.PatientHistoryScreen
import com.example.ui.screens.patient.PatientHomeScreen
import com.example.ui.screens.patient.PatientSyncStatusScreen
import com.example.ui.screens.shared.AboutLimitationsScreen
import com.example.ui.screens.shared.DemoRoleSelectorScreen
import com.example.ui.screens.shared.NotificationsInboxScreen
import com.example.ui.screens.shared.ProfileSettingsScreen
import com.example.ui.screens.shared.SplashScreen
import com.example.ui.screens.shared.WelcomeDisclosureScreen
import com.example.ui.theme.AirTonTheme
import com.example.viewmodel.AirTonViewModel

private enum class LaunchStage {
    SPLASH,
    WELCOME_DISCLOSURE,
    ROLE_SELECTOR,
    MAIN_APP
}

private sealed interface OverlayDestination {
    data class MeasurementDetail(val measurementId: String) : OverlayDestination
    data class ClinicianPatientDetail(val subjectId: String) : OverlayDestination
    data class ClinicianRecordReview(val measurementId: String) : OverlayDestination
    data class ClinicianCarePlanEditor(val subjectId: String?) : OverlayDestination
    data object NotificationsInbox : OverlayDestination
    data object ProfileSettings : OverlayDestination
    data object AboutLimitations : OverlayDestination
}

private data class NavTab(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
)

@Composable
fun AirTonApp(
    viewModel: AirTonViewModel = viewModel(),
    skipSplashForTest: Boolean = false
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val strings = stringsFor(state.preferences.language)

    var launchStage by remember {
        mutableStateOf(if (skipSplashForTest) LaunchStage.WELCOME_DISCLOSURE else LaunchStage.SPLASH)
    }

    // If the user previously completed disclosure on a prior session, skip directly to main app after splash
    LaunchedEffect(launchStage, state.preferences.disclosureAcknowledged) {
        if (launchStage == LaunchStage.WELCOME_DISCLOSURE && state.preferences.disclosureAcknowledged && !skipSplashForTest) {
            launchStage = LaunchStage.MAIN_APP
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(Unit) {
        viewModel.snackbarEvents.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    AirTonTheme(
        themePreference = state.preferences.themePreference,
        textScalePreset = state.preferences.textScalePreset
    ) {
        CompositionLocalProvider(LocalAirTonStrings provides strings) {
            when (launchStage) {
                LaunchStage.SPLASH -> {
                    SplashScreen(
                        reducedMotion = state.preferences.reducedMotionEnabled,
                        onFinished = {
                            launchStage = if (state.preferences.disclosureAcknowledged) {
                                LaunchStage.MAIN_APP
                            } else {
                                LaunchStage.WELCOME_DISCLOSURE
                            }
                        }
                    )
                }

                LaunchStage.WELCOME_DISCLOSURE -> {
                    WelcomeDisclosureScreen(
                        onContinueToRoleSelect = { launchStage = LaunchStage.ROLE_SELECTOR }
                    )
                }

                LaunchStage.ROLE_SELECTOR -> {
                    BackHandler { launchStage = LaunchStage.WELCOME_DISCLOSURE }
                    DemoRoleSelectorScreen(
                        onSelectRole = { chosenRole ->
                            viewModel.completeDisclosureAndEnterRole(chosenRole)
                            launchStage = LaunchStage.MAIN_APP
                        }
                    )
                }

                LaunchStage.MAIN_APP -> {
                    MainRoleShell(
                        viewModel = viewModel,
                        strings = strings,
                        snackbarHostState = snackbarHostState
                    )
                }
            }
        }
    }
}

@Composable
private fun MainRoleShell(
    viewModel: AirTonViewModel,
    strings: AirTonStrings,
    snackbarHostState: SnackbarHostState
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val currentRole = state.preferences.selectedRole

    var selectedTabIndex by remember(currentRole) { mutableIntStateOf(0) }
    val overlayStack = remember { mutableStateListOf<OverlayDestination>() }
    var showRoleSwitcherSheet by remember { mutableStateOf(false) }

    val currentOverlay = overlayStack.lastOrNull()

    // Ensure hardware back button pops overlays cleanly before returning to home tab
    BackHandler(enabled = currentOverlay != null || selectedTabIndex != 0) {
        if (overlayStack.isNotEmpty()) {
            overlayStack.removeAt(overlayStack.lastIndex)
        } else if (selectedTabIndex != 0) {
            selectedTabIndex = 0
        }
    }

    val tabs = remember(currentRole, strings) {
        when (currentRole) {
            DemoRole.PATIENT -> listOf(
                NavTab(strings.navHome, Icons.Filled.Home, Icons.Outlined.Home, "nav_patient_home"),
                NavTab(strings.navMeasure, Icons.Filled.Science, Icons.Outlined.Science, "nav_patient_measure"),
                NavTab(strings.navHistory, Icons.Filled.History, Icons.Outlined.History, "nav_patient_history"),
                NavTab(strings.navCare, Icons.Filled.LocalHospital, Icons.Outlined.LocalHospital, "nav_patient_care"),
                NavTab(strings.navSync, Icons.Filled.Sync, Icons.Outlined.Sync, "nav_patient_sync")
            )

            DemoRole.CLINICIAN -> listOf(
                NavTab(strings.navOverview, Icons.Filled.Dashboard, Icons.Outlined.Dashboard, "nav_clinician_overview"),
                NavTab(strings.navPatients, Icons.Filled.People, Icons.Outlined.People, "nav_clinician_patients"),
                NavTab(strings.navQueue, Icons.Filled.FactCheck, Icons.Outlined.FactCheck, "nav_clinician_queue"),
                NavTab(strings.navCarePlans, Icons.Filled.EditNote, Icons.Outlined.EditNote, "nav_clinician_plans")
            )

            DemoRole.CLINIC_SERVICE -> listOf(
                NavTab(strings.navOverview, Icons.Filled.Dashboard, Icons.Outlined.Dashboard, "nav_clinic_overview"),
                NavTab(strings.navAssignments, Icons.Filled.AssignmentInd, Icons.Outlined.AssignmentInd, "nav_clinic_assignments"),
                NavTab(strings.navDevices, Icons.Filled.Devices, Icons.Outlined.Devices, "nav_clinic_devices"),
                NavTab(strings.navOpsQueue, Icons.Filled.Build, Icons.Outlined.Build, "nav_clinic_ops")
            )
        }
    }

    val safeTabIndex = selectedTabIndex.coerceIn(0, (tabs.size - 1).coerceAtLeast(0))

    val screenTitle = when (currentOverlay) {
        is OverlayDestination.MeasurementDetail -> "Record Provenance"
        is OverlayDestination.ClinicianPatientDetail -> "Patient History"
        is OverlayDestination.ClinicianRecordReview -> "Record Review"
        is OverlayDestination.ClinicianCarePlanEditor -> "Care-Plan Editor"
        OverlayDestination.NotificationsInbox -> strings.inboxTitle
        OverlayDestination.ProfileSettings -> strings.settingsTitle
        OverlayDestination.AboutLimitations -> strings.aboutTitle
        null -> tabs[safeTabIndex].label
    }

    if (showRoleSwitcherSheet) {
        RoleSwitcherBottomSheet(
            currentRole = currentRole,
            onSelectRole = { nextRole ->
                showRoleSwitcherSheet = false
                overlayStack.clear()
                selectedTabIndex = 0
                viewModel.switchDemoRole(nextRole)
            },
            onDismiss = { showRoleSwitcherSheet = false }
        )
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useNavRail = maxWidth >= 640.dp

        Scaffold(
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            topBar = {
                AirTonTopBar(
                    title = screenTitle,
                    currentRole = currentRole,
                    unreadInboxCount = state.unreadNotificationCount,
                    showBack = currentOverlay != null,
                    onBack = {
                        if (overlayStack.isNotEmpty()) overlayStack.removeAt(overlayStack.lastIndex)
                    },
                    onOpenRoleSwitcher = { showRoleSwitcherSheet = true },
                    onOpenInbox = {
                        if (currentOverlay != OverlayDestination.NotificationsInbox) {
                            overlayStack.add(OverlayDestination.NotificationsInbox)
                        }
                    },
                    onOpenSettings = {
                        if (currentOverlay != OverlayDestination.ProfileSettings) {
                            overlayStack.add(OverlayDestination.ProfileSettings)
                        }
                    }
                )
            },
            bottomBar = {
                if (!useNavRail && currentOverlay == null) {
                    NavigationBar {
                        tabs.forEachIndexed { idx, tab ->
                            val selected = idx == safeTabIndex
                            NavigationBarItem(
                                selected = selected,
                                onClick = { selectedTabIndex = idx },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = tab.label
                                    )
                                },
                                label = { Text(tab.label, maxLines = 1) },
                                modifier = Modifier.testTag(tab.testTag)
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(innerPadding)
            ) {
                if (useNavRail && currentOverlay == null) {
                    NavigationRail(modifier = Modifier.fillMaxHeight()) {
                        tabs.forEachIndexed { idx, tab ->
                            val selected = idx == safeTabIndex
                            NavigationRailItem(
                                selected = selected,
                                onClick = { selectedTabIndex = idx },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = tab.label
                                    )
                                },
                                label = { Text(tab.label) },
                                modifier = Modifier.testTag(tab.testTag)
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxHeight()
                            .widthIn(max = 720.dp)
                            .fillMaxWidth()
                    ) {
                        state.errorMessage?.let { err ->
                            Surface(
                                color = MaterialTheme.colorScheme.errorContainer,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = err,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onErrorContainer,
                                        modifier = Modifier.weight(1f)
                                    )
                                    TextButton(onClick = viewModel::dismissError) {
                                        Text("Dismiss")
                                    }
                                }
                            }
                        }

                        if (state.isLoading) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        } else if (currentOverlay != null) {
                            when (currentOverlay) {
                                is OverlayDestination.MeasurementDetail -> {
                                    val env = state.allEnvelopes.firstOrNull {
                                        it.measurement.measurementId == currentOverlay.measurementId
                                    }
                                    val patientName = env?.let { e ->
                                        state.patients.firstOrNull { it.subjectId == e.measurement.subjectId }?.fullName
                                    }
                                    MeasurementDetailScreen(envelope = env, patientName = patientName)
                                }

                                is OverlayDestination.ClinicianPatientDetail -> {
                                    val p = state.patients.firstOrNull { it.subjectId == currentOverlay.subjectId }
                                    ClinicianPatientDetailScreen(
                                        patient = p,
                                        state = state,
                                        onEditCarePlan = { subj ->
                                            overlayStack.add(OverlayDestination.ClinicianCarePlanEditor(subj))
                                        },
                                        onOpenRecordReview = { measId ->
                                            overlayStack.add(OverlayDestination.ClinicianRecordReview(measId))
                                        }
                                    )
                                }

                                is OverlayDestination.ClinicianRecordReview -> {
                                    val env = state.allEnvelopes.firstOrNull {
                                        it.measurement.measurementId == currentOverlay.measurementId
                                    }
                                    ClinicianRecordReviewScreen(
                                        envelope = env,
                                        state = state,
                                        onSubmitAction = viewModel::submitClinicianReviewAction
                                    )
                                }

                                is OverlayDestination.ClinicianCarePlanEditor -> {
                                    ClinicianCarePlanEditorScreen(
                                        initialSubjectId = currentOverlay.subjectId,
                                        state = state,
                                        onSavePlan = viewModel::saveClinicianCarePlan
                                    )
                                }

                                OverlayDestination.NotificationsInbox -> {
                                    NotificationsInboxScreen(
                                        notifications = state.notifications,
                                        onMarkRead = viewModel::markNotificationRead,
                                        onOpenMeasurement = { measId ->
                                            if (currentRole == DemoRole.CLINICIAN) {
                                                overlayStack.add(OverlayDestination.ClinicianRecordReview(measId))
                                            } else {
                                                overlayStack.add(OverlayDestination.MeasurementDetail(measId))
                                            }
                                        }
                                    )
                                }

                                OverlayDestination.ProfileSettings -> {
                                    ProfileSettingsScreen(
                                        state = state,
                                        onSelectRole = { role ->
                                            overlayStack.clear()
                                            selectedTabIndex = 0
                                            viewModel.switchDemoRole(role)
                                        },
                                        onSelectPatient = viewModel::selectActivePatient,
                                        onSelectClinician = viewModel::selectActiveClinician,
                                        onSelectLanguage = viewModel::setLanguage,
                                        onSelectTheme = viewModel::setThemePreference,
                                        onSelectTextScale = viewModel::setTextScalePreset,
                                        onToggleReminders = viewModel::setLocalRemindersEnabled,
                                        onToggleReducedMotion = viewModel::setReducedMotionEnabled,
                                        onOpenAbout = { overlayStack.add(OverlayDestination.AboutLimitations) },
                                        onResetDemoData = viewModel::resetDemoData
                                    )
                                }

                                OverlayDestination.AboutLimitations -> {
                                    AboutLimitationsScreen()
                                }
                            }
                        } else {
                            when (currentRole) {
                                DemoRole.PATIENT -> when (safeTabIndex) {
                                    0 -> PatientHomeScreen(
                                        state = state,
                                        onSelectPatient = viewModel::selectActivePatient,
                                        onStartSimulatedSession = {
                                            viewModel.resetGuidedSession()
                                            selectedTabIndex = 1
                                        },
                                        onOpenHistory = { selectedTabIndex = 2 },
                                        onOpenMeasurementDetail = { id ->
                                            overlayStack.add(OverlayDestination.MeasurementDetail(id))
                                        },
                                        onOpenSync = { selectedTabIndex = 4 },
                                        onOpenCarePlan = { selectedTabIndex = 3 }
                                    )

                                    1 -> GuidedMeasurementScreen(
                                        state = state,
                                        onStepChange = viewModel::setGuidedStep,
                                        onSelectEye = viewModel::selectGuidedEye,
                                        onSelectScenario = viewModel::selectSimulationScenario,
                                        onAlignmentChange = viewModel::updateAlignmentReticle,
                                        onSnapCenter = viewModel::snapReticleToCenter,
                                        onRunAcquisition = viewModel::runSimulatedAcquisition,
                                        onResetSession = { viewModel.resetGuidedSession() },
                                        onInspectSavedRecord = { id ->
                                            overlayStack.add(OverlayDestination.MeasurementDetail(id))
                                        },
                                        onExitToHome = { selectedTabIndex = 0 }
                                    )

                                    2 -> PatientHistoryScreen(
                                        state = state,
                                        onUpdateFilter = viewModel::updateHistoryFilter,
                                        onClearFilters = viewModel::clearHistoryFilters,
                                        onOpenDetail = { id ->
                                            overlayStack.add(OverlayDestination.MeasurementDetail(id))
                                        }
                                    )

                                    3 -> PatientCarePlanScreen(state = state)

                                    4 -> PatientSyncStatusScreen(
                                        state = state,
                                        onToggleOffline = viewModel::setSimulatedOfflineMode,
                                        onRetrySync = viewModel::retryPendingSync,
                                        onTriggerDuplicateRetry = viewModel::triggerDuplicateRetryDemo,
                                        onTriggerConflictReplay = viewModel::triggerConflictingReplayDemo,
                                        onOpenMeasurementDetail = { id ->
                                            overlayStack.add(OverlayDestination.MeasurementDetail(id))
                                        }
                                    )
                                }

                                DemoRole.CLINICIAN -> when (safeTabIndex) {
                                    0 -> ClinicianOverviewScreen(
                                        state = state,
                                        onOpenQueue = { selectedTabIndex = 2 },
                                        onOpenPatients = { selectedTabIndex = 1 },
                                        onOpenRecordReview = { id ->
                                            overlayStack.add(OverlayDestination.ClinicianRecordReview(id))
                                        }
                                    )

                                    1 -> ClinicianPatientRosterScreen(
                                        state = state,
                                        onOpenPatientDetail = { subj ->
                                            overlayStack.add(OverlayDestination.ClinicianPatientDetail(subj))
                                        }
                                    )

                                    2 -> ClinicianReviewQueueScreen(
                                        state = state,
                                        onOpenRecordReview = { id ->
                                            overlayStack.add(OverlayDestination.ClinicianRecordReview(id))
                                        }
                                    )

                                    3 -> ClinicianCarePlanEditorScreen(
                                        initialSubjectId = null,
                                        state = state,
                                        onSavePlan = viewModel::saveClinicianCarePlan
                                    )
                                }

                                DemoRole.CLINIC_SERVICE -> when (safeTabIndex) {
                                    0 -> ClinicOverviewScreen(
                                        state = state,
                                        onOpenAssignments = { selectedTabIndex = 1 },
                                        onOpenDevices = { selectedTabIndex = 2 },
                                        onOpenOpsQueue = { selectedTabIndex = 3 }
                                    )

                                    1 -> ClinicAssignmentsScreen(
                                        state = state,
                                        onAssignDevice = { devId, subjId ->
                                            viewModel.assignDeviceToPatient(devId, subjId)
                                        }
                                    )

                                    2 -> ClinicDevicesScreen(
                                        state = state,
                                        onLogServiceEvent = viewModel::logDeviceServiceEvent
                                    )

                                    3 -> ClinicOpsQueueScreen(state = state)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
