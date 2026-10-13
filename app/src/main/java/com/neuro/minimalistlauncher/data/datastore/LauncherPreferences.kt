package com.neuro.minimalistlauncher.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "launcher_preferences")

class LauncherPreferences(private val context: Context) {

    private object PreferencesKeys {
        val PINNED_APPS = stringSetPreferencesKey("pinned_packages")
        val FRICTION_TAGGED_APPS = stringSetPreferencesKey("friction_tagged_packages")
        val SCRATCHPAD_TEXT = stringPreferencesKey("scratchpad_text")
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
            preferences[PreferencesKeys.FRICTION_TAGGED_APPS] ?: emptySet()
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

    suspend fun toggleFrictionTag(packageName: String): Boolean {
        var isNowTagged = false
        context.dataStore.edit { preferences ->
            val current = preferences[PreferencesKeys.FRICTION_TAGGED_APPS] ?: emptySet()
            if (current.contains(packageName)) {
                preferences[PreferencesKeys.FRICTION_TAGGED_APPS] = current - packageName
                isNowTagged = false
            } else {
                preferences[PreferencesKeys.FRICTION_TAGGED_APPS] = current + packageName
                isNowTagged = true
            }
        }
        return isNowTagged
    }

    suspend fun updateScratchpad(text: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SCRATCHPAD_TEXT] = text
        }
    }
}
