package com.planora.ai.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.planora.ai.data.local.converter.Converters
import com.planora.ai.data.local.dao.*
import com.planora.ai.data.local.entity.*

@Database(
    entities = [
        TaskEntity::class,
        GoalEntity::class,
        HabitEntity::class,
        HabitLogEntity::class,
        ChatMessageEntity::class,
        ConversationEntity::class,
        UserPreferencesEntity::class,
        NoteEntity::class,
        TagEntity::class,
        ProgramEntity::class,
        ProgramSessionEntity::class
    ],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class PlanoraDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun goalDao(): GoalDao
    abstract fun habitDao(): HabitDao
    abstract fun chatDao(): ChatDao
    abstract fun preferencesDao(): PreferencesDao
    abstract fun programDao(): ProgramDao

    companion object {
        @Volatile
        private var INSTANCE: PlanoraDatabase? = null

        fun getInstance(context: Context): PlanoraDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PlanoraDatabase::class.java,
                    "planora_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
