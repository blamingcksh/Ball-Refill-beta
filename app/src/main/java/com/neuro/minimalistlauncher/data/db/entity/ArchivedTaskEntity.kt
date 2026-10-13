package com.neuro.minimalistlauncher.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "archived_tasks",
    foreignKeys = [
        ForeignKey(
            entity = DailyArchiveEntity::class,
            parentColumns = ["date"],
            childColumns = ["archiveDate"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["archiveDate"])]
)
data class ArchivedTaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val archiveDate: String,
    val text: String,
    val isCompleted: Boolean,
    val position: Int
)
