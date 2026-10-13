package com.neuro.minimalistlauncher.data.repository

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.os.Build
import com.neuro.minimalistlauncher.data.datastore.LauncherPreferences
import com.neuro.minimalistlauncher.data.model.AppItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AppRepository(
    private val context: Context,
    private val preferences: LauncherPreferences
) {
    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val packageManager: PackageManager = context.packageManager

    private val _rawApps = MutableStateFlow<List<RawAppInfo>>(emptyList())

    val apps: StateFlow<List<AppItem>> = combine(
        _rawApps,
        preferences.pinnedPackages,
        preferences.frictionDurations
    ) { rawList, pinnedSet, frictionMap ->
        rawList.map { raw ->
            val duration = frictionMap[raw.packageName]
            val isTagged = duration != null && duration > 0
            AppItem(
                packageName = raw.packageName,
                activityName = raw.activityName,
                label = raw.label,
                isPinned = pinnedSet.contains(raw.packageName),
                isFrictionTagged = isTagged,
                frictionDurationSeconds = duration ?: 5
            )
        }.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.label })
    }.stateIn(
        scope = repositoryScope,
        started = SharingStarted.Eagerly,
        initialValue = emptyList()
    )

    init {
        registerPackageChangeReceiver()
        refreshApps()
    }

    fun refreshApps() {
        repositoryScope.launch {
            loadAppsFromSystem()
        }
    }

    private suspend fun loadAppsFromSystem() = withContext(Dispatchers.IO) {
        val launcherIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val resolveInfos: List<ResolveInfo> = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.queryIntentActivities(
                launcherIntent,
                PackageManager.ResolveInfoFlags.of(0L)
            )
        } else {
            @Suppress("DEPRECATION")
            packageManager.queryIntentActivities(launcherIntent, 0)
        }

        val ownPackageName = context.packageName
        val loadedApps = resolveInfos
            .filter { it.activityInfo != null && it.activityInfo.packageName != ownPackageName }
            .map { resolveInfo ->
                val pkgName = resolveInfo.activityInfo.packageName
                val actName = resolveInfo.activityInfo.name
                val appLabel = resolveInfo.loadLabel(packageManager)?.toString()?.trim() ?: pkgName
                RawAppInfo(
                    packageName = pkgName,
                    activityName = actName,
                    label = appLabel
                )
            }

        _rawApps.value = loadedApps
    }

    private fun registerPackageChangeReceiver() {
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addAction(Intent.ACTION_PACKAGE_CHANGED)
            addDataScheme("package")
        }

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                refreshApps()
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            context.registerReceiver(receiver, filter)
        }
    }

    private data class RawAppInfo(
        val packageName: String,
        val activityName: String,
        val label: String
    )
}
