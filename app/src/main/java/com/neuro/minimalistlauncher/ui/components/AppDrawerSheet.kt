package com.neuro.minimalistlauncher.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neuro.minimalistlauncher.ui.home.LauncherUiState
import com.neuro.minimalistlauncher.ui.home.LauncherViewModel
import com.neuro.minimalistlauncher.ui.theme.CharcoalElevated
import com.neuro.minimalistlauncher.ui.theme.CharcoalSurface
import com.neuro.minimalistlauncher.ui.theme.PrimaryOffWhite
import com.neuro.minimalistlauncher.ui.theme.PureBlack
import com.neuro.minimalistlauncher.ui.theme.SecondaryMutedAsh
import com.neuro.minimalistlauncher.ui.util.HapticsHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AppDrawerSheet(
    uiState: LauncherUiState,
    viewModel: LauncherViewModel,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val listState = rememberLazyListState()

    // Handle Back button to close drawer
    BackHandler(enabled = uiState.isAppDrawerOpen) {
        focusManager.clearFocus()
        keyboardController?.hide()
        onClose()
    }

    // Intent-First Search: Auto-focus search input and immediately open the keyboard
    LaunchedEffect(uiState.isAppDrawerOpen, uiState.autoFocusSearchOnDrawerOpen) {
        if (uiState.isAppDrawerOpen) {
            delay(60L)
            if (uiState.autoFocusSearchOnDrawerOpen) {
                focusRequester.requestFocus()
                keyboardController?.show()
            }
        }
    }

    // Auto-Launch on Single Match:
    // When typing a query, if the filtered app list narrows down to 1 matching app (and is not a math expression),
    // automatically launch that app without requiring an extra tap.
    LaunchedEffect(uiState.searchQuery, uiState.filteredApps, uiState.mathResult) {
        val trimmed = uiState.searchQuery.trim()
        if (uiState.isAppDrawerOpen && trimmed.isNotEmpty() && uiState.mathResult == null && uiState.filteredApps.size == 1) {
            val singleMatch = uiState.filteredApps.first()
            delay(120L)
            focusManager.clearFocus()
            keyboardController?.hide()
            viewModel.onAppClick(context, singleMatch)
        }
    }

    AnimatedVisibility(
        visible = uiState.isAppDrawerOpen,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
        modifier = modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(PureBlack)
                .systemBarsPadding()
                .draggable(
                    state = rememberDraggableState { delta ->
                        // Detect swipe-down dismiss
                        if (delta > 25f && listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset == 0) {
                            focusManager.clearFocus()
                            keyboardController?.hide()
                            onClose()
                        }
                    },
                    orientation = Orientation.Vertical,
                    onDragStopped = { velocity ->
                        if (velocity > 350f) {
                            focusManager.clearFocus()
                            keyboardController?.hide()
                            onClose()
                        }
                    }
                )
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Top Dismiss Handle Pill
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp, bottom = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .width(36.dp)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(SecondaryMutedAsh.copy(alpha = 0.4f))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                focusManager.clearFocus()
                                keyboardController?.hide()
                                onClose()
                            }
                    )
                }

                // Dedicated Intent-First Search Input Bar
                DrawerSearchBar(
                    query = uiState.searchQuery,
                    onQueryChange = viewModel::onSearchQueryChange,
                    onClear = viewModel::clearSearchQuery,
                    focusRequester = focusRequester,
                    onDone = {
                        focusManager.clearFocus()
                        keyboardController?.hide()
                    }
                )

                // Inline Math Calculation Result (if applicable)
                if (uiState.mathResult != null) {
                    DrawerInlineCalculationResult(
                        result = uiState.mathResult,
                        onCopy = {
                            HapticsHelper.performLightTick(context)
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Math Result", uiState.mathResult))
                            Toast.makeText(context, "Result copied: ${uiState.mathResult}", Toast.LENGTH_SHORT).show()
                        }
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // App List and Fast A-Z Side Scrubber
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    ) {
                        if (uiState.filteredApps.isEmpty() && uiState.mathResult == null) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 24.dp, vertical = 36.dp),
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
                                        keyboardController?.hide()
                                        viewModel.onAppClick(context, app)
                                    },
                                    onLongClick = {
                                        focusManager.clearFocus()
                                        keyboardController?.hide()
                                        viewModel.onAppLongClick(app)
                                    }
                                )
                            }
                        }
                    }

                    // Minimal A-Z Fast Scrubber along the right edge
                    if (uiState.searchQuery.isEmpty() && uiState.filteredApps.isNotEmpty()) {
                        AlphabetScrubber(
                            onLetterSelected = { letter ->
                                val targetIndex = uiState.filteredApps.indexOfFirst {
                                    it.label.firstOrNull()?.uppercaseChar() == letter
                                }
                                val fallbackIndex = uiState.filteredApps.indexOfFirst {
                                    val ch = it.label.firstOrNull()?.uppercaseChar() ?: 'A'
                                    ch >= letter
                                }
                                val indexToScroll = if (targetIndex >= 0) targetIndex else fallbackIndex
                                if (indexToScroll >= 0) {
                                    coroutineScope.launch {
                                        listState.scrollToItem(indexToScroll)
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DrawerSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit,
    focusRequester: FocusRequester,
    onDone: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(8.dp))
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
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(focusRequester),
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
                    onSearch = { onDone() }
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
private fun DrawerInlineCalculationResult(
    result: String,
    onCopy: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp)
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
