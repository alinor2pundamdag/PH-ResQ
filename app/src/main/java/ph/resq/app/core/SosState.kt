package ph.resq.app.core

enum class SosState(val label: String) {
    IDLE("Idle"),
    QUEUED("Queued"),
    SENDING("Sending"),
    HANDED_OFF("Handed off to messaging app — delivery unconfirmed"),
    DELIVERED("Delivered — requires confirmation"),
    ACKNOWLEDGED("Acknowledged"),
    CANCELLED("Cancelled"),
    FAILED("Failed")
}

object SosStateMachine {
    private val transitions = mapOf(
        SosState.IDLE to setOf(SosState.QUEUED, SosState.CANCELLED),
        SosState.QUEUED to setOf(SosState.SENDING, SosState.HANDED_OFF, SosState.CANCELLED, SosState.FAILED),
        SosState.SENDING to setOf(SosState.HANDED_OFF, SosState.DELIVERED, SosState.FAILED, SosState.CANCELLED),
        SosState.HANDED_OFF to setOf(SosState.DELIVERED, SosState.ACKNOWLEDGED, SosState.FAILED),
        SosState.DELIVERED to setOf(SosState.ACKNOWLEDGED, SosState.FAILED),
        SosState.ACKNOWLEDGED to emptySet(),
        SosState.CANCELLED to emptySet(),
        SosState.FAILED to setOf(SosState.QUEUED)
    )
    fun canTransition(from: SosState, to: SosState): Boolean = transitions[from]?.contains(to) == true
}
