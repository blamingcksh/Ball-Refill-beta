package com.neuro.minimalistlauncher.ui.home

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neuro.minimalistlauncher.ui.components.AppDrawerSheet
import com.neuro.minimalistlauncher.ui.components.AppOptionsDialog
import com.neuro.minimalistlauncher.ui.components.ClockHeader
import com.neuro.minimalistlauncher.ui.components.PinnedAppsSection
import com.neuro.minimalistlauncher.ui.friction.FrictionInterceptorScreen
import com.neuro.minimalistlauncher.ui.history.HistoryScreen
import com.neuro.minimalistlauncher.ui.scratchpad.ScratchpadScreen
import com.neuro.minimalistlauncher.ui.theme.CharcoalSurface
import com.neuro.minimalistlauncher.ui.theme.PureBlack
import com.neuro.minimalistlauncher.ui.theme.SecondaryMutedAsh
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
    BackHandler(enabled = pagerState.currentPage == 0 || uiState.isHistoryOpen) {
        if (uiState.isHistoryOpen) {
            viewModel.closeHistory()
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
                    ZeroScrollHomeScreen(
                        uiState = uiState,
                        viewModel = viewModel,
                        onOpenAppDrawer = { autoFocus -> viewModel.openAppDrawer(autoFocus) }
                    )
                }
            }
        }

        // Dedicated Full-Screen Swipe-Up App Drawer & Search Modal
        AppDrawerSheet(
            uiState = uiState,
            viewModel = viewModel,
            onClose = viewModel::closeAppDrawer
        )

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

/**
 * 1. Zero-Scroll Home Screen (Static & Calm):
 * Strictly non-scrollable. Displays:
 *  a) The Clock and Date header at the top.
 *  b) The "ESSENTIALS" pinned apps list (max 4-6 items), positioned in the natural thumb zone with generous negative space above it.
 *  c) The "Search apps or calculate..." pill anchored cleanly near the bottom.
 *
 * Trigger: Swiping UP anywhere or tapping the bottom search bar opens the dedicated App Drawer.
 */
@Composable
private fun ZeroScrollHomeScreen(
    uiState: LauncherUiState,
    viewModel: LauncherViewModel,
    onOpenAppDrawer: (autoFocus: Boolean) -> Unit
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .draggable(
                state = rememberDraggableState { delta ->
                    // Detect upward swipe anywhere on the Home Screen
                    if (delta < -20f) {
                        onOpenAppDrawer(true)
                    }
                },
                orientation = Orientation.Vertical,
                onDragStopped = { velocity ->
                    if (velocity < -250f) {
                        onOpenAppDrawer(true)
                    }
                }
            )
    ) {
        // a) Clock and Date header at the top
        ClockHeader()

        // Generous negative space between Clock and the thumb zone
        Spacer(modifier = Modifier.weight(1f))

        // b) "ESSENTIALS" pinned apps list positioned in the natural thumb zone
        PinnedAppsSection(
            pinnedApps = uiState.pinnedApps,
            onAppClick = { app -> viewModel.onAppClick(context, app) },
            onAppLongClick = viewModel::onAppLongClick
        )

        Spacer(modifier = Modifier.height(14.dp))

        // c) "Search apps or calculate..." pill anchored cleanly near the bottom
        HomeSearchPill(
            onClick = {
                onOpenAppDrawer(true)
            }
        )

        Spacer(modifier = Modifier.height(10.dp))
    }
}

@Composable
private fun HomeSearchPill(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(CharcoalSurface)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                HapticsHelper.performLightTick(context)
                onClick()
            }
            .padding(horizontal = 16.dp, vertical = 13.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Search apps or calculate…",
                fontSize = 15.sp,
                fontWeight = FontWeight.Normal,
                color = SecondaryMutedAsh
            )
        }
    }
}
