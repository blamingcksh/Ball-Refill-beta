package com.neuro.minimalistlauncher.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.neuro.minimalistlauncher.data.db.dao.DailyArchiveDao
import com.neuro.minimalistlauncher.data.db.entity.ArchivedTaskEntity
import com.neuro.minimalistlauncher.data.db.entity.DailyArchiveEntity

@Database(
    entities = [
        DailyArchiveEntity::class,
        ArchivedTaskEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun dailyArchiveDao(): DailyArchiveDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "neuro_launcher_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
