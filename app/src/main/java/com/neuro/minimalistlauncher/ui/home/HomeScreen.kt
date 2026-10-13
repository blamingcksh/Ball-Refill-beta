package com.neuro.minimalistlauncher.ui.home

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neuro.minimalistlauncher.ui.components.AppListItem
import com.neuro.minimalistlauncher.ui.components.AppOptionsDialog
import com.neuro.minimalistlauncher.ui.components.ClockHeader
import com.neuro.minimalistlauncher.ui.components.PinnedAppsSection
import com.neuro.minimalistlauncher.ui.friction.FrictionInterceptorScreen
import com.neuro.minimalistlauncher.ui.history.HistoryScreen
import com.neuro.minimalistlauncher.ui.scratchpad.ScratchpadScreen
import com.neuro.minimalistlauncher.ui.theme.CharcoalElevated
import com.neuro.minimalistlauncher.ui.theme.CharcoalSurface
import com.neuro.minimalistlauncher.ui.theme.PrimaryOffWhite
import com.neuro.minimalistlauncher.ui.theme.PureBlack
import com.neuro.minimalistlauncher.ui.theme.SecondaryMutedAsh
import com.neuro.minimalistlauncher.ui.theme.SubtleBorder
import com.neuro.minimalistlauncher.ui.util.HapticsHelper
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    uiState: LauncherUiState,
    viewModel: LauncherViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    // Page 0: Scratchpad, Page 1: Main Launcher Home
    val pagerState = rememberPagerState(initialPage = 1, pageCount = { 2 })

    // Back button handling
    BackHandler(enabled = pagerState.currentPage == 0 || uiState.searchQuery.isNotEmpty() || uiState.isHistoryOpen) {
        if (uiState.isHistoryOpen) {
            viewModel.closeHistory()
        } else if (uiState.searchQuery.isNotEmpty()) {
            viewModel.clearSearchQuery()
        } else if (pagerState.currentPage == 0) {
            coroutineScope.launch {
                pagerState.animateScrollToPage(1)
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PureBlack)
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            when (page) {
                0 -> {
                    if (uiState.isHistoryOpen) {
                        HistoryScreen(
                            archives = uiState.archives,
                            onBack = viewModel::closeHistory
                        )
                    } else {
                        ScratchpadScreen(
                            notesText = uiState.scratchpadText,
                            onNotesTextChange = viewModel::onScratchpadTextChange,
                            tasks = uiState.activeTasks,
                            onAddTask = viewModel::onAddTask,
                            onToggleTask = viewModel::onToggleTask,
                            onDeleteTask = viewModel::onDeleteTask,
                            onClearAll = viewModel::onClearScratchpad,
                            onOpenHistory = viewModel::openHistory
                        )
                    }
                }
                1 -> {
                    MainLauncherContent(
                        uiState = uiState,
                        viewModel = viewModel
                    )
                }
            }
        }

        // Dopamine Intercept Modal Overlay
        if (uiState.interceptedApp != null) {
            FrictionInterceptorScreen(
                app = uiState.interceptedApp,
                onCancel = viewModel::cancelFrictionLaunch,
                onProceed = {
                    viewModel.proceedWithFrictionLaunch(context, uiState.interceptedApp)
                }
            )
        }

        // App Options Dialog
        if (uiState.appOptionsTarget != null) {
            AppOptionsDialog(
                app = uiState.appOptionsTarget,
                onDismiss = viewModel::dismissAppOptions,
                onTogglePin = viewModel::togglePin,
                onSetFrictionDuration = viewModel::setFrictionDuration
            )
        }
    }
}

@Composable
private fun MainLauncherContent(
    uiState: LauncherUiState,
    viewModel: LauncherViewModel
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
    ) {
        // Digital Clock and Date
        ClockHeader()

        // Pinned Apps (Only shown when not actively filtering)
        if (uiState.searchQuery.isEmpty()) {
            PinnedAppsSection(
                pinnedApps = uiState.pinnedApps,
                onAppClick = { app -> viewModel.onAppClick(context, app) },
                onAppLongClick = viewModel::onAppLongClick
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Search & Calculation Input Bar
        SearchBar(
            query = uiState.searchQuery,
            onQueryChange = viewModel::onSearchQueryChange,
            onClear = viewModel::clearSearchQuery
        )

        // Inline Math Calculation Result
        if (uiState.mathResult != null) {
            InlineCalculationResult(
                query = uiState.searchQuery,
                result = uiState.mathResult,
                onCopy = {
                    HapticsHelper.performLightTick(context)
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("Math Result", uiState.mathResult))
                    Toast.makeText(context, "Result copied: ${uiState.mathResult}", Toast.LENGTH_SHORT).show()
                }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Alphabetical App Drawer List
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            if (uiState.filteredApps.isEmpty() && uiState.mathResult == null) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No apps found",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal,
                            color = SecondaryMutedAsh
                        )
                    }
                }
            } else {
                items(
                    items = uiState.filteredApps,
                    key = { it.packageName }
                ) { app ->
                    AppListItem(
                        app = app,
                        onClick = {
                            focusManager.clearFocus()
                            viewModel.onAppClick(context, app)
                        },
                        onLongClick = {
                            focusManager.clearFocus()
                            viewModel.onAppLongClick(app)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(CharcoalSurface)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.weight(1f),
                textStyle = TextStyle(
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Normal,
                    fontSize = 15.sp,
                    color = PrimaryOffWhite
                ),
                singleLine = true,
                cursorBrush = SolidColor(PrimaryOffWhite),
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Search
                ),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        focusManager.clearFocus()
                    }
                ),
                decorationBox = { innerTextField ->
                    if (query.isEmpty()) {
                        Text(
                            text = "Search apps or calculate…",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Normal,
                            color = SecondaryMutedAsh
                        )
                    }
                    innerTextField()
                }
            )

            if (query.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            HapticsHelper.performLightTick(context)
                            onClear()
                        }
                        .padding(start = 12.dp),
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
    }
}

@Composable
private fun InlineCalculationResult(
    query: String,
    result: String,
    onCopy: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(CharcoalElevated)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onCopy
            )
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "= $result",
                fontSize = 18.sp,
                fontWeight = FontWeight.Normal,
                color = PrimaryOffWhite
            )

            Text(
                text = "tap to copy",
                fontSize = 11.sp,
                fontWeight = FontWeight.Normal,
                color = SecondaryMutedAsh
            )
        }
    }
}
