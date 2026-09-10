package com.example

import android.app.Application
import com.example.data.db.PWDatabase
import com.example.data.repository.PWRepository
import com.example.util.NotificationHelper
import com.example.widget.PWDeskWidgetProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PWApplication : Application() {

    lateinit var database: PWDatabase
        private set

    lateinit var repository: PWRepository
        private set

    override fun onCreate() {
        super.onCreate()
        database = PWDatabase.getDatabase(this)
        repository = PWRepository(database.lectureDao(), database.homeworkDao())

        // Initialize Notification Channels
        NotificationHelper.createNotificationChannels(this)

        // Seed initial sample data & update widget
        CoroutineScope(Dispatchers.IO).launch {
            repository.seedSamplePWDataIfEmpty()
            PWDeskWidgetProvider.triggerUpdate(this@PWApplication)
        }
    }
}
