package com.neuro.minimalistlauncher.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.neuro.minimalistlauncher.data.db.entity.ArchivedTaskEntity
import com.neuro.minimalistlauncher.data.db.entity.DailyArchiveEntity
import com.neuro.minimalistlauncher.data.db.entity.DailyArchiveWithTasks
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyArchiveDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArchive(archive: DailyArchiveEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArchivedTasks(tasks: List<ArchivedTaskEntity>)

    @Query("DELETE FROM archived_tasks WHERE archiveDate = :date")
    suspend fun deleteArchivedTasksForDate(date: String)

    @Transaction
    suspend fun saveArchiveWithTasks(archive: DailyArchiveEntity, tasks: List<ArchivedTaskEntity>) {
        insertArchive(archive)
        deleteArchivedTasksForDate(archive.date)
        if (tasks.isNotEmpty()) {
            insertArchivedTasks(tasks)
        }
    }

    @Transaction
    @Query("SELECT * FROM daily_archives ORDER BY date DESC")
    fun getAllArchivesWithTasks(): Flow<List<DailyArchiveWithTasks>>

    @Transaction
    @Query("SELECT * FROM daily_archives WHERE date = :date LIMIT 1")
    suspend fun getArchiveWithTasks(date: String): DailyArchiveWithTasks?

    @Query("SELECT * FROM daily_archives ORDER BY date DESC")
    fun getAllArchives(): Flow<List<DailyArchiveEntity>>

    @Query("SELECT * FROM archived_tasks WHERE archiveDate = :date ORDER BY position ASC")
    suspend fun getTasksForDate(date: String): List<ArchivedTaskEntity>
}
