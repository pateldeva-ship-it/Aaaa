package com.example.util

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.CalendarContract
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.data.model.HomeworkAssignment
import com.example.data.model.Lecture
import java.util.TimeZone

object CalendarSyncHelper {

    fun hasCalendarPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.WRITE_CALENDAR
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Seamlessly opens the System / Google Calendar with pre-populated details
     * for this homework assignment deadline.
     */
    fun launchCalendarIntentForHomework(context: Context, homework: HomeworkAssignment) {
        val startMillis = homework.dueDate - (3600 * 1000) // 1 hour before due
        val endMillis = homework.dueDate

        val intent = Intent(Intent.ACTION_INSERT).apply {
            data = CalendarContract.Events.CONTENT_URI
            putExtra(CalendarContract.Events.TITLE, "[PW HW] ${homework.subject}: ${homework.title}")
            putExtra(CalendarContract.Events.DESCRIPTION,
                "PW Homework Assignment\nBatch: ${homework.batchName}\nQuestions: ${homework.totalQuestions}\nStatus: ${homework.status}\nNotes: ${homework.notes ?: "None"}"
            )
            putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startMillis)
            putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endMillis)
            putExtra(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_BUSY)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Could not open calendar: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Seamlessly opens the Calendar with pre-populated details for scheduling
     * a make-up session for a missed lecture.
     */
    fun launchCalendarIntentForMissedLecture(context: Context, lecture: Lecture, targetTimeMillis: Long) {
        val durationMillis = lecture.durationMinutes * 60 * 1000L
        val endMillis = targetTimeMillis + durationMillis

        val intent = Intent(Intent.ACTION_INSERT).apply {
            data = CalendarContract.Events.CONTENT_URI
            putExtra(CalendarContract.Events.TITLE, "[PW Backlog] ${lecture.subject}: ${lecture.chapterName} (L-${lecture.lectureNumber})")
            putExtra(CalendarContract.Events.DESCRIPTION,
                "Make-up session for missed PW class.\nFaculty: ${lecture.facultyName}\nBatch: ${lecture.batchName}\nScheduled Duration: ${lecture.durationMinutes} min\nReason: ${lecture.missedReason ?: "Unattended"}"
            )
            putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, targetTimeMillis)
            putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endMillis)
            putExtra(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_BUSY)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Could not open calendar: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Direct sync via ContentProvider if permissions are granted.
     */
    fun syncDirectToCalendar(
        context: Context,
        title: String,
        description: String,
        startTimeMillis: Long,
        endTimeMillis: Long
    ): Long? {
        if (!hasCalendarPermission(context)) return null
        return try {
            val values = ContentValues().apply {
                put(CalendarContract.Events.CALENDAR_ID, 1) // default calendar id
                put(CalendarContract.Events.TITLE, title)
                put(CalendarContract.Events.DESCRIPTION, description)
                put(CalendarContract.Events.DTSTART, startTimeMillis)
                put(CalendarContract.Events.DTEND, endTimeMillis)
                put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
                put(CalendarContract.Events.HAS_ALARM, 1)
            }
            val uri: Uri? = context.contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)
            uri?.lastPathSegment?.toLongOrNull()
        } catch (e: Exception) {
            null
        }
    }
}
