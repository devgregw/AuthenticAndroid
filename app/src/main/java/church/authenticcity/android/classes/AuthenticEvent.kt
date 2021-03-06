package church.authenticcity.android.classes

import church.authenticcity.android.helpers.RecurrenceRule
import church.authenticcity.android.helpers.getAs
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.ZoneId
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.format.DateTimeFormatter

/**
 * Project AuthenticAndroid
 * Created by Greg Whatley on 4/6/2018 at 1:51 PM.
 * Licensed under the MIT License.
 */

open class AuthenticEvent(val id: String, val title: String, val hideTitle: Boolean, val description: String, val header: ImageResource, dateTime: HashMap<String, Any>, val hideEndDate: Boolean, recurrence: HashMap<String, Any>?, val location: String, val address: String, registration: HashMap<String, Any>?) {
    constructor() : this("INVALID", "INVALID", false, "INVALID", ImageResource("unknown.png", 720, 1080), HashMap<String, Any>().apply {
        put("start", ZonedDateTime.now(ZoneId.systemDefault()).format(DateTimeFormatter.ISO_DATE_TIME))
        put("end", ZonedDateTime.now(ZoneId.systemDefault()).plusDays(1L).format(DateTimeFormatter.ISO_DATE_TIME))
    }, false, null, "INVALID", "", null)

    val startDate = OffsetDateTime.parse(dateTime["start"] as String, DateTimeFormatter.ISO_DATE_TIME).atZoneSameInstant(ZoneId.systemDefault())!!
    val endDate = OffsetDateTime.parse(dateTime["end"] as String, DateTimeFormatter.ISO_DATE_TIME).atZoneSameInstant(ZoneId.systemDefault())!!

    val registrationUrl = registration?.getAs("url") ?: ""
    val price = registration?.getAs("price") ?: 0f

    val recurrenceRule = if (recurrence !== null) RecurrenceRule(recurrence) else null
    val recurs = recurrence !== null

    fun getNextOccurrence() = recurrenceRule?.getNextOccurrence(startDate, endDate) ?: RecurrenceRule.Occurrence(startDate, endDate)//if (recurs) recurrenceRule!!.getNextOccurrence(startDate, endDate) else RecurrenceRule.Occurrence(startDate, endDate)

    open val isVisible
        get() = !getShouldBeHidden()

    private fun getShouldBeHidden() = ZonedDateTime.now() > getNextOccurrence().endDate
}