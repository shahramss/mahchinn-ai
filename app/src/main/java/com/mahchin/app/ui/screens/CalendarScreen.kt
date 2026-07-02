package com.mahchin.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mahchin.app.data.model.TaskItem
import com.mahchin.app.domain.JalaliCalendar
import com.mahchin.app.domain.JalaliDate
import com.mahchin.app.domain.toPersianDigits
import com.mahchin.app.ui.components.JalaliDateDialog
import com.mahchin.app.ui.components.MindMapAwareTaskList
import com.mahchin.app.ui.components.TaskAlarmDialog
import com.mahchin.app.ui.components.TaskEditorDialog
import com.mahchin.app.ui.viewmodel.MainViewModel

@Composable
fun CalendarScreen(vm: MainViewModel) {
    val month by vm.calendarMonth.collectAsState()
    val counts by vm.monthCounts.collectAsState()
    val selectedDate by vm.selectedDate.collectAsState()
    val selectedTasks by vm.selectedDateTasks.collectAsState()
    val projects by vm.projects.collectAsState()
    val mindMapNodes by vm.allMindMapNodes.collectAsState()

    var addDialog by remember { mutableStateOf(false) }
    var editTask by remember { mutableStateOf<TaskItem?>(null) }
    var moveTask by remember { mutableStateOf<TaskItem?>(null) }
    var alarmTask by remember { mutableStateOf<TaskItem?>(null) }
    var moveGroupTasks by remember { mutableStateOf<List<TaskItem>?>(null) }
    var alarmGroupTasks by remember { mutableStateOf<List<TaskItem>?>(null) }
    var clearDayDialog by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            OutlinedButton(onClick = vm::previousMonth, enabled = !(month.year == 1405 && month.month == 1)) { Text("قبلی") }
            Text("${JalaliCalendar.monthName(month.month)} ${month.year.toPersianDigits()}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            OutlinedButton(onClick = vm::nextMonth, enabled = !(month.year == 1500 && month.month == 12)) { Text("بعدی") }
        }
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
            listOf("شنبه", "یک", "دو", "سه", "چهار", "پنج", "جمعه").forEach { Text(it, style = MaterialTheme.typography.bodySmall) }
        }
        Spacer(Modifier.height(4.dp))
        MonthGrid(month.year, month.month, selectedDate, counts, onSelect = vm::selectDate)
        Spacer(Modifier.height(14.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text("برنامه ${selectedDate.display}", fontWeight = FontWeight.Bold)
                AssistChip(onClick = {}, label = { Text("${selectedTasks.size.toPersianDigits()} تسک") })
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedButton(onClick = { clearDayDialog = true }, enabled = selectedTasks.isNotEmpty()) { Text("پاک‌سازی روز") }
                Button(onClick = { addDialog = true }) { Text("تسک اختصاصی") }
            }
        }
        Spacer(Modifier.height(8.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (selectedTasks.isEmpty()) {
                item { Text("برای این تاریخ کاری وجود ندارد.") }
            } else {
                item {
                    MindMapAwareTaskList(
                        tasks = selectedTasks,
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
    }

    if (clearDayDialog) {
        AlertDialog(
            onDismissRequest = { clearDayDialog = false },
            title = { Text("پاک کردن تسک‌های این روز") },
            text = { Text("همه تسک‌های همین روز به‌جز تسک‌های قالب تکرارشونده پاک شوند؟") },
            confirmButton = {
                Button(onClick = {
                    vm.clearNonTemplateTasksForDate(selectedDate)
                    clearDayDialog = false
                }) { Text("پاک کن") }
            },
            dismissButton = { TextButton(onClick = { clearDayDialog = false }) { Text("انصراف") } }
        )
    }

    if (addDialog) {
        TaskEditorDialog(
            titleText = "تسک اختصاصی برای ${selectedDate.display}",
            projects = projects,
            onDismiss = { addDialog = false },
            onSave = { title, desc, _, priority, projectId ->
                vm.addOneTimeTask(selectedDate, title, desc, priority, projectId)
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
            initialDate = selectedDate.plusDays(1),
            onDismiss = { moveTask = null },
            onSave = { date -> vm.moveToCustomDate(task, date); moveTask = null }
        )
    }

    alarmTask?.let { task ->
        TaskAlarmDialog(
            initialDate = JalaliDate(task.jalaliYear, task.jalaliMonth, task.jalaliDay),
            onDismiss = { alarmTask = null },
            onClear = { vm.clearTaskAlarm(task); alarmTask = null },
            onSave = { date, hour, minute -> vm.setTaskAlarm(task, date, hour, minute); alarmTask = null }
        )
    }

    moveGroupTasks?.let { groupTasks ->
        JalaliDateDialog(
            initialDate = selectedDate.plusDays(1),
            onDismiss = { moveGroupTasks = null },
            onSave = { date ->
                vm.moveTaskGroupToCustomDate(groupTasks, date)
                moveGroupTasks = null
            }
        )
    }

    alarmGroupTasks?.let { groupTasks ->
        TaskAlarmDialog(
            initialDate = selectedDate,
            onDismiss = { alarmGroupTasks = null },
            onClear = { alarmGroupTasks = null },
            onSave = { date, hour, minute ->
                vm.setTaskGroupAlarm(groupTasks, date, hour, minute)
                alarmGroupTasks = null
            }
        )
    }
}

@Composable
private fun MonthGrid(
    year: Int,
    month: Int,
    selectedDate: JalaliDate,
    counts: Map<Int, Int>,
    onSelect: (JalaliDate) -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val offset = JalaliCalendar.firstDayOffsetSaturdayBased(year, month)
    val len = JalaliCalendar.monthLength(year, month)
    val cells = List(offset) { 0 } + (1..len).toList()
    LazyVerticalGrid(
        columns = GridCells.Fixed(7),
        modifier = Modifier.fillMaxWidth().height(285.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        items(cells) { day ->
            if (day == 0) {
                Box(Modifier.aspectRatio(1f))
            } else {
                val isSelected = selectedDate.year == year && selectedDate.month == month && selectedDate.day == day
                val count = counts[day] ?: 0
                val dayColor = when {
                    isSelected -> MaterialTheme.colorScheme.primaryContainer
                    isDark -> Color(0xFF355A82)
                    else -> MaterialTheme.colorScheme.surface
                }
                Card(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .clickable { onSelect(JalaliDate(year, month, day)) },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = dayColor),
                    border = if (!isDark) BorderStroke(0.8.dp, Color.Black.copy(alpha = 0.22f)) else BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.30f))
                ) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(day.toPersianDigits(), fontWeight = FontWeight.Bold)
                            if (count > 0) Text("${count.toPersianDigits()} کار", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
    }
}
