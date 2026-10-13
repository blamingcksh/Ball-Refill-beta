package com.neuro.minimalistlauncher.data

import com.neuro.minimalistlauncher.data.datastore.LauncherPreferences
import com.neuro.minimalistlauncher.data.model.AppItem
import com.neuro.minimalistlauncher.data.model.DailyTask
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LauncherPreferencesSerializationTest {

    @Test
    fun frictionMap_serializationAndDeserialization_preservesValues() {
        val input = mapOf(
            "com.instagram.android" to 15,
            "com.twitter.android" to 10,
            "com.zhiliaoapp.musically" to 5
        )

        val serialized = LauncherPreferences.serializeFrictionMap(input)
        val deserialized = LauncherPreferences.parseFrictionMap(serialized)

        assertEquals(3, deserialized.size)
        assertEquals(15, deserialized["com.instagram.android"])
        assertEquals(10, deserialized["com.twitter.android"])
        assertEquals(5, deserialized["com.zhiliaoapp.musically"])
    }

    @Test
    fun frictionMap_emptyOrInvalid_returnsEmptyMap() {
        val empty = LauncherPreferences.parseFrictionMap("")
        assertTrue(empty.isEmpty())

        val invalid = LauncherPreferences.parseFrictionMap("invalid-json{")
        assertTrue(invalid.isEmpty())
    }

    @Test
    fun tasks_serializationAndDeserialization_preservesTasksAndState() {
        val tasks = listOf(
            DailyTask(id = "1", text = "Read 10 pages of book", isCompleted = true),
            DailyTask(id = "2", text = "Go for a walk", isCompleted = false)
        )

        val serialized = LauncherPreferences.serializeTasks(tasks)
        val deserialized = LauncherPreferences.parseTasks(serialized)

        assertEquals(2, deserialized.size)
        assertEquals("1", deserialized[0].id)
        assertEquals("Read 10 pages of book", deserialized[0].text)
        assertTrue(deserialized[0].isCompleted)

        assertEquals("2", deserialized[1].id)
        assertEquals("Go for a walk", deserialized[1].text)
        assertFalse(deserialized[1].isCompleted)
    }

    @Test
    fun tasks_emptyOrInvalid_returnsEmptyList() {
        val empty = LauncherPreferences.parseTasks("")
        assertTrue(empty.isEmpty())

        val invalid = LauncherPreferences.parseTasks("not a json array")
        assertTrue(invalid.isEmpty())
    }

    @Test
    fun appItem_defaultFrictionDuration_isFiveSeconds() {
        val item = AppItem(
            packageName = "com.test.app",
            activityName = "com.test.app.MainActivity",
            label = "Test App"
        )
        assertEquals(5, item.frictionDurationSeconds)
        assertFalse(item.isFrictionTagged)
        assertFalse(item.isPinned)
    }
}
