package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.PWApplication
import com.example.data.model.Lecture
import com.example.data.model.LectureStatus
import com.example.data.repository.PWRepository
import com.example.util.NotificationHelper
import com.example.widget.PWDeskWidgetProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel specifically responsible for managing Lectures,
 * persisting status changes (Attended / Missed) to the Room database,
 * and driving the UI toggle state.
 */
class LecturesViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: PWRepository = (application as PWApplication).repository

    val allLectures: StateFlow<List<Lecture>> = repository.allLectures
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val missedLectures: StateFlow<List<Lecture>> = repository.missedLectures
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val missedLecturesCount: StateFlow<Int> = repository.missedLecturesCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val _subjectFilter = MutableStateFlow("All")
    val subjectFilter: StateFlow<String> = _subjectFilter.asStateFlow()

    val filteredLectures: StateFlow<List<Lecture>> = combine(allLectures, _subjectFilter) { list, filter ->
        if (filter == "All") list else list.filter { it.subject.contains(filter, ignoreCase = true) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setSubjectFilter(subject: String) {
        _subjectFilter.value = subject
    }

    /**
     * Toggles the lecture's status between ATTENDED and MISSED in the Room database.
     */
    fun toggleLectureAttendance(lecture: Lecture, context: Context? = null) {
        val targetStatus = if (lecture.status == LectureStatus.ATTENDED) {
            LectureStatus.MISSED
        } else {
            LectureStatus.ATTENDED
        }
        setLectureStatus(lecture, targetStatus, context)
    }

    /**
     * Sets specific lecture status in Room and triggers notifications/widget updates as needed.
     */
    fun setLectureStatus(
        lecture: Lecture,
        status: LectureStatus,
        context: Context? = null,
        reason: String? = null
    ) {
        viewModelScope.launch {
            val missedReason = if (status == LectureStatus.MISSED) {
                reason ?: "Marked Missed"
            } else null

            repository.markLectureStatus(
                lectureId = lecture.id,
                status = status,
                reason = missedReason
            )

            context?.let { ctx ->
                if (status == LectureStatus.MISSED) {
                    NotificationHelper.sendMissedLectureAlert(
                        ctx,
                        lecture.copy(status = status, missedReason = missedReason)
                    )
                }
                PWDeskWidgetProvider.triggerUpdate(ctx)
            }
        }
    }

    fun addLecture(
        batchName: String = "Lakshya JEE",
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
}
