package com.neuro.minimalistlauncher

import android.app.Application
import com.neuro.minimalistlauncher.data.datastore.LauncherPreferences
import com.neuro.minimalistlauncher.data.db.AppDatabase
import com.neuro.minimalistlauncher.data.repository.AppRepository

class NeuroMinimalApplication : Application() {

    lateinit var preferences: LauncherPreferences
        private set

    lateinit var appRepository: AppRepository
        private set

    lateinit var database: AppDatabase
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        database = AppDatabase.getDatabase(this)
        preferences = LauncherPreferences(this)
        appRepository = AppRepository(this, preferences)
    }

    companion object {
        lateinit var instance: NeuroMinimalApplication
            private set
    }
}
