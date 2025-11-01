package ru.netology.nework.util

import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale

fun formatDate(isoDate: String): String {
    val outputFormat = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm", Locale.getDefault())
    return try {
        val odt = OffsetDateTime.parse(isoDate, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        outputFormat.format(odt)
    } catch (e: DateTimeParseException) {
        try {
            val odt = OffsetDateTime.parse(isoDate + "+00:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME)
            outputFormat.format(odt)
        } catch (e2: DateTimeParseException) {
            e2.printStackTrace()
            ""
        }
    } catch (e: Exception) {
        e.printStackTrace()
        ""
    }
}
