package com.mahchin.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mahchin.app.data.model.FinanceTask
import com.mahchin.app.data.model.Project
import com.mahchin.app.domain.JalaliCalendar
import com.mahchin.app.domain.JalaliDate
import com.mahchin.app.domain.toEnglishDigits
import com.mahchin.app.domain.toPersianDigits
import com.mahchin.app.ui.viewmodel.MainViewModel

@Composable
fun FinanceScreen(vm: MainViewModel) {
    val projects by vm.projects.collectAsState()
    val financeTasks by vm.financeTasks.collectAsState()
    var selectedProjectId by remember { mutableStateOf<Long?>(null) }
    var search by remember { mutableStateOf("") }
    var month by remember { mutableStateOf(JalaliDate(vm.today.year, vm.today.month, 1)) }
    val selectedProject = projects.firstOrNull { it.id == selectedProjectId }

    if (selectedProject == null) {
        FinanceProjectList(
            projects = projects,
            query = search,
            onQueryChange = { search = it },
            onSelect = { selectedProjectId = it.id },
            tasks = financeTasks
        )
    } else {
        FinanceProjectDetail(
            project = selectedProject,
            month = month,
            tasks = financeTasks.filter { it.projectId == selectedProject.id && it.jalaliYear == month.year && it.jalaliMonth == month.month && it.isActive },
            onBack = { selectedProjectId = null },
            onPrev = { month = JalaliCalendar.previousMonth(month) },
            onNext = { month = JalaliCalendar.nextMonth(month) },
            onAdd = { title, amount, customerDate -> vm.addFinanceTask(selectedProject.id, month, title, amount, customerDate) },
            onToggle = { task -> vm.toggleFinanceDone(task.id, !task.isDone) },
            onDelete = { task -> vm.deleteFinanceTask(task.id) },
            onCarry = { vm.carryOpenFinanceToNextMonth(selectedProject.id, month) }
        )
    }
}

@Composable
private fun FinanceProjectList(
    projects: List<Project>,
    query: String,
    onQueryChange: (String) -> Unit,
    onSelect: (Project) -> Unit,
    tasks: List<FinanceTask>
) {
    val q = query.trim().toEnglishDigits()
    val filtered = projects
        .filter { q.isBlank() || it.name.contains(q, ignoreCase = true) }
        .sortedWith(compareByDescending<Project> { it.priority.weight }.thenBy { it.name })
    val taskCountByProject = tasks.filter { it.isActive }.groupBy { it.projectId }.mapValues { it.value.size }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("مدیریت مالی", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text("هزینه‌ها، پرداخت‌ها و فاکتور ماهانه هر پروژه را جدا مدیریت کن.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        item {
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                label = { Text("جستجوی پروژه مالی") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }
        if (filtered.isEmpty()) {
            item { Text("پروژه‌ای پیدا نشد.", color = MaterialTheme.colorScheme.onSurfaceVariant) }
        } else {
            items(filtered) { project ->
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { onSelect(project) },
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.32f)),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text(project.name.toPersianDigits(), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            Text("${(taskCountByProject[project.id] ?: 0).toPersianDigits()} تسک مالی", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                        }
                        Text(project.priority.fa, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun FinanceProjectDetail(
    project: Project,
    month: JalaliDate,
    tasks: List<FinanceTask>,
    onBack: () -> Unit,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onAdd: (String, Long, String?) -> Unit,
    onToggle: (FinanceTask) -> Unit,
    onDelete: (FinanceTask) -> Unit,
    onCarry: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var customerDate by remember(month) { mutableStateOf(month.key) }
    val doneSum = tasks.filter { it.isDone }.sumOf { it.amount }
    val openSum = tasks.filter { !it.isDone }.sumOf { it.amount }
    val totalSum = tasks.sumOf { it.amount }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "بازگشت") }
                Column(Modifier.weight(1f)) {
                    Text(project.name.toPersianDigits(), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                    Text("فاکتور ${month.year.toPersianDigits()}/${month.month.toPersianDigits()}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onPrev, modifier = Modifier.weight(1f)) { Text("ماه قبل") }
                OutlinedButton(onClick = onNext, modifier = Modifier.weight(1f)) { Text("ماه بعد") }
            }
        }
        item {
            Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)), border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.30f))) {
                Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    FinanceLine("کل فاکتور", totalSum)
                    FinanceLine("خرج انجام‌شده", doneSum)
                    FinanceLine("مانده برای خرج", openSum)
                }
            }
        }
        item {
            Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.30f))) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("افزودن تسک مالی", fontWeight = FontWeight.Bold)
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("عنوان هزینه") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                    OutlinedTextField(value = amount.toPersianDigits(), onValueChange = { amount = it.toEnglishDigits().filter { ch -> ch.isDigit() } }, label = { Text("مبلغ") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth(), singleLine = true)
                    OutlinedTextField(value = customerDate.toPersianDigits(), onValueChange = { customerDate = it.toEnglishDigits() }, label = { Text("تاریخ پرداخت مشتری") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                    Button(onClick = {
                        val a = amount.toLongOrNull() ?: 0L
                        if (title.isNotBlank() && a > 0) {
                            onAdd(title, a, customerDate.ifBlank { month.key })
                            title = ""
                            amount = ""
                        }
                    }, modifier = Modifier.fillMaxWidth().height(48.dp)) {
                        Icon(Icons.Outlined.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.size(7.dp))
                        Text("افزودن")
                    }
                }
            }
        }
        item {
            OutlinedButton(onClick = onCarry, modifier = Modifier.fillMaxWidth().height(46.dp)) {
                Text("انتقال هزینه‌های پرداخت‌نشده به ماه بعد")
            }
        }
        if (tasks.isEmpty()) {
            item { Text("برای این ماه تسک مالی ثبت نشده.", color = MaterialTheme.colorScheme.onSurfaceVariant) }
        } else {
            items(tasks) { task -> FinanceTaskCard(task, onToggle, onDelete) }
        }
    }
}

@Composable
private fun FinanceLine(title: String, amount: Long) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(title, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
        Text("${amount.toPersianDigits()} تومان", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun FinanceTaskCard(task: FinanceTask, onToggle: (FinanceTask) -> Unit, onDelete: (FinanceTask) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = if (task.isDone) Color(0xFFDCFCE7).copy(alpha = 0.22f) else MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, if (task.isDone) Color(0xFF22C55E).copy(alpha = 0.45f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.30f)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(Modifier.fillMaxWidth().padding(10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            IconButton(onClick = { onToggle(task) }) {
                Icon(if (task.isDone) Icons.Outlined.CheckCircle else Icons.Outlined.RadioButtonUnchecked, contentDescription = null, tint = if (task.isDone) Color(0xFF22C55E) else MaterialTheme.colorScheme.outline)
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(task.title.toPersianDigits(), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("${task.amount.toPersianDigits()} تومان", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                Text(
                    "پرداخت مشتری: ${task.customerPaymentDateKey?.toPersianDigits() ?: "ثبت نشده"}  •  انجام ما: ${task.completedDateKey?.toPersianDigits() ?: "انجام نشده"}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            IconButton(onClick = { onDelete(task) }) { Icon(Icons.Default.Delete, contentDescription = "حذف", tint = Color(0xFFFF5C5C)) }
        }
    }
}
