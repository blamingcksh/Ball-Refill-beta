package com.neuro.minimalistlauncher.data.db.entity

import androidx.room.Embedded
import androidx.room.Relation

data class DailyArchiveWithTasks(
    @Embedded
    val archive: DailyArchiveEntity,
    @Relation(
        parentColumn = "date",
        entityColumn = "archiveDate"
    )
    val tasks: List<ArchivedTaskEntity>
)
