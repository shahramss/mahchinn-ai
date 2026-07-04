
package com.mahchin.app.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun JalaliDateDialog(
    onDismiss: () -> Unit = {},
    onSelect: (String) -> Unit = {}
) { /* stub */ }

@Composable
fun TaskAlarmDialog(
    onDismiss: () -> Unit = {},
    onSelect: (String, Int, Int) -> Unit = { _,_,_ -> }
) { /* stub */ }

@Composable
fun VoiceOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // fallback simple text field placeholder
    androidx.compose.material3.OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
    )
}
