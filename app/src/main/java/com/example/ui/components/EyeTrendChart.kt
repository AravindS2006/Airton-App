package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.domain.model.EyeSide
import com.example.domain.model.MeasurementEnvelope
import com.example.domain.model.QualityStatus
import com.example.ui.theme.LocalAirTonColors
import com.example.ui.theme.MonoMetadataStyle
import java.util.Locale
import kotlin.math.hypot

/**
 * Dual-series eye-specific synthetic trend visualization.
 * - Keeps OD (Right Eye) and OS (Left Eye) strictly separate (never averaged).
 * - Renders soft gradient area fills beneath OD/OS trajectories (zero clinical threshold shading).
 * - Displays rejected acquisitions as hollow diamond markers on the baseline with NO IOP number.
 */
@Composable
fun EyeTrendChart(
    envelopes: List<MeasurementEnvelope>,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    onSelectMeasurement: ((String) -> Unit)? = null
) {
    val semantic = LocalAirTonColors.current
    val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
    val surfaceColor = MaterialTheme.colorScheme.surface
    val ordered = remember(envelopes) { envelopes.sortedBy { it.measurement.capturedAt } }
    var selectedEnvelope by remember(ordered) { mutableStateOf<MeasurementEnvelope?>(null) }

    val odAccepted = remember(ordered) {
        ordered.filter { it.measurement.eye == EyeSide.OD && it.measurement.qualityStatus == QualityStatus.PASS && it.measurement.iopEstimateMmhg != null }
    }
    val osAccepted = remember(ordered) {
        ordered.filter { it.measurement.eye == EyeSide.OS && it.measurement.qualityStatus == QualityStatus.PASS && it.measurement.iopEstimateMmhg != null }
    }
    val rejectedCount = remember(ordered) {
        ordered.count { it.measurement.qualityStatus == QualityStatus.REJECT }
    }

    val accessibilitySummary =
        "Synthetic demonstration chart with ${odAccepted.size} accepted Right Eye (OD) points, " +
            "${osAccepted.size} accepted Left Eye (OS) points, and $rejectedCount rejected records with no pressure value. No clinical threshold bands shown."

    Column(
        modifier = modifier
            .fillMaxWidth()
            .semantics { contentDescription = accessibilitySummary },
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LegendDot(color = semantic.odSeries, label = "OD (Right)")
            LegendDot(color = semantic.osSeries, label = "OS (Left)")
            LegendHollowDiamond(color = semantic.statusErrorContent, label = "Rejected (No value)")
        }

        if (ordered.isEmpty()) {
            Text(
                text = "No synthetic records match the current filters.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            return
        }

        val yValues = ordered.mapNotNull { it.measurement.iopEstimateMmhg }
        val minY = ((yValues.minOrNull() ?: 12.0) - 2.0).coerceAtLeast(8.0)
        val maxY = ((yValues.maxOrNull() ?: 24.0) + 2.0).coerceAtLeast(minY + 4.0)

        var plottedPoints by remember { mutableStateOf<List<Pair<Offset, MeasurementEnvelope>>>(emptyList()) }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (compact) 160.dp else 214.dp)
        ) {
            Canvas(
                modifier = Modifier
                    .matchParentSize()
                    .pointerInput(ordered) {
                        detectTapGestures { tap ->
                            val nearest = plottedPoints.minByOrNull { (pos, _) ->
                                hypot(pos.x - tap.x, pos.y - tap.y)
                            }
                            if (nearest != null && hypot(nearest.first.x - tap.x, nearest.first.y - tap.y) <= 46f) {
                                selectedEnvelope = nearest.second
                                onSelectMeasurement?.invoke(nearest.second.measurement.measurementId)
                            }
                        }
                    }
            ) {
                val leftPad = 24f
                val rightPad = 24f
                val topPad = 20f
                val bottomPad = 28f
                val plotWidth = (size.width - leftPad - rightPad).coerceAtLeast(1f)
                val plotHeight = (size.height - topPad - bottomPad).coerceAtLeast(1f)
                val baselineY = topPad + plotHeight

                // Neutral horizontal reference gridlines (no diagnostic/threshold bands!)
                for (i in 0..3) {
                    val y = topPad + (plotHeight / 3f) * i
                    drawLine(
                        color = gridColor,
                        start = Offset(leftPad, y),
                        end = Offset(size.width - rightPad, y),
                        strokeWidth = 1.5f,
                        pathEffect = if (i < 3) PathEffect.dashPathEffect(floatArrayOf(6f, 6f)) else null
                    )
                }

                fun xForIndex(index: Int): Float {
                    return if (ordered.size == 1) {
                        leftPad + plotWidth / 2f
                    } else {
                        leftPad + (plotWidth * index.toFloat() / (ordered.size - 1).toFloat())
                    }
                }

                fun yForValue(v: Double): Float {
                    val ratio = ((v - minY) / (maxY - minY)).toFloat().coerceIn(0f, 1f)
                    return topPad + plotHeight * (1f - ratio)
                }

                val odCoords = mutableListOf<Offset>()
                val osCoords = mutableListOf<Offset>()
                val allClickable = mutableListOf<Pair<Offset, MeasurementEnvelope>>()

                ordered.forEachIndexed { idx, env ->
                    val x = xForIndex(idx)
                    val value = env.measurement.iopEstimateMmhg
                    if (env.measurement.qualityStatus == QualityStatus.PASS && value != null) {
                        val pt = Offset(x, yForValue(value))
                        if (env.measurement.eye == EyeSide.OD) odCoords += pt else osCoords += pt
                        allClickable += pt to env
                    } else {
                        val baselinePt = Offset(x, baselineY + 11f)
                        allClickable += baselinePt to env
                    }
                }
                plottedPoints = allClickable

                if (odCoords.size >= 2) {
                    val fillPath = Path().apply {
                        moveTo(odCoords.first().x, baselineY)
                        odCoords.forEach { lineTo(it.x, it.y) }
                        lineTo(odCoords.last().x, baselineY)
                        close()
                    }
                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(semantic.odSeries.copy(alpha = 0.14f), Color.Transparent),
                            startY = topPad,
                            endY = baselineY
                        )
                    )
                    val linePath = Path().apply {
                        moveTo(odCoords.first().x, odCoords.first().y)
                        odCoords.drop(1).forEach { lineTo(it.x, it.y) }
                    }
                    drawPath(
                        path = linePath,
                        color = semantic.odSeries,
                        style = Stroke(width = 4.5f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )
                }

                if (osCoords.size >= 2) {
                    val linePath = Path().apply {
                        moveTo(osCoords.first().x, osCoords.first().y)
                        osCoords.drop(1).forEach { lineTo(it.x, it.y) }
                    }
                    drawPath(
                        path = linePath,
                        color = semantic.osSeries,
                        style = Stroke(
                            width = 4.2f,
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 8f))
                        )
                    )
                }

                allClickable.forEach { (pt, env) ->
                    val isSelected = selectedEnvelope?.measurement?.measurementId == env.measurement.measurementId
                    if (env.measurement.qualityStatus == QualityStatus.PASS) {
                        val color = if (env.measurement.eye == EyeSide.OD) semantic.odSeries else semantic.osSeries
                        if (isSelected) {
                            drawCircle(color = color.copy(alpha = 0.22f), radius = 20f, center = pt)
                        }
                        drawCircle(color = surfaceColor, radius = 10f, center = pt)
                        drawCircle(color = color, radius = 7.2f, center = pt)
                    } else {
                        val r = if (isSelected) 11.5f else 8.5f
                        val diamond = Path().apply {
                            moveTo(pt.x, pt.y - r)
                            lineTo(pt.x + r, pt.y)
                            lineTo(pt.x, pt.y + r)
                            lineTo(pt.x - r, pt.y)
                            close()
                        }
                        drawPath(diamond, color = surfaceColor)
                        drawPath(diamond, color = semantic.statusErrorContent, style = Stroke(width = 3f))
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Scale: ${String.format(Locale.US, "%.0f", minY)}–${String.format(Locale.US, "%.0f", maxY)} mmHg (Synthetic demo only)",
                style = MonoMetadataStyle,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Tap point to inspect",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        selectedEnvelope?.let { env ->
            val m = env.measurement
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${m.eye.labelEn} · ${m.measurementId}",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = m.iopEstimateMmhg?.let {
                                String.format(Locale.US, "%.1f mmHg (Synthetic)", it)
                            } ?: "Rejected · No IOP value",
                            style = MonoMetadataStyle,
                            fontWeight = FontWeight.SemiBold,
                            color = if (m.qualityStatus == QualityStatus.PASS) {
                                MaterialTheme.colorScheme.primary
                            } else semantic.statusErrorContent
                        )
                    }
                    Text(
                        text = m.capturedAt,
                        style = MonoMetadataStyle,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (m.qualityReasons.isNotEmpty()) {
                        Text(
                            text = "Reason: ${m.qualityReasons.joinToString(", ") { it.labelEn }}",
                            style = MaterialTheme.typography.bodySmall,
                            color = semantic.statusErrorContent
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(text = label, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun LegendHollowDiamond(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
        Canvas(modifier = Modifier.size(11.dp)) {
            val mid = size.width / 2f
            val path = Path().apply {
                moveTo(mid, 1f)
                lineTo(size.width - 1f, mid)
                lineTo(mid, size.height - 1f)
                lineTo(1f, mid)
                close()
            }
            drawPath(path, color = color, style = Stroke(width = 2.2f))
        }
        Text(text = label, style = MaterialTheme.typography.labelSmall)
    }
}
