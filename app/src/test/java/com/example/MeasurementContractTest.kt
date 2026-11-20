package com.example

import com.example.data.fixtures.DeterministicFixtures
import com.example.domain.contract.MeasurementValidator
import com.example.domain.model.ClockStatus
import com.example.domain.model.EyeSide
import com.example.domain.model.QualityReason
import com.example.domain.model.QualityStatus
import com.example.domain.model.SimulationScenario
import com.example.domain.model.SyntheticMeasurementV1
import com.example.services.SimulatedDeviceGateway
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Domain and v1.0 synthetic contract test suite verifying every AirTon safety invariant.
 */
class MeasurementContractTest {

    @Test
    fun allDeterministicFixturesPassStrictV1Validation() {
        assertTrue(DeterministicFixtures.patients.size >= 3)
        assertTrue(DeterministicFixtures.clinicians.size >= 2)
        assertTrue(DeterministicFixtures.measurements.size >= 12)

        val eyesPresent = DeterministicFixtures.measurements.map { it.record.eye }.toSet()
        assertEquals(setOf(EyeSide.OD, EyeSide.OS), eyesPresent)

        DeterministicFixtures.measurements.forEach { spec ->
            val result = MeasurementValidator.validate(spec.record)
            assertTrue(
                "Fixture ${spec.record.measurementId} failed validation: $result",
                result is MeasurementValidator.ValidationResult.Valid
            )
            if (spec.record.qualityStatus == QualityStatus.REJECT) {
                assertNull(
                    "Rejected fixture ${spec.record.measurementId} must have null IOP",
                    spec.record.iopEstimateMmhg
                )
                assertTrue(spec.record.qualityReasons.isNotEmpty())
            }
        }
    }

    @Test
    fun rejectedRecordWithNumericIopFailsValidation() {
        val invalid = DeterministicFixtures.measurements.first().record.copy(
            qualityStatus = QualityStatus.REJECT,
            qualityReasons = listOf(QualityReason.BLINK),
            iopEstimateMmhg = 18.0
        )
        val result = MeasurementValidator.validate(invalid)
        assertTrue(result is MeasurementValidator.ValidationResult.Invalid)
    }

    @Test
    fun untrustedClockMustBeRejectedWithClockUntrustedReason() {
        val invalidPass = DeterministicFixtures.measurements.first().record.copy(
            clockStatus = ClockStatus.UNTRUSTED,
            qualityStatus = QualityStatus.PASS,
            qualityReasons = emptyList(),
            iopEstimateMmhg = 16.0
        )
        assertTrue(MeasurementValidator.validate(invalidPass) is MeasurementValidator.ValidationResult.Invalid)

        val validReject = invalidPass.copy(
            qualityStatus = QualityStatus.REJECT,
            qualityReasons = listOf(QualityReason.CLOCK_UNTRUSTED),
            iopEstimateMmhg = null
        )
        assertTrue(MeasurementValidator.validate(validReject) is MeasurementValidator.ValidationResult.Valid)
    }

    @Test
    fun timestampWithoutExplicitTimezoneOffsetIsRejected() {
        val missingTz = DeterministicFixtures.measurements.first().record.copy(
            capturedAt = "2026-11-20T09:15:00"
        )
        assertTrue(MeasurementValidator.validate(missingTz) is MeasurementValidator.ValidationResult.Invalid)
    }

    @Test
    fun strictJsonParserRoundTripsValidRecordAndRejectsUnknownFields() {
        val sample = DeterministicFixtures.measurements.first().record
        val json = MeasurementValidator.toStrictJson(sample)
        val parsed = MeasurementValidator.parseStrictJson(json)
        assertTrue(parsed is MeasurementValidator.ParseResult.Success)
        assertEquals(sample, (parsed as MeasurementValidator.ParseResult.Success).measurement)

        val jsonWithExtraField = json.dropLast(1) + ",\"glaucoma_risk\":\"high\"}"
        val extraResult = MeasurementValidator.parseStrictJson(jsonWithExtraField)
        assertTrue(extraResult is MeasurementValidator.ParseResult.Failure)
        val errors = (extraResult as MeasurementValidator.ParseResult.Failure).errors.joinToString()
        assertTrue(errors.contains("Unknown extra fields"))
    }

    @Test
    fun unconfiguredCarePlanFixturePreservesNullClinicalParameters() {
        val lakshmiPlan = DeterministicFixtures.carePlans.first {
            it.subjectId == DeterministicFixtures.PATIENT_LAKSHMI_ID
        }
        assertFalse(lakshmiPlan.isScheduleConfigured)
        assertNull(lakshmiPlan.scheduleSummary)
        assertNull(lakshmiPlan.repetitionsPerSession)
        assertNull(lakshmiPlan.maxRetriesPerEye)
        assertNull(lakshmiPlan.clinicalAlertThresholdNote)
        assertNull(lakshmiPlan.calibrationIntervalNote)
        assertNull(lakshmiPlan.escalationPolicyNote)
    }

    @Test
    fun simulatedDeviceGatewayEnforcesNullIopOnAllRejectedScenariosAndOffCenterAlignment() {
        val gateway = SimulatedDeviceGateway()
        assertTrue(gateway.isSimulatedOnly)

        val rejectedScenarios = listOf(
            SimulationScenario.ALIGNMENT_REJECTED,
            SimulationScenario.BLINK_REJECTED,
            SimulationScenario.SIGNAL_MISSING,
            SimulationScenario.CALIBRATION_UNAVAILABLE,
            SimulationScenario.CLOCK_UNTRUSTED
        )

        rejectedScenarios.forEachIndexed { idx, sc ->
            val out = gateway.performSimulatedAcquisition(
                subjectId = DeterministicFixtures.PATIENT_MEERA_ID,
                deviceId = "demo-dev-01",
                sessionId = "demo-sess-test",
                measurementId = "demo-meas-test-$idx",
                eye = EyeSide.OS,
                scenario = sc,
                alignmentScore = 0.95f,
                sequenceIndex = idx + 1
            )
            val m = out.measurement
            assertNotNull(m)
            assertEquals(QualityStatus.REJECT, m!!.qualityStatus)
            assertNull("Rejected scenario ${sc.id} must have null IOP", m.iopEstimateMmhg)
            assertEquals(EyeSide.OS, m.eye)
            assertTrue(MeasurementValidator.validate(m) is MeasurementValidator.ValidationResult.Valid)
        }

        // Off-center reticle during READY_ACCEPTED must downgrade to ALIGNMENT reject with null IOP
        val misaligned = gateway.performSimulatedAcquisition(
            subjectId = DeterministicFixtures.PATIENT_MEERA_ID,
            deviceId = "demo-dev-01",
            sessionId = "demo-sess-misaligned",
            measurementId = "demo-meas-misaligned",
            eye = EyeSide.OD,
            scenario = SimulationScenario.READY_ACCEPTED,
            alignmentScore = 0.30f,
            sequenceIndex = 9
        )
        val misalignedRecord = misaligned.measurement!!
        assertEquals(QualityStatus.REJECT, misalignedRecord.qualityStatus)
        assertNull(misalignedRecord.iopEstimateMmhg)
        assertEquals(listOf(QualityReason.ALIGNMENT), misalignedRecord.qualityReasons)
    }
}
