package church.authenticcity.android.classes

import org.threeten.bp.OffsetDateTime
import org.threeten.bp.ZoneId
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.format.DateTimeFormatter
import java.util.*

/**
 * Project AuthenticAndroid
 * Created by Greg Whatley on 8/28/2018 at 9:55 AM.
 * Licensed under the MIT License.
 */
class AuthenticEventPlaceholder(id: String, val index: Int, title: String, hideTitle: Boolean, header: ImageResource, private val elements: List<HashMap<String, Any>>?, val action: ButtonAction?, visibilityString: String?) : AuthenticEvent(id, title, hideTitle, "", header, HashMap<String, Any>().apply {
    put("start", ZonedDateTime.now(ZoneId.systemDefault()).format(DateTimeFormatter.ISO_DATE_TIME))
    put("end", ZonedDateTime.now(ZoneId.systemDefault()).plusDays(1L).format(DateTimeFormatter.ISO_DATE_TIME))
}, false, null, "", "", null) {
    private var _elements: List<AuthenticElement> = ArrayList()

    val visibility: ZonedDateTime? = when (visibilityString) {
        null -> null
        else -> OffsetDateTime.parse(visibilityString, DateTimeFormatter.ISO_DATE_TIME).atZoneSameInstant(ZoneId.systemDefault())!!
    }

    override val isVisible
        get() = when (visibility) {
            null -> true
            else -> ZonedDateTime.now(ZoneId.systemDefault()).isBefore(visibility)
        }

    val convertedElements: List<AuthenticElement>
        get() {
            if (elements == null)
                _elements = ArrayList()
            else if (_elements.count() == 0)
                _elements = elements.map { AuthenticElement(it) }
            return _elements
        }

    private val elementCount
        get() = convertedElements.count()

    val canOpen
        get() = elementCount > 0
}