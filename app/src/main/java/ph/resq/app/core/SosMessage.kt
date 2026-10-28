package ph.resq.app.core

object ContactList {
    fun parse(raw: String): List<String> = raw
        .split(',', ';', '\n', '\r', '\t', ' ')
        .map { it.trim() }
        .filter { it.matches(Regex("^\\+?[0-9]{7,15}$")) }
        .distinct()
        .take(10)
}

object SosMessage {
    fun build(note: String, locationLine: String = "Lokasyon: hindi pa available sa app build na ito"): String {
        val trimmed = note.trim().take(240)
        return buildString {
            append("EMERGENCY / TULONG KAILANGAN. PH-ResQ SOS. ")
            append(locationLine)
            append(". Oras (UTC ms): ")
            append(System.currentTimeMillis())
            if (trimmed.isNotEmpty()) {
                append(". Mensahe: ")
                append(trimmed)
            }
        }
    }
}
