package com.planora.ai.data.local.dao

import androidx.room.*
import com.planora.ai.data.local.entity.TaskEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks ORDER BY dueDate ASC, priority DESC")
    fun getAllTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE status != 2 ORDER BY dueDate ASC, priority DESC")
    fun getActiveTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE date(dueDate) = date(:date) ORDER BY scheduledStart ASC, priority DESC")
    fun getTasksForDate(date: LocalDateTime): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE dueDate BETWEEN :start AND :end ORDER BY dueDate ASC")
    fun getTasksBetween(start: LocalDateTime, end: LocalDateTime): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getTaskById(id: Long): TaskEntity?

    @Query("SELECT * FROM tasks WHERE title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%'")
    fun searchTasks(query: String): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE status = 0 AND dueDate < :now")
    fun getOverdueTasks(now: LocalDateTime): Flow<List<TaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: TaskEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tasks: List<TaskEntity>)

    @Update
    suspend fun update(task: TaskEntity)

    @Delete
    suspend fun delete(task: TaskEntity)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE tasks SET status = 2, completedAt = :completedAt, updatedAt = :completedAt WHERE id = :id")
    suspend fun markCompleted(id: Long, completedAt: LocalDateTime)

    @Query("SELECT COUNT(*) FROM tasks WHERE status = 2 AND date(completedAt) = date(:date)")
    suspend fun countCompletedOn(date: LocalDateTime): Int

    @Query("SELECT COUNT(*) FROM tasks WHERE status != 2")
    suspend fun countPending(): Int
}
