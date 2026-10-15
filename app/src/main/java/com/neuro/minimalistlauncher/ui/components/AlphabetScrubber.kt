package com.neuro.minimalistlauncher.ui.components

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neuro.minimalistlauncher.ui.theme.PrimaryOffWhite
import com.neuro.minimalistlauncher.ui.theme.SecondaryMutedAsh
import com.neuro.minimalistlauncher.ui.util.HapticsHelper

private val ALPHABET = ('A'..'Z').toList()

@Composable
fun AlphabetScrubber(
    onLetterSelected: (Char) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var totalHeightPx by remember { mutableStateOf(1f) }
    var currentLetter by remember { mutableStateOf<Char?>(null) }

    fun handleTouchAtY(y: Float) {
        if (totalHeightPx <= 0f) return
        val fraction = (y / totalHeightPx).coerceIn(0f, 0.999f)
        val index = (fraction * ALPHABET.size).toInt().coerceIn(0, ALPHABET.size - 1)
        val selected = ALPHABET[index]
        if (selected != currentLetter) {
            currentLetter = selected
            HapticsHelper.performLightTick(context)
            onLetterSelected(selected)
        }
    }

    Box(
        modifier = modifier
            .width(28.dp)
            .fillMaxHeight()
            .onGloballyPositioned { coordinates ->
                totalHeightPx = coordinates.size.height.toFloat()
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = { offset ->
                        handleTouchAtY(offset.y)
                        tryAwaitRelease()
                        currentLetter = null
                    }
                )
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        handleTouchAtY(offset.y)
                    },
                    onDragEnd = {
                        currentLetter = null
                    },
                    onDragCancel = {
                        currentLetter = null
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        handleTouchAtY(change.position.y)
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(vertical = 12.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ALPHABET.forEach { letter ->
                val isActive = currentLetter == letter
                Text(
                    text = letter.toString(),
                    fontSize = if (isActive) 11.sp else 9.sp,
                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                    color = if (isActive) PrimaryOffWhite else SecondaryMutedAsh,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
