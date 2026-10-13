package com.neuro.minimalistlauncher.ui.scratchpad

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neuro.minimalistlauncher.data.model.DailyTask
import com.neuro.minimalistlauncher.ui.theme.CharcoalSurface
import com.neuro.minimalistlauncher.ui.theme.PrimaryOffWhite
import com.neuro.minimalistlauncher.ui.theme.PureBlack
import com.neuro.minimalistlauncher.ui.theme.SecondaryMutedAsh
import com.neuro.minimalistlauncher.ui.theme.SubtleBorder
import com.neuro.minimalistlauncher.ui.util.HapticsHelper
import kotlinx.coroutines.delay

@Composable
fun ScratchpadScreen(
    notesText: String,
    onNotesTextChange: (String) -> Unit,
    tasks: List<DailyTask>,
    onAddTask: (String) -> Unit,
    onToggleTask: (String) -> Unit,
    onDeleteTask: (String) -> Unit,
    onClearAll: () -> Unit,
    onOpenHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showClearConfirm by remember { mutableStateOf(false) }
    var newTaskInput by rememberSaveable { mutableStateOf("") }

    // 1. Manage note text input state locally using synchronous Compose state (rememberSaveable)
    // 2. Disconnect real-time keystrokes from direct disk writes
    var localText by rememberSaveable { mutableStateOf(notesText) }

    // Sync local state when external state changes (e.g. daily rollover or clear)
    LaunchedEffect(notesText) {
        if (notesText != localText) {
            localText = notesText
        }
    }

    // 3. Debounce note changes with 300ms delay before persisting
    LaunchedEffect(localText) {
        if (localText != notesText) {
            delay(300L)
            onNotesTextChange(localText)
        }
    }

    val completedTasksCount = tasks.count { it.isCompleted }
    val totalTasksCount = tasks.size
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PureBlack)
            .systemBarsPadding()
            .padding(horizontal = 24.dp, vertical = 20.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
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
                // Live progress header: "X/Y tasks completed • auto-saved"
                Text(
                    text = "$completedTasksCount/$totalTasksCount tasks completed • auto-saved",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    color = SecondaryMutedAsh,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // History Button (min 48dp touch target)
                Box(
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            HapticsHelper.performLightTick(context)
                            onOpenHistory()
                        }
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "History",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Normal,
                        color = SecondaryMutedAsh
                    )
                }

                // Clear / Reset Button
                if (localText.isNotEmpty() || tasks.isNotEmpty()) {
                    if (showClearConfirm) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Clear all?",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Normal,
                                color = SecondaryMutedAsh
                            )
                            Box(
                                modifier = Modifier
                                    .heightIn(min = 48.dp)
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) {
                                        HapticsHelper.performLightTick(context)
                                        localText = ""
                                        onClearAll()
                                        showClearConfirm = false
                                    }
                                    .padding(horizontal = 6.dp, vertical = 6.dp),
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
                                    .heightIn(min = 48.dp)
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) {
                                        HapticsHelper.performLightTick(context)
                                        showClearConfirm = false
                                    }
                                    .padding(horizontal = 6.dp, vertical = 6.dp),
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
                                .heightIn(min = 48.dp)
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
        }

        HorizontalDivider(color = SubtleBorder, thickness = 1.dp)

        // Scrollable content containing Notes and Daily To-Do tasks
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(scrollState)
                .padding(top = 12.dp, bottom = 16.dp)
        ) {
            // Notes Section Header
            Text(
                text = "NOTES",
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.2.sp,
                color = SecondaryMutedAsh
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Multi-line Notes Editor with synchronous state
            BasicTextField(
                value = localText,
                onValueChange = { localText = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 140.dp),
                textStyle = TextStyle(
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Normal,
                    fontSize = 15.sp,
                    lineHeight = 24.sp,
                    letterSpacing = 0.2.sp,
                    color = PrimaryOffWhite
                ),
                cursorBrush = SolidColor(PrimaryOffWhite),
                decorationBox = { innerTextField ->
                    if (localText.isEmpty()) {
                        Text(
                            text = "Write your thoughts, daily intentions, or scratch notes here…",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal,
                            lineHeight = 22.sp,
                            color = SecondaryMutedAsh
                        )
                    }
                    innerTextField()
                }
            )

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = SubtleBorder, thickness = 1.dp)
            Spacer(modifier = Modifier.height(16.dp))

            // Daily To-Do Section Header
            Text(
                text = "DAILY TO-DO",
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.2.sp,
                color = SecondaryMutedAsh
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Single-line Minimalist Task Input Field
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(CharcoalSurface)
                    .border(1.dp, SubtleBorder, RoundedCornerShape(6.dp))
                    .padding(start = 14.dp, end = 4.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BasicTextField(
                        value = newTaskInput,
                        onValueChange = { newTaskInput = it },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        textStyle = TextStyle(
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.Normal,
                            fontSize = 14.sp,
                            color = PrimaryOffWhite
                        ),
                        cursorBrush = SolidColor(PrimaryOffWhite),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                if (newTaskInput.isNotBlank()) {
                                    HapticsHelper.performLightTick(context)
                                    onAddTask(newTaskInput.trim())
                                    newTaskInput = ""
                                }
                            }
                        ),
                        decorationBox = { innerTextField ->
                            if (newTaskInput.isEmpty()) {
                                Text(
                                    text = "Add a task…",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Normal,
                                    color = SecondaryMutedAsh
                                )
                            }
                            innerTextField()
                        }
                    )

                    // Add Task Button (min 48dp touch target)
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clickable(
                                enabled = newTaskInput.isNotBlank(),
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                if (newTaskInput.isNotBlank()) {
                                    HapticsHelper.performLightTick(context)
                                    onAddTask(newTaskInput.trim())
                                    newTaskInput = ""
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "+",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Light,
                            color = if (newTaskInput.isNotBlank()) PrimaryOffWhite else SecondaryMutedAsh
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Interactive Task Items List
            if (tasks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = "No tasks yet today.",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Normal,
                        color = SecondaryMutedAsh
                    )
                }
            } else {
                for (task in tasks) {
                    TaskItemRow(
                        task = task,
                        onToggle = {
                            HapticsHelper.performLightTick(context)
                            onToggleTask(task.id)
                        },
                        onDelete = {
                            HapticsHelper.performLightTick(context)
                            onDeleteTask(task.id)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun TaskItemRow(
    task: DailyTask,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Tick button (minimum 48dp touch target)
        Box(
            modifier = Modifier
                .size(48.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onToggle
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .border(
                        width = 1.dp,
                        color = if (task.isCompleted) SecondaryMutedAsh else PrimaryOffWhite,
                        shape = RoundedCornerShape(4.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (task.isCompleted) {
                    Text(
                        text = "✓",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = SecondaryMutedAsh
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(4.dp))

        // Task plain text label
        Text(
            text = task.text,
            fontSize = 15.sp,
            fontWeight = FontWeight.Normal,
            color = if (task.isCompleted) SecondaryMutedAsh else PrimaryOffWhite,
            textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null,
            modifier = Modifier
                .weight(1f)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onToggle
                )
                .padding(vertical = 12.dp)
        )

        // Delete / cross button (minimum 48dp touch target)
        Box(
            modifier = Modifier
                .size(48.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDelete
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "✕",
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                color = SecondaryMutedAsh
            )
        }
    }
}
