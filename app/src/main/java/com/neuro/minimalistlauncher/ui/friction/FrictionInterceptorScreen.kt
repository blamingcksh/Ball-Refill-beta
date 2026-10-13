package com.neuro.minimalistlauncher.ui.friction

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neuro.minimalistlauncher.data.model.AppItem
import com.neuro.minimalistlauncher.ui.theme.DisabledText
import com.neuro.minimalistlauncher.ui.theme.PrimaryOffWhite
import com.neuro.minimalistlauncher.ui.theme.PureBlack
import com.neuro.minimalistlauncher.ui.theme.SecondaryMutedAsh
import com.neuro.minimalistlauncher.ui.util.HapticsHelper
import kotlinx.coroutines.delay

@Composable
fun FrictionInterceptorScreen(
    app: AppItem,
    onCancel: () -> Unit,
    onProceed: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val totalDelay = if (app.frictionDurationSeconds > 0) app.frictionDurationSeconds else 5
    var secondsRemaining by remember(app.packageName, totalDelay) { mutableIntStateOf(totalDelay) }

    // Dynamic non-skippable countdown starting at the configured duration
    LaunchedEffect(app.packageName, totalDelay) {
        secondsRemaining = totalDelay
        while (secondsRemaining > 0) {
            delay(1000L)
            secondsRemaining -= 1
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
            .padding(24.dp)
    ) {
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

            Spacer(modifier = Modifier.height(36.dp))

            Text(
                text = "Take a breath.\nDo you need to open this right now?",
                fontSize = 18.sp,
                fontWeight = FontWeight.Normal,
                lineHeight = 28.sp,
                color = SecondaryMutedAsh,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(36.dp))

            Text(
                text = if (secondsRemaining > 0) {
                    "Pause for ${secondsRemaining}s"
                } else {
                    "Ready"
                },
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                letterSpacing = 0.5.sp,
                color = if (secondsRemaining > 0) SecondaryMutedAsh else PrimaryOffWhite
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Cancel button
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
                    color = PrimaryOffWhite
                )
            }

            // Continue button (disabled until countdown finishes)
            val canContinue = secondsRemaining == 0
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
                    color = if (canContinue) PrimaryOffWhite else DisabledText
                )
            }
        }
    }
}
