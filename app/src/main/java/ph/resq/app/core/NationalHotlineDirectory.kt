package ph.resq.app.core

enum class HotlineMethod(val label: String) { DIAL("Call"), SMS("Text") }

data class NationalHotline(
    val agency: String,
    val displayNumber: String,
    val uriNumber: String,
    val method: HotlineMethod,
    val note: String
)

/**
 * Offline national agency contacts copied from the Philippine Government Emergency Hotlines portal.
 * Reviewed 2026-10-27. These are not a substitute for local LGU/barangay emergency contacts.
 */
object NationalHotlineDirectory {
    private val contacts = listOf(
        NationalHotline("National Emergency Hotline", "911", "911", HotlineMethod.DIAL, "National emergency hotline; prioritize this for urgent emergencies."),
        NationalHotline("Philippine Red Cross", "143", "143", HotlineMethod.DIAL, "Red Cross hotline."),
        NationalHotline("Philippine National Police", "117", "117", HotlineMethod.DIAL, "PNP emergency hotline."),
        NationalHotline("Metropolitan Manila Development Authority", "136", "136", HotlineMethod.DIAL, "MMDA hotline; Metro Manila scope."),
        NationalHotline("Land Transportation Franchising and Regulatory Board", "1342", "1342", HotlineMethod.DIAL, "LTFRB 24/7 hotline."),
        NationalHotline("Department of Transportation", "7890", "7890", HotlineMethod.DIAL, "DOTr Action Center."),
        NationalHotline("Department of Social Welfare and Development", "0918-912-2813", "09189122813", HotlineMethod.SMS, "DSWD text hotline."),
        NationalHotline("PNP Text Hotline", "0917-847-5757", "09178475757", HotlineMethod.SMS, "PNP text hotline."),
        NationalHotline("Philippine Coast Guard Text Hotline", "0917-724-3682", "09177243682", HotlineMethod.SMS, "PCG text hotline."),
        NationalHotline("Philippine Coast Guard", "(02) 8527-8481", "0285278481", HotlineMethod.DIAL, "PCG office trunk line listed on the portal."),
        NationalHotline("National Disaster Risk Reduction and Management Council", "(02) 8911-5061", "0289115061", HotlineMethod.DIAL, "NDRRMC office trunk line; portal lists local extension 100."),
        NationalHotline("Bureau of Fire Protection", "(02) 8426-0219", "0284260219", HotlineMethod.DIAL, "BFP office line listed on the portal."),
        NationalHotline("PAGASA", "(02) 8284-0800", "0282840800", HotlineMethod.DIAL, "National weather service office line."),
        NationalHotline("PHIVOLCS", "(02) 8426-1468", "0284261468", HotlineMethod.DIAL, "Volcano and earthquake information office line.")
    )

    fun search(query: String): List<NationalHotline> {
        val q = query.trim().lowercase()
        if (q.isEmpty()) return contacts
        return contacts.filter {
            it.agency.lowercase().contains(q) ||
                it.displayNumber.lowercase().contains(q) ||
                it.note.lowercase().contains(q)
        }
    }
}
