package dev.budsscope.app.domain

import dev.budsscope.app.data.local.entity.ConnectionEventEntity

object ConnectionStateTracker {

    /**
     * Debounce window in milliseconds to avoid duplicate events from rapid Bluetooth profile toggles.
     */
    const val DEBOUNCE_WINDOW_MS = 3_000L

    /**
     * Determines whether an event is a duplicate within the debounce threshold.
     */
    fun shouldRecordEvent(
        eventType: String,
        deviceAddress: String,
        lastEvent: ConnectionEventEntity?,
        nowMs: Long = System.currentTimeMillis()
    ): Boolean {
        if (lastEvent == null) return true

        val isSameDevice = lastEvent.deviceAddress.equals(deviceAddress, ignoreCase = true)
        val isSameType = lastEvent.eventType.equals(eventType, ignoreCase = true)
        val timeDiff = nowMs - lastEvent.timestamp

        // Debounce if same device and same type occurred within DEBOUNCE_WINDOW_MS
        if (isSameDevice && isSameType && timeDiff < DEBOUNCE_WINDOW_MS) {
            return false
        }

        return true
    }

    /**
     * Formats a human-readable duration since last connected.
     */
    fun formatLastSeen(lastConnectedEpochMs: Long?, nowMs: Long = System.currentTimeMillis()): String {
        if (lastConnectedEpochMs == null || lastConnectedEpochMs <= 0L) {
            return "Never connected"
        }

        val diffMs = (nowMs - lastConnectedEpochMs).coerceAtLeast(0L)
        val minutes = diffMs / (60 * 1000L)
        val hours = minutes / 60
        val days = hours / 24

        return when {
            minutes < 1 -> "Just now"
            minutes < 60 -> "$minutes min ago"
            hours < 24 -> "$hours hr ago"
            days == 1L -> "Yesterday"
            else -> "$days days ago"
        }
    }
}
