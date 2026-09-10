package com.example.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.data.model.HomeworkAssignment
import com.example.data.model.Lecture

object NotificationHelper {
    const val CHANNEL_ASSIGNMENTS = "pw_assignments_channel"
    const val CHANNEL_MISSED_LECTURES = "pw_missed_lectures_channel"
    const val CHANNEL_REMINDERS = "pw_reminders_channel"

    private const val NOTIFICATION_ID_ASSIGNMENT = 1001
    private const val NOTIFICATION_ID_MISSED = 2002
    private const val NOTIFICATION_ID_REMINDER = 3003

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val assignmentsChannel = NotificationChannel(
                CHANNEL_ASSIGNMENTS,
                "New PW Assignments & DPPs",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alerts when new assignments, DPPs, and problem sets are posted"
                enableVibration(true)
            }

            val missedLecturesChannel = NotificationChannel(
                CHANNEL_MISSED_LECTURES,
                "Missed Lecture & Backlog Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reminds you of classes you did not attend today and helps schedule make-up sessions"
                enableVibration(true)
            }

            val remindersChannel = NotificationChannel(
                CHANNEL_REMINDERS,
                "PW Homework Deadlines",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Reminders for upcoming homework and assignment submission deadlines"
            }

            notificationManager.createNotificationChannels(
                listOf(assignmentsChannel, missedLecturesChannel, remindersChannel)
            )
        }
    }

    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun sendNewAssignmentNotification(context: Context, assignment: HomeworkAssignment) {
        if (!hasNotificationPermission(context)) return

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("EXTRA_NAV_TARGET", "homework")
            putExtra("EXTRA_ITEM_ID", assignment.id)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            (System.currentTimeMillis() % 10000).toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ASSIGNMENTS)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("New PW Assignment Posted!")
            .setContentText("${assignment.subject}: ${assignment.title} (Due ${assignment.dueTime})")
            .setStyle(NotificationCompat.BigTextStyle().bigText(
                "New ${assignment.subject} assignment posted for ${assignment.batchName}: \"${assignment.title}\".\n" +
                "Total Questions: ${assignment.totalQuestions}. Due: ${assignment.dueTime}. Tap to review and attach your solution PDF."
            ))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_ASSIGNMENT, notification)
        } catch (_: SecurityException) {
            // Handled safely
        }
    }

    fun sendMissedLectureAlert(context: Context, lecture: Lecture) {
        if (!hasNotificationPermission(context)) return

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("EXTRA_NAV_TARGET", "missed_lectures")
            putExtra("EXTRA_ITEM_ID", lecture.id)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            (System.currentTimeMillis() % 10000).toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_MISSED_LECTURES)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("Missed Lecture Reminder!")
            .setContentText("Did not attend: ${lecture.subject} (L-${lecture.lectureNumber})")
            .setStyle(NotificationCompat.BigTextStyle().bigText(
                "You marked today's class as missed: ${lecture.subject} - ${lecture.chapterName} by ${lecture.facultyName}.\n" +
                "Don't let backlogs pile up! Tap to schedule your make-up lecture on your calendar."
            ))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_MISSED, notification)
        } catch (_: SecurityException) {
            // Handled safely
        }
    }
}
