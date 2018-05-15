package church.authenticcity.android.classes

import church.authenticcity.android.helpers.isNullOrWhiteSpace
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.ZoneId
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.format.DateTimeFormatter
import java.util.*
import kotlin.collections.HashMap

class AuthenticTab(val header: String, val id: String, val index: Int, val hideTitle: Boolean, val hideHeader: Boolean, val title: String, val elements: List<HashMap<String, Any>>?, val visibility: HashMap<String, Any>) {
    constructor() : this("", "INVALID", Int.MAX_VALUE, false, false, "INVALID", ArrayList<HashMap<String, Any>>(), HashMap<String, Any>().apply { put("override", false) })

    val convertedElements
        get() = elements?.map(::AuthenticElement) ?: ArrayList()

    val elementCount
        get() = convertedElements.count()

    fun getShouldBeHidden(): Boolean {
        if (convertedElements.count() == 0)
            return true
        if (visibility["override"].toString().toBoolean())
            return false
        val start = visibility["start"] as String?
        val end = visibility["end"] as String?
        if (String.isNullOrWhiteSpace(start) || String.isNullOrWhiteSpace(end))
            return true
        val startDate = OffsetDateTime.parse(start, DateTimeFormatter.ISO_DATE_TIME).atZoneSameInstant(ZoneId.systemDefault())
        val endDate = OffsetDateTime.parse(end, DateTimeFormatter.ISO_DATE_TIME).atZoneSameInstant(ZoneId.systemDefault())
        val now = ZonedDateTime.now(ZoneId.systemDefault())
        return now.isBefore(startDate) || now.isAfter(endDate)
    }
}