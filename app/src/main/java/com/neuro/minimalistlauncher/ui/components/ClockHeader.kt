package com.neuro.minimalistlauncher.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neuro.minimalistlauncher.ui.theme.PrimaryOffWhite
import com.neuro.minimalistlauncher.ui.theme.SecondaryMutedAsh
import com.neuro.minimalistlauncher.ui.util.HapticsHelper
import com.neuro.minimalistlauncher.ui.util.IntentHelper
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ClockHeader(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var currentTime by remember { mutableStateOf(Date()) }

    LaunchedEffect(Unit) {
        while (true) {
            currentTime = Date()
            delay(1000L)
        }
    }

    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val dateFormat = remember { SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()) }

    val formattedTime = timeFormat.format(currentTime)
    val formattedDate = dateFormat.format(currentTime)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = formattedTime,
            fontSize = 54.sp,
            fontWeight = FontWeight.Light,
            letterSpacing = (-1).sp,
            color = PrimaryOffWhite,
            modifier = Modifier.clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                HapticsHelper.performLightTick(context)
                IntentHelper.openClock(context)
            }
        )

        Text(
            text = formattedDate,
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            color = SecondaryMutedAsh,
            modifier = Modifier.clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                HapticsHelper.performLightTick(context)
                IntentHelper.openCalendar(context)
            }
        )
    }
}
