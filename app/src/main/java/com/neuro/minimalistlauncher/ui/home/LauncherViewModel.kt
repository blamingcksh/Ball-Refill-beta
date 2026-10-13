package com.neuro.minimalistlauncher.ui.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.neuro.minimalistlauncher.data.datastore.LauncherPreferences
import com.neuro.minimalistlauncher.data.model.AppItem
import com.neuro.minimalistlauncher.data.repository.AppRepository
import com.neuro.minimalistlauncher.domain.calculator.MathEvaluator
import com.neuro.minimalistlauncher.ui.util.IntentHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LauncherViewModel(
    private val appRepository: AppRepository,
    private val preferences: LauncherPreferences
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _interceptedApp = MutableStateFlow<AppItem?>(null)
    private val _appOptionsTarget = MutableStateFlow<AppItem?>(null)

    val uiState: StateFlow<LauncherUiState> = combine(
        appRepository.apps,
        _searchQuery,
        preferences.scratchpadText,
        _interceptedApp,
        _appOptionsTarget
    ) { apps, query, scratchpad, intercepted, optionsTarget ->
        val trimmedQuery = query.trim()
        val mathCalc = MathEvaluator.evaluate(trimmedQuery)

        val filtered = if (trimmedQuery.isEmpty()) {
            apps
        } else {
            apps.filter { it.label.contains(trimmedQuery, ignoreCase = true) }
        }

        val pinned = apps.filter { it.isPinned }.take(4)

        LauncherUiState(
            allApps = apps,
            filteredApps = filtered,
            pinnedApps = pinned,
            searchQuery = query,
            mathResult = mathCalc,
            scratchpadText = scratchpad,
            interceptedApp = intercepted,
            appOptionsTarget = optionsTarget
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = LauncherUiState()
    )

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun clearSearchQuery() {
        _searchQuery.value = ""
    }

    fun onAppClick(context: Context, app: AppItem) {
        if (app.isFrictionTagged) {
            _interceptedApp.value = app
        } else {
            launchAppDirectly(context, app)
        }
    }

    fun proceedWithFrictionLaunch(context: Context, app: AppItem) {
        _interceptedApp.value = null
        launchAppDirectly(context, app)
    }

    fun cancelFrictionLaunch() {
        _interceptedApp.value = null
    }

    private fun launchAppDirectly(context: Context, app: AppItem) {
        IntentHelper.launchApp(context, app.packageName, app.activityName)
    }

    fun onAppLongClick(app: AppItem) {
        _appOptionsTarget.value = app
    }

    fun dismissAppOptions() {
        _appOptionsTarget.value = null
    }

    fun togglePin(app: AppItem) {
        viewModelScope.launch {
            preferences.togglePin(app.packageName)
        }
    }

    fun toggleFrictionTag(app: AppItem) {
        viewModelScope.launch {
            preferences.toggleFrictionTag(app.packageName)
        }
    }

    fun onScratchpadTextChange(text: String) {
        viewModelScope.launch {
            preferences.updateScratchpad(text)
        }
    }

    class Factory(
        private val appRepository: AppRepository,
        private val preferences: LauncherPreferences
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(LauncherViewModel::class.java)) {
                return LauncherViewModel(appRepository, preferences) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
