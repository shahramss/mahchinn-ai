
package com.mahchin.app.ui.components

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import com.mahchin.app.data.model.Project

@OptIn(ExperimentalMaterial3Api::class)
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
        else projects.filter { it.name.contains(search, ignoreCase = true) }
    }

    Column {

        OutlinedButton(onClick = { expanded = true }) {
            Text(projects.find { it.id == selectedId }?.name ?: "انتخاب پروژه")
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false; search = "" }
        ) {

            OutlinedTextField(
                value = search,
                onValueChange = { search = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("جستجو پروژه") }
            )

            Divider()

            filtered.forEach { p ->
                DropdownMenuItem(
                    text = { Text(p.name) },
                    onClick = {
                        onSelect(p)
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
    var projectId by remember { mutableStateOf(selectedProjectId) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                onSave(title, desc, projectId)
                onDismiss()
            }) { Text("OK") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
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

                Text("Project ID: ${projectId ?: -1}")
            }
        }
    )
}

@Composable fun JalaliDateDialog() {}
@Composable fun TaskAlarmDialog() {}
@Composable fun VoiceOutlinedTextField(value:String,onValueChange:(String)->Unit,modifier:Modifier=Modifier){
    OutlinedTextField(value,onValueChange,modifier)
}
