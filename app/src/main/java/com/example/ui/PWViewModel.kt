package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.PWApplication
import com.example.data.model.HomeworkAssignment
import com.example.data.model.HomeworkPriority
import com.example.data.model.HomeworkStatus
import com.example.data.model.Lecture
import com.example.data.model.LectureStatus
import com.example.data.repository.PWRepository
import com.example.util.CalendarSyncHelper
import com.example.util.NotificationHelper
import com.example.widget.PWDeskWidgetProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PWViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: PWRepository = (application as PWApplication).repository

    val allLectures: StateFlow<List<Lecture>> = repository.allLectures
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val missedLectures: StateFlow<List<Lecture>> = repository.missedLectures
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val missedLecturesCount: StateFlow<Int> = repository.missedLecturesCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val allHomework: StateFlow<List<HomeworkAssignment>> = repository.allHomework
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingHomework: StateFlow<List<HomeworkAssignment>> = repository.pendingHomework
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingHomeworkCount: StateFlow<Int> = repository.pendingHomeworkCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // UI State Filters
    private val _selectedSubjectFilter = MutableStateFlow("All")
    val selectedSubjectFilter: StateFlow<String> = _selectedSubjectFilter.asStateFlow()

    private val _homeworkFilter = MutableStateFlow("All")
    val homeworkFilter: StateFlow<String> = _homeworkFilter.asStateFlow()

    // Filtered lists
    val filteredLectures: StateFlow<List<Lecture>> = combine(allLectures, _selectedSubjectFilter) { list, subject ->
        if (subject == "All") list else list.filter { it.subject.contains(subject, ignoreCase = true) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredHomework: StateFlow<List<HomeworkAssignment>> = combine(allHomework, _homeworkFilter, _selectedSubjectFilter) { list, filter, subject ->
        var res = list
        if (subject != "All") {
            res = res.filter { it.subject.contains(subject, ignoreCase = true) }
        }
        when (filter) {
            "Pending" -> res.filter { it.status != HomeworkStatus.COMPLETED }
            "In Progress" -> res.filter { it.status == HomeworkStatus.IN_PROGRESS }
            "Completed" -> res.filter { it.status == HomeworkStatus.COMPLETED }
            "High Priority" -> res.filter { it.priority == HomeworkPriority.HIGH }
            else -> res
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setSubjectFilter(subject: String) {
        _selectedSubjectFilter.value = subject
    }

    fun setHomeworkFilter(filter: String) {
        _homeworkFilter.value = filter
    }

    fun toggleLectureAttendance(lecture: Lecture, context: Context? = null) {
        val nextStatus = if (lecture.status == LectureStatus.ATTENDED) {
            LectureStatus.MISSED
        } else {
            LectureStatus.ATTENDED
        }
        markLectureAttendance(
            lecture = lecture,
            status = nextStatus,
            reason = if (nextStatus == LectureStatus.MISSED) "Marked Missed" else null,
            context = context
        )
    }

    fun markLectureAttendance(
        lecture: Lecture,
        status: LectureStatus,
        reason: String? = null,
        backlogDate: Long? = null,
        context: Context? = null
    ) {
        viewModelScope.launch {
            repository.markLectureStatus(lecture.id, status, reason, backlogDate)
            context?.let { ctx ->
                if (status == LectureStatus.MISSED) {
                    NotificationHelper.sendMissedLectureAlert(ctx, lecture.copy(status = status, missedReason = reason))
                }
                PWDeskWidgetProvider.triggerUpdate(ctx)
            }
        }
    }

    fun addLecture(
        batchName: String,
        subject: String,
        chapterName: String,
        lectureNumber: Int,
        facultyName: String,
        scheduledDate: Long,
        scheduledTime: String,
        status: LectureStatus = LectureStatus.UPCOMING,
        context: Context? = null
    ) {
        viewModelScope.launch {
            val lecture = Lecture(
                batchName = batchName,
                subject = subject,
                chapterName = chapterName,
                lectureNumber = lectureNumber,
                facultyName = facultyName,
                scheduledDate = scheduledDate,
                scheduledTime = scheduledTime,
                status = status
            )
            repository.insertLecture(lecture)
            context?.let { PWDeskWidgetProvider.triggerUpdate(it) }
        }
    }

    fun deleteLecture(lecture: Lecture, context: Context? = null) {
        viewModelScope.launch {
            repository.deleteLecture(lecture)
            context?.let { PWDeskWidgetProvider.triggerUpdate(it) }
        }
    }

    fun scheduleBacklogWithCalendar(
        lecture: Lecture,
        plannedDateMillis: Long,
        context: Context
    ) {
        viewModelScope.launch {
            repository.markLectureStatus(
                lecture.id,
                LectureStatus.MISSED,
                lecture.missedReason ?: "Missed class backlog",
                plannedDateMillis
            )
            CalendarSyncHelper.launchCalendarIntentForMissedLecture(context, lecture, plannedDateMillis)
            repository.updateLectureCalendarSync(lecture.id, true, null)
            PWDeskWidgetProvider.triggerUpdate(context)
        }
    }

    fun addHomework(
        title: String,
        subject: String,
        batchName: String,
        pdfUri: String?,
        pdfFileName: String?,
        pdfFileSize: String?,
        dueDate: Long,
        dueTime: String,
        totalQuestions: Int,
        priority: HomeworkPriority,
        notes: String?,
        syncToCalendar: Boolean,
        sendPushAlert: Boolean,
        context: Context
    ) {
        viewModelScope.launch {
            val hw = HomeworkAssignment(
                title = title,
                subject = subject,
                batchName = batchName,
                pdfUri = pdfUri,
                pdfFileName = pdfFileName,
                pdfFileSize = pdfFileSize,
                dueDate = dueDate,
                dueTime = dueTime,
                totalQuestions = totalQuestions,
                solvedQuestions = 0,
                status = HomeworkStatus.PENDING,
                priority = priority,
                notes = notes,
                isSyncedToCalendar = syncToCalendar
            )
            val insertedId = repository.insertHomework(hw)
            val savedHw = hw.copy(id = insertedId)

            if (syncToCalendar) {
                CalendarSyncHelper.launchCalendarIntentForHomework(context, savedHw)
            }
            if (sendPushAlert) {
                NotificationHelper.sendNewAssignmentNotification(context, savedHw)
            }
            PWDeskWidgetProvider.triggerUpdate(context)
        }
    }

    fun updateHomeworkProgress(homeworkId: Long, solvedCount: Int, context: Context? = null) {
        viewModelScope.launch {
            repository.updateHomeworkProgress(homeworkId, solvedCount)
            context?.let { PWDeskWidgetProvider.triggerUpdate(it) }
        }
    }

    fun deleteHomework(homework: HomeworkAssignment, context: Context? = null) {
        viewModelScope.launch {
            repository.deleteHomework(homework)
            context?.let { PWDeskWidgetProvider.triggerUpdate(it) }
        }
    }

    fun syncHomeworkToCalendar(homework: HomeworkAssignment, context: Context) {
        viewModelScope.launch {
            CalendarSyncHelper.launchCalendarIntentForHomework(context, homework)
            repository.updateHomeworkCalendarSync(homework.id, true, null)
        }
    }

    fun triggerSampleAssignmentAlert(context: Context) {
        val sample = HomeworkAssignment(
            id = 999,
            title = "DPP 08: Work, Energy & Conservation Laws",
            subject = "Physics",
            batchName = "Lakshya JEE 2025",
            pdfFileName = "PW_Lakshya_Physics_DPP_08.pdf",
            pdfFileSize = "2.3 MB",
            dueDate = System.currentTimeMillis() + (3600 * 1000 * 8),
            dueTime = "09:00 PM",
            totalQuestions = 15,
            status = HomeworkStatus.PENDING,
            priority = HomeworkPriority.HIGH,
            notes = "Recently posted by Rajwant Sir on PW App. Solve after Lecture 8."
        )
        NotificationHelper.sendNewAssignmentNotification(context, sample)
    }

    fun triggerSampleMissedLectureAlert(context: Context) {
        val sample = Lecture(
            id = 888,
            batchName = "Lakshya JEE 2025",
            subject = "Physical Chemistry",
            chapterName = "Thermodynamics & Heat",
            lectureNumber = 5,
            facultyName = "Pankaj Sir",
            scheduledDate = System.currentTimeMillis(),
            scheduledTime = "04:30 PM - 06:15 PM",
            status = LectureStatus.MISSED,
            missedReason = "Did not attend class today"
        )
        NotificationHelper.sendMissedLectureAlert(context, sample)
    }
}
