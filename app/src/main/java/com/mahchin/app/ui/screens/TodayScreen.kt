package com.mahchin.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mahchin.app.data.model.TaskItem
import com.mahchin.app.domain.JalaliCalendar
import com.mahchin.app.domain.toEnglishDigits
import com.mahchin.app.domain.toPersianDigits
import com.mahchin.app.ui.components.JalaliDateDialog
import com.mahchin.app.ui.components.MindMapAwareTaskList
import com.mahchin.app.ui.components.TaskEditorDialog
import com.mahchin.app.ui.components.TaskAlarmDialog
import com.mahchin.app.ui.viewmodel.MainViewModel

@Composable
fun TodayScreen(vm: MainViewModel) {
    val tasks by vm.todayTasks.collectAsState()
    val settings by vm.settings.collectAsState()
    val projects by vm.projects.collectAsState()
    val mindMapNodes by vm.allMindMapNodes.collectAsState()
    val today = vm.today
    val done = tasks.count { it.status.isClosed() }
    val remaining = tasks.size - done
    val progress = if (tasks.isEmpty()) 0f else done.toFloat() / tasks.size.toFloat()

    var addDialog by remember { mutableStateOf(false) }
    var editTask by remember { mutableStateOf<TaskItem?>(null) }
    var moveTask by remember { mutableStateOf<TaskItem?>(null) }
    var alarmTask by remember { mutableStateOf<TaskItem?>(null) }
    var moveGroupTasks by remember { mutableStateOf<List<TaskItem>?>(null) }
    var alarmGroupTasks by remember { mutableStateOf<List<TaskItem>?>(null) }
    var projectSearch by remember { mutableStateOf("") }
    var taskSearch by remember { mutableStateOf("") }
    val visibleTasks = remember(tasks, projectSearch, taskSearch) {
        val projectQ = projectSearch.trim().toEnglishDigits()
        val taskQ = taskSearch.trim().toEnglishDigits()
        tasks.filter { task ->
            val projectOk = projectQ.isBlank() || (task.projectName ?: "بدون پروژه").contains(projectQ, ignoreCase = true)
            val taskText = listOf(task.title, task.description, task.mindMapPath.orEmpty()).joinToString(" ")
            val taskOk = taskQ.isBlank() || taskText.contains(taskQ, ignoreCase = true)
            projectOk && taskOk
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 16.dp, bottom = 98.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        "امروز",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        "${JalaliCalendar.weekdayName(today)}، ${today.display}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.30f)),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Column(Modifier.padding(13.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "${done.toPersianDigits()} انجام‌شده",
                                modifier = Modifier.weight(1f),
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "${remaining.toPersianDigits()} باقی‌مانده",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())
                        Text(
                            "تیک را دوباره بزنی، برداشته می‌شود. گزینه‌های بیشتر با نگه‌داشتن روی تسک.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "یادآوری ${settings.reminderIntensity.fa} از ${settings.startHour.toPersianDigits()} تا ${settings.endHour.toPersianDigits()}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.78f)
                        )
                    }
                }
            }

            if (remaining > 0) {
                item {
                    OutlinedButton(
                        onClick = vm::moveAllRemainingTodayToTomorrow,
                        modifier = Modifier.fillMaxWidth().height(42.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) { Text("انتقال همه کارهای باز به فردا") }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.28f)),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("جستجوی تسک‌ها", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        OutlinedTextField(
                            value = projectSearch,
                            onValueChange = { projectSearch = it },
                            label = { Text("جستجو بر اساس پروژه") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = taskSearch,
                            onValueChange = { taskSearch = it },
                            label = { Text("جستجو بر اساس عنوان یا متن تسک") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                }
            }

            if (visibleTasks.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(0.dp)
                    ) {
                        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("امروز خلوت است", fontWeight = FontWeight.Bold)
                            Text("یک تسک اضافه کن یا برنامه ثابت را از قالب ماهانه بچین.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            FilledTonalButton(onClick = { addDialog = true }) { Text("افزودن اولین تسک") }
                        }
                    }
                }
            } else {
                item {
                    MindMapAwareTaskList(
                        tasks = visibleTasks,
                        mindMapNodes = mindMapNodes,
                        projects = projects,
                        onSetGroupStatus = { groupTasks, status -> vm.setTaskGroupStatus(groupTasks, status) },
                        onDone = { task -> vm.toggleDone(task) },
                        onEdit = { task -> editTask = task },
                        onDelete = { task -> vm.deleteTask(task) },
                        onMoveTomorrow = { task -> vm.moveToTomorrow(task) },
                        onMoveCustom = { task -> moveTask = task },
                        onCancel = { task -> vm.cancelToday(task) },
                        onInProgress = { task -> vm.inProgress(task) },
                        onReset = { task -> vm.resetStatus(task) },
                        onSetAlarm = { task -> alarmTask = task },
                        onBatchStatus = { groupTasks, status -> vm.setTaskGroupStatus(groupTasks, status) },
                        onBatchDelete = { groupTasks -> vm.deleteTaskGroup(groupTasks) },
                        onBatchMoveTomorrow = { groupTasks -> vm.moveTaskGroupToTomorrow(groupTasks) },
                        onBatchMoveCustom = { groupTasks -> moveGroupTasks = groupTasks },
                        onBatchAlarm = { groupTasks -> alarmGroupTasks = groupTasks },
                        onUpdateProjectPriority = { projectId, priority -> vm.updateProjectPriority(projectId, priority) }
                    )
                }
            }
        }

        Button(
            onClick = { addDialog = true },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 14.dp, vertical = 12.dp)
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            contentPadding = PaddingValues(horizontal = 18.dp)
        ) {
            Icon(Icons.Outlined.Add, contentDescription = null, modifier = Modifier.size(21.dp))
            Spacer(Modifier.size(8.dp))
            Text("افزودن تسک امروز", fontWeight = FontWeight.Bold)
        }
    }

    if (addDialog) {
        TaskEditorDialog(
            titleText = "تسک اختصاصی امروز",
            projects = projects,
            onDismiss = { addDialog = false },
            onSave = { title, desc, _, priority, projectId ->
                vm.addTodayTask(title, desc, priority, projectId)
                addDialog = false
            }
        )
    }

    editTask?.let { task ->
        TaskEditorDialog(
            titleText = "ویرایش فقط همین تاریخ",
            initialTitle = task.title,
            initialDescription = task.description,
            initialPriority = task.priority,
            initialProjectId = task.projectId,
            projects = projects,
            onDismiss = { editTask = null },
            onSave = { title, desc, _, priority, projectId ->
                vm.editOnlyThisDate(task, title, desc, priority, projectId)
                editTask = null
            }
        )
    }

    moveTask?.let { task ->
        JalaliDateDialog(
            initialDate = today.plusDays(1),
            onDismiss = { moveTask = null },
            onSave = { date ->
                vm.moveToCustomDate(task, date)
                moveTask = null
            }
        )
    }

    alarmTask?.let { task ->
        TaskAlarmDialog(
            initialDate = com.mahchin.app.domain.JalaliDate(task.jalaliYear, task.jalaliMonth, task.jalaliDay),
            onDismiss = { alarmTask = null },
            onClear = {
                vm.clearTaskAlarm(task)
                alarmTask = null
            },
            onSave = { date, hour, minute ->
                vm.setTaskAlarm(task, date, hour, minute)
                alarmTask = null
            }
        )
    }


    moveGroupTasks?.let { groupTasks ->
        JalaliDateDialog(
            initialDate = today.plusDays(1),
            onDismiss = { moveGroupTasks = null },
            onSave = { date ->
                vm.moveTaskGroupToCustomDate(groupTasks, date)
                moveGroupTasks = null
            }
        )
    }

    alarmGroupTasks?.let { groupTasks ->
        TaskAlarmDialog(
            initialDate = today,
            onDismiss = { alarmGroupTasks = null },
            onClear = { alarmGroupTasks = null },
            onSave = { date, hour, minute ->
                vm.setTaskGroupAlarm(groupTasks, date, hour, minute)
                alarmGroupTasks = null
            }
        )
    }

}
