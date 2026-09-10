package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class HomeworkStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED,
    OVERDUE
}

enum class HomeworkPriority {
    HIGH,
    MEDIUM,
    LOW
}

@Entity(tableName = "homework_assignments")
data class HomeworkAssignment(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val subject: String,
    val batchName: String,
    val pdfUri: String? = null,
    val pdfFileName: String? = null,
    val pdfFileSize: String? = null,
    val dueDate: Long,
    val dueTime: String = "09:00 PM",
    val totalQuestions: Int = 15,
    val solvedQuestions: Int = 0,
    val status: HomeworkStatus = HomeworkStatus.PENDING,
    val priority: HomeworkPriority = HomeworkPriority.HIGH,
    val notes: String? = null,
    val isSyncedToCalendar: Boolean = false,
    val calendarEventId: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)
