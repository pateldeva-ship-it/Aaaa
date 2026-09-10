package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.data.db.PWDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PWDeskWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    companion object {
        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_pw_desk)

            // Click listener to open main app
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)

            // Asynchronously fetch current stats from Room DB
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = PWDatabase.getDatabase(context)
                    val missedCount = db.lectureDao().getMissedLecturesCountSync()
                    val hwCount = db.homeworkDao().getPendingHomeworkCountSync()
                    val nextHw = db.homeworkDao().getNextPendingHomeworkSync()
                    val latestMissed = db.lectureDao().getLatestMissedLectureSync()

                    views.setTextViewText(R.id.widget_missed_count, "$missedCount Missed")
                    views.setTextViewText(R.id.widget_hw_count, "$hwCount Due")

                    val detailMsg = when {
                        missedCount > 0 && latestMissed != null -> {
                            "⚠️ Backlog: ${latestMissed.subject} (L-${latestMissed.lectureNumber})"
                        }
                        hwCount > 0 && nextHw != null -> {
                            "📝 Next Due: ${nextHw.title} (${nextHw.dueTime})"
                        }
                        else -> "🎉 All lectures attended & DPPs completed!"
                    }
                    views.setTextViewText(R.id.widget_detail_text, detailMsg)

                    appWidgetManager.updateAppWidget(appWidgetId, views)
                } catch (_: Exception) {
                    views.setTextViewText(R.id.widget_missed_count, "0 Missed")
                    views.setTextViewText(R.id.widget_hw_count, "0 Due")
                    appWidgetManager.updateAppWidget(appWidgetId, views)
                }
            }
        }

        fun triggerUpdate(context: Context) {
            val intent = Intent(context, PWDeskWidgetProvider::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            }
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val ids = appWidgetManager.getAppWidgetIds(
                ComponentName(context, PWDeskWidgetProvider::class.java)
            )
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
            context.sendBroadcast(intent)
        }
    }
}
