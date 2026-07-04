
package com.mahchin.app.ui.components

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import com.mahchin.app.data.model.Project
import androidx.compose.ui.text.input.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType

// ---------------- SEARCHABLE DROPDOWN ----------------

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
        else projects.filter { it.name.contains(query, true) }
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selected?.name ?: query,
            onValueChange = {
                query = it
                expanded = true
            },
            modifier = modifier,
            label = { Text("انتخاب پروژه") },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded)
            }
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            filtered.forEach { p ->
                DropdownMenuItem(
                    text = { Text(p.name) },
                    onClick = {
                        onSelect(p)
                        query = p.name
                        expanded = false
                    }
                )
            }
        }
    }
}

// ---------------- TASK DIALOG (COMPAT SAFE) ----------------

@Composable
fun TaskEditorDialog(
    titleText: String,
    initialTitle: String,
    initialDescription: String,
    selectedProjectId: Long?,
    onDismiss: () -> Unit,
    onSave: (String, String, Long?) -> Unit
) {
    var title by remember { mutableStateOf(initialTitle) }
    var desc by remember { mutableStateOf(initialDescription) }
    var projectId by remember { mutableStateOf(selectedProjectId) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = {
                onSave(title, desc, projectId)
                onDismiss()
            }) { Text("ذخیره") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("لغو") }
        },
        title = { Text(titleText) },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("عنوان") }
                )

                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("توضیحات") }
                )

                Spacer(Modifier.height(8.dp))

                if (selectedProjectId != null) {
                    Text("Project selected: $selectedProjectId")
                }
            }
        }
    )
}

// ---------------- STUBS FOR BUILD SAFETY ----------------

@Composable
fun JalaliDateDialog(onDismiss: () -> Unit = {}, onSelect: (String) -> Unit = {}) {}

@Composable
fun TaskAlarmDialog(
    onDismiss: () -> Unit = {},
    onSelect: (String, Int, Int) -> Unit = {_,_,_ ->}
) {}

@Composable
fun VoiceOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        label = { Text("") }
    )
}
