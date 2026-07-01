package com.example.data

import android.content.Context
import android.database.Cursor
import android.provider.CalendarContract
import java.util.Calendar

data class CalendarEvent(
    val title: String,
    val startTime: Long,
    val endTime: Long,
    val location: String?
)

class CalendarHelper(private val context: Context) {
    fun getUpcomingEvents(): List<CalendarEvent> {
        val events = mutableListOf<CalendarEvent>()
        val projection = arrayOf(
            CalendarContract.Events.TITLE,
            CalendarContract.Events.DTSTART,
            CalendarContract.Events.DTEND,
            CalendarContract.Events.EVENT_LOCATION
        )

        val now = Calendar.getInstance().timeInMillis
        // Look ahead 7 days
        val nextWeek = now + 7 * 24 * 60 * 60 * 1000L

        val selection = "${CalendarContract.Events.DTSTART} >= ? AND ${CalendarContract.Events.DTSTART} <= ?"
        val selectionArgs = arrayOf(now.toString(), nextWeek.toString())

        try {
            val cursor: Cursor? = context.contentResolver.query(
                CalendarContract.Events.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                "${CalendarContract.Events.DTSTART} ASC"
            )

            cursor?.use {
                val titleIdx = it.getColumnIndex(CalendarContract.Events.TITLE)
                val startIdx = it.getColumnIndex(CalendarContract.Events.DTSTART)
                val endIdx = it.getColumnIndex(CalendarContract.Events.DTEND)
                val locIdx = it.getColumnIndex(CalendarContract.Events.EVENT_LOCATION)

                while (it.moveToNext()) {
                    events.add(
                        CalendarEvent(
                            title = it.getString(titleIdx) ?: "Untitled Event",
                            startTime = it.getLong(startIdx),
                            endTime = it.getLong(endIdx),
                            location = it.getString(locIdx)
                        )
                    )
                }
            }
        } catch (e: SecurityException) {
            // Permission not granted, return empty list
        }
        return events
    }
}
