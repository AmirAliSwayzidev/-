package com.planora.ai.data.local.dao

import androidx.room.*
import com.planora.ai.data.local.entity.ProgramEntity
import com.planora.ai.data.local.entity.ProgramSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProgramDao {
    @Query("SELECT * FROM programs ORDER BY createdAt DESC")
    fun getAllPrograms(): Flow<List<ProgramEntity>>

    @Query("SELECT * FROM programs WHERE id = :id")
    suspend fun getProgram(id: Long): ProgramEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgram(p: ProgramEntity): Long

    @Update
    suspend fun updateProgram(p: ProgramEntity)

    @Query("DELETE FROM programs WHERE id = :id")
    suspend fun deleteProgram(id: Long)

    @Query("SELECT * FROM program_sessions WHERE programId = :programId ORDER BY dayIndex ASC")
    fun getSessions(programId: Long): Flow<List<ProgramSessionEntity>>

    @Query("SELECT * FROM program_sessions WHERE programId = :programId ORDER BY dayIndex ASC")
    suspend fun getSessionsOnce(programId: Long): List<ProgramSessionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSessions(sessions: List<ProgramSessionEntity>)

    @Update
    suspend fun updateSession(s: ProgramSessionEntity)

    @Query("UPDATE program_sessions SET done = :done, doneAt = :doneAt WHERE id = :id")
    suspend fun setSessionDone(id: Long, done: Boolean, doneAt: Long?)
}
