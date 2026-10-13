package com.neuro.minimalistlauncher.ui.scratchpad

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neuro.minimalistlauncher.ui.theme.PrimaryOffWhite
import com.neuro.minimalistlauncher.ui.theme.PureBlack
import com.neuro.minimalistlauncher.ui.theme.SecondaryMutedAsh
import com.neuro.minimalistlauncher.ui.util.HapticsHelper

@Composable
fun ScratchpadScreen(
    text: String,
    onTextChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showClearConfirm by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PureBlack)
            .systemBarsPadding()
            .padding(horizontal = 24.dp, vertical = 20.dp)
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "SCRATCHPAD",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.2.sp,
                    color = SecondaryMutedAsh
                )
                val linesCount = if (text.isEmpty()) 0 else text.lines().size
                Text(
                    text = "$linesCount items · auto-saved",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    color = SecondaryMutedAsh,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            if (text.isNotEmpty()) {
                if (showClearConfirm) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Clear all?",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Normal,
                            color = SecondaryMutedAsh
                        )
                        Box(
                            modifier = Modifier
                                .heightIn(min = 40.dp)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    HapticsHelper.performLightTick(context)
                                    onTextChange("")
                                    showClearConfirm = false
                                }
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Yes",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = PrimaryOffWhite
                            )
                        }
                        Box(
                            modifier = Modifier
                                .heightIn(min = 40.dp)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    HapticsHelper.performLightTick(context)
                                    showClearConfirm = false
                                }
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Normal,
                                color = SecondaryMutedAsh
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .heightIn(min = 40.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                HapticsHelper.performLightTick(context)
                                showClearConfirm = true
                            }
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Clear",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Normal,
                            color = SecondaryMutedAsh
                        )
                    }
                }
            }
        }

        // Full-page Plain Text Editor
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            BasicTextField(
                value = text,
                onValueChange = onTextChange,
                modifier = Modifier.fillMaxSize(),
                textStyle = TextStyle(
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Normal,
                    fontSize = 16.sp,
                    lineHeight = 26.sp,
                    letterSpacing = 0.2.sp,
                    color = PrimaryOffWhite
                ),
                cursorBrush = SolidColor(PrimaryOffWhite),
                decorationBox = { innerTextField ->
                    if (text.isEmpty()) {
                        Text(
                            text = "Write your thoughts, daily intentions, or quick tasks here…\n\nSwipe right to return home.",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Normal,
                            lineHeight = 24.sp,
                            color = SecondaryMutedAsh
                        )
                    }
                    innerTextField()
                }
            )
        }
    }
}
