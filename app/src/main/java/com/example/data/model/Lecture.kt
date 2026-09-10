package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class LectureStatus {
    ATTENDED,
    MISSED,
    UPCOMING
}

@Entity(tableName = "lectures")
data class Lecture(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val batchName: String, // e.g. "Lakshya JEE 2025", "Yakeen NEET"
    val subject: String,   // "Physics", "Chemistry", "Mathematics", "Biology"
    val chapterName: String,
    val lectureNumber: Int,
    val facultyName: String, // e.g. "Rajwant Sir", "Saleem Sir", "MR Sir"
    val scheduledDate: Long, // timestamp millis of lecture date
    val scheduledTime: String, // e.g. "04:00 PM - 05:45 PM"
    val status: LectureStatus = LectureStatus.UPCOMING,
    val missedReason: String? = null,
    val backlogPlannedDate: Long? = null,
    val durationMinutes: Int = 105,
    val isSyncedToCalendar: Boolean = false,
    val calendarEventId: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
) {
    val isAttended: Boolean get() = status == LectureStatus.ATTENDED
    val isMissed: Boolean get() = status == LectureStatus.MISSED
}
