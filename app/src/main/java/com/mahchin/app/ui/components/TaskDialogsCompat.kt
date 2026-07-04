
package com.mahchin.app.ui.components

import androidx.compose.runtime.Composable
import com.mahchin.app.data.model.Project

@Composable
fun TaskEditorDialog(
    titleText: String,
    initialTitle: String,
    initialDescription: String,
    selectedProjectId: Long?,
    projects: List<Project>,
    onDismiss: () -> Unit,
    onSave: (String, String, Long?) -> Unit
) {
    // fallback wrapper
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            androidx.compose.material3.TextButton(onClick = {
                onSave(initialTitle, initialDescription, selectedProjectId)
                onDismiss()
            }) { androidx.compose.material3.Text("OK") }
        },
        text = { androidx.compose.material3.Text("Task dialog") }
    )
}

// overloads to fix compile mismatches
@Composable
fun TaskEditorDialog(
    titleText: String,
    initialTitle: String,
    initialDescription: String,
    selectedProjectId: Long?,
    onDismiss: () -> Unit,
    onSave: (String, String, Long?) -> Unit
) {
    TaskEditorDialog(titleText, initialTitle, initialDescription, selectedProjectId, emptyList(), onDismiss, onSave)
}
