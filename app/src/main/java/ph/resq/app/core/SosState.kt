package ph.resq.app.core

enum class SosState(val label: String) {
    IDLE("Idle"),
    QUEUED("Queued"),
    SENDING("Sending"),
    HANDOFF_UNCONFIRMED("App was interrupted during SMS handoff — check the messaging app; do not assume it was sent"),
    HANDED_OFF("Handed off to messaging app — delivery unconfirmed"),
    DELIVERED("Delivered — requires confirmation"),
    ACKNOWLEDGED("Acknowledged"),
    CANCELLED("Cancelled"),
    RETRY_PENDING("Retry pending"),
    FAILED("Failed")
}

object SosStateMachine {
    private val transitions = mapOf(
        SosState.IDLE to setOf(SosState.QUEUED, SosState.CANCELLED),
        SosState.QUEUED to setOf(SosState.SENDING, SosState.HANDED_OFF, SosState.CANCELLED, SosState.FAILED),
        SosState.SENDING to setOf(SosState.HANDED_OFF, SosState.HANDOFF_UNCONFIRMED, SosState.RETRY_PENDING, SosState.FAILED, SosState.CANCELLED),
        SosState.HANDOFF_UNCONFIRMED to emptySet(),
        SosState.HANDED_OFF to setOf(SosState.FAILED),
        SosState.DELIVERED to emptySet(),
        SosState.ACKNOWLEDGED to emptySet(),
        SosState.CANCELLED to emptySet(),
        SosState.RETRY_PENDING to setOf(SosState.SENDING, SosState.CANCELLED, SosState.FAILED),
        SosState.FAILED to setOf(SosState.QUEUED)
    )
    /** Delivery and acknowledgement are unreachable until receipt-verifying transports exist. */
    fun canTransition(from: SosState, to: SosState): Boolean =
        to !in setOf(SosState.DELIVERED, SosState.ACKNOWLEDGED) && transitions[from]?.contains(to) == true
}
