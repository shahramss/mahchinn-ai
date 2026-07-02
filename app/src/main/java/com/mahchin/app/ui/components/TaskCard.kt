package com.mahchin.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mahchin.app.data.model.TaskItem
import com.mahchin.app.data.model.TaskPriority
import com.mahchin.app.data.model.TaskStatus

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TaskCard(
    task: TaskItem,
    onDone: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onMoveTomorrow: () -> Unit,
    onMoveCustom: () -> Unit,
    onCancel: () -> Unit,
    onInProgress: () -> Unit,
    onReset: (() -> Unit)? = null,
    onSetAlarm: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    onLongSelect: (() -> Unit)? = null,
    containerOverride: Color? = null,
    borderOverride: Color? = null
) {
    val closed = task.status.isClosed()
    var showActions by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val isDark = isSystemInDarkTheme()
    val forceBlackText = containerOverride != null && task.sourceMindMapNodeId != null
    val mainTextColor = if (forceBlackText) Color.Black.copy(alpha = 0.96f) else MaterialTheme.colorScheme.onSurface
    val secondaryTextColor = if (forceBlackText) Color.Black.copy(alpha = 0.72f) else MaterialTheme.colorScheme.onSurfaceVariant
    val breadcrumbTextColor = if (forceBlackText) Color.Black.copy(alpha = 0.82f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.88f)
    val iconTextColor = if (forceBlackText) Color.Black.copy(alpha = 0.78f) else MaterialTheme.colorScheme.onSurfaceVariant

    val statusColor = when (task.status) {
        TaskStatus.NOT_STARTED -> MaterialTheme.colorScheme.outline
        TaskStatus.IN_PROGRESS -> Color(0xFFFFB454)
        TaskStatus.DONE -> MaterialTheme.colorScheme.primary
        TaskStatus.MOVED_TO_TOMORROW, TaskStatus.MOVED_TO_CUSTOM_DATE -> Color(0xFF7DD3FC)
        TaskStatus.CANCELED -> Color(0xFFFF6B6B)
    }

    val priorityText = when (task.priority) {
        TaskPriority.NORMAL -> null
        TaskPriority.IMPORTANT -> "مهم"
        TaskPriority.URGENT -> "فوری"
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { if (selected) onLongSelect?.invoke() },
                onLongClick = { if (onLongSelect != null) onLongSelect.invoke() else showActions = true }
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                selected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
                containerOverride != null -> containerOverride
                closed && isDark -> Color(0xFF1A2A3F)
                closed -> MaterialTheme.colorScheme.surface.copy(alpha = 0.58f)
                isDark -> Color(0xFF223A56)
                else -> MaterialTheme.colorScheme.surface
            }
        ),
        border = BorderStroke(1.dp, if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.78f) else borderOverride ?: MaterialTheme.colorScheme.outline.copy(alpha = if (isDark) 0.68f else 0.26f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(
                onClick = onDone,
                modifier = Modifier.size(38.dp)
            ) {
                Icon(
                    imageVector = if (task.status == TaskStatus.DONE) Icons.Outlined.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                    contentDescription = if (task.status == TaskStatus.DONE) "برداشتن تیک" else "انجام شد",
                    tint = if (forceBlackText) Color.Black.copy(alpha = if (task.status == TaskStatus.DONE) 0.94f else 0.64f) else if (task.status == TaskStatus.DONE) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(26.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = task.title,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Clip,
                        style = MaterialTheme.typography.titleMedium.copy(
                            textDecoration = if (task.status == TaskStatus.DONE) TextDecoration.LineThrough else TextDecoration.None
                        ),
                        fontWeight = FontWeight.SemiBold,
                        color = if (forceBlackText) mainTextColor else if (closed) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                    )
                    if (task.alarmAtMillis != null) {
                        MiniPill("آلارم", Color(0xFF7DD3FC), if (forceBlackText) Color.Black else null)
                    }
                    if (priorityText != null) {
                        MiniPill(priorityText, if (task.priority == TaskPriority.URGENT) Color(0xFFFF6B6B) else Color(0xFFFFB454), if (forceBlackText) Color.Black else null)
                    }
                }

                val breadcrumb = task.mindMapPath
                    ?.let { path -> listOfNotNull(task.projectName, path).joinToString("  ←  ") }
                    .orEmpty()
                if (breadcrumb.isNotBlank()) {
                    Text(
                        text = breadcrumb,
                        style = MaterialTheme.typography.labelSmall,
                        color = breadcrumbTextColor,
                        maxLines = 2,
                        overflow = TextOverflow.Clip
                    )
                } else if (task.description.isNotBlank()) {
                    Text(
                        text = task.description,
                        maxLines = 2,
                        overflow = TextOverflow.Clip,
                        style = MaterialTheme.typography.bodySmall,
                        color = secondaryTextColor
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    StatusDot(statusColor)
                    Text(
                        text = task.status.fa,
                        style = MaterialTheme.typography.bodySmall,
                        color = secondaryTextColor,
                        maxLines = 1
                    )
                    Text("•", color = if (forceBlackText) Color.Black.copy(alpha = 0.42f) else MaterialTheme.colorScheme.outline)
                    Text(
                        text = task.projectName ?: task.taskType.fa,
                        style = MaterialTheme.typography.bodySmall,
                        color = secondaryTextColor,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Clip
                    )
                }
            }

            IconButton(onClick = { showActions = true }, modifier = Modifier.size(34.dp)) {
                Icon(
                    Icons.Outlined.MoreVert,
                    contentDescription = "گزینه‌های تسک",
                    tint = iconTextColor
                )
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
                    text = task.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Clip
                )
                Text(
                    text = listOfNotNull(task.projectName, task.mindMapPath).joinToString(" • ").ifBlank { "گزینه‌ها با نگه‌داشتن روی تسک باز می‌شوند." },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))

                ActionRow(
                    if (task.status == TaskStatus.DONE) "برداشتن تیک" else "انجام شد",
                    if (task.status == TaskStatus.DONE) Icons.Outlined.RadioButtonUnchecked else Icons.Outlined.CheckCircle
                ) { showActions = false; onDone() }

                if (task.status != TaskStatus.NOT_STARTED && onReset != null) {
                    ActionRow("برگردان به انجام‌نشده", Icons.Outlined.RadioButtonUnchecked) { showActions = false; onReset() }
                }

                if (!closed) {
                    ActionRow("در حال انجام", Icons.Outlined.RadioButtonUnchecked) { showActions = false; onInProgress() }
                    ActionRow("انتقال به فردا", Icons.Outlined.KeyboardArrowLeft) { showActions = false; onMoveTomorrow() }
                    ActionRow("انتقال به تاریخ دلخواه", Icons.Outlined.KeyboardArrowLeft) { showActions = false; onMoveCustom() }
                    ActionRow("لغو برای امروز", Icons.Outlined.Close) { showActions = false; onCancel() }
                }

                onSetAlarm?.let {
                    ActionRow("تنظیم آلارم", Icons.Outlined.Alarm) { showActions = false; it() }
                }
                HorizontalDivider(Modifier.padding(vertical = 6.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.18f))
                ActionRow("ویرایش", Icons.Outlined.Edit) { showActions = false; onEdit() }
                ActionRow("حذف", Icons.Outlined.Delete, danger = true) { showActions = false; onDelete() }
                Spacer(Modifier.height(18.dp))
            }
        }
    }
}

@Composable
private fun MiniPill(text: String, color: Color, textColor: Color? = null) {
    Box(
        modifier = Modifier
            .background(color.copy(alpha = 0.14f), RoundedCornerShape(999.dp))
            .padding(horizontal = 8.dp, vertical = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = textColor ?: color,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun ActionRow(
    title: String,
    icon: ImageVector,
    danger: Boolean = false,
    onClick: () -> Unit
) {
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

@Composable
private fun StatusDot(color: Color) {
    Box(
        modifier = Modifier
            .size(7.dp)
            .background(color, CircleShape)
    )
}
