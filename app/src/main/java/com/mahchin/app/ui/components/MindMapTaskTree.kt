package com.mahchin.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.mahchin.app.data.model.MindMapNode
import com.mahchin.app.data.model.Project
import com.mahchin.app.data.model.TaskItem
import com.mahchin.app.data.model.TaskPriority
import com.mahchin.app.data.model.TaskStatus
import com.mahchin.app.domain.toPersianDigits

@Composable
fun MindMapAwareTaskList(
    tasks: List<TaskItem>,
    mindMapNodes: List<MindMapNode>,
    projects: List<Project> = emptyList(),
    onSetGroupStatus: (List<TaskItem>, TaskStatus) -> Unit,
    onDone: (TaskItem) -> Unit,
    onEdit: (TaskItem) -> Unit,
    onDelete: (TaskItem) -> Unit,
    onMoveTomorrow: (TaskItem) -> Unit,
    onMoveCustom: (TaskItem) -> Unit,
    onCancel: (TaskItem) -> Unit,
    onInProgress: (TaskItem) -> Unit,
    onReset: (TaskItem) -> Unit,
    onSetAlarm: (TaskItem) -> Unit,
    onBatchStatus: (List<TaskItem>, TaskStatus) -> Unit = onSetGroupStatus,
    onBatchDelete: (List<TaskItem>) -> Unit = { it.forEach(onDelete) },
    onBatchMoveTomorrow: (List<TaskItem>) -> Unit = { it.forEach(onMoveTomorrow) },
    onBatchMoveCustom: (List<TaskItem>) -> Unit = { it.firstOrNull()?.let(onMoveCustom) },
    onBatchAlarm: (List<TaskItem>) -> Unit = { it.firstOrNull()?.let(onSetAlarm) },
    onUpdateProjectPriority: (Long, TaskPriority) -> Unit = { _, _ -> }
) {
    val activeNodes = remember(mindMapNodes) { mindMapNodes.filter { it.isActive } }
    val nodeMap = remember(activeNodes) { activeNodes.associateBy { it.id } }
    val children = remember(activeNodes) { activeNodes.groupBy { it.parentId } }
    val rootOrder = remember(activeNodes) {
        activeNodes
            .filter { it.parentId == null }
            .sortedWith(compareBy({ it.projectId }, { it.orderIndex }, { it.createdAt }))
    }
    val expandableState = remember { mutableStateMapOf<String, Boolean>() }
    var selectedKeys by remember { mutableStateOf<Set<String>>(emptySet()) }

    fun key(task: TaskItem): String = task.origin.name + "_" + task.id
    val selectedTasks = tasks.filter { key(it) in selectedKeys }

    fun setSelected(list: List<TaskItem>) {
        val keys = list.map { key(it) }.toSet()
        selectedKeys = if (keys.all { it in selectedKeys }) selectedKeys - keys else selectedKeys + keys
    }

    val mindTasks = tasks
        .filter { it.sourceMindMapNodeId != null && nodeMap.containsKey(it.sourceMindMapNodeId) }
        .sortedWith(compareBy({ it.projectName ?: "" }, { it.mindMapPath ?: "" }, { it.createdAt }))
    val normalTasks = tasks.filterNot { it.sourceMindMapNodeId != null && nodeMap.containsKey(it.sourceMindMapNodeId) }
    val taskByNode = mindTasks.mapNotNull { t -> t.sourceMindMapNodeId?.let { it to t } }.toMap()
    val projectMap = remember(projects) { projects.associateBy { it.id } }

    fun nodeHasChildren(node: MindMapNode): Boolean = children[node.id].orEmpty().any { it.isActive }

    fun sortedMindNodes(list: List<MindMapNode>): List<MindMapNode> = list
        .filter { it.isActive }
        .sortedWith(
            compareByDescending<MindMapNode> { nodeHasChildren(it) }
                .thenBy { it.orderIndex }
                .thenBy { it.createdAt }
        )

    fun subtreeTasks(node: MindMapNode): List<TaskItem> {
        val result = mutableListOf<TaskItem>()
        taskByNode[node.id]?.let { result += it }
        sortedMindNodes(children[node.id].orEmpty()).forEach { child ->
            result += subtreeTasks(child)
        }
        return result
    }

    @Composable
    fun RenderNode(node: MindMapNode, level: Int) {
        val groupTasks = subtreeTasks(node)
        if (groupTasks.isEmpty()) return
        val childNodes = sortedMindNodes(children[node.id].orEmpty())
            .filter { subtreeTasks(it).isNotEmpty() }
        val ownTask = taskByNode[node.id]
        val hasChildren = childNodes.isNotEmpty()
        val indent = ((level - 1).coerceAtLeast(0) * 12).dp
        val nodeColor = mindMapTaskNodeColor(node, level, nodeMap, rootOrder)
        val nodeBorderColor = nodeColor.darker(0.34f).copy(alpha = 0.64f)

        if (hasChildren) {
            val k = "node_${node.id}"
            val expanded = expandableState[k] ?: true
            ParentTaskAccordionCard(
                title = ownTask?.title ?: node.title,
                subtitle = "${groupTasks.size.toPersianDigits()} تسک زیرمجموعه",
                tasks = groupTasks,
                expanded = expanded,
                level = level,
                selected = groupTasks.any { key(it) in selectedKeys },
                modifier = Modifier.padding(start = indent),
                containerColor = nodeColor,
                borderColor = nodeBorderColor,
                onToggleExpand = { expandableState[k] = !expanded },
                onToggleDone = { targetStatus -> onSetGroupStatus(groupTasks, targetStatus) },
                onLongSelect = { setSelected(groupTasks) }
            )
            if (expanded) {
                childNodes.forEach { child -> RenderNode(child, level + 1) }
            }
        } else if (ownTask != null) {
            TaskCard(
                task = ownTask,
                selected = key(ownTask) in selectedKeys,
                onLongSelect = { setSelected(listOf(ownTask)) },
                onDone = { onDone(ownTask) },
                onEdit = { onEdit(ownTask) },
                onDelete = { onDelete(ownTask) },
                onMoveTomorrow = { onMoveTomorrow(ownTask) },
                onMoveCustom = { onMoveCustom(ownTask) },
                onCancel = { onCancel(ownTask) },
                onInProgress = { onInProgress(ownTask) },
                onReset = { onReset(ownTask) },
                onSetAlarm = { onSetAlarm(ownTask) },
                modifier = Modifier.padding(start = indent + 8.dp),
                containerOverride = nodeColor,
                borderOverride = nodeBorderColor.copy(alpha = 0.64f)
            )
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (selectedTasks.isNotEmpty()) {
            BatchActionBar(
                count = selectedTasks.size,
                onDone = { onBatchStatus(selectedTasks, TaskStatus.DONE); selectedKeys = emptySet() },
                onInProgress = { onBatchStatus(selectedTasks, TaskStatus.IN_PROGRESS); selectedKeys = emptySet() },
                onCancel = { onBatchStatus(selectedTasks, TaskStatus.CANCELED); selectedKeys = emptySet() },
                onReset = { onBatchStatus(selectedTasks, TaskStatus.NOT_STARTED); selectedKeys = emptySet() },
                onTomorrow = { onBatchMoveTomorrow(selectedTasks); selectedKeys = emptySet() },
                onCustom = { onBatchMoveCustom(selectedTasks); selectedKeys = emptySet() },
                onAlarm = { onBatchAlarm(selectedTasks); selectedKeys = emptySet() },
                onDelete = { onBatchDelete(selectedTasks); selectedKeys = emptySet() },
                onClear = { selectedKeys = emptySet() }
            )
        }

        normalTasks
            .groupBy { it.projectId ?: -1L }
            .toList()
            .sortedWith(
                compareByDescending<Pair<Long, List<TaskItem>>> { (projectId, _) -> projectMap[projectId]?.priority?.weight ?: 0 }
                    .thenBy { (projectId, groupTasks) -> projectMap[projectId]?.name ?: groupTasks.firstOrNull()?.projectName ?: "بدون پروژه" }
            )
            .forEach { (projectId, groupTasksRaw) ->
                val project = projectMap[projectId]
                val groupTasks = groupTasksRaw.sortedWith(compareByDescending<TaskItem> { it.priority.weight }.thenBy { it.createdAt })
                val k = "normal_project_$projectId"
                val expanded = expandableState[k] ?: true
                NormalProjectAccordionCard(
                    title = project?.name ?: groupTasks.firstOrNull()?.projectName ?: "بدون پروژه",
                    subtitle = "${groupTasks.size.toPersianDigits()} تسک معمولی",
                    priority = project?.priority ?: TaskPriority.NORMAL,
                    canEditPriority = project != null,
                    expanded = expanded,
                    selected = groupTasks.any { key(it) in selectedKeys },
                    onToggleExpand = { expandableState[k] = !expanded },
                    onLongSelect = { setSelected(groupTasks) },
                    onPriorityChange = { priority -> if (project != null) onUpdateProjectPriority(project.id, priority) }
                )
                if (expanded) {
                    groupTasks.forEach { task ->
                        TaskCard(
                            task = task,
                            selected = key(task) in selectedKeys,
                            onLongSelect = { setSelected(listOf(task)) },
                            onDone = { onDone(task) },
                            onEdit = { onEdit(task) },
                            onDelete = { onDelete(task) },
                            onMoveTomorrow = { onMoveTomorrow(task) },
                            onMoveCustom = { onMoveCustom(task) },
                            onCancel = { onCancel(task) },
                            onInProgress = { onInProgress(task) },
                            onReset = { onReset(task) },
                            onSetAlarm = { onSetAlarm(task) },
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }

        mindTasks.groupBy { it.projectId ?: -1L }.forEach { (projectId, projectTasks) ->
            val projectName = projectTasks.firstOrNull()?.projectName ?: "بدون پروژه"
            val projectKey = "project_$projectId"
            val expanded = expandableState[projectKey] ?: true
            ProjectAccordionCard(
                title = projectName,
                subtitle = "${projectTasks.size.toPersianDigits()} تسک از مایندمپ",
                tasks = projectTasks,
                expanded = expanded,
                selected = projectTasks.any { key(it) in selectedKeys },
                onToggleExpand = { expandableState[projectKey] = !expanded },
                onToggleDone = { targetStatus -> onSetGroupStatus(projectTasks, targetStatus) },
                onLongSelect = { setSelected(projectTasks) }
            )
            if (expanded) {
                val rootNodes = sortedMindNodes(children[null].orEmpty())
                    .filter { it.projectId == projectId && subtreeTasks(it).isNotEmpty() }
                rootNodes.forEach { root -> RenderNode(root, 1) }
            }
        }
    }
}

private val mindTaskPalette = listOf(
    Color(0xFFFF6B6B), Color(0xFFFF8E72), Color(0xFFFFA94D), Color(0xFFFFC857), Color(0xFFF4E04D),
    Color(0xFFB8DE6F), Color(0xFF7ED957), Color(0xFF4ECDC4), Color(0xFF45B7D1), Color(0xFF5DADE2),
    Color(0xFF74B9FF), Color(0xFF81ECEC), Color(0xFF55EFC4), Color(0xFFA3F7BF), Color(0xFFC7F464),
    Color(0xFFFFF176), Color(0xFFFFD166), Color(0xFFFFB347), Color(0xFFFF9AA2), Color(0xFFFFB7B2),
    Color(0xFFFFDAC1), Color(0xFFE2F0CB), Color(0xFFB5EAD7), Color(0xFFC7CEEA), Color(0xFFE0BBE4),
    Color(0xFFD291BC), Color(0xFFFEC8D8), Color(0xFFFFDFD3), Color(0xFFA0E7E5), Color(0xFFB4F8C8),
    Color(0xFFFBE7C6), Color(0xFFFFAEBC), Color(0xFFA0C4FF), Color(0xFFBDB2FF), Color(0xFFFFC6FF),
    Color(0xFF9BF6FF), Color(0xFFCAFFBF), Color(0xFFFDFFB6), Color(0xFFFFD6A5), Color(0xFFFFADAD),
    Color(0xFFE4C1F9), Color(0xFFD0F4DE), Color(0xFFA9DEF9), Color(0xFFEDE7B1), Color(0xFFFFC09F),
    Color(0xFFCBF3F0), Color(0xFF2EC4B6), Color(0xFFFFBF69), Color(0xFFBDE0FE), Color(0xFFA2D2FF),
    Color(0xFFFFC8DD), Color(0xFFD8E2DC), Color(0xFFFFF1E6), Color(0xFFE5989B), Color(0xFFB5838D),
    Color(0xFF90DBF4), Color(0xFF98F5E1), Color(0xFFF9C74F), Color(0xFFF9844A), Color(0xFF8EECF5)
)

private fun mindMapTaskNodeColor(
    node: MindMapNode,
    level: Int,
    nodeMap: Map<Long, MindMapNode>,
    rootOrder: List<MindMapNode>
): Color {
    var root = node
    while (root.parentId != null) {
        root = nodeMap[root.parentId] ?: break
    }
    val projectRoots = rootOrder.filter { it.projectId == root.projectId }
    val index = projectRoots.indexOfFirst { it.id == root.id }.let { if (it >= 0) it else rootOrder.indexOfFirst { r -> r.id == root.id } }.coerceAtLeast(0)
    val base = mindTaskPalette[index % mindTaskPalette.size]
    return base.darker(0.18f)
}

private fun Color.lighter(amount: Float): Color = mixWith(Color.White, amount)
private fun Color.darker(amount: Float): Color = mixWith(Color.Black, amount)

private fun Color.mixWith(target: Color, amount: Float): Color {
    val a = amount.coerceIn(0f, 1f)
    return Color(
        red = red + (target.red - red) * a,
        green = green + (target.green - green) * a,
        blue = blue + (target.blue - blue) * a,
        alpha = alpha
    )
}

@Composable
private fun BatchActionBar(
    count: Int,
    onDone: () -> Unit,
    onInProgress: () -> Unit,
    onCancel: () -> Unit,
    onReset: () -> Unit,
    onTomorrow: () -> Unit,
    onCustom: () -> Unit,
    onAlarm: () -> Unit,
    onDelete: () -> Unit,
    onClear: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.92f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.32f)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("${count.toPersianDigits()} تسک انتخاب شده", fontWeight = FontWeight.Bold)
                Text("پاک انتخاب", modifier = Modifier.clickable(onClick = onClear), color = MaterialTheme.colorScheme.primary)
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Button(onClick = onDone, modifier = Modifier.weight(1f)) { Text("انجام شد") }
                OutlinedButton(onClick = onInProgress, modifier = Modifier.weight(1f)) { Text("در حال انجام") }
                OutlinedButton(onClick = onReset, modifier = Modifier.weight(1f)) { Text("برگشت") }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedButton(onClick = onTomorrow, modifier = Modifier.weight(1f)) { Text("فردا") }
                OutlinedButton(onClick = onCustom, modifier = Modifier.weight(1f)) { Text("تاریخ") }
                OutlinedButton(onClick = onAlarm, modifier = Modifier.weight(1f)) { Text("آلارم") }
                Button(
                    onClick = onDelete,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5C5C))
                ) { Text("حذف") }
            }
            OutlinedButton(onClick = onCancel, modifier = Modifier.fillMaxWidth()) { Text("لغو برای امروز") }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ParentTaskAccordionCard(
    title: String,
    subtitle: String,
    tasks: List<TaskItem>,
    expanded: Boolean,
    level: Int,
    selected: Boolean,
    modifier: Modifier = Modifier,
    containerColor: Color,
    borderColor: Color,
    onToggleExpand: () -> Unit,
    onToggleDone: (TaskStatus) -> Unit,
    onLongSelect: () -> Unit
) {
    val allDone = tasks.isNotEmpty() && tasks.all { it.status == TaskStatus.DONE }
    val anyDone = tasks.any { it.status == TaskStatus.DONE }
    val check = when {
        allDone -> "✓"
        anyDone -> "◐"
        else -> "○"
    }
    val target = if (allDone) TaskStatus.NOT_STARTED else TaskStatus.DONE
    val baseColor = containerColor

    Card(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onToggleExpand, onLongClick = onLongSelect),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = if (selected) baseColor.darker(0.14f) else baseColor),
        border = BorderStroke(1.dp, if (selected) borderColor.copy(alpha = 0.90f) else borderColor.copy(alpha = 0.58f)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = check,
                modifier = Modifier
                    .clickable(enabled = tasks.isNotEmpty()) { onToggleDone(target) }
                    .padding(horizontal = 4.dp),
                color = Color.Black.copy(alpha = if (allDone) 0.95f else 0.78f),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = title.toPersianDigits(),
                    fontWeight = FontWeight.Bold,
                    color = Color.Black.copy(alpha = 0.90f),
                    textDecoration = if (allDone) TextDecoration.LineThrough else TextDecoration.None,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(subtitle, color = Color.Black.copy(alpha = 0.62f), style = MaterialTheme.typography.bodySmall)
            }
            Text(
                text = if (expanded) "⌄" else "›",
                color = Color.Black.copy(alpha = 0.76f),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
    Spacer(Modifier.height(2.dp))
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun NormalProjectAccordionCard(
    title: String,
    subtitle: String,
    priority: TaskPriority,
    canEditPriority: Boolean,
    expanded: Boolean,
    selected: Boolean,
    onToggleExpand: () -> Unit,
    onLongSelect: () -> Unit,
    onPriorityChange: (TaskPriority) -> Unit
) {
    val priorityColor = when (priority) {
        TaskPriority.URGENT -> Color(0xFFFF6B6B)
        TaskPriority.IMPORTANT -> Color(0xFFFFC857)
        TaskPriority.NORMAL -> MaterialTheme.colorScheme.primary
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onToggleExpand, onLongClick = onLongSelect),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = if (selected) priorityColor.copy(alpha = 0.20f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.56f)),
        border = BorderStroke(1.dp, priorityColor.copy(alpha = if (selected) 0.78f else 0.38f)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
                    Text(title.toPersianDigits(), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleMedium)
                    Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                }
                Text(priority.fa, color = priorityColor, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                Text(if (expanded) "⌄" else "›", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
            if (canEditPriority) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    TaskPriority.entries.forEach { p ->
                        TextButton(
                            onClick = { onPriorityChange(p) },
                            modifier = Modifier.weight(1f)
                        ) { Text(p.fa, color = if (priority == p) priorityColor else MaterialTheme.colorScheme.onSurfaceVariant) }
                    }
                }
            }
        }
    }
    Spacer(Modifier.height(2.dp))
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ProjectAccordionCard(
    title: String,
    subtitle: String,
    tasks: List<TaskItem>,
    expanded: Boolean,
    selected: Boolean,
    onToggleExpand: () -> Unit,
    onToggleDone: (TaskStatus) -> Unit,
    onLongSelect: () -> Unit
) {
    val allDone = tasks.isNotEmpty() && tasks.all { it.status == TaskStatus.DONE }
    val anyDone = tasks.any { it.status == TaskStatus.DONE }
    val check = when {
        allDone -> "✓"
        anyDone -> "◐"
        else -> "○"
    }
    val target = if (allDone) TaskStatus.NOT_STARTED else TaskStatus.DONE
    val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.50f
    val projectTitleColor = if (isDarkTheme) Color.White else Color.Black

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onToggleExpand, onLongClick = onLongSelect),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.20f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.13f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = if (selected) 0.78f else 0.32f)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = check,
                modifier = Modifier
                    .clickable(enabled = tasks.isNotEmpty()) { onToggleDone(target) }
                    .padding(horizontal = 4.dp),
                color = if (allDone) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
                Text(
                    text = title.toPersianDigits(),
                    fontWeight = FontWeight.Bold,
                    color = projectTitleColor,
                    textDecoration = if (allDone) TextDecoration.LineThrough else TextDecoration.None,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(text = subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            }
            Text(
                text = if (expanded) "⌄" else "›",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
    Spacer(Modifier.height(2.dp))
}
