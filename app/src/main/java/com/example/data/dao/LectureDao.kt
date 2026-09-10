package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Lecture
import com.example.data.model.LectureStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface LectureDao {
    @Query("SELECT * FROM lectures ORDER BY scheduledDate DESC, id DESC")
    fun getAllLectures(): Flow<List<Lecture>>

    @Query("SELECT * FROM lectures WHERE status = :status ORDER BY scheduledDate ASC")
    fun getLecturesByStatus(status: LectureStatus): Flow<List<Lecture>>

    @Query("SELECT * FROM lectures WHERE status = 'MISSED' ORDER BY scheduledDate DESC")
    fun getMissedLectures(): Flow<List<Lecture>>

    @Query("SELECT * FROM lectures WHERE scheduledDate >= :startOfDay AND scheduledDate <= :endOfDay ORDER BY scheduledDate ASC")
    fun getLecturesForDate(startOfDay: Long, endOfDay: Long): Flow<List<Lecture>>

    @Query("SELECT COUNT(*) FROM lectures WHERE status = 'MISSED'")
    fun getMissedLecturesCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM lectures WHERE status = 'MISSED'")
    suspend fun getMissedLecturesCountSync(): Int

    @Query("SELECT * FROM lectures WHERE status = 'MISSED' LIMIT 1")
    suspend fun getLatestMissedLectureSync(): Lecture?

    @Query("SELECT * FROM lectures WHERE id = :id")
    suspend fun getLectureById(id: Long): Lecture?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLecture(lecture: Lecture): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllLectures(lectures: List<Lecture>)

    @Update
    suspend fun updateLecture(lecture: Lecture)

    @Delete
    suspend fun deleteLecture(lecture: Lecture)

    @Query("DELETE FROM lectures WHERE id = :id")
    suspend fun deleteLectureById(id: Long)
}
