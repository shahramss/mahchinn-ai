package com.mahchin.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mahchin.app.data.model.MonthlyTemplateTask
import com.mahchin.app.data.model.TaskPriority
import com.mahchin.app.data.model.TaskStatus
import com.mahchin.app.domain.toPersianDigits
import com.mahchin.app.ui.components.TaskAlarmDialog
import com.mahchin.app.ui.components.TaskEditorDialog
import com.mahchin.app.ui.viewmodel.MainViewModel

@Composable
fun MonthlyTemplateScreen(vm: MainViewModel) {
    val templates by vm.templates.collectAsState()
    val projects by vm.projects.collectAsState()
    var addDialog by remember { mutableStateOf(false) }
    var editTemplate by remember { mutableStateOf<MonthlyTemplateTask?>(null) }
    var alarmTemplate by remember { mutableStateOf<MonthlyTemplateTask?>(null) }

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
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("قالب ماهانه", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text(
                        "برنامه‌ای که اینجا می‌چینی، هر ماه تکرار می‌شود.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        "برای خلوت ماندن صفحه، ویرایش و حذف با نگه‌داشتن روی تسک باز می‌شود.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.78f),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            if (templates.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.22f)),
                        elevation = CardDefaults.cardElevation(0.dp)
                    ) {
                        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("هنوز قالبی تعریف نشده", fontWeight = FontWeight.Bold)
                            Text("مثلاً روز ۱۰ هر ماه: بررسی فروش", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            } else {
                items(templates, key = { it.id }) { item ->
                    TemplateTodoCard(
                        template = item,
                        onToggle = { vm.toggleTemplateDone(item) },
                        onEdit = { editTemplate = item },
                        onDelete = { vm.deleteTemplate(item.id) },
                        onAlarm = { alarmTemplate = item }
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
            Text("افزودن تسک ثابت ماهانه", fontWeight = FontWeight.Bold)
        }
    }

    if (addDialog) {
        TaskEditorDialog(
            titleText = "تسک ثابت ماهانه",
            projects = projects,
            dayOfMonth = 1,
            onDismiss = { addDialog = false },
            onSave = { title, desc, day, priority, projectId ->
                vm.addTemplateTask(title, desc, day ?: 1, priority, projectId)
                addDialog = false
            }
        )
    }

    editTemplate?.let { t ->
        TaskEditorDialog(
            titleText = "ویرایش همه ماه‌ها",
            initialTitle = t.title,
            initialDescription = t.description,
            initialPriority = t.priority,
            initialProjectId = t.projectId,
            projects = projects,
            dayOfMonth = t.dayOfMonth,
            onDismiss = { editTemplate = null },
            onSave = { title, desc, day, priority, projectId ->
                vm.updateTemplate(t.id, title, desc, day ?: t.dayOfMonth, priority, projectId)
                editTemplate = null
            }
        )
    }

    alarmTemplate?.let { t ->
        TaskAlarmDialog(
            initialDate = vm.today.copy(day = t.dayOfMonth.coerceAtMost(com.mahchin.app.domain.JalaliCalendar.monthLength(vm.today.year, vm.today.month))),
            initialHour = t.alarmHour ?: 8,
            initialMinute = t.alarmMinute ?: 0,
            onDismiss = { alarmTemplate = null },
            onClear = {
                vm.setTemplateAlarm(t.id, null, null)
                alarmTemplate = null
            },
            onSave = { _, hour, minute ->
                vm.setTemplateAlarm(t.id, hour, minute)
                alarmTemplate = null
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun TemplateTodoCard(
    template: MonthlyTemplateTask,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onAlarm: () -> Unit
) {
    var showActions by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val checked = template.status == TaskStatus.DONE
    val priorityText = when (template.priority) {
        TaskPriority.NORMAL -> null
        TaskPriority.IMPORTANT -> "مهم"
        TaskPriority.URGENT -> "فوری"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { },
                onLongClick = { showActions = true }
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (checked) MaterialTheme.colorScheme.surface.copy(alpha = 0.58f) else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.26f)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(onClick = onToggle, modifier = Modifier.size(38.dp)) {
                Icon(
                    imageVector = if (checked) Icons.Outlined.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                    contentDescription = if (checked) "برداشتن تیک" else "تیک قالب",
                    tint = if (checked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(26.dp)
                )
            }

            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = template.title,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.titleMedium.copy(
                            textDecoration = if (checked) TextDecoration.LineThrough else TextDecoration.None
                        ),
                        fontWeight = FontWeight.SemiBold,
                        color = if (checked) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                    )
                    if (template.alarmHour != null) {
                        TemplatePill("آلارم", Color(0xFF7DD3FC))
                    }
                    if (priorityText != null) {
                        TemplatePill(priorityText, if (template.priority == TaskPriority.URGENT) Color(0xFFFF6B6B) else Color(0xFFFFB454))
                    }
                }

                if (template.description.isNotBlank()) {
                    Text(
                        text = template.description,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = "روز ${template.dayOfMonth.toPersianDigits()} هر ماه",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(onClick = { showActions = true }, modifier = Modifier.size(34.dp)) {
                Icon(Icons.Outlined.MoreVert, contentDescription = "گزینه‌های قالب", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }

    if (showActions) {
        ModalBottomSheet(
            onDismissRequest = { showActions = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = template.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "روز ${template.dayOfMonth.toPersianDigits()} هر ماه تکرار می‌شود.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                TemplateActionRow(
                    if (checked) "برداشتن تیک" else "تیک زدن",
                    if (checked) Icons.Outlined.RadioButtonUnchecked else Icons.Outlined.CheckCircle
                ) { showActions = false; onToggle() }
                TemplateActionRow("آلارم ماهانه", Icons.Outlined.Alarm) { showActions = false; onAlarm() }
                HorizontalDivider(Modifier.padding(vertical = 6.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.18f))
                TemplateActionRow("ویرایش", Icons.Outlined.Edit) { showActions = false; onEdit() }
                TemplateActionRow("حذف", Icons.Outlined.Delete, danger = true) { showActions = false; onDelete() }
                Spacer(Modifier.height(18.dp))
            }
        }
    }
}

@Composable
private fun TemplatePill(text: String, color: Color) {
    Box(
        modifier = Modifier
            .background(color.copy(alpha = 0.14f), RoundedCornerShape(999.dp))
            .padding(horizontal = 8.dp, vertical = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun TemplateActionRow(title: String, icon: ImageVector, danger: Boolean = false, onClick: () -> Unit) {
    val color = if (danger) Color(0xFFFF6B6B) else MaterialTheme.colorScheme.onSurface
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(22.dp))
        Text(title, color = color, style = MaterialTheme.typography.titleMedium)
    }
}
