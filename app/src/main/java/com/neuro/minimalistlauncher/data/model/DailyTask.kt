package com.neuro.minimalistlauncher.data.model

import java.util.UUID

data class DailyTask(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val isCompleted: Boolean = false
)
