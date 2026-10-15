package com.neuro.minimalistlauncher.ui.friction

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neuro.minimalistlauncher.data.model.AppItem
import com.neuro.minimalistlauncher.ui.theme.CancelWhite
import com.neuro.minimalistlauncher.ui.theme.DisabledText
import com.neuro.minimalistlauncher.ui.theme.PrimaryOffWhite
import com.neuro.minimalistlauncher.ui.theme.PureBlack
import com.neuro.minimalistlauncher.ui.theme.SecondaryMutedAsh
import com.neuro.minimalistlauncher.ui.util.HapticsHelper
import kotlin.math.ceil
import kotlin.math.cos

/**
 * EaseInOutSine interpolator for biologically coherent breathing cycles.
 * Smoothly accelerates and decelerates at the cycle endpoints.
 */
private fun easeInOutSine(fraction: Float): Float {
    val clamped = fraction.coerceIn(0f, 1f)
    return -0.5f * (cos(Math.PI.toFloat() * clamped) - 1f)
}

@Composable
fun FrictionInterceptorScreen(
    app: AppItem,
    onCancel: () -> Unit,
    onProceed: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    // Valid delays are 5s, 10s, or 15s (multiples of the 5s breathing cycle)
    val totalDelay = if (app.frictionDurationSeconds in listOf(5, 10, 15)) app.frictionDurationSeconds else 5
    val totalMillis = totalDelay * 1000L

    var currentScale by remember(app.packageName, totalDelay) { mutableFloatStateOf(0.6f) }
    var currentAlpha by remember(app.packageName, totalDelay) { mutableFloatStateOf(0.20f) }
    var dynamicSubtext by remember(app.packageName, totalDelay) { mutableStateOf("Inhale slowly...") }
    var secondsRemaining by remember(app.packageName, totalDelay) { mutableIntStateOf(totalDelay) }
    var isCycleFinished by remember(app.packageName, totalDelay) { mutableStateOf(false) }

    // Synchronized 5-second breathing cycle (2.5s Inhale + 2.5s Exhale)
    LaunchedEffect(app.packageName, totalDelay) {
        val startNanos = withFrameNanos { it }
        var lastPeakHapticCycle = -1

        while (true) {
            val currentNanos = withFrameNanos { it }
            val elapsedMillis = ((currentNanos - startNanos) / 1_000_000L).coerceAtMost(totalMillis)
            val remainingMillis = totalMillis - elapsedMillis
            secondsRemaining = ceil(remainingMillis / 1000.0).toInt()

            if (elapsedMillis >= totalMillis) {
                isCycleFinished = true
                secondsRemaining = 0
                dynamicSubtext = "Mindful pause complete"
                currentScale = 0.8f
                currentAlpha = 0.35f
                break
            }

            val cycleMillis = (elapsedMillis % 5000L).toFloat()
            val currentCycleIndex = (elapsedMillis / 5000L).toInt()

            if (cycleMillis < 2500f) {
                // Inhale: 0ms -> 2500ms (0.6x to 1.0x scale, 20% to 55% opacity)
                val t = (cycleMillis / 2500f).coerceIn(0f, 1f)
                val eased = easeInOutSine(t)
                currentScale = (0.6f + (1.0f - 0.6f) * eased).coerceIn(0.6f, 1.0f)
                currentAlpha = (0.20f + (0.55f - 0.20f) * eased).coerceIn(0.20f, 0.55f)
                dynamicSubtext = "Inhale slowly..."
            } else {
                // Peak of Inhale: subtle haptic cue so user can breathe with eyes closed
                if (lastPeakHapticCycle != currentCycleIndex) {
                    lastPeakHapticCycle = currentCycleIndex
                    HapticsHelper.performLightTick(context)
                }
                // Exhale: 2500ms -> 5000ms (1.0x back to 0.6x scale, 55% back to 20% opacity)
                val t = ((cycleMillis - 2500f) / 2500f).coerceIn(0f, 1f)
                val eased = easeInOutSine(t)
                currentScale = (1.0f - (1.0f - 0.6f) * eased).coerceIn(0.6f, 1.0f)
                currentAlpha = (0.55f - (0.55f - 0.20f) * eased).coerceIn(0.20f, 0.55f)
                dynamicSubtext = "Exhale..."
            }
        }
    }

    // Intercept back button to safely cancel and return to home
    BackHandler {
        HapticsHelper.performLightTick(context)
        onCancel()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PureBlack)
            .systemBarsPadding()
            .padding(horizontal = 24.dp, vertical = 20.dp)
    ) {
        // Center Content: App Header, Concentric Breathing Halo, Prompt & Countdown
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "OPENING",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.5.sp,
                color = SecondaryMutedAsh,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = app.label,
                fontSize = 28.sp,
                fontWeight = FontWeight.Light,
                color = PrimaryOffWhite,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Minimalist Concentric Breathing Halo Component
            Box(
                modifier = Modifier
                    .size(150.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(
                    modifier = Modifier
                        .size(140.dp)
                        .graphicsLayer {
                            scaleX = currentScale
                            scaleY = currentScale
                            alpha = currentAlpha
                        }
                ) {
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val radius = size.minDimension / 2f

                    // Soft inner filled halo
                    drawCircle(
                        color = PrimaryOffWhite.copy(alpha = 0.10f),
                        radius = radius * 0.70f,
                        center = center
                    )

                    // Inner concentric ring
                    drawCircle(
                        color = PrimaryOffWhite,
                        radius = radius * 0.52f,
                        center = center,
                        style = Stroke(width = 1.5.dp.toPx())
                    )

                    // Outer concentric halo ring
                    drawCircle(
                        color = PrimaryOffWhite,
                        radius = radius * 0.96f,
                        center = center,
                        style = Stroke(width = 1.dp.toPx())
                    )
                }

                // Breathing dynamic cue text inside the concentric halo
                Text(
                    text = dynamicSubtext,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Normal,
                    letterSpacing = 0.8.sp,
                    color = PrimaryOffWhite.copy(alpha = if (isCycleFinished) 0.6f else 0.85f),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "Take a breath.\nDo you need to open this right now?",
                fontSize = 17.sp,
                fontWeight = FontWeight.Normal,
                lineHeight = 26.sp,
                color = SecondaryMutedAsh,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(28.dp))

            val breathsRemaining = ceil(secondsRemaining / 5.0).toInt()
            val countdownLabel = if (secondsRemaining > 0) {
                if (totalDelay > 5) {
                    "Pause for ${secondsRemaining}s · $breathsRemaining ${if (breathsRemaining == 1) "breath" else "breaths"} remaining"
                } else {
                    "Pause for ${secondsRemaining}s"
                }
            } else {
                "Ready"
            }

            Text(
                text = countdownLabel,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                letterSpacing = 0.5.sp,
                color = if (secondsRemaining > 0) SecondaryMutedAsh else PrimaryOffWhite
            )
        }

        // Bottom Actions: High-contrast Cancel on left, Dimmed/Fading Continue on right
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Cancel button: High-contrast pure white (#FFFFFF), immediate exit
            Box(
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        HapticsHelper.performLightTick(context)
                        onCancel()
                    }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Cancel",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    color = CancelWhite
                )
            }

            // Continue button: Dimmed (#444444) and disabled until breathing cycle completes, then smoothly fades to #EAEAEA
            val canContinue = isCycleFinished && secondsRemaining == 0
            val continueTextColor by animateColorAsState(
                targetValue = if (canContinue) PrimaryOffWhite else DisabledText,
                animationSpec = tween(durationMillis = 500),
                label = "ContinueTextColor"
            )

            Box(
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .clickable(
                        enabled = canContinue,
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        if (canContinue) {
                            HapticsHelper.performLightTick(context)
                            onProceed()
                        }
                    }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Continue",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    color = continueTextColor
                )
            }
        }
    }
}
