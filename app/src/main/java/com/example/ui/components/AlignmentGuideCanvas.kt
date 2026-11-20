package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.domain.model.EyeSide
import com.example.ui.theme.LocalAirTonColors
import com.example.ui.theme.MonoMetadataStyle

/**
 * Interactive conceptual positioning guide on Compose Canvas.
 * Drag the optical reticle toward the target center to experience guided alignment feedback.
 * Explicitly labeled as a software concept illustration — no camera or sensor is active.
 */
@Composable
fun AlignmentGuideCanvas(
    eye: EyeSide,
    offsetX: Float,
    offsetY: Float,
    alignmentScore: Float,
    isAligned: Boolean,
    onOffsetChange: (Float, Float) -> Unit,
    onCenterAssist: () -> Unit,
    modifier: Modifier = Modifier
) {
    val semantic = LocalAirTonColors.current
    val primaryColor = if (eye == EyeSide.OD) semantic.odSeries else semantic.osSeries
    val activeRingColor = if (isAligned) semantic.statusOkContent else semantic.demoBannerBorder
    val outlineColor = MaterialTheme.colorScheme.outline

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Conceptual Positioning Guide (${eye.code})",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${(alignmentScore * 100).toInt()}% centered",
                    style = MonoMetadataStyle,
                    fontWeight = FontWeight.SemiBold,
                    color = activeRingColor
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp)
                    .testTag("alignment_guide_canvas")
                    .semantics {
                        contentDescription =
                            "Interactive conceptual eye alignment guide for ${eye.labelEn}. Currently ${(alignmentScore * 100).toInt()} percent centered. Drag to center or use Center Assist button."
                    }
            ) {
                Canvas(
                    modifier = Modifier
                        .matchParentSize()
                        .pointerInput(offsetX, offsetY) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                val radius = (minOf(size.width, size.height) / 2f).coerceAtLeast(1f)
                                val nextX = (offsetX + dragAmount.x / radius).coerceIn(-0.85f, 0.85f)
                                val nextY = (offsetY + dragAmount.y / radius).coerceIn(-0.85f, 0.85f)
                                onOffsetChange(nextX, nextY)
                            }
                        }
                ) {
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val maxRadius = (size.minDimension / 2f) - 18f

                    // Outer reference halo
                    drawCircle(
                        color = outlineColor.copy(alpha = 0.45f),
                        radius = maxRadius,
                        center = center,
                        style = Stroke(width = 2.5f)
                    )
                    // Crosshair lines
                    drawLine(
                        color = outlineColor.copy(alpha = 0.35f),
                        start = Offset(center.x - maxRadius, center.y),
                        end = Offset(center.x + maxRadius, center.y),
                        strokeWidth = 1.5f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f))
                    )
                    drawLine(
                        color = outlineColor.copy(alpha = 0.35f),
                        start = Offset(center.x, center.y - maxRadius),
                        end = Offset(center.x, center.y + maxRadius),
                        strokeWidth = 1.5f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f))
                    )

                    // Target sweet-spot ring
                    val sweetSpotRadius = maxRadius * 0.30f
                    drawCircle(
                        color = activeRingColor.copy(alpha = if (isAligned) 0.16f else 0.08f),
                        radius = sweetSpotRadius,
                        center = center
                    )
                    drawCircle(
                        color = activeRingColor,
                        radius = sweetSpotRadius,
                        center = center,
                        style = Stroke(width = 3.5f)
                    )

                    // Draggable simulated pupil reticle
                    val reticleCenter = Offset(
                        x = center.x + (offsetX * maxRadius * 0.85f),
                        y = center.y + (offsetY * maxRadius * 0.85f)
                    )
                    drawCircle(
                        color = primaryColor.copy(alpha = 0.22f),
                        radius = 28f,
                        center = reticleCenter
                    )
                    drawCircle(
                        color = primaryColor,
                        radius = 18f,
                        center = reticleCenter,
                        style = Stroke(width = 4f)
                    )
                    drawCircle(
                        color = primaryColor,
                        radius = 6f,
                        center = reticleCenter
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isAligned) {
                        "Alignment within simulated target ring"
                    } else {
                        "Drag ring to center (or leave off-center to test alignment reject)"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                OutlinedButton(
                    onClick = onCenterAssist,
                    modifier = Modifier.testTag("alignment_center_assist_button")
                ) {
                    Text("Center")
                }
            }
        }
    }
}
