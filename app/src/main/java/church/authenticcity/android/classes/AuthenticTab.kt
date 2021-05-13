package church.authenticcity.android.classes

import church.authenticcity.android.helpers.isNullOrWhiteSpace
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.ZoneId
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.format.DateTimeFormatter
import java.security.MessageDigest
import java.util.*
import kotlin.collections.HashMap

class AuthenticTab(val header: ImageResource, val id: String, val index: Int, val title: String, actionMap: HashMap<String, Any>?, private val elements: List<HashMap<String, Any>?>?, val visibility: HashMap<String, Any>, val specialType: String?, private val password: String?) {
    constructor() : this(ImageResource("unknown.png", 720, 1080), "INVALID", Int.MAX_VALUE, "INVALID", null, null, HashMap<String, Any>().apply { put("override", false) }, null, null)

    private var _elements: List<AuthenticElement> = ArrayList()

    val action = if (actionMap == null) null else ButtonAction(actionMap)

    val convertedElements: List<AuthenticElement>
        get() {
            if (elements == null)
                _elements = ArrayList()
            else if (_elements.count() == 0)
                _elements = elements.asSequence().filterNotNull().map { AuthenticElement(it) }.toList()
            return _elements
        }

    val elementCount
        get() = convertedElements.count()

    val isVisible
        get() = !getShouldBeHidden()

    val hasPassword = password != null

    fun verifyPassword(input: String): Boolean {
        if (!hasPassword) return false
        val hashed = MessageDigest.getInstance("SHA-256").digest(input.toByteArray()).fold("", {str, it -> str + "%02x".format(it) })
        return password == hashed
    }

    private fun getShouldBeHidden(): Boolean {
        if (elementCount == 0 && action == null)
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