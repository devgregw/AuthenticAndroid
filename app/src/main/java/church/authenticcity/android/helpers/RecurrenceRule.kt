package church.authenticcity.android.helpers

import org.threeten.bp.OffsetDateTime
import org.threeten.bp.ZoneId
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.format.DateTimeFormatter
import java.util.*

/**
 * Project AuthenticAndroid
 * Created by Greg Whatley on 4/8/2018 at 9:29 PM.
 * Licensed under the MIT License.
 */
class RecurrenceRule(val frequency: String, val interval: Int, val endDate: ZonedDateTime?, val count: Int?) {
    class Occurrence(val startDate: ZonedDateTime, val endDate: ZonedDateTime) {
        fun format(): String {
            if (startDate.dayOfYear != endDate.dayOfYear || startDate.year != endDate.year) {
                return "Starts on ${startDate.format(Utils.datePattern)} at ${startDate.format(Utils.timePattern)} and ends on ${endDate.format(Utils.datePattern)} at ${endDate.format(Utils.timePattern)}"
            }
            else
                return "${startDate.format(Utils.datePattern)} from ${startDate.format(Utils.timePattern)} to ${endDate.format(Utils.timePattern)}"
        }
    }

    val infinite: Boolean = count == null && endDate == null

    fun getRRule(): String {
        val main = "FREQ=${frequency.toUpperCase()};INTERVAL=$interval"
        if (endDate != null)
            return main + ";UNTIL=${endDate.format(DateTimeFormatter.ISO_DATE_TIME)}"
        else if (count != null)
            return main + ";COUNT=${count - 1}"
        return main
    }

    fun format(): String {
        val amount = if (interval == 1) "every" else if (interval == 2) "every other" else "every $interval"
        val main = "Repeats $amount $frequency${if (interval > 2) "s" else ""}"
        return when {
            endDate != null -> main + " until ${endDate.format(Utils.datePattern)} at ${endDate.format(Utils.timePattern)}"
            count != null -> main + " $count more time${if (count == 1) "s" else ""}"
            else -> main
        }
    }

    constructor(data: HashMap<String, Any>) : this(data["frequency"] as String, data["interval"].toString().toInt(), if (data.containsKey("date")) OffsetDateTime.parse(data["date"] as String, DateTimeFormatter.ISO_DATE_TIME).atZoneSameInstant(ZoneId.systemDefault()) else null, if (data.containsKey("number")) data["number"].toString().toInt() else null)

    private fun getOccurrences(initialStart: ZonedDateTime, initialEnd: ZonedDateTime): List<Occurrence> {
        val occurrences = ArrayList<Occurrence>()
        occurrences.add(Occurrence(initialStart, initialEnd))
        if (endDate !== null) {
            while (occurrences.maxBy { it.startDate }!!.startDate.isBefore(endDate)) {
                val max = occurrences.maxBy { it.startDate }!!
                occurrences.add(Occurrence(addInterval(max.startDate), addInterval(max.endDate)))
            }
            if (occurrences.maxBy { it.startDate }!!.startDate.isAfter(endDate))
                occurrences.remove(occurrences.maxBy { it.startDate }!!)
        } else if (count !== null)
            while (occurrences.count() < count) {
                val max = occurrences.maxBy { it.startDate }!!
                occurrences.add(Occurrence(addInterval(max.startDate), addInterval(max.endDate)))
            }
        else return RecurrenceRule(frequency, interval, null, 30).getOccurrences(initialStart, initialEnd)
        return occurrences
    }

    private fun addInterval(date: ZonedDateTime): ZonedDateTime =
        when (frequency) {
            "daily" -> date.plusDays(interval.toLong())
            "weekly" -> date.plusWeeks(interval.toLong())
            "monthly" -> date.plusMonths(interval.toLong())
            "yearly" -> date.plusYears(interval.toLong())
            else -> throw IllegalArgumentException("Invalid frequency $frequency")
        }

    fun getNextOccurrence(initialStart: ZonedDateTime, initialEnd: ZonedDateTime): Occurrence = getOccurrences(initialStart, initialEnd).first { it.startDate.isAfter(ZonedDateTime.now()) }
}