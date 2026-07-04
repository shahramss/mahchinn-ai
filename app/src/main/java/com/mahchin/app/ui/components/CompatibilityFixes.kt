
package com.mahchin.app.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.material3.*
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier

@Composable
fun JalaliDateDialog(
    initialDate: String? = null,
    onSave: (String) -> Unit = {},
    onClear: () -> Unit = {},
    onDismiss: () -> Unit = {}
) {}

@Composable
fun TaskAlarmDialog(
    initialDate: String? = null,
    hour: Int? = null,
    minute: Int? = null,
    onSave: (String, Int, Int) -> Unit = {_,_,_->},
    onDismiss: () -> Unit = {}
) {}

@Composable
fun VoiceOutlinedTextField(
    value: String = "",
    onValueChange: (String) -> Unit = {},
    modifier: Modifier = Modifier,
    label: String = ""
) {
    OutlinedTextField(value = value, onValueChange = onValueChange, modifier = modifier, label = { Text(label) })
}

@Composable
fun TaskEditorDialog(
    titleText: String = "",
    initialTitle: String = "",
    initialDescription: String = "",
    selectedProjectId: Long? = null,
    onDismiss: () -> Unit = {},
    onSave: (String, String, Long?) -> Unit = {_,_,_->}
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { onSave(initialTitle, initialDescription, selectedProjectId); onDismiss() }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        title = { Text(titleText) },
        text = { Text("Task") }
    )
}
