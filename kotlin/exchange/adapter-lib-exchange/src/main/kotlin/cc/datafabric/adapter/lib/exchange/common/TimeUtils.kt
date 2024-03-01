package cc.datafabric.adapter.lib.exchange.common

import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

object TimeUtils {
    fun ZonedDateTime.toFormattedString(): String {
        //преобразование к UTC, например 2022-09-08T10:30:01.001+03:00 -> 2022-09-08T07:30:01.001Z
        return this.withZoneSameInstant(ZoneOffset.UTC).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)!!
    }

    fun parseZonedDateTimeString(value: String):ZonedDateTime{
        return ZonedDateTime.parse(value,DateTimeFormatter.ISO_OFFSET_DATE_TIME)
    }
}