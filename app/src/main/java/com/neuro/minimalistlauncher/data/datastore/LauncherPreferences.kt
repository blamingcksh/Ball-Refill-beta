package com.neuro.minimalistlauncher.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.neuro.minimalistlauncher.data.model.DailyTask
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.UUID

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "launcher_preferences")

class LauncherPreferences(private val context: Context) {

    private object PreferencesKeys {
        val PINNED_APPS = stringSetPreferencesKey("pinned_packages")
        val FRICTION_TAGGED_APPS = stringSetPreferencesKey("friction_tagged_packages")
        val FRICTION_DURATIONS_MAP = stringPreferencesKey("friction_durations_map")
        val SCRATCHPAD_TEXT = stringPreferencesKey("scratchpad_text")
        val ACTIVE_TASKS = stringPreferencesKey("active_tasks_json")
        val ACTIVE_SESSION_DATE = stringPreferencesKey("active_session_date")
    }

    val pinnedPackages: Flow<Set<String>> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[PreferencesKeys.PINNED_APPS] ?: emptySet()
        }

    val frictionTaggedPackages: Flow<Set<String>> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val set = preferences[PreferencesKeys.FRICTION_TAGGED_APPS] ?: emptySet()
            val mapStr = preferences[PreferencesKeys.FRICTION_DURATIONS_MAP] ?: ""
            val map = parseFrictionMap(mapStr)
            set + map.keys
        }

    val frictionDurations: Flow<Map<String, Int>> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val mapStr = preferences[PreferencesKeys.FRICTION_DURATIONS_MAP] ?: ""
            val parsedMap = parseFrictionMap(mapStr).toMutableMap()
            // Ensure backwards compatibility with any packages tagged in legacy stringSet
            val legacyTagged = preferences[PreferencesKeys.FRICTION_TAGGED_APPS] ?: emptySet()
            for (pkg in legacyTagged) {
                if (!parsedMap.containsKey(pkg)) {
                    parsedMap[pkg] = 5
                }
            }
            parsedMap
        }

    val scratchpadText: Flow<String> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[PreferencesKeys.SCRATCHPAD_TEXT] ?: ""
        }

    val activeTasks: Flow<List<DailyTask>> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val json = preferences[PreferencesKeys.ACTIVE_TASKS] ?: ""
            parseTasks(json)
        }

    val activeSessionDate: Flow<String?> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[PreferencesKeys.ACTIVE_SESSION_DATE]
        }

    suspend fun togglePin(packageName: String): Boolean {
        var isNowPinned = false
        context.dataStore.edit { preferences ->
            val current = preferences[PreferencesKeys.PINNED_APPS] ?: emptySet()
            if (current.contains(packageName)) {
                preferences[PreferencesKeys.PINNED_APPS] = current - packageName
                isNowPinned = false
            } else {
                if (current.size < 4) {
                    preferences[PreferencesKeys.PINNED_APPS] = current + packageName
                    isNowPinned = true
                } else {
                    isNowPinned = false
                }
            }
        }
        return isNowPinned
    }

    suspend fun setPinned(packageName: String, pin: Boolean) {
        context.dataStore.edit { preferences ->
            val current = preferences[PreferencesKeys.PINNED_APPS] ?: emptySet()
            if (pin) {
                if (current.size < 4) {
                    preferences[PreferencesKeys.PINNED_APPS] = current + packageName
                }
            } else {
                preferences[PreferencesKeys.PINNED_APPS] = current - packageName
            }
        }
    }

    suspend fun setFrictionDuration(packageName: String, durationSeconds: Int?) {
        context.dataStore.edit { preferences ->
            val currentMapStr = preferences[PreferencesKeys.FRICTION_DURATIONS_MAP] ?: ""
            val currentMap = parseFrictionMap(currentMapStr).toMutableMap()
            val currentTaggedSet = (preferences[PreferencesKeys.FRICTION_TAGGED_APPS] ?: emptySet()).toMutableSet()

            if (durationSeconds == null || durationSeconds <= 0) {
                currentMap.remove(packageName)
                currentTaggedSet.remove(packageName)
            } else {
                currentMap[packageName] = durationSeconds
                currentTaggedSet.add(packageName)
            }

            preferences[PreferencesKeys.FRICTION_DURATIONS_MAP] = serializeFrictionMap(currentMap)
            preferences[PreferencesKeys.FRICTION_TAGGED_APPS] = currentTaggedSet
        }
    }

    suspend fun toggleFrictionTag(packageName: String): Boolean {
        var isNowTagged = false
        context.dataStore.edit { preferences ->
            val currentMapStr = preferences[PreferencesKeys.FRICTION_DURATIONS_MAP] ?: ""
            val currentMap = parseFrictionMap(currentMapStr).toMutableMap()
            val currentTaggedSet = (preferences[PreferencesKeys.FRICTION_TAGGED_APPS] ?: emptySet()).toMutableSet()

            if (currentMap.containsKey(packageName) || currentTaggedSet.contains(packageName)) {
                currentMap.remove(packageName)
                currentTaggedSet.remove(packageName)
                isNowTagged = false
            } else {
                currentMap[packageName] = 5
                currentTaggedSet.add(packageName)
                isNowTagged = true
            }

            preferences[PreferencesKeys.FRICTION_DURATIONS_MAP] = serializeFrictionMap(currentMap)
            preferences[PreferencesKeys.FRICTION_TAGGED_APPS] = currentTaggedSet
        }
        return isNowTagged
    }

    suspend fun updateScratchpad(text: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SCRATCHPAD_TEXT] = text
        }
    }

    suspend fun updateActiveTasks(tasks: List<DailyTask>) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ACTIVE_TASKS] = serializeTasks(tasks)
        }
    }

    suspend fun setActiveSessionDate(date: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ACTIVE_SESSION_DATE] = date
        }
    }

    suspend fun getActiveSessionDate(): String? {
        val prefs = context.dataStore.data.catch { emit(emptyPreferences()) }.first()
        return prefs[PreferencesKeys.ACTIVE_SESSION_DATE]
    }

    suspend fun getScratchpadText(): String {
        val prefs = context.dataStore.data.catch { emit(emptyPreferences()) }.first()
        return prefs[PreferencesKeys.SCRATCHPAD_TEXT] ?: ""
    }

    suspend fun getActiveTasks(): List<DailyTask> {
        val prefs = context.dataStore.data.catch { emit(emptyPreferences()) }.first()
        val json = prefs[PreferencesKeys.ACTIVE_TASKS] ?: ""
        return parseTasks(json)
    }

    companion object {
        fun serializeFrictionMap(map: Map<String, Int>): String {
            val obj = JSONObject()
            for ((key, value) in map) {
                obj.put(key, value)
            }
            return obj.toString()
        }

        fun parseFrictionMap(jsonStr: String): Map<String, Int> {
            if (jsonStr.isBlank()) return emptyMap()
            return try {
                val obj = JSONObject(jsonStr)
                val map = mutableMapOf<String, Int>()
                val keys = obj.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    map[key] = obj.getInt(key)
                }
                map
            } catch (e: Exception) {
                emptyMap()
            }
        }

        fun serializeTasks(tasks: List<DailyTask>): String {
            val array = JSONArray()
            for (task in tasks) {
                val obj = JSONObject().apply {
                    put("id", task.id)
                    put("text", task.text)
                    put("isCompleted", task.isCompleted)
                }
                array.put(obj)
            }
            return array.toString()
        }

        fun parseTasks(jsonStr: String): List<DailyTask> {
            if (jsonStr.isBlank()) return emptyList()
            return try {
                val array = JSONArray(jsonStr)
                val list = mutableListOf<DailyTask>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(
                        DailyTask(
                            id = obj.optString("id", UUID.randomUUID().toString()),
                            text = obj.optString("text", ""),
                            isCompleted = obj.optBoolean("isCompleted", false)
                        )
                    )
                }
                list
            } catch (e: Exception) {
                emptyList()
            }
        }
    }
}
