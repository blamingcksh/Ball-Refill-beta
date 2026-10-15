package com.neuro.minimalistlauncher.domain

import com.neuro.minimalistlauncher.data.model.AppItem
import com.neuro.minimalistlauncher.domain.calculator.MathEvaluator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AutoLaunchMatcherTest {

    private val sampleApps = listOf(
        AppItem("com.google.android.apps.camera", "MainActivity", "Camera"),
        AppItem("com.deepseek.chat", "MainActivity", "DeepSeek"),
        AppItem("com.google.android.apps.bard", "MainActivity", "Gemini"),
        AppItem("com.spotify.music", "MainActivity", "Spotify"),
        AppItem("com.whatsapp", "MainActivity", "WhatsApp", isFrictionTagged = true, frictionDurationSeconds = 10),
        AppItem("com.instagram.android", "MainActivity", "Instagram", isFrictionTagged = true, frictionDurationSeconds = 15),
        AppItem("com.slack", "MainActivity", "Slack")
    )

    private fun checkAutoLaunchEligible(
        query: String,
        apps: List<AppItem>
    ): AppItem? {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return null

        val mathResult = MathEvaluator.evaluate(trimmed)
        if (mathResult != null) return null // Math expressions must NOT auto-launch

        val matches = apps.filter { it.label.contains(trimmed, ignoreCase = true) }
        return if (matches.size == 1) matches.first() else null
    }

    @Test
    fun singleMatch_narrowsDownToOneApp_isEligibleForAutoLaunch() {
        val matchCamera = checkAutoLaunchEligible("cam", sampleApps)
        assertNotNull(matchCamera)
        assertEquals("Camera", matchCamera?.label)

        val matchSpotify = checkAutoLaunchEligible("spot", sampleApps)
        assertNotNull(matchSpotify)
        assertEquals("Spotify", matchSpotify?.label)

        val matchWhatsApp = checkAutoLaunchEligible("whats", sampleApps)
        assertNotNull(matchWhatsApp)
        assertEquals("WhatsApp", matchWhatsApp?.label)
        assertTrue(matchWhatsApp?.isFrictionTagged == true)
        assertEquals(10, matchWhatsApp?.frictionDurationSeconds)
    }

    @Test
    fun mathCalculation_isNotAutoLaunched() {
        val mathSum = checkAutoLaunchEligible("2 + 2", sampleApps)
        assertNull(mathSum)

        val mathMult = checkAutoLaunchEligible("12 * 5", sampleApps)
        assertNull(mathMult)

        val mathDiv = checkAutoLaunchEligible("100 / 4", sampleApps)
        assertNull(mathDiv)
    }

    @Test
    fun multipleMatches_doNotAutoLaunch() {
        // "a" matches Camera, DeepSeek (no), WhatsApp, Instagram, Slack
        val multipleMatch = checkAutoLaunchEligible("a", sampleApps)
        assertNull(multipleMatch)
    }

    @Test
    fun emptyQuery_doesNotAutoLaunch() {
        assertNull(checkAutoLaunchEligible("", sampleApps))
        assertNull(checkAutoLaunchEligible("   ", sampleApps))
    }

    @Test
    fun noMatch_doesNotAutoLaunch() {
        assertNull(checkAutoLaunchEligible("xyz123", sampleApps))
    }

    @Test
    fun pinnedAppsLimit_canHoldUpToSixApps() {
        val pinnedList = sampleApps.map { it.copy(isPinned = true) }
        val capped = pinnedList.take(6)
        assertEquals(6, capped.size)
        assertEquals("Camera", capped[0].label)
        assertEquals("Instagram", capped[5].label)
    }

    @Test
    fun frictionTaggedApps_haveCorrectPauseDuration() {
        val whatsApp = sampleApps.first { it.label == "WhatsApp" }
        assertTrue(whatsApp.isFrictionTagged)
        assertEquals(10, whatsApp.frictionDurationSeconds)

        val instagram = sampleApps.first { it.label == "Instagram" }
        assertTrue(instagram.isFrictionTagged)
        assertEquals(15, instagram.frictionDurationSeconds)

        val camera = sampleApps.first { it.label == "Camera" }
        assertFalse(camera.isFrictionTagged)
    }
}
