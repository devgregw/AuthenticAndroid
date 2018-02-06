package church.authenticcity.android.classes

import church.authenticcity.android.helpers.isNullOrWhiteSpace
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.format.DateTimeFormatter
import java.util.*

class AuthenticTab(val header: String? = null, val id: String = "", val index: Int = -1, val hideTitle: Boolean = false, val hideHeader: Boolean = false, val title: String = "", val elements: List<HashMap<String, Any>>? = null, val visibility: HashMap<String, Any>? = null) {
    val convertedElements: List<AuthenticElement>
        get() = elements?.map(::AuthenticElement) ?: ArrayList()

    val elementCount: Int
        get() = convertedElements.count()

    fun getShouldBeHidden(): Boolean {
        if (visibility == null)
            return true
        if (convertedElements.count() == 0)
            return true
        if (visibility["override"] as Boolean)
            return false
        val start = visibility["start"] as String
        val end = visibility["end"] as String
        if (String.isNullOrWhiteSpace(start) || String.isNullOrWhiteSpace(end))
            return true
        val startDate = OffsetDateTime.parse(start, DateTimeFormatter.ISO_DATE_TIME)
        val endDate = OffsetDateTime.parse(end, DateTimeFormatter.ISO_DATE_TIME)
        val now = OffsetDateTime.now()
        return now.isBefore(startDate) || now.isAfter(endDate)

    }
}