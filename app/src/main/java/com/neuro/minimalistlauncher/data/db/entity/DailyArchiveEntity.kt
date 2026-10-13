package com.neuro.minimalistlauncher.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_archives")
data class DailyArchiveEntity(
    @PrimaryKey
    val date: String, // e.g. "2026-10-12" (ISO-8601 format: YYYY-MM-DD)
    val notesText: String,
    val totalTasks: Int,
    val completedTasks: Int,
    val timestamp: Long = System.currentTimeMillis()
)
