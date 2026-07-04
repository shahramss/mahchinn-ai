
package com.mahchin.app.ui.components

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import com.mahchin.app.data.model.Project
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType

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
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
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

@Composable
fun TaskEditorDialog(
    titleText: String,
    initialTitle: String,
    initialDescription: String,
    projects: List<Project>,
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
            }) {
                Text("ذخیره")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("لغو") }
        },
        title = { Text(titleText) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("عنوان") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                )

                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("توضیحات") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                )

                if (projects.isNotEmpty()) {
                    SearchableProjectDropdown(
                        projects = projects,
                        selected = projects.firstOrNull { it.id == projectId },
                        onSelect = { project ->
                            projectId = project.id
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    )
}
