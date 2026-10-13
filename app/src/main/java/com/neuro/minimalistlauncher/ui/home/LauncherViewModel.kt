package com.neuro.minimalistlauncher.ui.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.neuro.minimalistlauncher.data.datastore.LauncherPreferences
import com.neuro.minimalistlauncher.data.db.dao.DailyArchiveDao
import com.neuro.minimalistlauncher.data.db.entity.ArchivedTaskEntity
import com.neuro.minimalistlauncher.data.db.entity.DailyArchiveEntity
import com.neuro.minimalistlauncher.data.model.AppItem
import com.neuro.minimalistlauncher.data.model.DailyTask
import com.neuro.minimalistlauncher.data.repository.AppRepository
import com.neuro.minimalistlauncher.domain.calculator.MathEvaluator
import com.neuro.minimalistlauncher.ui.util.IntentHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

class LauncherViewModel(
    private val appRepository: AppRepository,
    private val preferences: LauncherPreferences,
    private val archiveDao: DailyArchiveDao
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _interceptedApp = MutableStateFlow<AppItem?>(null)
    private val _appOptionsTarget = MutableStateFlow<AppItem?>(null)
    private val _isHistoryOpen = MutableStateFlow(false)

    init {
        checkDailyRollover()
    }

    private data class LauncherMainState(
        val apps: List<AppItem>,
        val query: String,
        val intercepted: AppItem?,
        val optionsTarget: AppItem?
    )

    private data class ScratchpadState(
        val scratchpadText: String,
        val activeTasks: List<DailyTask>,
        val archives: List<com.neuro.minimalistlauncher.data.db.entity.DailyArchiveWithTasks>,
        val isHistoryOpen: Boolean
    )

    private val mainFlow = combine(
        appRepository.apps,
        _searchQuery,
        _interceptedApp,
        _appOptionsTarget
    ) { apps, query, intercepted, optionsTarget ->
        LauncherMainState(apps, query, intercepted, optionsTarget)
    }

    private val scratchpadFlow = combine(
        preferences.scratchpadText,
        preferences.activeTasks,
        archiveDao.getAllArchivesWithTasks(),
        _isHistoryOpen
    ) { text, tasks, archives, isHistoryOpen ->
        ScratchpadState(text, tasks, archives, isHistoryOpen)
    }

    val uiState: StateFlow<LauncherUiState> = combine(
        mainFlow,
        scratchpadFlow
    ) { main, scratch ->
        val trimmedQuery = main.query.trim()
        val mathCalc = MathEvaluator.evaluate(trimmedQuery)

        val filtered = if (trimmedQuery.isEmpty()) {
            main.apps
        } else {
            main.apps.filter { it.label.contains(trimmedQuery, ignoreCase = true) }
        }

        val pinned = main.apps.filter { it.isPinned }.take(4)

        LauncherUiState(
            allApps = main.apps,
            filteredApps = filtered,
            pinnedApps = pinned,
            searchQuery = main.query,
            mathResult = mathCalc,
            scratchpadText = scratch.scratchpadText,
            activeTasks = scratch.activeTasks,
            archives = scratch.archives,
            isHistoryOpen = scratch.isHistoryOpen,
            interceptedApp = main.intercepted,
            appOptionsTarget = main.optionsTarget
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

    fun setFrictionDuration(app: AppItem, durationSeconds: Int?) {
        viewModelScope.launch {
            preferences.setFrictionDuration(app.packageName, durationSeconds)
        }
    }

    fun toggleFrictionTag(app: AppItem) {
        viewModelScope.launch {
            preferences.toggleFrictionTag(app.packageName)
        }
    }

    // Scratchpad & To-Do Handlers
    fun onScratchpadTextChange(text: String) {
        viewModelScope.launch {
            preferences.updateScratchpad(text)
        }
    }

    fun onAddTask(taskText: String) {
        if (taskText.isBlank()) return
        viewModelScope.launch {
            val currentTasks = preferences.getActiveTasks()
            val newTask = DailyTask(text = taskText.trim())
            preferences.updateActiveTasks(currentTasks + newTask)
        }
    }

    fun onToggleTask(taskId: String) {
        viewModelScope.launch {
            val currentTasks = preferences.getActiveTasks()
            val updated = currentTasks.map { task ->
                if (task.id == taskId) {
                    task.copy(isCompleted = !task.isCompleted)
                } else {
                    task
                }
            }
            preferences.updateActiveTasks(updated)
        }
    }

    fun onDeleteTask(taskId: String) {
        viewModelScope.launch {
            val currentTasks = preferences.getActiveTasks()
            val updated = currentTasks.filterNot { it.id == taskId }
            preferences.updateActiveTasks(updated)
        }
    }

    fun onClearScratchpad() {
        viewModelScope.launch {
            preferences.updateScratchpad("")
            preferences.updateActiveTasks(emptyList())
        }
    }

    // History View Navigation
    fun openHistory() {
        _isHistoryOpen.value = true
    }

    fun closeHistory() {
        _isHistoryOpen.value = false
    }

    // Daily Midnight Rollover Check
    fun checkDailyRollover() {
        viewModelScope.launch {
            val today = LocalDate.now().toString()
            val storedDate = preferences.getActiveSessionDate()

            if (storedDate == null) {
                // Initialize today as active session date
                preferences.setActiveSessionDate(today)
                return@launch
            }

            if (storedDate != today) {
                val previousNotes = preferences.getScratchpadText()
                val previousTasks = preferences.getActiveTasks()

                // a) Snapshot the previous day if there was any content
                if (previousNotes.isNotBlank() || previousTasks.isNotEmpty()) {
                    val archiveEntity = DailyArchiveEntity(
                        date = storedDate,
                        notesText = previousNotes,
                        totalTasks = previousTasks.size,
                        completedTasks = previousTasks.count { it.isCompleted },
                        timestamp = System.currentTimeMillis()
                    )
                    val taskEntities = previousTasks.mapIndexed { index, task ->
                        ArchivedTaskEntity(
                            archiveDate = storedDate,
                            text = task.text,
                            isCompleted = task.isCompleted,
                            position = index
                        )
                    }
                    // b) Save this snapshot into a local Room Database
                    archiveDao.saveArchiveWithTasks(archiveEntity, taskEntities)
                }

                // c) Clear today's active notes and active task list to present a clean, fresh page
                preferences.updateScratchpad("")
                preferences.updateActiveTasks(emptyList())

                // d) Update active date to today
                preferences.setActiveSessionDate(today)
            }
        }
    }

    class Factory(
        private val appRepository: AppRepository,
        private val preferences: LauncherPreferences,
        private val archiveDao: DailyArchiveDao
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(LauncherViewModel::class.java)) {
                return LauncherViewModel(appRepository, preferences, archiveDao) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
