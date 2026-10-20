package com.planora.ai.data.local.dao

import androidx.room.*
import com.planora.ai.data.local.entity.GoalEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GoalDao {
    @Query("SELECT * FROM goals WHERE status = 0 ORDER BY targetDate ASC")
    fun getActiveGoals(): Flow<List<GoalEntity>>

    @Query("SELECT * FROM goals ORDER BY updatedAt DESC")
    fun getAllGoals(): Flow<List<GoalEntity>>

    @Query("SELECT * FROM goals WHERE id = :id")
    suspend fun getGoalById(id: Long): GoalEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(goal: GoalEntity): Long

    @Update
    suspend fun update(goal: GoalEntity)

    @Delete
    suspend fun delete(goal: GoalEntity)

    @Query("UPDATE goals SET progress = :progress, updatedAt = CURRENT_TIMESTAMP WHERE id = :id")
    suspend fun updateProgress(id: Long, progress: Float)
}
