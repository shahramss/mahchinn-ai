package com.mahchin.app.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.mahchin.app.data.model.DailyTaskInstance
import com.mahchin.app.data.model.FinanceTask
import com.mahchin.app.data.model.MindMapNode
import com.mahchin.app.data.model.MonthlyTemplateTask
import com.mahchin.app.data.model.OneTimeTask
import com.mahchin.app.data.model.Project
import com.mahchin.app.data.model.UserSettings
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: Project): Long

    @Update
    suspend fun updateProject(project: Project)

    @Query("UPDATE projects SET priority = :priority, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateProjectPriority(id: Long, priority: String, updatedAt: Long = System.currentTimeMillis())

    @Query("SELECT * FROM projects WHERE isActive = 1 ORDER BY CASE priority WHEN 'URGENT' THEN 2 WHEN 'IMPORTANT' THEN 1 ELSE 0 END DESC, createdAt ASC")
    fun observeProjects(): Flow<List<Project>>

    @Query("SELECT * FROM projects WHERE isActive = 1 ORDER BY CASE priority WHEN 'URGENT' THEN 2 WHEN 'IMPORTANT' THEN 1 ELSE 0 END DESC, createdAt ASC")
    suspend fun getProjects(): List<Project>

    @Query("SELECT * FROM projects WHERE id = :id LIMIT 1")
    suspend fun getProject(id: Long): Project?

    @Query("SELECT * FROM projects ORDER BY id ASC")
    suspend fun getAllProjectsForBackup(): List<Project>

    @Query("DELETE FROM projects")
    suspend fun hardDeleteAllProjects()

    @Query("SELECT COUNT(*) FROM projects WHERE isActive = 1")
    suspend fun activeProjectCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMindMapNode(node: MindMapNode): Long

    @Update
    suspend fun updateMindMapNode(node: MindMapNode)

    @Query("SELECT * FROM mind_map_nodes WHERE isActive = 1 AND projectId = :projectId ORDER BY parentId ASC, orderIndex ASC, createdAt ASC")
    fun observeMindMapNodes(projectId: Long): Flow<List<MindMapNode>>

    @Query("SELECT * FROM mind_map_nodes WHERE isActive = 1 AND projectId = :projectId ORDER BY parentId ASC, orderIndex ASC, createdAt ASC")
    suspend fun getMindMapNodes(projectId: Long): List<MindMapNode>

    @Query("SELECT * FROM mind_map_nodes WHERE isActive = 1 ORDER BY projectId ASC, parentId ASC, orderIndex ASC, createdAt ASC")
    fun observeAllMindMapNodes(): Flow<List<MindMapNode>>

    @Query("SELECT * FROM mind_map_nodes WHERE isActive = 1 ORDER BY projectId ASC, parentId ASC, orderIndex ASC, createdAt ASC")
    suspend fun getAllMindMapNodes(): List<MindMapNode>

    @Query("SELECT * FROM mind_map_nodes WHERE id = :id LIMIT 1")
    suspend fun getMindMapNode(id: Long): MindMapNode?

    @Query("SELECT * FROM mind_map_nodes ORDER BY projectId ASC, parentId ASC, orderIndex ASC, createdAt ASC")
    suspend fun getAllMindMapNodesForBackup(): List<MindMapNode>

    @Query("DELETE FROM mind_map_nodes")
    suspend fun hardDeleteAllMindMapNodes()

    @Query("UPDATE mind_map_nodes SET x = :x, y = :y, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateMindMapNodePosition(id: Long, x: Float, y: Float, updatedAt: Long = System.currentTimeMillis())

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplate(task: MonthlyTemplateTask): Long

    @Update
    suspend fun updateTemplate(task: MonthlyTemplateTask)

    @Delete
    suspend fun deleteTemplate(task: MonthlyTemplateTask)

    @Query("SELECT * FROM monthly_template_tasks WHERE isActive = 1 ORDER BY dayOfMonth ASC, priority DESC, createdAt ASC")
    fun observeTemplates(): Flow<List<MonthlyTemplateTask>>

    @Query("SELECT * FROM monthly_template_tasks WHERE isActive = 1 ORDER BY dayOfMonth ASC, priority DESC, createdAt ASC")
    suspend fun getActiveTemplates(): List<MonthlyTemplateTask>

    @Query("SELECT * FROM monthly_template_tasks WHERE id = :id LIMIT 1")
    suspend fun getTemplate(id: Long): MonthlyTemplateTask?

    @Query("SELECT * FROM monthly_template_tasks ORDER BY id ASC")
    suspend fun getAllTemplatesForBackup(): List<MonthlyTemplateTask>

    @Query("DELETE FROM monthly_template_tasks")
    suspend fun hardDeleteAllTemplates()

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertDailyInstance(task: DailyTaskInstance): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDailyInstanceForRestore(task: DailyTaskInstance): Long

    @Update
    suspend fun updateDailyInstance(task: DailyTaskInstance)

    @Delete
    suspend fun deleteDailyInstance(task: DailyTaskInstance)

    @Query("SELECT * FROM daily_task_instances WHERE jalaliYear = :year AND jalaliMonth = :month AND jalaliDay = :day ORDER BY priority DESC, createdAt ASC")
    fun observeDailyInstances(year: Int, month: Int, day: Int): Flow<List<DailyTaskInstance>>

    @Query("SELECT * FROM daily_task_instances WHERE jalaliYear = :year AND jalaliMonth = :month AND jalaliDay = :day ORDER BY priority DESC, createdAt ASC")
    suspend fun getDailyInstances(year: Int, month: Int, day: Int): List<DailyTaskInstance>

    @Query("SELECT * FROM daily_task_instances WHERE id = :id LIMIT 1")
    suspend fun getDailyInstance(id: Long): DailyTaskInstance?

    @Query("SELECT * FROM daily_task_instances ORDER BY id ASC")
    suspend fun getAllDailyInstancesForBackup(): List<DailyTaskInstance>

    @Query("DELETE FROM daily_task_instances")
    suspend fun hardDeleteAllDailyInstances()

    @Query("SELECT * FROM daily_task_instances WHERE sourceTemplateId = :sourceTemplateId AND jalaliYear = :year AND jalaliMonth = :month AND jalaliDay = :day LIMIT 1")
    suspend fun getDailyInstanceBySource(sourceTemplateId: Long, year: Int, month: Int, day: Int): DailyTaskInstance?

    @Query("SELECT * FROM daily_task_instances WHERE alarmAtMillis IS NOT NULL AND alarmAtMillis >= :now")
    suspend fun getFutureDailyTasksWithAlarms(now: Long): List<DailyTaskInstance>

    @Query("SELECT * FROM daily_task_instances WHERE jalaliYear = :year AND jalaliMonth = :month ORDER BY jalaliDay ASC")
    fun observeDailyInstancesForMonth(year: Int, month: Int): Flow<List<DailyTaskInstance>>

    @Query("SELECT * FROM daily_task_instances WHERE jalaliYear = :year AND jalaliMonth = :month ORDER BY jalaliDay ASC")
    suspend fun getDailyInstancesForMonth(year: Int, month: Int): List<DailyTaskInstance>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOneTimeTask(task: OneTimeTask): Long

    @Update
    suspend fun updateOneTimeTask(task: OneTimeTask)

    @Delete
    suspend fun deleteOneTimeTask(task: OneTimeTask)

    @Query("SELECT * FROM one_time_tasks WHERE jalaliYear = :year AND jalaliMonth = :month AND jalaliDay = :day ORDER BY priority DESC, createdAt ASC")
    fun observeOneTimeTasks(year: Int, month: Int, day: Int): Flow<List<OneTimeTask>>

    @Query("SELECT * FROM one_time_tasks WHERE jalaliYear = :year AND jalaliMonth = :month AND jalaliDay = :day ORDER BY priority DESC, createdAt ASC")
    suspend fun getOneTimeTasks(year: Int, month: Int, day: Int): List<OneTimeTask>

    @Query("SELECT * FROM one_time_tasks WHERE id = :id LIMIT 1")
    suspend fun getOneTimeTask(id: Long): OneTimeTask?

    @Query("SELECT * FROM one_time_tasks ORDER BY id ASC")
    suspend fun getAllOneTimeTasksForBackup(): List<OneTimeTask>

    @Query("DELETE FROM one_time_tasks")
    suspend fun hardDeleteAllOneTimeTasks()

    @Query("SELECT * FROM one_time_tasks WHERE alarmAtMillis IS NOT NULL AND alarmAtMillis >= :now")
    suspend fun getFutureOneTimeTasksWithAlarms(now: Long): List<OneTimeTask>

    @Query("SELECT * FROM one_time_tasks WHERE jalaliYear = :year AND jalaliMonth = :month ORDER BY jalaliDay ASC")
    fun observeOneTimeTasksForMonth(year: Int, month: Int): Flow<List<OneTimeTask>>

    @Query("SELECT * FROM one_time_tasks WHERE jalaliYear = :year AND jalaliMonth = :month ORDER BY jalaliDay ASC")
    suspend fun getOneTimeTasksForMonth(year: Int, month: Int): List<OneTimeTask>


    @Query("DELETE FROM daily_task_instances WHERE jalaliYear = :year AND jalaliMonth = :month AND jalaliDay = :day")
    suspend fun deleteDailyInstancesForDate(year: Int, month: Int, day: Int)

    @Query("DELETE FROM daily_task_instances WHERE sourceTemplateId IS NULL AND jalaliYear = :year AND jalaliMonth = :month AND jalaliDay = :day")
    suspend fun deleteNonTemplateDailyInstancesForDate(year: Int, month: Int, day: Int)

    @Query("DELETE FROM one_time_tasks WHERE jalaliYear = :year AND jalaliMonth = :month AND jalaliDay = :day")
    suspend fun deleteOneTimeTasksForDate(year: Int, month: Int, day: Int)

    @Query("DELETE FROM daily_task_instances")
    suspend fun deleteAllDailyInstances()

    @Query("DELETE FROM one_time_tasks")
    suspend fun deleteAllOneTimeTasks()

    @Query("UPDATE monthly_template_tasks SET isActive = 0, updatedAt = :updatedAt WHERE isActive = 1")
    suspend fun deactivateAllTemplates(updatedAt: Long = System.currentTimeMillis())

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFinanceTask(task: FinanceTask): Long

    @Update
    suspend fun updateFinanceTask(task: FinanceTask)

    @Delete
    suspend fun deleteFinanceTask(task: FinanceTask)

    @Query("SELECT * FROM finance_tasks WHERE isActive = 1 ORDER BY jalaliYear DESC, jalaliMonth DESC, createdAt ASC")
    fun observeFinanceTasks(): Flow<List<FinanceTask>>

    @Query("SELECT * FROM finance_tasks WHERE isActive = 1 AND projectId = :projectId ORDER BY jalaliYear DESC, jalaliMonth DESC, createdAt ASC")
    fun observeFinanceTasksForProject(projectId: Long): Flow<List<FinanceTask>>

    @Query("SELECT * FROM finance_tasks WHERE isActive = 1 AND projectId = :projectId AND jalaliYear = :year AND jalaliMonth = :month ORDER BY isDone ASC, createdAt ASC")
    suspend fun getFinanceTasks(projectId: Long, year: Int, month: Int): List<FinanceTask>

    @Query("SELECT * FROM finance_tasks WHERE id = :id LIMIT 1")
    suspend fun getFinanceTask(id: Long): FinanceTask?

    @Query("SELECT * FROM finance_tasks ORDER BY id ASC")
    suspend fun getAllFinanceTasksForBackup(): List<FinanceTask>

    @Query("DELETE FROM finance_tasks")
    suspend fun hardDeleteAllFinanceTasks()

    @Query("UPDATE finance_tasks SET isActive = 0, updatedAt = :updatedAt")
    suspend fun deactivateAllFinanceTasks(updatedAt: Long = System.currentTimeMillis())

    @Query("SELECT * FROM user_settings WHERE id = 1 LIMIT 1")
    fun observeSettings(): Flow<UserSettings?>

    @Query("SELECT * FROM user_settings WHERE id = 1 LIMIT 1")
    suspend fun getSettings(): UserSettings?

    @Query("DELETE FROM user_settings")
    suspend fun hardDeleteSettings()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSettings(settings: UserSettings)
}
