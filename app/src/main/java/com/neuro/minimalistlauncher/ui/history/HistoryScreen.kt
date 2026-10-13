package com.neuro.minimalistlauncher.ui.history

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neuro.minimalistlauncher.data.db.entity.DailyArchiveWithTasks
import com.neuro.minimalistlauncher.ui.theme.PrimaryOffWhite
import com.neuro.minimalistlauncher.ui.theme.PureBlack
import com.neuro.minimalistlauncher.ui.theme.SecondaryMutedAsh
import com.neuro.minimalistlauncher.ui.theme.SubtleBorder
import com.neuro.minimalistlauncher.ui.util.HapticsHelper
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun HistoryScreen(
    archives: List<DailyArchiveWithTasks>,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedDate by remember { mutableStateOf<String?>(null) }

    val selectedArchive = remember(selectedDate, archives) {
        archives.find { it.archive.date == selectedDate }
    }

    // Intercept back button: if in detail view, return to list; if in list, return to Scratchpad
    BackHandler {
        HapticsHelper.performLightTick(context)
        if (selectedDate != null) {
            selectedDate = null
        } else {
            onBack()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PureBlack)
            .systemBarsPadding()
    ) {
        if (selectedArchive != null) {
            ArchiveDetailView(
                archiveWithTasks = selectedArchive,
                onBack = {
                    HapticsHelper.performLightTick(context)
                    selectedDate = null
                }
            )
        } else {
            ArchiveListView(
                archives = archives,
                onSelectArchive = { date ->
                    HapticsHelper.performLightTick(context)
                    selectedDate = date
                },
                onBack = {
                    HapticsHelper.performLightTick(context)
                    onBack()
                }
            )
        }
    }
}

@Composable
private fun ArchiveListView(
    archives: List<DailyArchiveWithTasks>,
    onSelectArchive: (String) -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 20.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "SCRATCHPAD HISTORY",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.2.sp,
                    color = SecondaryMutedAsh
                )
                Text(
                    text = if (archives.isEmpty()) "No archives yet" else "${archives.size} daily ${if (archives.size == 1) "snapshot" else "snapshots"}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    color = SecondaryMutedAsh,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            // Clean Back Button to return to active scratchpad (min 48dp touch target)
            Box(
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onBack
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Back",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    color = PrimaryOffWhite
                )
            }
        }

        HorizontalDivider(color = SubtleBorder, thickness = 1.dp)

        if (archives.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No archived days yet.\nDaily pages are preserved automatically at midnight.",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    lineHeight = 22.sp,
                    color = SecondaryMutedAsh
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                items(
                    items = archives,
                    key = { it.archive.date }
                ) { item ->
                    val formattedDate = formatArchiveListDate(item.archive.date)
                    val taskSummary = "${item.archive.completedTasks}/${item.archive.totalTasks} tasks completed"

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { onSelectArchive(item.archive.date) }
                            )
                            .padding(vertical = 14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "$formattedDate • $taskSummary",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Normal,
                                color = PrimaryOffWhite,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "›",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Light,
                                color = SecondaryMutedAsh,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                    HorizontalDivider(color = SubtleBorder, thickness = 0.5.dp)
                }
            }
        }
    }
}

@Composable
private fun ArchiveDetailView(
    archiveWithTasks: DailyArchiveWithTasks,
    onBack: () -> Unit
) {
    val scrollState = rememberScrollState()
    val formattedDetailDate = formatArchiveDetailDate(archiveWithTasks.archive.date)

    Column(
        modifier = Modifier
            .fillMaxSize()
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
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = formattedDetailDate,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Medium,
                    color = PrimaryOffWhite
                )
                Text(
                    text = "${archiveWithTasks.archive.completedTasks}/${archiveWithTasks.archive.totalTasks} tasks completed",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    color = SecondaryMutedAsh,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            // Back to history list (min 48dp touch target)
            Box(
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onBack
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Back",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    color = PrimaryOffWhite
                )
            }
        }

        HorizontalDivider(color = SubtleBorder, thickness = 1.dp)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(scrollState)
                .padding(top = 16.dp, bottom = 24.dp)
        ) {
            // Notes section
            Text(
                text = "NOTES",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.2.sp,
                color = SecondaryMutedAsh
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (archiveWithTasks.archive.notesText.isNotBlank()) {
                Text(
                    text = archiveWithTasks.archive.notesText,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Normal,
                    lineHeight = 24.sp,
                    color = PrimaryOffWhite,
                    modifier = Modifier.padding(bottom = 20.dp)
                )
            } else {
                Text(
                    text = "No notes recorded for this day.",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    color = SecondaryMutedAsh,
                    modifier = Modifier.padding(bottom = 20.dp)
                )
            }

            HorizontalDivider(color = SubtleBorder, thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(16.dp))

            // Tasks section
            Text(
                text = "TASKS (${archiveWithTasks.archive.completedTasks}/${archiveWithTasks.archive.totalTasks})",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.2.sp,
                color = SecondaryMutedAsh
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (archiveWithTasks.tasks.isEmpty()) {
                Text(
                    text = "No tasks recorded for this day.",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    color = SecondaryMutedAsh
                )
            } else {
                for (task in archiveWithTasks.tasks) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Tick / circle icon indicator
                        Box(
                            modifier = Modifier.size(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (task.isCompleted) "✓" else "○",
                                fontSize = 14.sp,
                                fontWeight = if (task.isCompleted) FontWeight.Bold else FontWeight.Normal,
                                color = SecondaryMutedAsh
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                            text = task.text,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Normal,
                            color = if (task.isCompleted) SecondaryMutedAsh else PrimaryOffWhite,
                            textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

private fun formatArchiveListDate(dateStr: String): String {
    return try {
        val date = LocalDate.parse(dateStr)
        date.format(DateTimeFormatter.ofPattern("EEEE, MMM d"))
    } catch (e: Exception) {
        dateStr
    }
}

private fun formatArchiveDetailDate(dateStr: String): String {
    return try {
        val date = LocalDate.parse(dateStr)
        date.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy"))
    } catch (e: Exception) {
        dateStr
    }
}
