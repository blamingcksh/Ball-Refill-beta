package com.neuro.minimalistlauncher.ui.home

import com.neuro.minimalistlauncher.data.db.entity.DailyArchiveWithTasks
import com.neuro.minimalistlauncher.data.model.AppItem
import com.neuro.minimalistlauncher.data.model.DailyTask

data class LauncherUiState(
    val allApps: List<AppItem> = emptyList(),
    val filteredApps: List<AppItem> = emptyList(),
    val pinnedApps: List<AppItem> = emptyList(),
    val searchQuery: String = "",
    val mathResult: String? = null,
    val scratchpadText: String = "",
    val activeTasks: List<DailyTask> = emptyList(),
    val archives: List<DailyArchiveWithTasks> = emptyList(),
    val isHistoryOpen: Boolean = false,
    val interceptedApp: AppItem? = null,
    val appOptionsTarget: AppItem? = null,
    val isAppDrawerOpen: Boolean = false,
    val autoFocusSearchOnDrawerOpen: Boolean = true
)
