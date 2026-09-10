package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.HomeworkAssignment
import com.example.data.model.HomeworkStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface HomeworkDao {
    @Query("SELECT * FROM homework_assignments ORDER BY dueDate ASC, priority ASC")
    fun getAllHomework(): Flow<List<HomeworkAssignment>>

    @Query("SELECT * FROM homework_assignments WHERE status != 'COMPLETED' ORDER BY dueDate ASC")
    fun getPendingHomework(): Flow<List<HomeworkAssignment>>

    @Query("SELECT COUNT(*) FROM homework_assignments WHERE status != 'COMPLETED'")
    fun getPendingHomeworkCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM homework_assignments WHERE status != 'COMPLETED'")
    suspend fun getPendingHomeworkCountSync(): Int

    @Query("SELECT * FROM homework_assignments WHERE status != 'COMPLETED' ORDER BY dueDate ASC LIMIT 1")
    suspend fun getNextPendingHomeworkSync(): HomeworkAssignment?

    @Query("SELECT * FROM homework_assignments WHERE id = :id")
    suspend fun getHomeworkById(id: Long): HomeworkAssignment?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHomework(homework: HomeworkAssignment): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllHomework(assignments: List<HomeworkAssignment>)

    @Update
    suspend fun updateHomework(homework: HomeworkAssignment)

    @Delete
    suspend fun deleteHomework(homework: HomeworkAssignment)

    @Query("DELETE FROM homework_assignments WHERE id = :id")
    suspend fun deleteHomeworkById(id: Long)
}
