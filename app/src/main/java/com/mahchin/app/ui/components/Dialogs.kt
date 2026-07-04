
package com.mahchin.app.ui.components

import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mahchin.app.data.model.Project

@Composable
fun SearchableProjectDropdown(
    projects: List<Project>,
    selectedId: Long?,
    onSelect: (Project) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var search by remember { mutableStateOf("") }

    val filtered = remember(search, projects) {
        if (search.isBlank()) projects
        else projects.filter { it.name.contains(search, true) }
    }

    Column {
        OutlinedButton(onClick = { expanded = true }) {
            Text(projects.find { it.id == selectedId }?.name ?: "انتخاب پروژه")
        }

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false; search = "" }) {

            OutlinedTextField(
                value = search,
                onValueChange = { search = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("جستجو") }
            )

            filtered.forEach {
                DropdownMenuItem(
                    text = { Text(it.name) },
                    onClick = {
                        onSelect(it)
                        expanded = false
                        search = ""
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
    selectedProjectId: Long?,
    onDismiss: () -> Unit,
    onSave: (String, String, Long?) -> Unit
) {
    var title by remember { mutableStateOf(initialTitle) }
    var desc by remember { mutableStateOf(initialDescription) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                onSave(title, desc, selectedProjectId)
                onDismiss()
            }) { Text("OK") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        title = { Text(titleText) },
        text = {
            Column {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("عنوان") })
                OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("توضیحات") })
            }
        }
    )
}

    OutlinedTextField(value,onValueChange,modifier,label={Text("")})
}