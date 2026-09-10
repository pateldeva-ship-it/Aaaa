package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.HomeworkDao
import com.example.data.dao.LectureDao
import com.example.data.model.HomeworkAssignment
import com.example.data.model.Lecture

@Database(
    entities = [Lecture::class, HomeworkAssignment::class],
    version = 1,
    exportSchema = false
)
abstract class PWDatabase : RoomDatabase() {
    abstract fun lectureDao(): LectureDao
    abstract fun homeworkDao(): HomeworkDao

    companion object {
        @Volatile
        private var INSTANCE: PWDatabase? = null

        fun getDatabase(context: Context): PWDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PWDatabase::class.java,
                    "pw_companion_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
