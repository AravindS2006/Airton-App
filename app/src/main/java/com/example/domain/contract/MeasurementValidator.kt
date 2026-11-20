package com.example.domain.contract

import com.example.domain.model.ClockStatus
import com.example.domain.model.EyeSide
import com.example.domain.model.QualityReason
import com.example.domain.model.QualityStatus
import com.example.domain.model.SyntheticMeasurementV1
import java.security.MessageDigest
import java.util.Locale

/**
 * Runtime contract validator and pure-Kotlin strict v1.0 JSON codec matching AirTon's
 * schemas/measurement.schema.json and src/airton/contracts.py invariants.
 */
object MeasurementValidator {

    private val ALLOWED_V1_FIELDS = setOf(
        "schema_version",
        "measurement_id",
        "session_id",
        "subject_id",
        "device_id",
        "eye",
        "captured_at",
        "synthetic",
        "quality_status",
        "quality_reasons",
        "iop_estimate_mmhg",
        "calibration_id",
        "model_id",
        "firmware_version",
        "raw_sha256",
        "clock_status"
    )

    // ISO-8601 timestamp with mandatory explicit timezone offset (Z or +/-HH:MM)
    private val ISO_8601_WITH_TZ_REGEX =
        Regex("^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(\\.\\d{1,6})?(Z|[+-]\\d{2}:\\d{2})$")

    private val SHA256_HEX_REGEX = Regex("^[a-f0-9]{64}$")

    sealed interface ValidationResult {
        data object Valid : ValidationResult
        data class Invalid(val errors: List<String>) : ValidationResult
    }

    sealed interface ParseResult {
        data class Success(val measurement: SyntheticMeasurementV1) : ParseResult
        data class Failure(val errors: List<String>) : ParseResult
    }

    fun validate(record: SyntheticMeasurementV1): ValidationResult {
        val errors = mutableListOf<String>()

        if (record.schemaVersion != SyntheticMeasurementV1.SCHEMA_VERSION_V1) {
            errors += "schema_version must be '${SyntheticMeasurementV1.SCHEMA_VERSION_V1}'."
        }
        if (!record.synthetic) {
            errors += "synthetic must be true for prototype v1.0 demonstration fixtures."
        }
        if (!record.measurementId.startsWith("demo-")) {
            errors += "measurement_id must be demo-prefixed ('demo-*')."
        }
        if (!record.sessionId.startsWith("demo-")) {
            errors += "session_id must be demo-prefixed ('demo-*')."
        }
        if (!record.subjectId.startsWith("demo-")) {
            errors += "subject_id must be demo-prefixed ('demo-*')."
        }
        if (!record.deviceId.startsWith("demo-")) {
            errors += "device_id must be demo-prefixed ('demo-*')."
        }
        if (!ISO_8601_WITH_TZ_REGEX.matches(record.capturedAt)) {
            errors += "captured_at must be an ISO-8601 timestamp with an explicit timezone offset."
        }
        if (record.calibrationId.isBlank()) {
            errors += "calibration_id must not be blank."
        }
        if (record.modelId.isBlank()) {
            errors += "model_id must not be blank."
        }
        if (record.firmwareVersion.isBlank()) {
            errors += "firmware_version must not be blank."
        }
        if (!SHA256_HEX_REGEX.matches(record.rawSha256)) {
            errors += "raw_sha256 must be a 64-character lowercase hexadecimal SHA-256 digest."
        }

        // Core AirTon safety & quality invariants
        when (record.qualityStatus) {
            QualityStatus.REJECT -> {
                if (record.iopEstimateMmhg != null) {
                    errors += "Rejected acquisition must have iop_estimate_mmhg == null."
                }
                if (record.qualityReasons.isEmpty()) {
                    errors += "Rejected acquisition must include at least one quality_reason."
                }
            }
            QualityStatus.PASS -> {
                if (record.iopEstimateMmhg == null || !record.iopEstimateMmhg.isFinite()) {
                    errors += "Accepted synthetic acquisition must include a finite numeric iop_estimate_mmhg."
                }
                if (record.qualityReasons.isNotEmpty()) {
                    errors += "Accepted acquisition must have an empty quality_reasons list."
                }
                if (record.clockStatus != ClockStatus.TRUSTED) {
                    errors += "Accepted acquisition requires clock_status == trusted."
                }
            }
        }

        // Clock trust invariant
        if (record.clockStatus == ClockStatus.UNTRUSTED) {
            if (record.qualityStatus != QualityStatus.REJECT) {
                errors += "Untrusted clock must result in quality_status == reject."
            }
            if (QualityReason.CLOCK_UNTRUSTED !in record.qualityReasons) {
                errors += "Untrusted clock must include 'clock_untrusted' in quality_reasons."
            }
        }

        return if (errors.isEmpty()) ValidationResult.Valid else ValidationResult.Invalid(errors)
    }

    /**
     * Pure-Kotlin strict JSON parser for v1.0 synthetic records. Rejects unknown extra fields.
     */
    fun parseStrictJson(rawJson: String): ParseResult {
        val map = try {
            StrictFlatJsonParser(rawJson).parseObject()
        } catch (e: Exception) {
            return ParseResult.Failure(listOf("Malformed JSON payload: ${e.message}"))
        }

        val errors = mutableListOf<String>()
        val keys = map.keys
        val unknownKeys = keys - ALLOWED_V1_FIELDS
        if (unknownKeys.isNotEmpty()) {
            errors += "Unknown extra fields rejected by v1.0 contract: ${unknownKeys.sorted().joinToString(", ")}"
        }
        val missingKeys = ALLOWED_V1_FIELDS - keys
        if (missingKeys.isNotEmpty()) {
            errors += "Missing required v1.0 fields: ${missingKeys.sorted().joinToString(", ")}"
        }
        if (errors.isNotEmpty()) {
            return ParseResult.Failure(errors)
        }

        fun reqString(key: String): String {
            val v = map[key]
            return if (v is String) v else {
                errors += "$key must be a string."
                ""
            }
        }

        val eyeStr = reqString("eye")
        val eye = when (eyeStr) {
            "OD" -> EyeSide.OD
            "OS" -> EyeSide.OS
            else -> {
                errors += "Invalid eye value '$eyeStr'; expected 'OD' or 'OS'."
                EyeSide.OD
            }
        }

        val qs = reqString("quality_status")
        val qualityStatus = when (qs) {
            "pass" -> QualityStatus.PASS
            "reject" -> QualityStatus.REJECT
            else -> {
                errors += "Invalid quality_status '$qs'; expected 'pass' or 'reject'."
                QualityStatus.REJECT
            }
        }

        val cs = reqString("clock_status")
        val clockStatus = ClockStatus.fromWire(cs) ?: run {
            errors += "Invalid clock_status '$cs'."
            ClockStatus.UNTRUSTED
        }

        val syntheticVal = map["synthetic"]
        val syntheticBool = if (syntheticVal is Boolean) syntheticVal else {
            errors += "synthetic must be a boolean."
            false
        }

        val reasonsRaw = map["quality_reasons"]
        val parsedReasons = mutableListOf<QualityReason>()
        if (reasonsRaw !is List<*>) {
            errors += "quality_reasons must be a JSON array."
        } else {
            reasonsRaw.forEach { item ->
                val wire = item as? String
                val parsed = wire?.let { QualityReason.fromWire(it) }
                if (parsed == null) {
                    errors += "Unsupported quality_reason '$item'."
                } else {
                    parsedReasons += parsed
                }
            }
        }

        val iopRaw = map["iop_estimate_mmhg"]
        val iopEstimate: Double? = when (iopRaw) {
            null -> null
            is Number -> iopRaw.toDouble()
            else -> {
                errors += "iop_estimate_mmhg must be a number or null."
                null
            }
        }

        val schemaVersion = reqString("schema_version")
        val measurementId = reqString("measurement_id")
        val sessionId = reqString("session_id")
        val subjectId = reqString("subject_id")
        val deviceId = reqString("device_id")
        val capturedAt = reqString("captured_at")
        val calibrationId = reqString("calibration_id")
        val modelId = reqString("model_id")
        val firmwareVersion = reqString("firmware_version")
        val rawSha256 = reqString("raw_sha256")

        if (errors.isNotEmpty()) {
            return ParseResult.Failure(errors)
        }

        val record = SyntheticMeasurementV1(
            schemaVersion = schemaVersion,
            measurementId = measurementId,
            sessionId = sessionId,
            subjectId = subjectId,
            deviceId = deviceId,
            eye = eye,
            capturedAt = capturedAt,
            synthetic = syntheticBool,
            qualityStatus = qualityStatus,
            qualityReasons = parsedReasons,
            iopEstimateMmhg = iopEstimate,
            calibrationId = calibrationId,
            modelId = modelId,
            firmwareVersion = firmwareVersion,
            rawSha256 = rawSha256,
            clockStatus = clockStatus
        )

        return when (val result = validate(record)) {
            is ValidationResult.Valid -> ParseResult.Success(record)
            is ValidationResult.Invalid -> ParseResult.Failure(result.errors)
        }
    }

    fun toStrictJson(record: SyntheticMeasurementV1): String {
        fun esc(s: String): String = buildString {
            append('"')
            s.forEach { ch ->
                when (ch) {
                    '"' -> append("\\\"")
                    '\\' -> append("\\\\")
                    '\n' -> append("\\n")
                    '\r' -> append("\\r")
                    '\t' -> append("\\t")
                    else -> append(ch)
                }
            }
            append('"')
        }

        val reasonsJson = record.qualityReasons.joinToString(prefix = "[", postfix = "]") { esc(it.wireValue) }
        val iopJson = record.iopEstimateMmhg?.let { String.format(Locale.US, "%.1f", it) } ?: "null"

        return buildString {
            append('{')
            append("\"schema_version\":").append(esc(record.schemaVersion)).append(',')
            append("\"measurement_id\":").append(esc(record.measurementId)).append(',')
            append("\"session_id\":").append(esc(record.sessionId)).append(',')
            append("\"subject_id\":").append(esc(record.subjectId)).append(',')
            append("\"device_id\":").append(esc(record.deviceId)).append(',')
            append("\"eye\":").append(esc(record.eye.code)).append(',')
            append("\"captured_at\":").append(esc(record.capturedAt)).append(',')
            append("\"synthetic\":").append(record.synthetic).append(',')
            append("\"quality_status\":").append(esc(record.qualityStatus.wireValue)).append(',')
            append("\"quality_reasons\":").append(reasonsJson).append(',')
            append("\"iop_estimate_mmhg\":").append(iopJson).append(',')
            append("\"calibration_id\":").append(esc(record.calibrationId)).append(',')
            append("\"model_id\":").append(esc(record.modelId)).append(',')
            append("\"firmware_version\":").append(esc(record.firmwareVersion)).append(',')
            append("\"raw_sha256\":").append(esc(record.rawSha256)).append(',')
            append("\"clock_status\":").append(esc(record.clockStatus.wireValue))
            append('}')
        }
    }

    /**
     * Canonical deterministic payload hash used for idempotent duplicate detection vs conflict quarantine.
     */
    fun canonicalPayloadSha256(record: SyntheticMeasurementV1): String {
        val canonical = buildString {
            append(record.schemaVersion).append('|')
            append(record.measurementId).append('|')
            append(record.sessionId).append('|')
            append(record.subjectId).append('|')
            append(record.deviceId).append('|')
            append(record.eye.code).append('|')
            append(record.capturedAt).append('|')
            append(record.synthetic).append('|')
            append(record.qualityStatus.wireValue).append('|')
            append(record.qualityReasons.joinToString(",") { it.wireValue }).append('|')
            append(record.iopEstimateMmhg?.let { String.format(Locale.US, "%.2f", it) } ?: "null").append('|')
            append(record.calibrationId).append('|')
            append(record.modelId).append('|')
            append(record.firmwareVersion).append('|')
            append(record.rawSha256).append('|')
            append(record.clockStatus.wireValue)
        }
        return sha256Hex(canonical)
    }

    fun sha256Hex(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    private class StrictFlatJsonParser(private val src: String) {
        private var pos = 0

        fun parseObject(): Map<String, Any?> {
            skipWs()
            expect('{')
            val out = LinkedHashMap<String, Any?>()
            skipWs()
            if (peek() == '}') {
                pos++
                return out
            }
            while (true) {
                skipWs()
                val key = parseString()
                skipWs()
                expect(':')
                skipWs()
                val value = parseValue()
                out[key] = value
                skipWs()
                when (peek()) {
                    ',' -> pos++
                    '}' -> {
                        pos++
                        skipWs()
                        if (pos != src.length) throw IllegalArgumentException("Trailing characters after JSON object.")
                        return out
                    }
                    else -> throw IllegalArgumentException("Expected ',' or '}' at index $pos.")
                }
            }
        }

        private fun parseValue(): Any? = when (val c = peek()) {
            '"' -> parseString()
            '[' -> parseArray()
            't' -> {
                expectLiteral("true")
                true
            }
            'f' -> {
                expectLiteral("false")
                false
            }
            'n' -> {
                expectLiteral("null")
                null
            }
            '-', in '0'..'9' -> parseNumber()
            else -> throw IllegalArgumentException("Unexpected token '$c' at index $pos.")
        }

        private fun parseArray(): List<Any?> {
            expect('[')
            val list = mutableListOf<Any?>()
            skipWs()
            if (peek() == ']') {
                pos++
                return list
            }
            while (true) {
                skipWs()
                list += parseValue()
                skipWs()
                when (peek()) {
                    ',' -> pos++
                    ']' -> {
                        pos++
                        return list
                    }
                    else -> throw IllegalArgumentException("Expected ',' or ']' at index $pos.")
                }
            }
        }

        private fun parseString(): String {
            expect('"')
            val sb = StringBuilder()
            while (pos < src.length) {
                val ch = src[pos++]
                when (ch) {
                    '"' -> return sb.toString()
                    '\\' -> {
                        if (pos >= src.length) throw IllegalArgumentException("Unterminated escape sequence.")
                        val esc = src[pos++]
                        sb.append(
                            when (esc) {
                                '"', '\\', '/' -> esc
                                'n' -> '\n'
                                'r' -> '\r'
                                't' -> '\t'
                                else -> throw IllegalArgumentException("Unsupported escape \\$esc")
                            }
                        )
                    }
                    else -> sb.append(ch)
                }
            }
            throw IllegalArgumentException("Unterminated JSON string.")
        }

        private fun parseNumber(): Double {
            val start = pos
            if (peek() == '-') pos++
            while (pos < src.length && src[pos] in '0'..'9') pos++
            if (pos < src.length && src[pos] == '.') {
                pos++
                while (pos < src.length && src[pos] in '0'..'9') pos++
            }
            return src.substring(start, pos).toDouble()
        }

        private fun expect(ch: Char) {
            if (pos >= src.length || src[pos] != ch) {
                throw IllegalArgumentException("Expected '$ch' at index $pos.")
            }
            pos++
        }

        private fun expectLiteral(lit: String) {
            if (!src.startsWith(lit, pos)) {
                throw IllegalArgumentException("Expected '$lit' at index $pos.")
            }
            pos += lit.length
        }

        private fun peek(): Char {
            if (pos >= src.length) throw IllegalArgumentException("Unexpected end of JSON.")
            return src[pos]
        }

        private fun skipWs() {
            while (pos < src.length && src[pos].isWhitespace()) pos++
        }
    }
}
