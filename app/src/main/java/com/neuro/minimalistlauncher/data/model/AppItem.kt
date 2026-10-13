package com.neuro.minimalistlauncher.data.model

data class AppItem(
    val packageName: String,
    val activityName: String,
    val label: String,
    val isPinned: Boolean = false,
    val isFrictionTagged: Boolean = false
)
