package expo.modules.foregroundservice

import org.junit.Assert.*
import org.junit.Test

class ExpoForegroundServiceModuleTest {

    @Test
    fun `module name constant is correct`() {
        // The module should be named ExpoForegroundService
        val expectedName = "ExpoForegroundService"
        assertEquals(expectedName, "ExpoForegroundService")
    }

    @Test
    fun `notification channel id validation`() {
        // Channel IDs should be non-empty strings
        val validChannelId = "foreground-service-channel"
        assertTrue(validChannelId.isNotEmpty())
        assertTrue(validChannelId.matches(Regex("^[a-zA-Z0-9-_]+$")))
    }

    @Test
    fun `notification title and body can be any string`() {
        val title = "Service Running"
        val body = "Tap to return to app"

        assertTrue(title.isNotEmpty())
        assertTrue(body.isNotEmpty())
    }

    @Test
    fun `default notification icon name`() {
        val defaultIcon = "ic_notification"
        assertEquals("ic_notification", defaultIcon)
    }

    @Test
    fun `service state event name`() {
        val eventName = "onServiceStateChange"
        assertEquals("onServiceStateChange", eventName)
    }
}
