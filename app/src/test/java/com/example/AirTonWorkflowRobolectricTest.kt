package com.example

import android.app.Application
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.fixtures.DeterministicFixtures
import com.example.data.persistence.AirTonDatabase
import com.example.data.preferences.AppPreferencesStore
import com.example.data.repositories.RoomAirTonStore
import com.example.domain.model.ClinicalReviewStatus
import com.example.domain.model.EyeSide
import com.example.domain.model.IngestOutcome
import com.example.domain.model.QualityStatus
import com.example.domain.model.ReviewActionType
import com.example.domain.model.SimulationScenario
import com.example.domain.model.TransportStatus
import com.example.services.SimulatedDeviceGateway
import com.example.ui.AirTonApp
import com.example.viewmodel.AirTonViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Robolectric local JVM tests covering Room persistence invariants, offline retries,
 * conflict quarantine, append-only clinician reviews, demo reset, and Compose UI workflows.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class AirTonWorkflowRobolectricTest {

    @get:Rule
    val composeRule = createComposeRule()

    private lateinit var app: Application
    private lateinit var db: AirTonDatabase
    private lateinit var store: RoomAirTonStore

    @Before
    fun setUp() {
        app = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(app, AirTonDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        store = RoomAirTonStore(db)
        runBlocking { store.ensureSeeded() }
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun duplicateIngestIsIdempotentAndConflictingReplayQuarantinesWithoutOverwrite() = runBlocking {
        val original = store.observeEnvelope("demo-meas-001").first()!!
        val originalIop = original.measurement.iopEstimateMmhg

        // Identical duplicate retry -> idempotent no-op
        val dupOutcome = store.simulateDuplicateIdempotentRetry("demo-meas-001")
        assertTrue(dupOutcome is IngestOutcome.DuplicateIdempotent)

        // Conflicting replay -> quarantined without mutating original measurement value
        val conflictOutcome = store.simulateConflictingReplay("demo-meas-001")
        assertTrue(conflictOutcome is IngestOutcome.ConflictQuarantined)

        val afterConflict = store.observeEnvelope("demo-meas-001").first()!!
        assertEquals(originalIop, afterConflict.measurement.iopEstimateMmhg)
        assertEquals(TransportStatus.QUARANTINED, afterConflict.transportStatus)
        assertTrue(afterConflict.conflicts.isNotEmpty())
    }

    @Test
    fun offlineRetryPreservesMeasurementIdAndDecouplesDeliveryFromClinicianReview() = runBlocking {
        val gateway = SimulatedDeviceGateway()
        val id = store.nextDemoMeasurementId()
        val generated = gateway.performSimulatedAcquisition(
            subjectId = DeterministicFixtures.PATIENT_MEERA_ID,
            deviceId = "demo-dev-01",
            sessionId = store.nextDemoSessionId(),
            measurementId = id,
            eye = EyeSide.OD,
            scenario = SimulationScenario.READY_ACCEPTED,
            alignmentScore = 0.96f,
            sequenceIndex = 2
        ).measurement!!

        // Save while offline -> LOCAL_ONLY, review NOT_APPLICABLE
        store.ingestMeasurement(generated, TransportStatus.LOCAL_ONLY)
        val localSaved = store.observeEnvelope(id).first()!!
        assertEquals(TransportStatus.LOCAL_ONLY, localSaved.transportStatus)
        assertEquals(ClinicalReviewStatus.NOT_APPLICABLE, localSaved.reviewStatus)

        // Retry while offline -> FAILED, same ID preserved
        store.retryPendingUploads(isOfflineSimulated = true)
        val failedOffline = store.observeEnvelope(id).first()!!
        assertEquals(id, failedOffline.measurement.measurementId)
        assertEquals(TransportStatus.FAILED, failedOffline.transportStatus)

        // Retry once online -> DELIVERED, still AWAITING_REVIEW (not automatically reviewed!)
        store.retryPendingUploads(isOfflineSimulated = false)
        val delivered = store.observeEnvelope(id).first()!!
        assertEquals(id, delivered.measurement.measurementId)
        assertEquals(TransportStatus.DELIVERED, delivered.transportStatus)
        assertEquals(ClinicalReviewStatus.AWAITING_REVIEW, delivered.reviewStatus)

        // Clinician acknowledges review & appends a note -> append-only trail grows to 2 events
        store.appendReviewEvent(
            measurementId = id,
            actionType = ReviewActionType.ACKNOWLEDGED_REVIEW,
            authorClinicianId = DeterministicFixtures.CLINICIAN_KAVITHA_ID,
            authorName = "Dr. Kavitha Raman",
            note = "Reviewed in Robolectric test"
        )
        store.appendReviewEvent(
            measurementId = id,
            actionType = ReviewActionType.NOTE_APPENDED,
            authorClinicianId = DeterministicFixtures.CLINICIAN_NAVEEN_ID,
            authorName = "Dr. Naveen Balaji",
            note = "Second immutable note"
        )

        val reviewed = store.observeEnvelope(id).first()!!
        assertEquals(ClinicalReviewStatus.REVIEWED, reviewed.reviewStatus)
        assertEquals(2, reviewed.reviewEvents.size)
        assertEquals("Dr. Kavitha Raman", reviewed.reviewEvents[0].authorName)
        assertEquals("Dr. Naveen Balaji", reviewed.reviewEvents[1].authorName)
    }

    @Test
    fun demoResetRestoresBaselineFixtureCount() = runBlocking {
        val beforeCount = store.observeAllEnvelopes().first().size
        assertEquals(DeterministicFixtures.measurements.size, beforeCount)

        val extra = SimulatedDeviceGateway().performSimulatedAcquisition(
            subjectId = DeterministicFixtures.PATIENT_ARJUN_ID,
            deviceId = "demo-dev-02",
            sessionId = "demo-sess-extra",
            measurementId = "demo-meas-extra-1",
            eye = EyeSide.OS,
            scenario = SimulationScenario.BLINK_REJECTED,
            alignmentScore = 0.9f,
            sequenceIndex = 5
        ).measurement!!
        assertEquals(QualityStatus.REJECT, extra.qualityStatus)
        assertNull(extra.iopEstimateMmhg)

        store.ingestMeasurement(extra, TransportStatus.LOCAL_ONLY)
        assertEquals(beforeCount + 1, store.observeAllEnvelopes().first().size)

        store.resetDemoDatabase()
        assertEquals(beforeCount, store.observeAllEnvelopes().first().size)
        assertNull(store.observeEnvelope("demo-meas-extra-1").first())
    }

    @Test
    fun composeUiFirstLaunchDisclosureRoleSelectAndDeviceNotConnectedState() {
        val viewModel = AirTonViewModel(
            application = app,
            store = store,
            prefsStore = AppPreferencesStore(app),
            deviceGateway = SimulatedDeviceGateway()
        )

        composeRule.setContent {
            AirTonApp(viewModel = viewModel, skipSplashForTest = true)
        }
        composeRule.waitForIdle()

        // 1. Welcome Disclosure screen displays persistent prototype banner & Explore Demo CTA
        composeRule.onNodeWithTag("prototype_disclosure_banner").assertIsDisplayed()
        composeRule.onNodeWithTag("explore_demo_button").assertIsDisplayed().performClick()
        composeRule.waitForIdle()

        // 2. Demo Role Selector lets user enter Patient persona
        composeRule.onNodeWithTag("select_role_patient").assertIsDisplayed().performClick()
        composeRule.waitForIdle()

        // 3. Patient Home displays persistent simulation banner and truthful No AirTon device connected
        composeRule.onNodeWithTag("prototype_disclosure_banner").assertIsDisplayed()
        composeRule.onNodeWithTag("device_not_connected_label").assertIsDisplayed()
        composeRule.onNodeWithText("No AirTon device connected").assertIsDisplayed()

        // 4. Switch to Clinician via top-bar role switcher chip
        composeRule.onNodeWithTag("role_switcher_chip").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("switch_role_clinician").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("clinician_overview_screen").assertIsDisplayed()
        assertNotNull(viewModel.uiState.value.activeClinician)
    }
}
