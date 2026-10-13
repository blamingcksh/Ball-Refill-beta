package com.neuro.minimalistlauncher.ui.home

import com.neuro.minimalistlauncher.data.model.AppItem

data class LauncherUiState(
    val allApps: List<AppItem> = emptyList(),
    val filteredApps: List<AppItem> = emptyList(),
    val pinnedApps: List<AppItem> = emptyList(),
    val searchQuery: String = "",
    val mathResult: String? = null,
    val scratchpadText: String = "",
    val interceptedApp: AppItem? = null,
    val appOptionsTarget: AppItem? = null
)
