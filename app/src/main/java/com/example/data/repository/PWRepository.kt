package com.example.data.repository

import com.example.data.dao.HomeworkDao
import com.example.data.dao.LectureDao
import com.example.data.model.HomeworkAssignment
import com.example.data.model.HomeworkPriority
import com.example.data.model.HomeworkStatus
import com.example.data.model.Lecture
import com.example.data.model.LectureStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import java.util.Calendar

class PWRepository(
    private val lectureDao: LectureDao,
    private val homeworkDao: HomeworkDao
) {
    val allLectures: Flow<List<Lecture>> = lectureDao.getAllLectures()
    val missedLectures: Flow<List<Lecture>> = lectureDao.getMissedLectures()
    val missedLecturesCount: Flow<Int> = lectureDao.getMissedLecturesCount()

    val allHomework: Flow<List<HomeworkAssignment>> = homeworkDao.getAllHomework()
    val pendingHomework: Flow<List<HomeworkAssignment>> = homeworkDao.getPendingHomework()
    val pendingHomeworkCount: Flow<Int> = homeworkDao.getPendingHomeworkCount()

    suspend fun insertLecture(lecture: Lecture): Long = withContext(Dispatchers.IO) {
        lectureDao.insertLecture(lecture)
    }

    suspend fun updateLecture(lecture: Lecture) = withContext(Dispatchers.IO) {
        lectureDao.updateLecture(lecture)
    }

    suspend fun deleteLecture(lecture: Lecture) = withContext(Dispatchers.IO) {
        lectureDao.deleteLecture(lecture)
    }

    suspend fun markLectureStatus(lectureId: Long, status: LectureStatus, reason: String? = null, backlogDate: Long? = null) = withContext(Dispatchers.IO) {
        val existing = lectureDao.getLectureById(lectureId) ?: return@withContext
        val updated = existing.copy(
            status = status,
            missedReason = if (status == LectureStatus.MISSED) reason ?: existing.missedReason else null,
            backlogPlannedDate = if (status == LectureStatus.MISSED) backlogDate ?: existing.backlogPlannedDate else null
        )
        lectureDao.updateLecture(updated)
    }

    suspend fun insertHomework(homework: HomeworkAssignment): Long = withContext(Dispatchers.IO) {
        homeworkDao.insertHomework(homework)
    }

    suspend fun updateHomework(homework: HomeworkAssignment) = withContext(Dispatchers.IO) {
        homeworkDao.updateHomework(homework)
    }

    suspend fun deleteHomework(homework: HomeworkAssignment) = withContext(Dispatchers.IO) {
        homeworkDao.deleteHomework(homework)
    }

    suspend fun updateHomeworkProgress(homeworkId: Long, solvedCount: Int) = withContext(Dispatchers.IO) {
        val existing = homeworkDao.getHomeworkById(homeworkId) ?: return@withContext
        val newStatus = when {
            solvedCount >= existing.totalQuestions -> HomeworkStatus.COMPLETED
            solvedCount > 0 -> HomeworkStatus.IN_PROGRESS
            else -> HomeworkStatus.PENDING
        }
        val updated = existing.copy(
            solvedQuestions = solvedCount.coerceIn(0, existing.totalQuestions),
            status = newStatus
        )
        homeworkDao.updateHomework(updated)
    }

    suspend fun updateHomeworkCalendarSync(homeworkId: Long, isSynced: Boolean, eventId: Long?) = withContext(Dispatchers.IO) {
        val existing = homeworkDao.getHomeworkById(homeworkId) ?: return@withContext
        homeworkDao.updateHomework(existing.copy(isSyncedToCalendar = isSynced, calendarEventId = eventId))
    }

    suspend fun updateLectureCalendarSync(lectureId: Long, isSynced: Boolean, eventId: Long?) = withContext(Dispatchers.IO) {
        val existing = lectureDao.getLectureById(lectureId) ?: return@withContext
        lectureDao.updateLecture(existing.copy(isSyncedToCalendar = isSynced, calendarEventId = eventId))
    }

    suspend fun seedSamplePWDataIfEmpty() = withContext(Dispatchers.IO) {
        val existingLectures = lectureDao.getAllLectures().firstOrNull()
        if (existingLectures.isNullOrEmpty()) {
            val now = System.currentTimeMillis()
            val cal = Calendar.getInstance()

            // Today's lectures
            cal.timeInMillis = now
            val todayDate = cal.timeInMillis

            // Yesterday's lecture (Missed)
            cal.add(Calendar.DAY_OF_YEAR, -1)
            val yesterdayDate = cal.timeInMillis

            // Tomorrow's lecture
            cal.timeInMillis = now
            cal.add(Calendar.DAY_OF_YEAR, 1)
            val tomorrowDate = cal.timeInMillis

            val sampleLectures = listOf(
                Lecture(
                    batchName = "Lakshya JEE 2025",
                    subject = "Physics",
                    chapterName = "Rotational Motion",
                    lectureNumber = 6,
                    facultyName = "Rajwant Sir",
                    scheduledDate = yesterdayDate,
                    scheduledTime = "04:30 PM - 06:15 PM",
                    status = LectureStatus.MISSED,
                    missedReason = "School Practical Exam overlap",
                    backlogPlannedDate = now + (3600 * 1000 * 6),
                    durationMinutes = 105
                ),
                Lecture(
                    batchName = "Lakshya JEE 2025",
                    subject = "Physical Chemistry",
                    chapterName = "Chemical Kinetics",
                    lectureNumber = 4,
                    facultyName = "Pankaj Sir",
                    scheduledDate = todayDate,
                    scheduledTime = "02:00 PM - 03:45 PM",
                    status = LectureStatus.ATTENDED,
                    durationMinutes = 105
                ),
                Lecture(
                    batchName = "Lakshya JEE 2025",
                    subject = "Mathematics",
                    chapterName = "Definite Integration",
                    lectureNumber = 8,
                    facultyName = "Sachin Sir",
                    scheduledDate = todayDate,
                    scheduledTime = "06:30 PM - 08:15 PM",
                    status = LectureStatus.UPCOMING,
                    durationMinutes = 105
                ),
                Lecture(
                    batchName = "Lakshya JEE 2025",
                    subject = "Physics",
                    chapterName = "Rotational Motion (L-07)",
                    lectureNumber = 7,
                    facultyName = "Rajwant Sir",
                    scheduledDate = tomorrowDate,
                    scheduledTime = "04:30 PM - 06:15 PM",
                    status = LectureStatus.UPCOMING,
                    durationMinutes = 105
                )
            )
            lectureDao.insertAllLectures(sampleLectures)

            val sampleHomework = listOf(
                HomeworkAssignment(
                    title = "DPP 06: Moment of Inertia & Torque",
                    subject = "Physics",
                    batchName = "Lakshya JEE 2025",
                    pdfFileName = "PW_Physics_DPP_06_Rotational.pdf",
                    pdfFileSize = "1.8 MB",
                    dueDate = now + (3600 * 1000 * 10), // today 9 PM
                    dueTime = "09:00 PM",
                    totalQuestions = 15,
                    solvedQuestions = 8,
                    status = HomeworkStatus.IN_PROGRESS,
                    priority = HomeworkPriority.HIGH,
                    notes = "Questions 11-15 are advanced level (JEE Adv)."
                ),
                HomeworkAssignment(
                    title = "DPP 04: Rate Laws & Arrhenius Eqn",
                    subject = "Chemistry",
                    batchName = "Lakshya JEE 2025",
                    pdfFileName = "PW_Chem_Kinetics_DPP_04.pdf",
                    pdfFileSize = "2.1 MB",
                    dueDate = now + (3600 * 1000 * 24),
                    dueTime = "08:00 PM",
                    totalQuestions = 12,
                    solvedQuestions = 0,
                    status = HomeworkStatus.PENDING,
                    priority = HomeworkPriority.MEDIUM,
                    notes = "Attempt after reviewing Pankaj Sir's class notes."
                ),
                HomeworkAssignment(
                    title = "Module Exercise 2: Definite Integrals",
                    subject = "Mathematics",
                    batchName = "Lakshya JEE 2025",
                    pdfFileName = "PW_Maths_Calculus_Module_Ex2.pdf",
                    pdfFileSize = "4.2 MB",
                    dueDate = now + (3600 * 1000 * 48),
                    dueTime = "10:00 PM",
                    totalQuestions = 25,
                    solvedQuestions = 25,
                    status = HomeworkStatus.COMPLETED,
                    priority = HomeworkPriority.MEDIUM,
                    notes = "All 25 solved and self-verified with video solutions."
                )
            )
            homeworkDao.insertAllHomework(sampleHomework)
        }
    }
}
