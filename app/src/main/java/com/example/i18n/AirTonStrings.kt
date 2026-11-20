package com.example.i18n

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import com.example.domain.model.AppLanguage

/**
 * Structured localization supporting English (complete) and Tamil UI chrome labels.
 * Clinical-adjacent notices carry explicit [Review Required] tags in Tamil rather than
 * presenting unreviewed machine translation as clinical guidance.
 */
@Immutable
data class AirTonStrings(
    val appName: String,
    val prototypeBannerTitle: String,
    val prototypeBannerSubtitle: String,
    val simulationNoHardwareNotice: String,
    val notConfigured: String,
    val notAvailable: String,
    // Roles
    val rolePatient: String,
    val roleClinician: String,
    val roleClinicService: String,
    val rolePreviewNotice: String,
    // Patient Nav
    val navHome: String,
    val navMeasure: String,
    val navHistory: String,
    val navCare: String,
    val navSync: String,
    // Clinician Nav
    val navOverview: String,
    val navPatients: String,
    val navQueue: String,
    val navCarePlans: String,
    // Clinic Nav
    val navAssignments: String,
    val navDevices: String,
    val navOpsQueue: String,
    // Common Actions
    val exploreDemo: String,
    val switchPersona: String,
    val settingsTitle: String,
    val inboxTitle: String,
    val aboutTitle: String,
    val tryDeviceSimulation: String,
    val noDeviceConnected: String,
    val nonClinicalResultDisclaimer: String,
    val resetDemoData: String,
    val cancel: String,
    val confirm: String,
    val back: String
)

val EnglishStrings = AirTonStrings(
    appName = "AirTon",
    prototypeBannerTitle = "PROTOTYPE · SIMULATION ONLY",
    prototypeBannerSubtitle = "Fictional demo fixtures only · Not a medical device or clinical service",
    simulationNoHardwareNotice = "Simulation — no physical device or air puff is operating",
    notConfigured = "Not configured",
    notAvailable = "Not available",
    rolePatient = "Patient / Caregiver",
    roleClinician = "Treating Clinician",
    roleClinicService = "Clinic / Service",
    rolePreviewNotice = "Demo persona preview only — not real sign-in or authentication.",
    navHome = "Home",
    navMeasure = "Measure",
    navHistory = "History",
    navCare = "Care",
    navSync = "Sync",
    navOverview = "Overview",
    navPatients = "Patients",
    navQueue = "Queue",
    navCarePlans = "Plans",
    navAssignments = "Assign",
    navDevices = "Devices",
    navOpsQueue = "Ops",
    exploreDemo = "Explore Demo",
    switchPersona = "Switch Role",
    settingsTitle = "Profile & Settings",
    inboxTitle = "Notifications",
    aboutTitle = "About & Limitations",
    tryDeviceSimulation = "Try Device Simulation",
    noDeviceConnected = "No AirTon device connected",
    nonClinicalResultDisclaimer = "This is a fictional software demonstration, not a medical result.",
    resetDemoData = "Reset demo data",
    cancel = "Cancel",
    confirm = "Confirm",
    back = "Back"
)

val TamilStrings = AirTonStrings(
    appName = "AirTon",
    prototypeBannerTitle = "மாதிரி · உருவகப்படுத்துதல் மட்டும்",
    prototypeBannerSubtitle = "[Review Required] Fictional demo fixtures only · Not a medical device or clinical service",
    simulationNoHardwareNotice = "[Review Required] Simulation — no physical device or air puff is operating",
    notConfigured = "அமைக்கப்படவில்லை (Not configured)",
    notAvailable = "கிடைக்கவில்லை (Not available)",
    rolePatient = "நோயாளி / பராமரிப்பாளர்",
    roleClinician = "மருத்துவர்",
    roleClinicService = "மருத்துவமனை / சேவை",
    rolePreviewNotice = "[Review Required] Demo persona preview only — not real sign-in or authentication.",
    navHome = "முகப்பு",
    navMeasure = "அளவீடு",
    navHistory = "வரலாறு",
    navCare = "பராமரிப்பு",
    navSync = "ஒத்திசைவு",
    navOverview = "கண்ணோட்டம்",
    navPatients = "நோயாளிகள்",
    navQueue = "வரிசை",
    navCarePlans = "திட்டங்கள்",
    navAssignments = "ஒதுக்கீடு",
    navDevices = "சாதனங்கள்",
    navOpsQueue = "செயல்பாடு",
    exploreDemo = "மாதிரியை ஆராய்க",
    switchPersona = "பங்கு மாற்று",
    settingsTitle = "சுயவிவரம் & அமைப்புகள்",
    inboxTitle = "அறிவிப்புகள்",
    aboutTitle = "விவரம் & வரம்புகள்",
    tryDeviceSimulation = "சாதன உருவகப்படுத்துதல்",
    noDeviceConnected = "AirTon சாதனம் இணைக்கப்படவில்லை",
    nonClinicalResultDisclaimer = "[Review Required] This is a fictional software demonstration, not a medical result.",
    resetDemoData = "மாதிரி தரவை மீட்டமை",
    cancel = "ரத்து செய்",
    confirm = "உறுதி செய்",
    back = "பின்செல்"
)

fun stringsFor(language: AppLanguage): AirTonStrings = when (language) {
    AppLanguage.ENGLISH -> EnglishStrings
    AppLanguage.TAMIL -> TamilStrings
}

val LocalAirTonStrings = staticCompositionLocalOf { EnglishStrings }
