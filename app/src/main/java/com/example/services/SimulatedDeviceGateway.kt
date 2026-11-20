package com.example.services

import com.example.domain.contract.MeasurementValidator
import com.example.domain.model.ClockStatus
import com.example.domain.model.EyeSide
import com.example.domain.model.QualityReason
import com.example.domain.model.QualityStatus
import com.example.domain.model.SimulationScenario
import com.example.domain.model.SyntheticMeasurementV1
import com.example.domain.ports.DeviceGateway
import com.example.domain.ports.HardwareConnectionState
import com.example.domain.ports.SimulatedAcquisitionResult

/**
 * Strictly software-only simulated DeviceGateway.
 * Never connects to BLE, USB, or any physical hardware actuator.
 */
class SimulatedDeviceGateway : DeviceGateway {

    override val connectionState: HardwareConnectionState =
        HardwareConnectionState.NOT_CONNECTED_SIMULATION_ONLY

    override val isSimulatedOnly: Boolean = true

    override fun deviceSummaryLabel(): String =
        "No AirTon device connected (Software simulation adapter active)"

    override fun performSimulatedAcquisition(
        subjectId: String,
        deviceId: String,
        sessionId: String,
        measurementId: String,
        eye: EyeSide,
        scenario: SimulationScenario,
        alignmentScore: Float,
        sequenceIndex: Int
    ): SimulatedAcquisitionResult {
        if (scenario == SimulationScenario.DISCONNECTED || !scenario.producesRecord) {
            return SimulatedAcquisitionResult(
                scenario = scenario,
                measurement = null,
                statusMessage = "Simulation state: Device disconnected. No physical device is linked and no record was created."
            )
        }

        // Deterministic timestamp offset (+05:30) advancing cleanly per sequence index
        val minute = (10 + (sequenceIndex % 49)).toString().padStart(2, '0')
        val second = ((sequenceIndex * 7) % 60).toString().padStart(2, '0')
        val capturedAt = "2026-11-20T09:$minute:$second+05:30"

        // If the user selected READY_ACCEPTED but intentionally left the interactive reticle far off-center,
        // reflect alignment rejection honestly with null IOP!
        val effectiveRejection: QualityReason? = when {
            scenario.expectedQuality == QualityStatus.REJECT -> scenario.rejectionReason
            alignmentScore < 0.65f -> QualityReason.ALIGNMENT
            else -> null
        }

        val effectiveQuality = if (effectiveRejection == null) QualityStatus.PASS else QualityStatus.REJECT
        val effectiveClock = if (effectiveRejection == QualityReason.CLOCK_UNTRUSTED) {
            ClockStatus.UNTRUSTED
        } else {
            scenario.clockStatus
        }

        // Deterministic synthetic demonstration value only when accepted; strictly null when rejected
        val syntheticValue: Double? = if (effectiveQuality == QualityStatus.PASS) {
            val baseValues = listOf(15.6, 16.4, 17.2, 18.0, 16.8, 17.5, 19.1, 15.9)
            val eyeOffset = if (eye == EyeSide.OD) 0.3 else -0.2
            val chosen = baseValues[sequenceIndex % baseValues.size] + eyeOffset
            kotlin.math.round(chosen * 10.0) / 10.0
        } else {
            null
        }

        val calId = if (effectiveRejection == QualityReason.CALIBRATION_EXPIRED) {
            "demo-cal-2026-08-exp"
        } else {
            "demo-cal-2026-11-a"
        }

        val rawSha = MeasurementValidator.sha256Hex(
            "sim-waveform|$measurementId|$sessionId|$subjectId|${eye.code}|$capturedAt|${effectiveQuality.wireValue}"
        )

        val record = SyntheticMeasurementV1(
            schemaVersion = SyntheticMeasurementV1.SCHEMA_VERSION_V1,
            measurementId = measurementId,
            sessionId = sessionId,
            subjectId = subjectId,
            deviceId = deviceId.ifBlank { "demo-dev-sim" },
            eye = eye,
            capturedAt = capturedAt,
            synthetic = true,
            qualityStatus = effectiveQuality,
            qualityReasons = listOfNotNull(effectiveRejection),
            iopEstimateMmhg = syntheticValue,
            calibrationId = calId,
            modelId = "demo-sim-model-v0",
            firmwareVersion = "0.4.2-sim",
            rawSha256 = rawSha,
            clockStatus = effectiveClock
        )

        val message = if (effectiveQuality == QualityStatus.PASS) {
            "Simulated accepted record created for ${eye.labelEn}. Synthetic demonstration data only — not a medical measurement."
        } else {
            "Simulated acquisition rejected (${effectiveRejection?.labelEn}). No IOP estimate is shown or stored."
        }

        return SimulatedAcquisitionResult(
            scenario = scenario,
            measurement = record,
            statusMessage = message
        )
    }
}
