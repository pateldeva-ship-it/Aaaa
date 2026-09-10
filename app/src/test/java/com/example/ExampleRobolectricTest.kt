package com.example

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.Lecture
import com.example.data.model.LectureStatus
import com.example.ui.LecturesViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("PW Companion", appName)
  }

  @Test
  fun `test lecture status toggle and room persistence`() = runBlocking {
    val application = ApplicationProvider.getApplicationContext<Application>()
    val viewModel = LecturesViewModel(application)

    // Verify initial lectures exist
    val lectures = viewModel.allLectures.first { it.isNotEmpty() }
    assertNotNull(lectures)
    val testLecture = lectures.first()

    // Test helper properties
    val initialStatus = testLecture.status
    if (initialStatus == LectureStatus.ATTENDED) {
      assertTrue(testLecture.isAttended)
    }

    // Toggle status
    viewModel.toggleLectureAttendance(testLecture, application)

    // Fetch updated lectures from Flow
    val updatedLectures = viewModel.allLectures.first { list ->
      list.any { it.id == testLecture.id && it.status != initialStatus }
    }
    val updatedLecture = updatedLectures.first { it.id == testLecture.id }

    val expectedStatus = if (initialStatus == LectureStatus.ATTENDED) LectureStatus.MISSED else LectureStatus.ATTENDED
    assertEquals(expectedStatus, updatedLecture.status)
  }
}

