package ph.resq.app.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SosMessageTest {
    @Test fun parsesValidUniqueContactsAndCapsAtTen() {
        val input = (1..12).joinToString(",") { "+6391712345" + it.toString().padStart(2, '0') } + ",+639171234501"
        val result = ContactList.parse(input)
        assertEquals(10, result.size)
        assertEquals(10, result.distinct().size)
    }

    @Test fun rejectsMalformedContactValues() {
        assertTrue(ContactList.parse("hello,12,abc,+639171234567890123").isEmpty())
    }

    @Test fun buildsTagalogEmergencyMessageAndBoundsNote() {
        val message = SosMessage.build("  Kailangan ng tulong  ")
        assertTrue(message.contains("TULONG KAILANGAN"))
        assertTrue(message.contains("Kailangan ng tulong"))
        assertTrue(message.contains("Lokasyon: hindi pa available"))
    }

    @Test fun stateMachineRejectsFalseDeliveryFromIdle() {
        assertEquals(false, SosStateMachine.canTransition(SosState.IDLE, SosState.DELIVERED))
        assertEquals(true, SosStateMachine.canTransition(SosState.QUEUED, SosState.HANDED_OFF))
    }
}
