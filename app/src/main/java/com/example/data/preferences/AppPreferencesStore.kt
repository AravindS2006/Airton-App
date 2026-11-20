package com.example.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.fixtures.DeterministicFixtures
import com.example.domain.model.AppLanguage
import com.example.domain.model.DemoRole
import com.example.domain.model.TextScalePreset
import com.example.domain.model.ThemePreference
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.airTonDataStore by preferencesDataStore(name = "airton_demo_prefs")

data class AppPreferencesState(
    val disclosureAcknowledged: Boolean = false,
    val selectedRole: DemoRole = DemoRole.PATIENT,
    val activePatientId: String = DeterministicFixtures.PATIENT_MEERA_ID,
    val activeClinicianId: String = DeterministicFixtures.CLINICIAN_KAVITHA_ID,
    val language: AppLanguage = AppLanguage.ENGLISH,
    val themePreference: ThemePreference = ThemePreference.LIGHT,
    val textScalePreset: TextScalePreset = TextScalePreset.STANDARD,
    val simulatedOfflineMode: Boolean = false,
    val localDemoRemindersEnabled: Boolean = true,
    val reducedMotionEnabled: Boolean = false
)

class AppPreferencesStore(private val context: Context) {

    private object Keys {
        val DISCLOSURE_ACK = booleanPreferencesKey("disclosure_ack")
        val SELECTED_ROLE = stringPreferencesKey("selected_role")
        val ACTIVE_PATIENT_ID = stringPreferencesKey("active_patient_id")
        val ACTIVE_CLINICIAN_ID = stringPreferencesKey("active_clinician_id")
        val LANGUAGE = stringPreferencesKey("language")
        val THEME_PREF = stringPreferencesKey("theme_pref")
        val TEXT_SCALE = stringPreferencesKey("text_scale")
        val SIMULATED_OFFLINE = booleanPreferencesKey("simulated_offline")
        val LOCAL_REMINDERS = booleanPreferencesKey("local_reminders")
        val REDUCED_MOTION = booleanPreferencesKey("reduced_motion")
    }

    val preferencesFlow: Flow<AppPreferencesState> = context.airTonDataStore.data.map { prefs: Preferences ->
        AppPreferencesState(
            disclosureAcknowledged = prefs[Keys.DISCLOSURE_ACK] ?: false,
            selectedRole = prefs[Keys.SELECTED_ROLE]?.let { name ->
                DemoRole.entries.firstOrNull { it.name == name }
            } ?: DemoRole.PATIENT,
            activePatientId = prefs[Keys.ACTIVE_PATIENT_ID] ?: DeterministicFixtures.PATIENT_MEERA_ID,
            activeClinicianId = prefs[Keys.ACTIVE_CLINICIAN_ID] ?: DeterministicFixtures.CLINICIAN_KAVITHA_ID,
            language = prefs[Keys.LANGUAGE]?.let { code ->
                AppLanguage.entries.firstOrNull { it.code == code }
            } ?: AppLanguage.ENGLISH,
            themePreference = prefs[Keys.THEME_PREF]?.let { name ->
                ThemePreference.entries.firstOrNull { it.name == name }
            } ?: ThemePreference.LIGHT,
            textScalePreset = prefs[Keys.TEXT_SCALE]?.let { name ->
                TextScalePreset.entries.firstOrNull { it.name == name }
            } ?: TextScalePreset.STANDARD,
            simulatedOfflineMode = prefs[Keys.SIMULATED_OFFLINE] ?: false,
            localDemoRemindersEnabled = prefs[Keys.LOCAL_REMINDERS] ?: true,
            reducedMotionEnabled = prefs[Keys.REDUCED_MOTION] ?: false
        )
    }

    suspend fun acknowledgeDisclosureAndSelectRole(role: DemoRole) {
        context.airTonDataStore.edit {
            it[Keys.DISCLOSURE_ACK] = true
            it[Keys.SELECTED_ROLE] = role.name
        }
    }

    suspend fun setSelectedRole(role: DemoRole) {
        context.airTonDataStore.edit { it[Keys.SELECTED_ROLE] = role.name }
    }

    suspend fun setActivePatientId(subjectId: String) {
        context.airTonDataStore.edit { it[Keys.ACTIVE_PATIENT_ID] = subjectId }
    }

    suspend fun setActiveClinicianId(clinicianId: String) {
        context.airTonDataStore.edit { it[Keys.ACTIVE_CLINICIAN_ID] = clinicianId }
    }

    suspend fun setLanguage(language: AppLanguage) {
        context.airTonDataStore.edit { it[Keys.LANGUAGE] = language.code }
    }

    suspend fun setThemePreference(theme: ThemePreference) {
        context.airTonDataStore.edit { it[Keys.THEME_PREF] = theme.name }
    }

    suspend fun setTextScalePreset(preset: TextScalePreset) {
        context.airTonDataStore.edit { it[Keys.TEXT_SCALE] = preset.name }
    }

    suspend fun setSimulatedOfflineMode(offline: Boolean) {
        context.airTonDataStore.edit { it[Keys.SIMULATED_OFFLINE] = offline }
    }

    suspend fun setLocalRemindersEnabled(enabled: Boolean) {
        context.airTonDataStore.edit { it[Keys.LOCAL_REMINDERS] = enabled }
    }

    suspend fun setReducedMotionEnabled(enabled: Boolean) {
        context.airTonDataStore.edit { it[Keys.REDUCED_MOTION] = enabled }
    }

    suspend fun resetOperationalPreferences() {
        context.airTonDataStore.edit {
            it[Keys.SIMULATED_OFFLINE] = false
            it[Keys.ACTIVE_PATIENT_ID] = DeterministicFixtures.PATIENT_MEERA_ID
            it[Keys.ACTIVE_CLINICIAN_ID] = DeterministicFixtures.CLINICIAN_KAVITHA_ID
        }
    }
}
