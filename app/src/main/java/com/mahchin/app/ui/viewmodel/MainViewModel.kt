package com.mahchin.app.ui.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import android.util.Base64
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mahchin.app.data.model.FinanceTask
import com.mahchin.app.data.model.MindMapNode
import com.mahchin.app.data.model.MonthlyTemplateTask
import com.mahchin.app.data.model.Project
import com.mahchin.app.data.model.TaskItem
import com.mahchin.app.data.model.TaskPriority
import com.mahchin.app.data.model.TaskStatus
import com.mahchin.app.data.model.UserSettings
import com.mahchin.app.data.repository.MonthlyReport
import com.mahchin.app.data.repository.TaskRepository
import com.mahchin.app.domain.JalaliCalendar
import com.mahchin.app.domain.JalaliDate
import com.mahchin.app.notification.NotificationHelper
import com.mahchin.app.notification.ReminderScheduler
import com.mahchin.app.notification.TaskAlarmScheduler
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.mahchin.app.data.repository.ImportedMindMapBranch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URL

class MainViewModel(
    private val app: Application,
    private val repository: TaskRepository
) : AndroidViewModel(app) {

    val today: JalaliDate = JalaliCalendar.today()

    private val _selectedDate = MutableStateFlow(today)
    val selectedDate: StateFlow<JalaliDate> = _selectedDate

    private val _calendarMonth = MutableStateFlow(JalaliDate(today.year, today.month, 1))
    val calendarMonth: StateFlow<JalaliDate> = _calendarMonth

    private val _settingsMessage = MutableStateFlow<String?>(null)
    val settingsMessage: StateFlow<String?> = _settingsMessage

    private val _selectedProjectId = MutableStateFlow<Long?>(null)
    val selectedProjectId: StateFlow<Long?> = _selectedProjectId

    private val _mindMapAiBusy = MutableStateFlow(false)
    val mindMapAiBusy: StateFlow<Boolean> = _mindMapAiBusy

    val settings: StateFlow<UserSettings> = repository.settingsFlow.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        UserSettings()
    )

    val templates: StateFlow<List<MonthlyTemplateTask>> = repository.templatesFlow.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList()
    )

    val projects: StateFlow<List<Project>> = repository.projectsFlow.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList()
    )

    val allMindMapNodes: StateFlow<List<MindMapNode>> = repository.allMindMapNodesFlow.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList()
    )

    val financeTasks: StateFlow<List<FinanceTask>> = repository.financeTasksFlow.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList()
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val mindMapNodes: StateFlow<List<MindMapNode>> = selectedProjectId.flatMapLatest { id ->
        if (id == null) kotlinx.coroutines.flow.flowOf(emptyList()) else repository.observeMindMapNodes(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val todayTasks: StateFlow<List<TaskItem>> = MutableStateFlow(today).flatMapLatest {
        repository.observeTasksForDate(today)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val selectedDateTasks: StateFlow<List<TaskItem>> = selectedDate.flatMapLatest { date ->
        repository.observeTasksForDate(date)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val monthCounts: StateFlow<Map<Int, Int>> = calendarMonth.flatMapLatest { month ->
        repository.observeMonthCounts(month.year, month.month)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    @OptIn(ExperimentalCoroutinesApi::class)
    val report: StateFlow<MonthlyReport> = calendarMonth.flatMapLatest { month ->
        repository.observeReport(month.year, month.month)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MonthlyReport(0, 0, 0, 0, 0))

    init {
        viewModelScope.launch {
            repository.ensureDefaultSettings()
            val firstProject = repository.ensureDefaultProject()
            if (_selectedProjectId.value == null) _selectedProjectId.value = firstProject
            repository.ensureTasksForDate(today)
            repository.ensureTasksForMonth(today.year, today.month)
            ReminderScheduler.schedulePeriodic(app, repository.getSettingsOrDefault())
            rescheduleTaskAlarms()
        }
    }

    fun selectDate(date: JalaliDate) {
        _selectedDate.value = date
        viewModelScope.launch { repository.ensureTasksForDate(date) }
    }

    fun selectProject(projectId: Long) {
        _selectedProjectId.value = projectId
    }

    fun addProject(name: String) {
        viewModelScope.launch {
            val id = repository.addProject(name)
            _selectedProjectId.value = id
        }
    }

    fun updateProject(projectId: Long, name: String) {
        if (name.isBlank()) return
        viewModelScope.launch { repository.updateProject(projectId, name) }
    }

    fun updateProjectPriority(projectId: Long, priority: TaskPriority) {
        viewModelScope.launch { repository.updateProjectPriority(projectId, priority) }
    }

    fun deleteProject(projectId: Long) {
        viewModelScope.launch {
            val replacementId = repository.deleteProject(projectId)
            if (_selectedProjectId.value == projectId && replacementId != null) {
                _selectedProjectId.value = replacementId
            }
        }
    }

    fun addMindMapNode(parentId: Long?, title: String, description: String = "") {
        val projectId = _selectedProjectId.value ?: return
        if (title.isBlank()) return
        viewModelScope.launch { repository.addMindMapNode(projectId, parentId, title, description) }
    }

    fun updateMindMapNode(id: Long, title: String, description: String = "") {
        if (title.isBlank()) return
        viewModelScope.launch { repository.updateMindMapNode(id, title, description) }
    }

    fun deleteMindMapNode(id: Long) {
        viewModelScope.launch { repository.deleteMindMapNode(id) }
    }

    fun moveMindMapNode(id: Long, x: Float, y: Float) {
        viewModelScope.launch { repository.updateMindMapNodePosition(id, x, y) }
    }

    fun makeTasksFromMindMap(startDate: JalaliDate, tasksPerDay: Int) {
        val projectId = _selectedProjectId.value ?: return
        viewModelScope.launch {
            val count = repository.createTasksFromMindMap(projectId, startDate, tasksPerDay)
            repository.ensureTasksForMonth(startDate.year, startDate.month)
            _settingsMessage.value = "${count} تسک از مایندمپ ساخته شد."
            ReminderScheduler.scheduleImmediateCheck(app)
        }
    }

    fun nextMonth() {
        val next = JalaliCalendar.nextMonth(_calendarMonth.value)
        _calendarMonth.value = next
        viewModelScope.launch { repository.ensureTasksForMonth(next.year, next.month) }
    }

    fun previousMonth() {
        val prev = JalaliCalendar.previousMonth(_calendarMonth.value)
        _calendarMonth.value = prev
        viewModelScope.launch { repository.ensureTasksForMonth(prev.year, prev.month) }
    }

    fun addTodayTask(title: String, description: String, priority: TaskPriority, projectId: Long? = null) = addOneTimeTask(today, title, description, priority, projectId)

    fun addOneTimeTask(date: JalaliDate, title: String, description: String, priority: TaskPriority, projectId: Long? = null) {
        if (title.isBlank()) return
        viewModelScope.launch {
            repository.addOneTimeTask(date, title, description, priority, projectId = projectId ?: _selectedProjectId.value)
            ReminderScheduler.scheduleImmediateCheck(app)
            rescheduleTaskAlarms()
        }
    }

    fun addTemplateTask(title: String, description: String, dayOfMonth: Int, priority: TaskPriority, projectId: Long? = null) {
        if (title.isBlank()) return
        viewModelScope.launch {
            repository.addTemplateTask(title, description, dayOfMonth, priority, projectId = projectId ?: _selectedProjectId.value)
            repository.ensureTasksForDate(today)
            ReminderScheduler.scheduleImmediateCheck(app)
            rescheduleTaskAlarms()
        }
    }

    fun updateTemplate(id: Long, title: String, description: String, dayOfMonth: Int, priority: TaskPriority, projectId: Long? = null) {
        if (title.isBlank()) return
        viewModelScope.launch {
            repository.updateTemplateTask(id, title, description, dayOfMonth, priority, projectId)
            rescheduleTaskAlarms()
        }
    }

    fun setTemplateAlarm(templateId: Long, hour: Int?, minute: Int?) {
        viewModelScope.launch {
            repository.setTemplateAlarm(templateId, hour, minute)
            repository.ensureTasksForMonth(today.year, today.month)
            rescheduleTaskAlarms()
        }
    }

    fun deleteTemplate(id: Long) {
        viewModelScope.launch { repository.deleteTemplateTask(id) }
    }

    fun complete(item: TaskItem) = setStatus(item, TaskStatus.DONE)
    fun toggleDone(item: TaskItem) = setStatus(
        item,
        if (item.status == TaskStatus.DONE) TaskStatus.NOT_STARTED else TaskStatus.DONE
    )
    fun resetStatus(item: TaskItem) = setStatus(item, TaskStatus.NOT_STARTED)
    fun inProgress(item: TaskItem) = setStatus(item, TaskStatus.IN_PROGRESS)
    fun cancelToday(item: TaskItem) = setStatus(item, TaskStatus.CANCELED)

    fun toggleTemplateDone(template: MonthlyTemplateTask) {
        val next = if (template.status == TaskStatus.DONE) TaskStatus.NOT_STARTED else TaskStatus.DONE
        viewModelScope.launch { repository.setTemplateStatus(template.id, next) }
    }

    fun setStatus(item: TaskItem, status: TaskStatus) {
        viewModelScope.launch {
            repository.setStatus(item, status)
            if (status.isClosed()) TaskAlarmScheduler.cancel(app, item)
            ReminderScheduler.scheduleImmediateCheck(app)
        }
    }

    fun setTaskGroupStatus(items: List<TaskItem>, status: TaskStatus) {
        if (items.isEmpty()) return
        viewModelScope.launch {
            items.forEach { item ->
                repository.setStatus(item, status)
                if (status.isClosed()) TaskAlarmScheduler.cancel(app, item)
            }
            ReminderScheduler.scheduleImmediateCheck(app)
        }
    }


    fun deleteTaskGroup(items: List<TaskItem>) {
        if (items.isEmpty()) return
        viewModelScope.launch {
            items.forEach { item ->
                TaskAlarmScheduler.cancel(app, item)
                repository.deleteTask(item)
            }
            ReminderScheduler.scheduleImmediateCheck(app)
        }
    }

    fun moveTaskGroupToTomorrow(items: List<TaskItem>) {
        if (items.isEmpty()) return
        viewModelScope.launch {
            items.forEach { repository.moveTaskToTomorrow(it) }
            ReminderScheduler.scheduleImmediateCheck(app)
        }
    }

    fun moveTaskGroupToCustomDate(items: List<TaskItem>, date: JalaliDate) {
        if (items.isEmpty()) return
        viewModelScope.launch {
            items.forEach { repository.moveTaskToDate(it, date) }
            ReminderScheduler.scheduleImmediateCheck(app)
        }
    }

    fun setTaskGroupAlarm(items: List<TaskItem>, date: JalaliDate, hour: Int, minute: Int) {
        if (items.isEmpty()) return
        viewModelScope.launch {
            val millis = repository.toEpochMillis(date, hour, minute)
            items.forEach { repository.setTaskAlarm(it, millis) }
            rescheduleTaskAlarms()
            _settingsMessage.value = "آلارم گروهی تنظیم شد."
        }
    }

    fun setTaskAlarm(item: TaskItem, date: JalaliDate, hour: Int, minute: Int) {
        viewModelScope.launch {
            val millis = repository.toEpochMillis(date, hour, minute)
            repository.setTaskAlarm(item, millis)
            rescheduleTaskAlarms()
            _settingsMessage.value = "آلارم تسک تنظیم شد."
        }
    }

    fun clearTaskAlarm(item: TaskItem) {
        viewModelScope.launch {
            TaskAlarmScheduler.cancel(app, item)
            repository.setTaskAlarm(item, null)
            _settingsMessage.value = "آلارم تسک حذف شد."
        }
    }

    fun editOnlyThisDate(item: TaskItem, title: String, description: String, priority: TaskPriority, projectId: Long? = null) {
        if (title.isBlank()) return
        viewModelScope.launch {
            repository.editTaskOnlyThisDate(item, title, description, priority, projectId)
            ReminderScheduler.scheduleImmediateCheck(app)
            rescheduleTaskAlarms()
        }
    }

    fun deleteTask(item: TaskItem) {
        viewModelScope.launch {
            TaskAlarmScheduler.cancel(app, item)
            repository.deleteTask(item)
            ReminderScheduler.scheduleImmediateCheck(app)
        }
    }

    fun moveToTomorrow(item: TaskItem) {
        viewModelScope.launch {
            repository.moveTaskToTomorrow(item)
            ReminderScheduler.scheduleImmediateCheck(app)
        }
    }

    fun moveToCustomDate(item: TaskItem, date: JalaliDate) {
        viewModelScope.launch {
            repository.moveTaskToDate(item, date)
            ReminderScheduler.scheduleImmediateCheck(app)
        }
    }

    fun moveAllRemainingTodayToTomorrow() {
        viewModelScope.launch {
            repository.moveRemainingToTomorrow(today)
            ReminderScheduler.scheduleImmediateCheck(app)
        }
    }


    fun clearTodayTasks() {
        viewModelScope.launch {
            repository.clearTasksForDate(today)
            ReminderScheduler.scheduleImmediateCheck(app)
            _settingsMessage.value = "تسک‌های امروز پاک شد."
        }
    }

    fun clearNonTemplateTasksForDate(date: JalaliDate) {
        viewModelScope.launch {
            repository.clearNonTemplateTasksForDate(date)
            ReminderScheduler.scheduleImmediateCheck(app)
            _settingsMessage.value = "تسک‌های غیرتکراری این روز پاک شد."
        }
    }

    fun clearAllTasks() {
        viewModelScope.launch {
            repository.clearAllTasks()
            ReminderScheduler.scheduleImmediateCheck(app)
            _settingsMessage.value = "همه تسک‌ها پاک شد."
        }
    }

    fun clearAllTemplateTasks() {
        viewModelScope.launch {
            repository.clearAllTemplates()
            repository.clearTasksForDate(today)
            ReminderScheduler.scheduleImmediateCheck(app)
            _settingsMessage.value = "همه تسک‌های قالب پاک شد."
        }
    }

    fun saveReminderSettings(startHour: Int, endHour: Int) {
        val start = startHour.coerceIn(0, 23)
        val end = endHour.coerceIn(1, 24).coerceAtLeast((start + 1).coerceAtMost(24))
        updateSettings { it.copy(startHour = start, endHour = end) }
        _settingsMessage.value = "تنظیمات یادآوری ذخیره شد و زمان‌بندی دوباره فعال شد."
    }

    fun sendTestNotification() {
        val ok = NotificationHelper.showTestNotification(app)
        _settingsMessage.value = if (ok) {
            "نوتیفیکیشن آزمایشی ارسال شد."
        } else {
            "مجوز نوتیفیکیشن فعال نیست. از تنظیمات گوشی اجازه Notification را بده."
        }
    }


    fun exportFullBackupToUri(context: Context, uri: Uri?) {
        if (uri == null) return
        viewModelScope.launch {
            runCatching {
                val json = repository.exportFullBackupJson()
                context.contentResolver.openOutputStream(uri)?.use { out ->
                    out.write(json.toByteArray(Charsets.UTF_8))
                } ?: error("فایل باز نشد")
            }.onSuccess {
                _settingsMessage.value = "بکاپ کامل ذخیره شد."
            }.onFailure {
                _settingsMessage.value = "خطا در ذخیره بکاپ: ${it.message ?: "نامشخص"}"
            }
        }
    }

    fun restoreFullBackupFromUri(context: Context, uri: Uri?) {
        if (uri == null) return
        viewModelScope.launch {
            runCatching {
                val json = context.contentResolver.openInputStream(uri)?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }
                    ?: error("فایل خوانده نشد")
                repository.restoreFullBackupJson(json)
                val firstProject = repository.ensureDefaultProject()
                _selectedProjectId.value = firstProject
                rescheduleTaskAlarms()
            }.onSuccess {
                _settingsMessage.value = "بکاپ کامل بازگردانی شد."
            }.onFailure {
                _settingsMessage.value = "خطا در بازگردانی بکاپ: ${it.message ?: "نامشخص"}"
            }
        }
    }

    fun exportMindMapBackupToUri(context: Context, uri: Uri?, projectId: Long) {
        if (uri == null) return
        viewModelScope.launch {
            runCatching {
                val json = repository.exportMindMapBackupJson(projectId)
                context.contentResolver.openOutputStream(uri)?.use { out ->
                    out.write(json.toByteArray(Charsets.UTF_8))
                } ?: error("فایل باز نشد")
            }.onSuccess {
                _settingsMessage.value = "بکاپ مایندمپ ذخیره شد."
            }.onFailure {
                _settingsMessage.value = "خطا در بکاپ مایندمپ: ${it.message ?: "نامشخص"}"
            }
        }
    }

    fun restoreMindMapBackupFromUri(context: Context, uri: Uri?) {
        if (uri == null) return
        viewModelScope.launch {
            runCatching {
                val json = context.contentResolver.openInputStream(uri)?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }
                    ?: error("فایل خوانده نشد")
                val newProjectId = repository.restoreMindMapBackupJson(json)
                _selectedProjectId.value = newProjectId
            }.onSuccess {
                _settingsMessage.value = "مایندمپ بازگردانی شد."
            }.onFailure {
                _settingsMessage.value = "خطا در بازگردانی مایندمپ: ${it.message ?: "نامشخص"}"
            }
        }
    }

    fun addFinanceTask(projectId: Long, month: JalaliDate, title: String, amount: Long, customerDateKey: String? = null) {
        if (title.isBlank()) return
        viewModelScope.launch {
            repository.addFinanceTask(projectId, month, title, amount, customerDateKey ?: month.key)
            _settingsMessage.value = "تسک مالی اضافه شد."
        }
    }

    fun toggleFinanceDone(taskId: Long, done: Boolean) {
        viewModelScope.launch { repository.toggleFinanceDone(taskId, done, today) }
    }

    fun deleteFinanceTask(taskId: Long) {
        viewModelScope.launch { repository.deleteFinanceTask(taskId) }
    }

    fun carryOpenFinanceToNextMonth(projectId: Long, month: JalaliDate) {
        viewModelScope.launch {
            val count = repository.carryOpenFinanceTasksToNextMonth(projectId, month)
            _settingsMessage.value = "${count} هزینه باز به ماه بعد منتقل شد."
        }
    }

    fun clearAllFinanceTasks() {
        viewModelScope.launch {
            repository.clearAllFinanceTasks()
            _settingsMessage.value = "همه فاکتورهای مالی پاک شد."
        }
    }

    fun exportFinanceBackupToUri(context: Context, uri: Uri?) {
        if (uri == null) return
        viewModelScope.launch {
            runCatching {
                val json = repository.exportFinanceBackupJson()
                context.contentResolver.openOutputStream(uri)?.use { out -> out.write(json.toByteArray(Charsets.UTF_8)) }
                    ?: error("فایل باز نشد")
            }.onSuccess {
                _settingsMessage.value = "بکاپ مالی ذخیره شد."
            }.onFailure {
                _settingsMessage.value = "خطا در بکاپ مالی: ${it.message ?: "نامشخص"}"
            }
        }
    }

    fun restoreFinanceBackupFromUri(context: Context, uri: Uri?) {
        if (uri == null) return
        viewModelScope.launch {
            runCatching {
                val json = context.contentResolver.openInputStream(uri)?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }
                    ?: error("فایل خوانده نشد")
                repository.restoreFinanceBackupJson(json)
            }.onSuccess {
                _settingsMessage.value = "بکاپ مالی بازگردانی شد."
            }.onFailure {
                _settingsMessage.value = "خطا در بازگردانی مالی: ${it.message ?: "نامشخص"}"
            }
        }
    }

    fun importMindMapFromPaperImage(context: Context, bitmap: Bitmap) {
        val projectId = _selectedProjectId.value ?: return
        val activeNodeCount = mindMapNodes.value.count { it.isActive }
        val aiPrefs = context.getSharedPreferences("mahchin_ai", Context.MODE_PRIVATE)
        val apiKey = aiPrefs
            .getString("openai_api_key", "")
            .orEmpty()
            .trim()
        val aiModel = aiPrefs
            .getString("openai_model", "gpt-5")
            .orEmpty()
            .ifBlank { "gpt-5" }
            .trim()
        if (apiKey.isBlank()) {
            _settingsMessage.value = "برای تبدیل عکس به مایندمپ، کلید OpenAI را در تنظیمات وارد کن."
            return
        }
        if (activeNodeCount > 0) {
            _settingsMessage.value = "برای جلوگیری از قاطی شدن، این ابزار فقط وقتی مایندمپ خالی است فعال می‌شود."
            return
        }
        viewModelScope.launch {
            _mindMapAiBusy.value = true
            runCatching {
                val result = analyzePaperMindMapWithAi(apiKey, aiModel, bitmap)
                if (result.branches.isEmpty()) error("متن واقعی از تصویر تشخیص داده نشد. عکس را واضح‌تر، صاف‌تر و نزدیک‌تر بگیر.")
                result.centerTitle?.takeIf { it.isNotBlank() }?.let { center -> repository.updateProject(projectId, center) }
                repository.importMindMapBranches(projectId, result.branches)
            }.onSuccess { count ->
                _settingsMessage.value = "${count} نود واقعی از متن داخل عکس ساخته شد."
            }.onFailure { error ->
                _settingsMessage.value = "خطا در تبدیل عکس: ${error.message ?: "نامشخص"}"
            }
            _mindMapAiBusy.value = false
        }
    }

    private data class PaperMindMapAiResult(
        val centerTitle: String?,
        val branches: List<ImportedMindMapBranch>
    )

    
    private suspend fun analyzePaperMindMapWithAi(
        apiKey: String,
        aiModel: String,
        bitmap: Bitmap
    ): PaperMindMapAiResult {

        val projectId = _selectedProjectId.value ?: return PaperMindMapAiResult(
            centerTitle = null,
            branches = emptyList()
        )

        val ocrText = com.mahchin.app.ocr.OcrManager.extractTextSync(bitmap)

        return PaperMindMapAiResult(
            centerTitle = null,
            branches = emptyList()
        )
    }


    private fun bitmapToBase64(bitmap: Bitmap): String {
        val resized = resizeBitmapForAi(bitmap)
        val out = ByteArrayOutputStream()
        resized.compress(Bitmap.CompressFormat.JPEG, 95, out)
        return Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP)
    }

    private fun resizeBitmapForAi(bitmap: Bitmap): Bitmap {
        val maxSide = 2200
        val longest = maxOf(bitmap.width, bitmap.height)
        if (longest <= maxSide) return bitmap
        val ratio = maxSide.toFloat() / longest.toFloat()
        return Bitmap.createScaledBitmap(bitmap, (bitmap.width * ratio).toInt().coerceAtLeast(1), (bitmap.height * ratio).toInt().coerceAtLeast(1), true)
    }

    private fun parseImportedMindMapJson(raw: String): PaperMindMapAiResult {
        val clean = raw
            .replace("```json", "")
            .replace("```", "")
            .trim()
            .substringAfter('{', raw)
            .substringBeforeLast('}', raw)
        val json = JSONObject("{$clean}")
        val nodes = json.optJSONArray("nodes") ?: JSONArray()
        val centerTitle = cleanAiTitle(json.optString("centerTitle", ""))

        fun parseNode(obj: JSONObject): ImportedMindMapBranch? {
            val title = cleanAiTitle(obj.optString("title").trim())
            if (title.isBlank()) return null
            val childrenJson = obj.optJSONArray("children") ?: JSONArray()
            val children = buildList {
                for (i in 0 until childrenJson.length()) {
                    val child = childrenJson.optJSONObject(i) ?: continue
                    parseNode(child)?.let { add(it) }
                }
            }
            return ImportedMindMapBranch(title, children)
        }

        val branches = buildList {
            for (i in 0 until nodes.length()) {
                val obj = nodes.optJSONObject(i) ?: continue
                parseNode(obj)?.let { add(it) }
            }
        }
        return PaperMindMapAiResult(centerTitle = centerTitle.ifBlank { null }, branches = branches)
    }

    private fun cleanAiTitle(value: String): String {
        val title = value.trim().replace(Regex("\\s+"), " ")
        val forbidden = setOf(
            "شاخه", "زیرشاخه", "عنوان", "عنوان مرکزی", "موضوع", "ایده", "نود", "مرکزی", "متن", "نمونه",
            "متن واقعی شاخه", "متن واقعی زیرشاخه", "متن مرکزی واقعی", "شاخه اصلی", "زیر شاخه"
        )
        return if (title in forbidden) "" else title
    }

    fun showMessage(message: String) {
        _settingsMessage.value = message
    }

    fun clearSettingsMessage() {
        _settingsMessage.value = null
    }

    fun updateSettings(transform: (UserSettings) -> UserSettings) {
        viewModelScope.launch {
            val newSettings = transform(settings.value)
            repository.updateSettings(newSettings)
            ReminderScheduler.schedulePeriodic(app, newSettings)
        }
    }

    private suspend fun rescheduleTaskAlarms() {
        TaskAlarmScheduler.rescheduleAll(app, repository.getFutureAlarms())
    }
}

class MainViewModelFactory(
    private val app: Application,
    private val repository: TaskRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(app, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
