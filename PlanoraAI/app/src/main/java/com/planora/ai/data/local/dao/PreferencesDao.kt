package com.planora.ai.data.local.dao

import androidx.room.*
import com.planora.ai.data.local.entity.UserPreferencesEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PreferencesDao {
    @Query("SELECT * FROM user_preferences WHERE id = 1")
    fun getPreferences(): Flow<UserPreferencesEntity?>

    @Query("SELECT * FROM user_preferences WHERE id = 1")
    suspend fun getPreferencesOnce(): UserPreferencesEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(preferences: UserPreferencesEntity)

    @Update
    suspend fun update(preferences: UserPreferencesEntity)
}
