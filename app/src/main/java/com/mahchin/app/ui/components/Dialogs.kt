package com.mahchin.app.ui.components

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.mahchin.app.data.model.Project
import com.mahchin.app.data.model.TaskPriority
import com.mahchin.app.domain.JalaliCalendar
import com.mahchin.app.domain.JalaliDate
import com.mahchin.app.domain.toEnglishDigits
import com.mahchin.app.domain.toPersianDigits


@Composable
fun VoiceOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    minLines: Int = 1,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    prompt: String = "متن را بگو"
) {
    val speechLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val text = result.data
                ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                ?.firstOrNull()
                .orEmpty()
            if (text.isNotBlank()) onValueChange(text)
        }
    }

    fun startVoice() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "fa-IR")
            putExtra(RecognizerIntent.EXTRA_PROMPT, prompt)
        }
        try { speechLauncher.launch(intent) } catch (_: Exception) { }
    }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = singleLine,
        minLines = minLines,
        keyboardOptions = keyboardOptions,
        trailingIcon = {
            IconButton(onClick = ::startVoice) {
                Icon(Icons.Outlined.Mic, contentDescription = "ورود با ویس")
            }
        },
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskEditorDialog(
    titleText: String,
    initialTitle: String = "",
    initialDescription: String = "",
    initialPriority: TaskPriority = TaskPriority.NORMAL,
    initialProjectId: Long? = null,
    projects: List<Project> = emptyList(),
    dayOfMonth: Int? = null,
    onDismiss: () -> Unit,
    onSave: (title: String, description: String, day: Int?, priority: TaskPriority, projectId: Long?) -> Unit
) {
    var title by remember { mutableStateOf(initialTitle) }
    var desc by remember { mutableStateOf(initialDescription) }
    var day by remember { mutableStateOf(dayOfMonth?.toString() ?: "") }
    var priority by remember { mutableStateOf(initialPriority) }
    var selectedProjectId by remember { mutableStateOf(initialProjectId ?: projects.firstOrNull()?.id) }
    var expandedPriority by remember { mutableStateOf(false) }
    var expandedProject by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(titleText) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                VoiceOutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = "عنوان تسک",
                    singleLine = true,
                    prompt = "عنوان تسک را بگو",
                    modifier = Modifier.fillMaxWidth()
                )
                VoiceOutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = "توضیحات",
                    singleLine = false,
                    minLines = 2,
                    prompt = "توضیحات را بگو",
                    modifier = Modifier.fillMaxWidth()
                )
                if (dayOfMonth != null) {
                    OutlinedTextField(
                        value = day.toPersianDigits(),
                        onValueChange = { day = it.toEnglishDigits().filter { ch -> ch.isDigit() } },
                        label = { Text("روز ماه؛ ۱ تا ۳۱") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                if (projects.isNotEmpty()) {
                    ExposedDropdownMenuBox(expanded = expandedProject, onExpandedChange = { expandedProject = !expandedProject }) {
                        OutlinedTextField(
                            value = projects.firstOrNull { it.id == selectedProjectId }?.name ?: "بدون پروژه",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("پروژه") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandedProject) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(expanded = expandedProject, onDismissRequest = { expandedProject = false }) {
                            DropdownMenuItem(text = { Text("بدون پروژه") }, onClick = { selectedProjectId = null; expandedProject = false })
                            projects.forEach { p ->
                                DropdownMenuItem(text = { Text(p.name) }, onClick = { selectedProjectId = p.id; expandedProject = false })
                            }
                        }
                    }
                }
                ExposedDropdownMenuBox(expanded = expandedPriority, onExpandedChange = { expandedPriority = !expandedPriority }) {
                    OutlinedTextField(
                        value = priority.fa,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("اولویت") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandedPriority) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = expandedPriority, onDismissRequest = { expandedPriority = false }) {
                        TaskPriority.entries.forEach { p ->
                            DropdownMenuItem(text = { Text(p.fa) }, onClick = { priority = p; expandedPriority = false })
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onSave(title, desc, day.toIntOrNull(), priority, selectedProjectId) }) { Text("ذخیره") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("انصراف") } }
    )
}

@Composable
fun JalaliDateDialog(
    initialDate: JalaliDate,
    onDismiss: () -> Unit,
    onSave: (JalaliDate) -> Unit
) {
    var year by remember { mutableStateOf(initialDate.year.toString()) }
    var month by remember { mutableStateOf(initialDate.month.toString()) }
    var day by remember { mutableStateOf(initialDate.day.toString()) }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("انتخاب تاریخ شمسی") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("سال‌های مجاز: ۱۴۰۵ تا ۱۵۰۰")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    DateField("سال", year, { year = it }, Modifier.weight(1f))
                    DateField("ماه", month, { month = it }, Modifier.weight(1f))
                    DateField("روز", day, { day = it }, Modifier.weight(1f))
                }
                error?.let { Text(it, color = androidx.compose.material3.MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = {
            Button(onClick = {
                val y = year.toIntOrNull()
                val m = month.toIntOrNull()
                val d = day.toIntOrNull()
                if (y == null || m == null || d == null) {
                    error = "تاریخ را کامل وارد کن."
                } else {
                    try {
                        val safeDay = d.coerceAtMost(JalaliCalendar.monthLength(y, m))
                        onSave(JalaliDate(y, m, safeDay))
                    } catch (e: Exception) {
                        error = e.message ?: "تاریخ نامعتبر است."
                    }
                }
            }) { Text("تأیید") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("انصراف") } }
    )
}

@Composable
fun TaskAlarmDialog(
    initialDate: JalaliDate,
    initialHour: Int = 8,
    initialMinute: Int = 0,
    onDismiss: () -> Unit,
    onClear: (() -> Unit)? = null,
    onSave: (JalaliDate, Int, Int) -> Unit
) {
    var year by remember { mutableStateOf(initialDate.year.toString()) }
    var month by remember { mutableStateOf(initialDate.month.toString()) }
    var day by remember { mutableStateOf(initialDate.day.toString()) }
    var hour by remember { mutableStateOf(initialHour.toString()) }
    var minute by remember { mutableStateOf(initialMinute.toString()) }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("آلارم تسک") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("در تاریخ و ساعت مشخص، با صدای پیش‌فرض آلارم گوشی یادآوری می‌شود.")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    DateField("سال", year, { year = it }, Modifier.weight(1f))
                    DateField("ماه", month, { month = it }, Modifier.weight(1f))
                    DateField("روز", day, { day = it }, Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    DateField("ساعت", hour, { hour = it }, Modifier.weight(1f))
                    DateField("دقیقه", minute, { minute = it }, Modifier.weight(1f))
                }
                error?.let { Text(it, color = androidx.compose.material3.MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = {
            Button(onClick = {
                val y = year.toIntOrNull()
                val m = month.toIntOrNull()
                val d = day.toIntOrNull()
                val h = hour.toIntOrNull()
                val min = minute.toIntOrNull()
                if (y == null || m == null || d == null || h == null || min == null) {
                    error = "تاریخ و ساعت را کامل وارد کن."
                } else {
                    try {
                        val safeDay = d.coerceAtMost(JalaliCalendar.monthLength(y, m))
                        onSave(JalaliDate(y, m, safeDay), h.coerceIn(0, 23), min.coerceIn(0, 59))
                    } catch (e: Exception) {
                        error = e.message ?: "تاریخ نامعتبر است."
                    }
                }
            }) { Text("ذخیره آلارم") }
        },
        dismissButton = {
            Row {
                onClear?.let { TextButton(onClick = it) { Text("حذف آلارم") } }
                TextButton(onClick = onDismiss) { Text("انصراف") }
            }
        }
    )
}

@Composable
private fun DateField(label: String, value: String, onValueChange: (String) -> Unit, modifier: Modifier = Modifier) {
    OutlinedTextField(
        value = value.toPersianDigits(),
        onValueChange = { onValueChange(it.toEnglishDigits().filter { ch -> ch.isDigit() }) },
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        modifier = modifier.padding(top = 4.dp)
    )
}




@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchableProjectDropdown(
    projects: List<Project>,
    selected: Project?,
    onSelect: (Project) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }

    val filtered = remember(query, projects) {
        if (query.isBlank()) projects
        else projects.filter {
            it.name.contains(query, ignoreCase = true)
        }
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            modifier = modifier.menuAnchor(),
            value = selected?.name ?: query,
            onValueChange = {
                query = it
                expanded = true
            },
            readOnly = false,
            label = { Text("انتخاب پروژه") },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            }
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            filtered.forEach { project ->
                DropdownMenuItem(
                    text = { Text(project.name) },
                    onClick = {
                        onSelect(project)
                        query = project.name
                        expanded = false
                    }
                )
            }
        }
    }
}
