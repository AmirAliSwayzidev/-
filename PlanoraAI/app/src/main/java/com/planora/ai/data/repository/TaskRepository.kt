package com.planora.ai.data.repository

import com.planora.ai.data.local.dao.TaskDao
import com.planora.ai.data.local.entity.TaskEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

class TaskRepository(
    private val taskDao: TaskDao
) {
    fun getAllTasks(): Flow<List<TaskEntity>> = taskDao.getAllTasks()
    fun getActiveTasks(): Flow<List<TaskEntity>> = taskDao.getActiveTasks()
    fun getTasksForDate(date: LocalDateTime): Flow<List<TaskEntity>> = taskDao.getTasksForDate(date)
    fun getTasksBetween(start: LocalDateTime, end: LocalDateTime) = taskDao.getTasksBetween(start, end)
    fun searchTasks(query: String) = taskDao.searchTasks(query)
    fun getOverdueTasks(now: LocalDateTime = LocalDateTime.now()) = taskDao.getOverdueTasks(now)

    suspend fun getTaskById(id: Long) = taskDao.getTaskById(id)

    suspend fun insert(task: TaskEntity): Long = taskDao.insert(task)
    suspend fun insertAll(tasks: List<TaskEntity>) = taskDao.insertAll(tasks)
    suspend fun update(task: TaskEntity) = taskDao.update(task)
    suspend fun delete(task: TaskEntity) = taskDao.delete(task)
    suspend fun deleteById(id: Long) = taskDao.deleteById(id)
    suspend fun markCompleted(id: Long) = taskDao.markCompleted(id, LocalDateTime.now())
    suspend fun countCompletedOn(date: LocalDateTime) = taskDao.countCompletedOn(date)
    suspend fun countPending() = taskDao.countPending()
}
