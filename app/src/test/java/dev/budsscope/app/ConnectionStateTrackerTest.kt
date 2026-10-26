package dev.budsscope.app

import dev.budsscope.app.data.local.entity.ConnectionEventEntity
import dev.budsscope.app.domain.ConnectionStateTracker
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ConnectionStateTrackerTest {

    @Test
    fun `shouldRecordEvent allows first event when no previous event exists`() {
        val result = ConnectionStateTracker.shouldRecordEvent(
            eventType = "CONNECTED",
            deviceAddress = "AA:BB:CC",
            lastEvent = null
        )
        assertTrue(result)
    }

    @Test
    fun `shouldRecordEvent debounces duplicate event within debounce window`() {
        val now = 100_000L
        val recentEvent = ConnectionEventEntity(
            deviceAddress = "AA:BB:CC",
            deviceName = "Buds",
            timestamp = now - 1000L, // 1 second ago (window is 3s)
            eventType = "CONNECTED"
        )

        val shouldRecord = ConnectionStateTracker.shouldRecordEvent(
            eventType = "CONNECTED",
            deviceAddress = "AA:BB:CC",
            lastEvent = recentEvent,
            nowMs = now
        )
        assertFalse(shouldRecord)
    }

    @Test
    fun `shouldRecordEvent allows different event type within debounce window`() {
        val now = 100_000L
        val recentEvent = ConnectionEventEntity(
            deviceAddress = "AA:BB:CC",
            deviceName = "Buds",
            timestamp = now - 1000L,
            eventType = "CONNECTED"
        )

        val shouldRecord = ConnectionStateTracker.shouldRecordEvent(
            eventType = "DISCONNECTED",
            deviceAddress = "AA:BB:CC",
            lastEvent = recentEvent,
            nowMs = now
        )
        assertTrue(shouldRecord)
    }

    @Test
    fun `shouldRecordEvent allows duplicate event type after debounce window has elapsed`() {
        val now = 100_000L
        val oldEvent = ConnectionEventEntity(
            deviceAddress = "AA:BB:CC",
            deviceName = "Buds",
            timestamp = now - 5000L, // 5 seconds ago
            eventType = "CONNECTED"
        )

        val shouldRecord = ConnectionStateTracker.shouldRecordEvent(
            eventType = "CONNECTED",
            deviceAddress = "AA:BB:CC",
            lastEvent = oldEvent,
            nowMs = now
        )
        assertTrue(shouldRecord)
    }

    @Test
    fun `formatLastSeen handles null or zero gracefully`() {
        assertEquals("Never connected", ConnectionStateTracker.formatLastSeen(null))
        assertEquals("Never connected", ConnectionStateTracker.formatLastSeen(0L))
    }

    @Test
    fun `formatLastSeen formats ranges accurately`() {
        val now = 1_000_000_000_000L

        // 30 seconds ago
        assertEquals("Just now", ConnectionStateTracker.formatLastSeen(now - 30_000L, now))

        // 15 minutes ago
        assertEquals("15 min ago", ConnectionStateTracker.formatLastSeen(now - (15 * 60 * 1000L), now))

        // 3 hours ago
        assertEquals("3 hr ago", ConnectionStateTracker.formatLastSeen(now - (3 * 3600 * 1000L), now))

        // 1 day ago
        assertEquals("Yesterday", ConnectionStateTracker.formatLastSeen(now - (25 * 3600 * 1000L), now))

        // 4 days ago
        assertEquals("4 days ago", ConnectionStateTracker.formatLastSeen(now - (4 * 24 * 3600 * 1000L), now))
    }
}
