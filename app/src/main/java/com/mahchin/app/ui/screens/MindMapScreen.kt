package com.mahchin.app.ui.screens

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Paint
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.graphics.RectF
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.text.TextDirectionHeuristics
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.FileUpload
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mahchin.app.data.model.MindMapNode
import com.mahchin.app.data.model.Project
import com.mahchin.app.domain.JalaliCalendar
import com.mahchin.app.domain.JalaliDate
import com.mahchin.app.domain.toEnglishDigits
import com.mahchin.app.domain.toPersianDigits
import com.mahchin.app.ui.components.VoiceOutlinedTextField
import com.mahchin.app.ui.viewmodel.MainViewModel
import java.io.File
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MindMapScreen(vm: MainViewModel) {
    val projects by vm.projects.collectAsState()
    val selectedProjectId by vm.selectedProjectId.collectAsState()
    val nodes by vm.mindMapNodes.collectAsState()
    val allMindMapNodes by vm.allMindMapNodes.collectAsState()
    val selectedProject = projects.firstOrNull { it.id == selectedProjectId } ?: projects.firstOrNull()
    val context = LocalContext.current
    val mindMapAiBusy by vm.mindMapAiBusy.collectAsState()
    var pendingPaperMindMapCameraUri by remember { mutableStateOf<Uri?>(null) }
    val paperMindMapCameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        val uri = pendingPaperMindMapCameraUri
        if (success && uri != null) {
            val bitmap = loadBitmapFromUri(context, uri)
            if (bitmap != null) vm.importMindMapFromPaperImage(context, bitmap)
            else Toast.makeText(context, "عکس دوربین خوانده نشد.", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(context, "عکسی ثبت نشد.", Toast.LENGTH_SHORT).show()
        }
        pendingPaperMindMapCameraUri = null
    }
    val paperMindMapGalleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            val bitmap = loadBitmapFromUri(context, uri)
            if (bitmap != null) vm.importMindMapFromPaperImage(context, bitmap)
            else Toast.makeText(context, "عکس گالری خوانده نشد.", Toast.LENGTH_LONG).show()
        }
    }
    var projectMenu by remember { mutableStateOf(false) }
    var addProjectDialog by remember { mutableStateOf(false) }
    var editProjectDialog by remember { mutableStateOf(false) }
    var deleteProjectDialog by remember { mutableStateOf(false) }
    var projectActions by remember { mutableStateOf(false) }
    var nodeDialog by remember { mutableStateOf<NodeDialogState?>(null) }
    var distributeDialog by remember { mutableStateOf(false) }
    var actionNode by remember { mutableStateOf<MindMapNode?>(null) }
    var centerActions by remember { mutableStateOf(false) }
    var selectedNodeId by remember(selectedProjectId) { mutableStateOf<Long?>(null) }
    var openedProjectId by remember { mutableStateOf<Long?>(null) }
    var projectSearch by remember { mutableStateOf("") }
    var deleteProjectTarget by remember { mutableStateOf<Project?>(null) }
    var projectListActionsTarget by remember { mutableStateOf<Project?>(null) }
    var pendingMindMapBackupProject by remember { mutableStateOf<Project?>(null) }
    val mindMapBackupLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        pendingMindMapBackupProject?.let { project -> vm.exportMindMapBackupToUri(context, uri, project.id) }
        pendingMindMapBackupProject = null
    }
    val mindMapRestoreLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        vm.restoreMindMapBackupFromUri(context, uri)
        openedProjectId = null
        selectedNodeId = null
    }
    val selectedNode = nodes.firstOrNull { it.id == selectedNodeId && it.isActive }
    val openedProject = projects.firstOrNull { it.id == openedProjectId } ?: selectedProject

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (openedProjectId == null) {
            MindMapProjectListScreen(
                projects = projects,
                allNodes = allMindMapNodes,
                searchQuery = projectSearch,
                onSearchChange = { projectSearch = it },
                onAddProject = { addProjectDialog = true },
                onOpenProject = { project ->
                    vm.selectProject(project.id)
                    selectedNodeId = null
                    openedProjectId = project.id
                },
                onProjectLongPress = { project -> projectListActionsTarget = project },
                onDeleteProject = { project -> deleteProjectTarget = project },
                onRestoreBackup = { mindMapRestoreLauncher.launch(arrayOf("application/json", "text/*", "application/octet-stream")) }
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(onClick = {
                        openedProjectId = null
                        selectedNodeId = null
                    }) {
                        Icon(Icons.Outlined.ArrowBack, contentDescription = "بازگشت")
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(openedProject?.name ?: "مایندمپ", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(
                            "فضای اختصاصی نقشه ذهنی؛ افزودن شاخه، زیرشاخه، زوم و تسک‌سازی.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    OutlinedButton(
                        onClick = { projectActions = true },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.height(44.dp)
                    ) { Text("پروژه") }
                    OutlinedButton(
                        onClick = { distributeDialog = true },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.height(44.dp)
                    ) { Text("تسک‌سازی") }
                }

                XMindLikeCanvasCard(
                    projectTitle = openedProject?.name ?: "پروژه",
                    nodes = nodes,
                    selectedNodeId = selectedNodeId,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    onNodeTap = { node -> selectedNodeId = node.id },
                    onCenterTap = { selectedNodeId = null },
                    onNodeLongPress = { actionNode = it },
                    onCenterLongPress = { centerActions = true },
                    onEmptyTap = { selectedNodeId = null },
                    onNodeMove = { node, x, y -> vm.moveMindMapNode(node.id, x, y) },
                    isImportingPaperMindMap = mindMapAiBusy,
                    onImportFromCamera = {
                        runCatching {
                            val uri = createMindMapCameraImageUri(context)
                            pendingPaperMindMapCameraUri = uri
                            paperMindMapCameraLauncher.launch(uri)
                        }.onFailure {
                            pendingPaperMindMapCameraUri = null
                            Toast.makeText(
                                context,
                                "دوربین گوشی باز نشد. دسترسی یا برنامه دوربین را بررسی کنید.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    },
                    onImportFromGallery = {
                        runCatching { paperMindMapGalleryLauncher.launch("image/*") }
                            .onFailure {
                                Toast.makeText(context, "گالری باز نشد.", Toast.LENGTH_LONG).show()
                            }
                    }
                )
            }

            MindMapBottomBar(
                selectedNode = selectedNode,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(12.dp),
                onAddRoot = { nodeDialog = NodeDialogState(parentId = null) },
                onAddChild = { selectedNode?.let { nodeDialog = NodeDialogState(parentId = it.id) } },
                onEditSelected = { selectedNode?.let { nodeDialog = NodeDialogState(editNode = it, parentId = it.parentId) } },
                onDistribute = { distributeDialog = true }
            )
        }
    }

    if (addProjectDialog) {
        ProjectEditorDialog(
            title = "پروژه جدید",
            initialName = "",
            onDismiss = { addProjectDialog = false },
            onSave = {
                vm.addProject(it)
                selectedNodeId = null
                addProjectDialog = false
            }
        )
    }

    if (editProjectDialog && selectedProject != null) {
        ProjectEditorDialog(
            title = "ویرایش پروژه",
            initialName = selectedProject.name,
            onDismiss = { editProjectDialog = false },
            onSave = {
                vm.updateProject(selectedProject.id, it)
                editProjectDialog = false
            }
        )
    }

    deleteProjectTarget?.let { project ->
        AlertDialog(
            onDismissRequest = { deleteProjectTarget = null },
            title = { Text("حذف پروژه") },
            text = { Text("آیا می‌خواهید مایندمپ «${project.name}» حذف شود؟") },
            confirmButton = {
                Button(
                    onClick = {
                        vm.deleteProject(project.id)
                        if (openedProjectId == project.id) openedProjectId = null
                        selectedNodeId = null
                        deleteProjectTarget = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5C5C))
                ) { Text("حذف") }
            },
            dismissButton = { TextButton(onClick = { deleteProjectTarget = null }) { Text("انصراف") } }
        )
    }

    nodeDialog?.let { state ->
        NodeEditorDialog(
            state = state,
            onDismiss = { nodeDialog = null },
            onSave = { title, desc ->
                if (state.editNode == null) {
                    vm.addMindMapNode(state.parentId, title, desc)
                } else {
                    vm.updateMindMapNode(state.editNode.id, title, desc)
                }
                nodeDialog = null
            }
        )
    }

    if (distributeDialog) {
        DistributeMindMapDialog(
            initialDate = vm.today,
            onDismiss = { distributeDialog = false },
            onSave = { date, perDay ->
                vm.makeTasksFromMindMap(date, perDay)
                distributeDialog = false
            }
        )
    }

    if (projectActions) {
        MindMapActionSheet(
            title = selectedProject?.name ?: "پروژه",
            subtitle = "مدیریت پروژه فعلی؛ بدون گزینه‌های بکاپ و تصویر برای جلوگیری از شلوغی صفحه مایندمپ.",
            onDismiss = { projectActions = false },
            actions = listOf(
                MindAction("افزودن پروژه", Icons.Outlined.Add) {
                    projectActions = false
                    addProjectDialog = true
                },
                MindAction("ویرایش پروژه فعلی", Icons.Outlined.Edit) {
                    projectActions = false
                    editProjectDialog = selectedProject != null
                },
                MindAction("حذف پروژه فعلی", Icons.Outlined.Delete, danger = true) {
                    projectActions = false
                    deleteProjectTarget = openedProject ?: selectedProject
                }
            )
        )
    }

    if (centerActions) {
        MindMapActionSheet(
            title = selectedProject?.name ?: "پروژه",
            subtitle = "نود مرکزی پروژه است. شاخه اصلی از همین‌جا شروع می‌شود.",
            onDismiss = { centerActions = false },
            actions = listOf(
                MindAction("افزودن شاخه اصلی", Icons.Outlined.Add) {
                    centerActions = false
                    nodeDialog = NodeDialogState(parentId = null)
                }
            )
        )
    }

    projectListActionsTarget?.let { project ->
        val projectNodes = allMindMapNodes.filter { it.projectId == project.id && it.isActive }
        MindMapActionSheet(
            title = project.name,
            subtitle = "گزینه‌های این مایندمپ؛ تصویر، اشتراک، بکاپ یا حذف.",
            onDismiss = { projectListActionsTarget = null },
            actions = listOf(
                MindAction("ذخیره تصویر مایندمپ در گالری", Icons.Outlined.Image) {
                    projectListActionsTarget = null
                    runCatching {
                        saveMindMapImageToGallery(context, project.name, projectNodes)
                    }.onSuccess {
                        Toast.makeText(context, "تصویر مایندمپ در گالری ذخیره شد.", Toast.LENGTH_SHORT).show()
                    }.onFailure {
                        Toast.makeText(context, "خطا در ذخیره تصویر: ${it.message ?: "نامشخص"}", Toast.LENGTH_LONG).show()
                    }
                },
                MindAction("اشتراک تصویر مایندمپ", Icons.Outlined.Share) {
                    projectListActionsTarget = null
                    runCatching {
                        shareMindMapImage(context, project.name, projectNodes)
                    }.onFailure {
                        Toast.makeText(context, "خطا در اشتراک تصویر: ${it.message ?: "نامشخص"}", Toast.LENGTH_LONG).show()
                    }
                },
                MindAction("بکاپ این مایندمپ", Icons.Outlined.FileDownload) {
                    projectListActionsTarget = null
                    pendingMindMapBackupProject = project
                    mindMapBackupLauncher.launch("mahchin_mindmap_${safeBackupFileName(project.name)}.json")
                },
                MindAction("حذف", Icons.Outlined.Delete, danger = true) {
                    projectListActionsTarget = null
                    deleteProjectTarget = project
                }
            )
        )
    }

    actionNode?.let { node ->
        MindMapActionSheet(
            title = node.title,
            subtitle = if (node.description.isBlank()) "گزینه‌های این نود" else node.description,
            onDismiss = { actionNode = null },
            actions = listOf(
                MindAction("افزودن زیرشاخه", Icons.Outlined.Add) {
                    actionNode = null
                    selectedNodeId = node.id
                    nodeDialog = NodeDialogState(parentId = node.id)
                },
                MindAction("افزودن شاخه کنار این", Icons.Outlined.Add) {
                    actionNode = null
                    nodeDialog = NodeDialogState(parentId = node.parentId)
                },
                MindAction("ویرایش", Icons.Outlined.Edit) {
                    actionNode = null
                    nodeDialog = NodeDialogState(editNode = node, parentId = node.parentId)
                },
                MindAction("حذف", Icons.Outlined.Delete, danger = true) {
                    actionNode = null
                    if (selectedNodeId == node.id) selectedNodeId = null
                    vm.deleteMindMapNode(node.id)
                }
            )
        )
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MindMapProjectListScreen(
    projects: List<Project>,
    allNodes: List<MindMapNode>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onAddProject: () -> Unit,
    onOpenProject: (Project) -> Unit,
    onProjectLongPress: (Project) -> Unit,
    onDeleteProject: (Project) -> Unit,
    onRestoreBackup: () -> Unit
) {
    val activeNodes = allNodes.filter { it.isActive }
    val nodeCountByProject = activeNodes.groupingBy { it.projectId }.eachCount()
    val filteredProjects = remember(projects, activeNodes, searchQuery) {
        val q = searchQuery.trim()
        if (q.isBlank()) {
            projects
        } else {
            projects.filter { project ->
                project.name.contains(q, ignoreCase = true) ||
                    activeNodes.any { it.projectId == project.id && it.title.contains(q, ignoreCase = true) }
            }.sortedWith(
                compareBy<Project> { project ->
                    when {
                        project.name.equals(q, ignoreCase = true) -> 0
                        project.name.startsWith(q, ignoreCase = true) -> 1
                        project.name.contains(q, ignoreCase = true) -> 2
                        activeNodes.any { it.projectId == project.id && it.title.equals(q, ignoreCase = true) } -> 3
                        activeNodes.any { it.projectId == project.id && it.title.startsWith(q, ignoreCase = true) } -> 4
                        else -> 5
                    }
                }.thenByDescending { it.updatedAt }
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("مایندمپ‌ها", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(
                    "لیست پروژه‌های مایندمپ؛ جستجو کن یا روی هر پروژه بزن تا وارد نقشه شوی.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text("جستجو در مایندمپ‌ها") },
            placeholder = { Text("نام پروژه یا عنوان نود") }
        )

        OutlinedButton(
            onClick = onRestoreBackup,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(18.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.65f))
        ) {
            Icon(Icons.Outlined.FileUpload, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("بازگردانی بکاپ مایندمپ")
        }

        Text(
            "${filteredProjects.size.toPersianDigits()} پروژه از ${projects.size.toPersianDigits()} پروژه",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filteredProjects, key = { it.id }) { project ->
                val count = nodeCountByProject[project.id] ?: 0
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .combinedClickable(
                            onClick = { onOpenProject(project) },
                            onLongClick = { onProjectLongPress(project) }
                        ),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                project.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                "${count.toPersianDigits()} نود",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        IconButton(onClick = { onDeleteProject(project) }) {
                            Icon(
                                Icons.Outlined.Delete,
                                contentDescription = "حذف",
                                tint = Color(0xFFFF6B6B)
                            )
                        }
                    }
                }
            }
        }

        Button(
            onClick = onAddProject,
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp),
            shape = RoundedCornerShape(22.dp)
        ) {
            Icon(Icons.Outlined.Add, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("ساخت مایندمپ جدید", fontWeight = FontWeight.Bold)
        }
    }
}

data class NodeDialogState(val editNode: MindMapNode? = null, val parentId: Long? = null)

private data class GraphNode(
    val node: MindMapNode?,
    val title: String,
    val center: Offset,
    val width: Float,
    val height: Float,
    val color: Color,
    val textColor: Color,
    val level: Int,
    val side: Int,
    val fontSize: Float,
    val horizontalPadding: Float,
    val verticalPadding: Float,
    val selected: Boolean = false
)

private data class GraphLine(val from: Offset, val to: Offset, val color: Color, val side: Int, val angle: Float = 0f)
private data class MindGraphLayout(val nodes: List<GraphNode>, val lines: List<GraphLine>)

@Composable
private fun XMindLikeCanvasCard(
    projectTitle: String,
    nodes: List<MindMapNode>,
    selectedNodeId: Long?,
    modifier: Modifier,
    onNodeTap: (MindMapNode) -> Unit,
    onCenterTap: () -> Unit,
    onNodeLongPress: (MindMapNode) -> Unit,
    onCenterLongPress: () -> Unit,
    onEmptyTap: () -> Unit,
    onNodeMove: (MindMapNode, Float, Float) -> Unit,
    isImportingPaperMindMap: Boolean,
    onImportFromCamera: () -> Unit,
    onImportFromGallery: () -> Unit
) {
    val primary = MaterialTheme.colorScheme.primary
    val onPrimary = MaterialTheme.colorScheme.onPrimary
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val density = LocalDensity.current

    // بازنویسی کامل: زوم اولیه خوانا، بوم آزاد و چیدمان خودکار شبیه XMind.
    var scale by remember { mutableFloatStateOf(0.54f) }
    var pan by remember { mutableStateOf(Offset.Zero) }
    var manualPositions by remember { mutableStateOf<Map<Long, Offset>>(emptyMap()) }
    val layoutSignature = remember(nodes) {
        nodes.filter { it.isActive }
            .sortedBy { it.id }
            .joinToString("|") { node ->
                "${node.id}:${node.parentId}:${node.orderIndex}:${node.title}:${node.x}:${node.y}"
            }
    }
    LaunchedEffect(layoutSignature) {
        // وقتی هنگام ساخت مایندمپ نود جدید اضافه/حذف/ویرایش می‌شود،
        // کش موقت جابه‌جایی نودها باید با دیتابیس هماهنگ شود؛
        // وگرنه تا قبل از بستن و باز کردن برنامه، بعضی نودها روی هم می‌افتند یا متن‌ها موقع زوم می‌لرزند.
        manualPositions = nodes
            .filter { it.isActive && it.x != null && it.y != null }
            .associate { it.id to Offset(it.x!!, it.y!!) }
    }

    Card(
        modifier = modifier.padding(bottom = 82.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF071321)),
        border = BorderStroke(1.dp, Color(0xFF223244).copy(alpha = 0.72f)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val w = with(density) { maxWidth.toPx() }
            val h = with(density) { maxHeight.toPx() }
            val origin = Offset(w / 2f, h / 2f) + pan
            val pixelScale = density.density.coerceAtLeast(1f)
            val graph = remember(projectTitle, nodes, selectedNodeId, manualPositions, primary, onPrimary, onSurface, pixelScale) {
                buildXMindGraph(
                    projectTitle = projectTitle,
                    allNodes = nodes,
                    selectedNodeId = selectedNodeId,
                    primary = primary,
                    onPrimary = onPrimary,
                    onSurface = onSurface,
                    pixelScale = pixelScale,
                    manualPositions = manualPositions
                )
            }

            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(graph, scale, pan) {
                        var draggingNode: GraphNode? = null
                        detectDragGestures(
                            onDragStart = { start ->
                                val worldStart = screenToWorld(start, origin, scale)
                                draggingNode = graph.nodes.asReversed().firstOrNull { it.node != null && it.hit(worldStart) }
                                draggingNode?.node?.let { onNodeTap(it) }
                            },
                            onDragEnd = {
                                draggingNode?.let { g ->
                                    val node = g.node
                                    if (node != null) {
                                        val finalPos = manualPositions[node.id] ?: g.center
                                        onNodeMove(node, finalPos.x, finalPos.y)
                                    }
                                }
                                draggingNode = null
                            },
                            onDragCancel = { draggingNode = null },
                            onDrag = { change, dragAmount ->
                                val node = draggingNode?.node
                                if (node != null) {
                                    change.consume()
                                    val current = manualPositions[node.id] ?: draggingNode!!.center
                                    val next = current + Offset(dragAmount.x / scale, dragAmount.y / scale)
                                    manualPositions = manualPositions + (node.id to next)
                                }
                            }
                        )
                    }
                    .pointerInput(Unit) {
                        detectTransformGestures { _, panChange, zoomChange, _ ->
                            pan += panChange
                            scale = (scale * zoomChange).coerceIn(0.08f, 3.6f)
                        }
                    }
                    .pointerInput(graph, scale, pan) {
                        detectTapGestures(
                            onTap = { tap ->
                                val worldTap = screenToWorld(tap, origin, scale)
                                val hit = graph.nodes.asReversed().firstOrNull { it.hit(worldTap) }
                                when {
                                    hit?.node != null -> onNodeTap(hit.node)
                                    hit != null -> onCenterTap()
                                    else -> onEmptyTap()
                                }
                            },
                            onLongPress = { tap ->
                                val worldTap = screenToWorld(tap, origin, scale)
                                val hit = graph.nodes.asReversed().firstOrNull { it.hit(worldTap) }
                                when {
                                    hit?.node != null -> onNodeLongPress(hit.node)
                                    hit != null -> onCenterLongPress()
                                    else -> onEmptyTap()
                                }
                            }
                        )
                    }
            ) {
                drawRect(Color(0xFF071321))
                drawCircle(
                    color = Color(0xFF13243A).copy(alpha = 0.42f),
                    radius = size.maxDimension * 0.68f,
                    center = Offset(size.width / 2f, size.height / 2f)
                )

                graph.lines.forEach { line ->
                    val start = worldToScreen(line.from, origin, scale)
                    val end = worldToScreen(line.to, origin, scale)
                    val side = if (end.x >= start.x) 1f else -1f
                    val dx = abs(end.x - start.x)
                    val control = dx.coerceIn(90f * scale, 260f * scale)
                    val c1 = Offset(start.x + side * control, start.y)
                    val c2 = Offset(end.x - side * control, end.y)
                    val path = Path().apply {
                        moveTo(start.x, start.y)
                        cubicTo(c1.x, c1.y, c2.x, c2.y, end.x, end.y)
                    }
                    drawPath(
                        path = path,
                        color = line.color.copy(alpha = 0.90f),
                        style = Stroke(width = (if (line.side == 0) 4.2f else 3.4f) * scale.coerceIn(0.55f, 1.15f))
                    )
                }

                graph.nodes.forEach { graphNode ->
                    val center = worldToScreen(graphNode.center, origin, scale)
                    val nodeW = graphNode.width * scale
                    val nodeH = graphNode.height * scale
                    val topLeft = Offset(center.x - nodeW / 2f, center.y - nodeH / 2f)
                    val radiusValue = when (graphNode.level) {
                        0 -> 24f
                        1 -> 20f
                        else -> 18f
                    } * scale
                    val radius = CornerRadius(radiusValue, radiusValue)

                    drawRoundRect(
                        color = Color.Black.copy(alpha = if (graphNode.level == 0) 0.24f else 0.13f),
                        topLeft = topLeft + Offset(0f, 7f * scale),
                        size = Size(nodeW, nodeH),
                        cornerRadius = radius
                    )

                    if (graphNode.selected) {
                        drawRoundRect(
                            color = Color(0xFFFFD166).copy(alpha = 0.90f),
                            topLeft = topLeft - Offset(5f * scale, 5f * scale),
                            size = Size(nodeW + 10f * scale, nodeH + 10f * scale),
                            cornerRadius = CornerRadius(radiusValue + 5f * scale, radiusValue + 5f * scale),
                            style = Stroke(width = 3.3f * scale)
                        )
                    }

                    val fillColor = when (graphNode.level) {
                        0 -> Color.White
                        1 -> graphNode.color.copy(alpha = 0.99f)
                        2 -> graphNode.color.copy(alpha = 0.94f)
                        else -> graphNode.color.copy(alpha = 0.88f)
                    }
                    drawRoundRect(
                        color = fillColor,
                        topLeft = topLeft,
                        size = Size(nodeW, nodeH),
                        cornerRadius = radius
                    )

                    val textSize = graphNode.fontSize * pixelScale * scale
                    val horizontalPadding = graphNode.horizontalPadding * pixelScale * scale
                    val verticalPadding = graphNode.verticalPadding * pixelScale * scale
                    // متن دقیقاً داخل محدوده خود نود رسم می‌شود و از کادر بیرون نمی‌زند.
                    // فاصله داخلی همان مقدار قبلی است؛ فقط روش اندازه‌گیری/رسم متن اصلاح شده.
                    drawContext.canvas.nativeCanvas.drawXMindRtlTextInsideNode(
                        text = graphNode.title,
                        level = graphNode.level,
                        left = topLeft.x + horizontalPadding,
                        top = topLeft.y + verticalPadding,
                        right = topLeft.x + nodeW - horizontalPadding,
                        bottom = topLeft.y + nodeH - verticalPadding,
                        color = graphNode.textColor.toArgb(),
                        textSize = textSize,
                        bold = graphNode.level <= 1
                    )
                }
            }

            Column(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                Text(
                    "فضای آزاد مایندمپ",
                    color = onSurfaceVariant,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    "لمس نود = انتخاب  •  نگه‌داشتن = منو  •  دو انگشت = زوم",
                    color = onSurfaceVariant.copy(alpha = 0.82f),
                    style = MaterialTheme.typography.labelSmall
                )
                val nodeCounts = remember(nodes) { mindMapNodeCounts(nodes) }
                Text(
                    "نودها: کل ${nodeCounts.total.toPersianDigits()}  •  مرحله۱ ${nodeCounts.level1.toPersianDigits()}  •  مرحله۲ ${nodeCounts.level2.toPersianDigits()}  •  مرحله۳+ ${nodeCounts.level3Plus.toPersianDigits()}",
                    color = Color(0xFF7FF7EA),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold
                )
                val selectedInfoNode = nodes.firstOrNull { it.id == selectedNodeId && it.isActive }
                if (selectedInfoNode != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0C3C3B).copy(alpha = 0.92f)),
                        border = BorderStroke(1.dp, Color(0xFFD4AF37).copy(alpha = 0.55f)),
                        elevation = CardDefaults.cardElevation(0.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Text(
                                "انتخاب: ${selectedInfoNode.title}",
                                color = Color(0xFF7FF7EA),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (selectedInfoNode.description.isNotBlank()) {
                                Text(
                                    selectedInfoNode.description,
                                    color = Color.White.copy(alpha = 0.74f),
                                    style = MaterialTheme.typography.labelSmall,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(onClick = { scale = 0.54f; pan = Offset.Zero; manualPositions = emptyMap() }, shape = RoundedCornerShape(14.dp)) { Text("مرکز") }
                Button(onClick = { scale = (scale * 1.15f).coerceAtMost(3.6f) }, shape = RoundedCornerShape(14.dp)) { Text("+") }
                Button(onClick = { scale = (scale / 1.15f).coerceAtLeast(0.08f) }, shape = RoundedCornerShape(14.dp)) { Text("−") }
            }

            if (nodes.none { it.isActive }) {
                Card(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(18.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.94f)),
                    border = BorderStroke(1.dp, Color(0xFFD4AF37).copy(alpha = 0.55f)),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("مایندمپ خالی است", color = Color.Black, fontWeight = FontWeight.Bold)
                        Text(
                            "می‌توانی دستی شاخه بسازی یا از مایندمپ کاغذی عکس بگیری تا با هوش مصنوعی ساخته شود.",
                            color = Color.Black.copy(alpha = 0.72f),
                            style = MaterialTheme.typography.bodySmall
                        )
                        if (isImportingPaperMindMap) {
                            Button(
                                onClick = {},
                                enabled = false,
                                shape = RoundedCornerShape(18.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD4AF37), contentColor = Color.Black)
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = Color.Black)
                                Spacer(Modifier.width(8.dp))
                                Text("در حال تحلیل عکس...")
                            }
                        } else {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = onImportFromCamera,
                                    shape = RoundedCornerShape(18.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD4AF37), contentColor = Color.Black)
                                ) {
                                    Icon(Icons.Outlined.Image, contentDescription = null)
                                    Spacer(Modifier.width(8.dp))
                                    Text("دوربین")
                                }
                                OutlinedButton(
                                    onClick = onImportFromGallery,
                                    shape = RoundedCornerShape(18.dp),
                                    border = BorderStroke(1.dp, Color(0xFFD4AF37))
                                ) {
                                    Icon(Icons.Outlined.FileUpload, contentDescription = null, tint = Color.Black)
                                    Spacer(Modifier.width(8.dp))
                                    Text("گالری", color = Color.Black)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MindMapBottomBar(
    selectedNode: MindMapNode?,
    modifier: Modifier,
    onAddRoot: () -> Unit,
    onAddChild: () -> Unit,
    onEditSelected: () -> Unit,
    onDistribute: () -> Unit
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onAddRoot,
                modifier = Modifier.weight(1f).height(48.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                ToolbarButtonText("+", large = true)
            }
            Button(
                onClick = onAddChild,
                enabled = selectedNode != null,
                modifier = Modifier.weight(1f).height(48.dp),
                shape = RoundedCornerShape(18.dp)
            ) {
                ToolbarButtonText("++", large = true)
            }
            OutlinedButton(
                onClick = onEditSelected,
                enabled = selectedNode != null,
                modifier = Modifier.weight(0.85f).height(48.dp),
                shape = RoundedCornerShape(18.dp)
            ) { ToolbarButtonText("ویرایش") }
            OutlinedButton(
                onClick = onDistribute,
                modifier = Modifier.weight(0.95f).height(48.dp),
                shape = RoundedCornerShape(18.dp)
            ) { ToolbarButtonText("تسک") }
        }
    }
}

@Composable
private fun ToolbarButtonText(text: String, large: Boolean = false) {
    Text(
        text = text,
        maxLines = 1,
        softWrap = false,
        overflow = TextOverflow.Clip,
        fontSize = if (large) 24.sp else 11.sp,
        fontWeight = FontWeight.Bold
    )
}

private fun buildXMindGraph(
    projectTitle: String,
    allNodes: List<MindMapNode>,
    selectedNodeId: Long?,
    primary: Color,
    onPrimary: Color,
    onSurface: Color,
    pixelScale: Float,
    manualPositions: Map<Long, Offset>
): MindGraphLayout {
    val activeNodes = allNodes.filter { it.isActive }
    val children = activeNodes.groupBy { it.parentId }
    val graphNodes = mutableListOf<GraphNode>()

    val palette = listOf(
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

    val centerStyle = nodeStyle(0)
    val centerWidth = nodeWidth(projectTitle, 0, pixelScale)
    val centerHeight = nodeHeight(projectTitle, 0, pixelScale)
    graphNodes += GraphNode(
        node = null,
        title = projectTitle.ifBlank { "پروژه" },
        center = Offset.Zero,
        width = centerWidth,
        height = centerHeight,
        color = Color.White,
        textColor = Color.Black,
        level = 0,
        side = 0,
        fontSize = centerStyle.fontSize,
        horizontalPadding = centerStyle.horizontalPadding,
        verticalPadding = centerStyle.verticalPadding,
        selected = selectedNodeId == null
    )

    val roots = children[null].orEmpty().sortedWith(compareBy({ it.orderIndex }, { it.createdAt }))
    if (roots.isEmpty()) return MindGraphLayout(graphNodes, emptyList())

    val rightRoots = mutableListOf<MindMapNode>()
    val leftRoots = mutableListOf<MindMapNode>()
    var rightWeight = 0f
    var leftWeight = 0f
    roots.forEachIndexed { index, root ->
        val weight = branchBlockHeight(root, 1, children, pixelScale)
        if (index == 0 || rightWeight <= leftWeight) {
            rightRoots += root
            rightWeight += weight
        } else {
            leftRoots += root
            leftWeight += weight
        }
    }

    layoutBalancedRootSide(rightRoots, 1, 0, children, selectedNodeId, palette, graphNodes, centerWidth, pixelScale, manualPositions)
    layoutBalancedRootSide(leftRoots, -1, 1, children, selectedNodeId, palette, graphNodes, centerWidth, pixelScale, manualPositions)

    val byId = graphNodes.mapNotNull { g -> g.node?.id?.let { it to g } }.toMap()
    val lines = mutableListOf<GraphLine>()
    activeNodes.forEach { node ->
        val target = byId[node.id] ?: return@forEach
        val parentGraph = node.parentId?.let { byId[it] } ?: graphNodes.first()
        addGraphLine(
            lines = lines,
            fromCenter = parentGraph.center,
            fromWidth = parentGraph.width,
            fromHeight = parentGraph.height,
            toCenter = target.center,
            toWidth = target.width,
            toHeight = target.height,
            color = rootFamilyColor(node, activeNodes, children, palette),
            lineSide = target.side
        )
    }
    return MindGraphLayout(graphNodes, lines)
}

private data class MindMapNodeCounts(val total: Int, val level1: Int, val level2: Int, val level3Plus: Int)

private fun mindMapNodeCounts(nodes: List<MindMapNode>): MindMapNodeCounts {
    val active = nodes.filter { it.isActive }
    if (active.isEmpty()) return MindMapNodeCounts(0, 0, 0, 0)
    val children = active.groupBy { it.parentId }
    var level1 = 0
    var level2 = 0
    var level3Plus = 0
    fun visit(node: MindMapNode, level: Int) {
        when (level) {
            1 -> level1++
            2 -> level2++
            else -> level3Plus++
        }
        children[node.id].orEmpty().forEach { visit(it, level + 1) }
    }
    children[null].orEmpty().forEach { visit(it, 1) }
    return MindMapNodeCounts(active.size, level1, level2, level3Plus)
}

private data class NodeStyle(val fontSize: Float, val horizontalPadding: Float, val verticalPadding: Float)

private fun nodeStyle(level: Int): NodeStyle = when (level) {
    0 -> NodeStyle(fontSize = 84f, horizontalPadding = 34f, verticalPadding = 24f)
    1 -> NodeStyle(fontSize = 42f, horizontalPadding = 24f, verticalPadding = 16f)
    2 -> NodeStyle(fontSize = 35f, horizontalPadding = 20f, verticalPadding = 13f)
    else -> NodeStyle(fontSize = 30f, horizontalPadding = 18f, verticalPadding = 11f)
}

private fun layoutBalancedRootSide(
    roots: List<MindMapNode>,
    side: Int,
    colorOffset: Int,
    children: Map<Long?, List<MindMapNode>>,
    selectedNodeId: Long?,
    palette: List<Color>,
    graphNodes: MutableList<GraphNode>,
    centerWidth: Float,
    pixelScale: Float,
    manualPositions: Map<Long, Offset>
) {
    if (roots.isEmpty()) return
    val centerGap = 210f * pixelScale
    val totalHeight = roots.sumOf { branchBlockHeight(it, 1, children, pixelScale).toDouble() }.toFloat() +
        (roots.size - 1).coerceAtLeast(0) * rootVerticalGap(pixelScale)
    var cursor = -totalHeight / 2f

    roots.forEachIndexed { index, root ->
        val blockHeight = branchBlockHeight(root, 1, children, pixelScale)
        val rootWidth = nodeWidth(root.title, 1, pixelScale)
        val rootHeight = nodeHeight(root.title, 1, pixelScale)
        val x = side * (centerWidth / 2f + centerGap + rootWidth / 2f)
        val autoPosition = Offset(x, cursor + blockHeight / 2f)
        val position = manualPositions[root.id] ?: root.savedMindMapOffset() ?: autoPosition
        val color = palette[(index * 2 + colorOffset) % palette.size]
        val style = nodeStyle(1)
        graphNodes += GraphNode(root, root.title, position, rootWidth, rootHeight, color, Color.Black, 1, side, style.fontSize, style.horizontalPadding, style.verticalPadding, selectedNodeId == root.id)
        layoutChildrenStrictTree(root, position, rootWidth, side, 2, children, color, selectedNodeId, graphNodes, pixelScale, manualPositions)
        cursor += blockHeight + rootVerticalGap(pixelScale)
    }
}

private fun layoutChildrenStrictTree(
    parent: MindMapNode,
    parentPosition: Offset,
    parentWidth: Float,
    side: Int,
    level: Int,
    children: Map<Long?, List<MindMapNode>>,
    branchColor: Color,
    selectedNodeId: Long?,
    graphNodes: MutableList<GraphNode>,
    pixelScale: Float,
    manualPositions: Map<Long, Offset>
) {
    val directChildren = children[parent.id].orEmpty().filter { it.isActive }.sortedWith(compareBy({ it.orderIndex }, { it.createdAt }))
    if (directChildren.isEmpty()) return
    val totalHeight = directChildren.sumOf { branchBlockHeight(it, level, children, pixelScale).toDouble() }.toFloat() +
        (directChildren.size - 1).coerceAtLeast(0) * childVerticalGap(level, pixelScale)
    var cursor = parentPosition.y - totalHeight / 2f

    directChildren.forEach { child ->
        val blockHeight = branchBlockHeight(child, level, children, pixelScale)
        val width = nodeWidth(child.title, level, pixelScale)
        val height = nodeHeight(child.title, level, pixelScale)
        val x = parentPosition.x + side * (parentWidth / 2f + horizontalGap(level, pixelScale) + width / 2f)
        val autoPosition = Offset(x, cursor + blockHeight / 2f)
        val position = manualPositions[child.id] ?: child.savedMindMapOffset() ?: autoPosition
        val style = nodeStyle(level)
        graphNodes += GraphNode(
            child,
            child.title,
            position,
            width,
            height,
            branchColor.copy(alpha = when (level) { 2 -> 0.93f; 3 -> 0.84f; else -> 0.74f }),
            Color.Black,
            level,
            side,
            style.fontSize,
            style.horizontalPadding,
            style.verticalPadding,
            selectedNodeId == child.id
        )
        layoutChildrenStrictTree(child, position, width, side, level + 1, children, branchColor, selectedNodeId, graphNodes, pixelScale, manualPositions)
        cursor += blockHeight + childVerticalGap(level, pixelScale)
    }
}

private fun branchBlockHeight(node: MindMapNode, level: Int, children: Map<Long?, List<MindMapNode>>, pixelScale: Float): Float {
    val own = nodeHeight(node.title, level, pixelScale)
    val directChildren = children[node.id].orEmpty().filter { it.isActive }
    if (directChildren.isEmpty()) return own + 22f * pixelScale
    val kids = directChildren.sumOf { branchBlockHeight(it, level + 1, children, pixelScale).toDouble() }.toFloat() +
        (directChildren.size - 1).coerceAtLeast(0) * childVerticalGap(level + 1, pixelScale)
    return max(own, kids) + 28f * pixelScale
}

private fun rootVerticalGap(pixelScale: Float): Float = 180f * pixelScale
private fun childVerticalGap(level: Int, pixelScale: Float): Float = (when (level) { 2 -> 108f; 3 -> 86f; else -> 66f }) * pixelScale
private fun horizontalGap(level: Int, pixelScale: Float): Float = (when (level) { 2 -> 226f; 3 -> 186f; else -> 158f }) * pixelScale

private fun addGraphLine(lines: MutableList<GraphLine>, fromCenter: Offset, fromWidth: Float, fromHeight: Float, toCenter: Offset, toWidth: Float, toHeight: Float, color: Color, lineSide: Int) {
    val start = edgePoint(fromCenter, fromWidth, fromHeight, toCenter)
    val end = edgePoint(toCenter, toWidth, toHeight, fromCenter)
    lines += GraphLine(from = start, to = end, color = color, side = lineSide, angle = 0f)
}

private fun edgePoint(center: Offset, width: Float, height: Float, toward: Offset): Offset {
    val direction = (toward - center).normalizedOr(Offset(1f, 0f))
    val tx = if (abs(direction.x) < 0.001f) Float.MAX_VALUE else (width / 2f) / abs(direction.x)
    val ty = if (abs(direction.y) < 0.001f) Float.MAX_VALUE else (height / 2f) / abs(direction.y)
    return center + direction * min(tx, ty)
}

private fun rootFamilyColor(node: MindMapNode, activeNodes: List<MindMapNode>, children: Map<Long?, List<MindMapNode>>, palette: List<Color>): Color {
    val ordered = activeNodes.sortedWith(
        compareBy<MindMapNode>(
            { if (it.parentId == null) 0 else 1 },
            { it.parentId ?: 0L },
            { it.orderIndex },
            { it.createdAt },
            { it.id }
        )
    )
    val index = ordered.indexOfFirst { it.id == node.id }.coerceAtLeast(0)
    return palette[index % palette.size]
}

private fun MindMapNode.savedMindMapOffset(): Offset? {
    val safeX = x
    val safeY = y
    return if (safeX != null && safeY != null) Offset(safeX, safeY) else null
}

private fun nodeWidth(title: String, level: Int, pixelScale: Float): Float {
    val style = nodeStyle(level)
    val paint = mindMapTextPaint(style.fontSize * pixelScale, level <= 1, Color.Black.toArgb())
    val prepared = title.preparedNodeText(level)
    val longestMeasured = prepared.lines().maxOfOrNull { paint.measureText(it) } ?: paint.measureText("بدون عنوان")
    val minWidth = when (level) { 0 -> 360f; 1 -> 210f; 2 -> 184f; else -> 164f } * pixelScale
    val maxWidth = when (level) { 0 -> 900f; 1 -> 620f; 2 -> 520f; else -> 460f } * pixelScale
    val desired = longestMeasured * 1.18f + style.horizontalPadding * 2f * pixelScale
    return desired.coerceIn(minWidth, maxWidth)
}

private fun nodeHeight(title: String, level: Int, pixelScale: Float): Float {
    val style = nodeStyle(level)
    val width = nodeWidth(title, level, pixelScale)
    val innerWidth = (width - style.horizontalPadding * 2f * pixelScale).toInt().coerceAtLeast(32)
    val layoutHeight = buildMindMapStaticLayout(
        text = title.preparedNodeText(level),
        textSize = style.fontSize * pixelScale,
        bold = level <= 1,
        color = Color.Black.toArgb(),
        width = innerWidth
    ).height.toFloat()
    val minHeight = when (level) { 0 -> 170f; 1 -> 82f; 2 -> 66f; else -> 56f } * pixelScale
    return (layoutHeight + style.verticalPadding * 2f * pixelScale + 6f * pixelScale).coerceAtLeast(minHeight)
}

private fun GraphNode.hit(point: Offset): Boolean {
    return point.x >= center.x - width / 2f - 12f &&
        point.x <= center.x + width / 2f + 12f &&
        point.y >= center.y - height / 2f - 12f &&
        point.y <= center.y + height / 2f + 12f
}

private fun worldToScreen(world: Offset, origin: Offset, scale: Float): Offset {
    return Offset(origin.x + world.x * scale, origin.y + world.y * scale)
}

private fun screenToWorld(screen: Offset, origin: Offset, scale: Float): Offset {
    return Offset((screen.x - origin.x) / scale, (screen.y - origin.y) / scale)
}

private operator fun Offset.plus(other: Offset): Offset = Offset(x + other.x, y + other.y)
private operator fun Offset.minus(other: Offset): Offset = Offset(x - other.x, y - other.y)
private operator fun Offset.times(value: Float): Offset = Offset(x * value, y * value)

private fun Offset.normalizedOr(fallback: Offset): Offset {
    val length = sqrt(x * x + y * y)
    return if (length < 0.001f) fallback else Offset(x / length, y / length)
}

private fun String.wrapNodeTitle(level: Int): List<String> {
    val clean = trim().toPersianDigits().replace(Regex("\\s+"), " ")
    if (clean.isBlank()) return listOf("بدون عنوان")
    val words = clean.split(" ").filter { it.isNotBlank() }
    if (words.isEmpty()) return listOf("بدون عنوان")
    return words.chunked(7).map { it.joinToString(" ") }
}

private fun String.preparedNodeText(level: Int): String = wrapNodeTitle(level).joinToString("\n")

private fun mindMapTextPaint(textSize: Float, bold: Boolean, color: Int): TextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
    this.color = color
    this.textSize = textSize
    textAlign = Paint.Align.LEFT
    isFakeBoldText = bold
}

private fun buildMindMapStaticLayout(text: String, textSize: Float, bold: Boolean, color: Int, width: Int): StaticLayout {
    val paint = mindMapTextPaint(textSize, bold, color)
    return StaticLayout.Builder.obtain(text, 0, text.length, paint, width.coerceAtLeast(1))
        .setAlignment(Layout.Alignment.ALIGN_CENTER)
        .setTextDirection(TextDirectionHeuristics.RTL)
        .setLineSpacing(0f, 1.0f)
        .setIncludePad(true)
        .build()
}

private fun android.graphics.Canvas.drawXMindRtlTextInsideNode(
    text: String,
    level: Int,
    left: Float,
    top: Float,
    right: Float,
    bottom: Float,
    color: Int,
    textSize: Float,
    bold: Boolean
) {
    val width = (right - left).toInt().coerceAtLeast(1)
    val height = (bottom - top).coerceAtLeast(1f)
    val layout = buildMindMapStaticLayout(
        text = text.preparedNodeText(level),
        textSize = textSize,
        bold = bold,
        color = color,
        width = width
    )
    val y = top + ((height - layout.height).coerceAtLeast(0f) / 2f)
    save()
    clipRect(left, top, right, bottom)
    translate(left, y)
    layout.draw(this)
    restore()
}


private fun safeBackupFileName(name: String): String {
    return name.trim()
        .ifBlank { "mindmap" }
        .replace(Regex("""[^A-Za-z0-9آ-یءئؤژگچپ_-]+"""), "_")
        .take(42)
}

private fun createMindMapCameraImageUri(context: Context): Uri {
    val dir = File(context.cacheDir, "mindmap_camera").apply { mkdirs() }
    val file = File.createTempFile("paper_mindmap_", ".jpg", dir)
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
}

private fun loadBitmapFromUri(context: Context, uri: Uri): Bitmap? {
    return runCatching {
        context.contentResolver.openInputStream(uri)?.use { input ->
            BitmapFactory.decodeStream(input)
        }
    }.getOrNull()
}

private fun saveMindMapImageToGallery(context: Context, projectTitle: String, nodes: List<MindMapNode>): Uri {
    val bitmap = renderMindMapBitmap(projectTitle, nodes)
    val fileName = "mahchin_mindmap_${safeBackupFileName(projectTitle)}_${System.currentTimeMillis()}.png"
    val values = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
        put(MediaStore.Images.Media.MIME_TYPE, "image/png")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/MahChin")
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }
    }
    val resolver = context.contentResolver
    val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        ?: error("امکان ساخت فایل تصویر وجود ندارد")
    resolver.openOutputStream(uri)?.use { out ->
        if (!bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)) error("ذخیره تصویر ناموفق بود")
    } ?: error("فایل تصویر باز نشد")
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        values.clear()
        values.put(MediaStore.Images.Media.IS_PENDING, 0)
        resolver.update(uri, values, null, null)
    }
    return uri
}

private fun shareMindMapImage(context: Context, projectTitle: String, nodes: List<MindMapNode>) {
    val uri = saveMindMapImageToGallery(context, projectTitle, nodes)
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "image/png"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "اشتراک تصویر مایندمپ"))
}

private fun renderMindMapBitmap(projectTitle: String, nodes: List<MindMapNode>): Bitmap {
    val pixelScale = 2f
    val graph = buildXMindGraph(
        projectTitle = projectTitle,
        allNodes = nodes,
        selectedNodeId = null,
        primary = Color(0xFFD4AF37),
        onPrimary = Color.Black,
        onSurface = Color.Black,
        pixelScale = pixelScale,
        manualPositions = emptyMap()
    )
    val margin = 220f * pixelScale
    val nodeBounds = graph.nodes.flatMap { node ->
        listOf(
            Offset(node.center.x - node.width / 2f, node.center.y - node.height / 2f),
            Offset(node.center.x + node.width / 2f, node.center.y + node.height / 2f)
        )
    }
    val lineBounds = graph.lines.flatMap { listOf(it.from, it.to) }
    val allPoints = (nodeBounds + lineBounds).ifEmpty { listOf(Offset.Zero) }
    val minX = allPoints.minOf { it.x } - margin
    val maxX = allPoints.maxOf { it.x } + margin
    val minY = allPoints.minOf { it.y } - margin
    val maxY = allPoints.maxOf { it.y } + margin
    val rawW = (maxX - minX).coerceAtLeast(900f)
    val rawH = (maxY - minY).coerceAtLeast(900f)
    val fitScale = min(1f, min(12000f / rawW, 12000f / rawH)).coerceAtLeast(0.18f)
    val width = (rawW * fitScale).toInt().coerceAtLeast(900)
    val height = (rawH * fitScale).toInt().coerceAtLeast(900)
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)
    val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = android.graphics.Color.rgb(7, 19, 33) }
    canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

    fun sx(x: Float): Float = (x - minX) * fitScale
    fun sy(y: Float): Float = (y - minY) * fitScale

    graph.lines.forEach { line ->
        val start = Offset(sx(line.from.x), sy(line.from.y))
        val end = Offset(sx(line.to.x), sy(line.to.y))
        val side = if (end.x >= start.x) 1f else -1f
        val dx = abs(end.x - start.x)
        val control = dx.coerceIn(90f * fitScale, 260f * fitScale)
        val path = android.graphics.Path().apply {
            moveTo(start.x, start.y)
            cubicTo(start.x + side * control, start.y, end.x - side * control, end.y, end.x, end.y)
        }
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = line.color.copy(alpha = 0.90f).toArgb()
            style = Paint.Style.STROKE
            strokeWidth = (if (line.side == 0) 4.2f else 3.4f) * fitScale
        }
        canvas.drawPath(path, paint)
    }

    graph.nodes.forEach { node ->
        val cx = sx(node.center.x)
        val cy = sy(node.center.y)
        val nodeW = node.width * fitScale
        val nodeH = node.height * fitScale
        val left = cx - nodeW / 2f
        val top = cy - nodeH / 2f
        val right = cx + nodeW / 2f
        val bottom = cy + nodeH / 2f
        val radiusValue = when (node.level) { 0 -> 24f; 1 -> 20f; else -> 18f } * fitScale
        val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = android.graphics.Color.argb(if (node.level == 0) 62 else 34, 0, 0, 0) }
        canvas.drawRoundRect(RectF(left, top + 7f * fitScale, right, bottom + 7f * fitScale), radiusValue, radiusValue, shadowPaint)
        val fill = when (node.level) {
            0 -> Color.White
            1 -> node.color.copy(alpha = 0.99f)
            2 -> node.color.copy(alpha = 0.94f)
            else -> node.color.copy(alpha = 0.88f)
        }
        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = fill.toArgb(); style = Paint.Style.FILL }
        canvas.drawRoundRect(RectF(left, top, right, bottom), radiusValue, radiusValue, fillPaint)
        val padX = node.horizontalPadding * pixelScale * fitScale
        val padY = node.verticalPadding * pixelScale * fitScale
        canvas.drawXMindRtlTextInsideNode(
            text = node.title,
            level = node.level,
            left = left + padX,
            top = top + padY,
            right = right - padX,
            bottom = bottom - padY,
            color = node.textColor.toArgb(),
            textSize = node.fontSize * pixelScale * fitScale,
            bold = node.level <= 1
        )
    }
    return bitmap
}

private data class MindAction(
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val danger: Boolean = false,
    val onClick: () -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MindMapActionSheet(
    title: String,
    subtitle: String,
    onDismiss: () -> Unit,
    actions: List<MindAction>
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(title.toPersianDigits(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(subtitle.toPersianDigits(), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(8.dp))
            actions.forEachIndexed { index, action ->
                MindActionRow(action.title, action.icon, action.danger, action.onClick)
                if (index == actions.lastIndex - 1 && actions.last().danger) {
                    HorizontalDivider(Modifier.padding(vertical = 6.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.18f))
                }
            }
            Spacer(Modifier.height(18.dp))
        }
    }
}

@Composable
private fun MindActionRow(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    danger: Boolean = false,
    onClick: () -> Unit
) {
    val color = if (danger) Color(0xFFFF6B6B) else MaterialTheme.colorScheme.onSurface
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(icon, null, tint = color, modifier = Modifier.size(22.dp))
        Text(title, color = color, style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
private fun ProjectEditorDialog(
    title: String,
    initialName: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var projectName by remember(initialName) { mutableStateOf(initialName) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            VoiceOutlinedTextField(
                value = projectName,
                onValueChange = { projectName = it },
                label = "نام پروژه",
                singleLine = true,
                prompt = "نام پروژه را بگو",
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = { Button(onClick = { onSave(projectName) }) { Text("ذخیره") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("انصراف") } }
    )
}

@Composable
private fun NodeEditorDialog(
    state: NodeDialogState,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var title by remember { mutableStateOf(state.editNode?.title.orEmpty()) }
    var desc by remember { mutableStateOf(state.editNode?.description.orEmpty()) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (state.editNode == null) if (state.parentId == null) "شاخه اصلی جدید" else "زیرشاخه جدید" else "ویرایش نود") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                VoiceOutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = "عنوان",
                    singleLine = true,
                    prompt = "عنوان نود را بگو",
                    modifier = Modifier.fillMaxWidth()
                )
                VoiceOutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = "توضیح کوتاه",
                    singleLine = false,
                    minLines = 2,
                    prompt = "توضیح نود را بگو",
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = { Button(onClick = { onSave(title, desc) }) { Text("ذخیره") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("انصراف") } }
    )
}

@Composable
private fun DistributeMindMapDialog(
    initialDate: JalaliDate,
    onDismiss: () -> Unit,
    onSave: (JalaliDate, Int) -> Unit
) {
    var year by remember { mutableStateOf(initialDate.year.toString()) }
    var month by remember { mutableStateOf(initialDate.month.toString()) }
    var day by remember { mutableStateOf(initialDate.day.toString()) }
    var perDay by remember { mutableStateOf("5") }
    var error by remember { mutableStateOf<String?>(null) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("تقسیم مایندمپ به تسک") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("زیرشاخه‌های نهایی زیر پروژه و مسیر شاخه‌ها جمع می‌شوند. می‌توانی همه را در یک روز بسازی یا بین روزها تقسیم کنی.")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    SmallNumberField("سال", year, { year = it }, Modifier.weight(1f))
                    SmallNumberField("ماه", month, { month = it }, Modifier.weight(1f))
                    SmallNumberField("روز", day, { day = it }, Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    SmallNumberField("تعداد تسک در روز", perDay, { perDay = it }, Modifier.weight(1f))
                    OutlinedButton(
                        onClick = { perDay = "9999" },
                        modifier = Modifier.height(56.dp)
                    ) { Text("همه در یک روز") }
                }
                error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = {
            Button(onClick = {
                val y = year.toIntOrNull()
                val m = month.toIntOrNull()
                val d = day.toIntOrNull()
                val p = perDay.toIntOrNull()
                if (y == null || m == null || d == null || p == null) {
                    error = "اطلاعات را کامل وارد کن."
                } else {
                    try {
                        val safeDay = d.coerceAtMost(JalaliCalendar.monthLength(y, m))
                        onSave(JalaliDate(y, m, safeDay), p.coerceAtLeast(1))
                    } catch (e: Exception) {
                        error = e.message ?: "تاریخ نامعتبر است."
                    }
                }
            }) { Text("ساخت تسک‌ها") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("انصراف") } }
    )
}

@Composable
private fun SmallNumberField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier
) {
    OutlinedTextField(
        value = value.toPersianDigits(),
        onValueChange = { onValueChange(it.toEnglishDigits().filter { ch -> ch.isDigit() }) },
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = modifier
    )
}
