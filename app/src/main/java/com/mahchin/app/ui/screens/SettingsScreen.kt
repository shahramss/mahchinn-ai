package com.mahchin.app.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.mahchin.app.BuildConfig
import com.mahchin.app.data.model.ReminderIntensity
import com.mahchin.app.domain.toEnglishDigits
import com.mahchin.app.domain.toPersianDigits
import com.mahchin.app.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(vm: MainViewModel) {
    val settings by vm.settings.collectAsState()
    val message by vm.settingsMessage.collectAsState()
    var expanded by remember { mutableStateOf(false) }
    var startHour by remember(settings.startHour) { mutableStateOf(settings.startHour.toString()) }
    var endHour by remember(settings.endHour) { mutableStateOf(settings.endHour.toString()) }
    var confirmClear by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val aiPrefs = remember { context.getSharedPreferences("mahchin_ai", android.content.Context.MODE_PRIVATE) }
    var openAiKey by remember { mutableStateOf(aiPrefs.getString("openai_api_key", "") ?: "") }
    val aiModelOptions = listOf("gpt-5", "gpt-5-mini", "gpt-5-nano", "gpt-4.1", "gpt-4.1-mini", "gpt-4o", "gpt-4o-mini")
    var selectedAiModel by remember { mutableStateOf(aiPrefs.getString("openai_model", "gpt-5") ?: "gpt-5") }
    var aiModelExpanded by remember { mutableStateOf(false) }
    val fullBackupLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        vm.exportFullBackupToUri(context, uri)
    }
    val fullRestoreLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        vm.restoreFullBackupFromUri(context, uri)
    }
    val financeBackupLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        vm.exportFinanceBackupToUri(context, uri)
    }
    val financeRestoreLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        vm.restoreFinanceBackupFromUri(context, uri)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("تنظیمات", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text("یادآوری‌ها را ساده و مطمئن تنظیم کن.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        if (message != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f))
                ) {
                    Text(message ?: "", modifier = Modifier.padding(14.dp), color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }

        item {
            Card(
                Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.55f)),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Icon(Icons.Outlined.NotificationsActive, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text("تنظیمات یادآوری", fontWeight = FontWeight.Bold)
                    }

                    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                        OutlinedTextField(
                            value = settings.reminderIntensity.fa,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("شدت یادآوری") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            ReminderIntensity.entries.forEach { intensity ->
                                DropdownMenuItem(text = { Text(intensity.fa) }, onClick = {
                                    vm.updateSettings { it.copy(reminderIntensity = intensity) }
                                    expanded = false
                                })
                            }
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = startHour.toPersianDigits(),
                            onValueChange = { startHour = it.toEnglishDigits().filter { ch -> ch.isDigit() }.take(2) },
                            label = { Text("شروع") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = endHour.toPersianDigits(),
                            onValueChange = { endHour = it.toEnglishDigits().filter { ch -> ch.isDigit() }.take(2) },
                            label = { Text("پایان") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    Button(onClick = {
                        val s = startHour.toIntOrNull() ?: 8
                        val e = endHour.toIntOrNull() ?: 22
                        vm.saveReminderSettings(s, e)
                    }, modifier = Modifier.fillMaxWidth().height(52.dp)) { Text("ذخیره تنظیمات یادآوری") }

                    OutlinedButton(onClick = vm::sendTestNotification, modifier = Modifier.fillMaxWidth().height(50.dp)) {
                        Text("ارسال نوتیفیکیشن آزمایشی")
                    }
                }
            }
        }

        item { SettingSwitch("فعال‌سازی صدا", settings.soundEnabled) { checked -> vm.updateSettings { it.copy(soundEnabled = checked) } } }
        item { SettingSwitch("فعال‌سازی ویبره", settings.vibrationEnabled) { checked -> vm.updateSettings { it.copy(vibrationEnabled = checked) } } }
        item { SettingSwitch("حالت تاریک", settings.darkMode) { checked -> vm.updateSettings { it.copy(darkMode = checked) } } }
        item { SettingSwitch("بکاپ خودکار اندروید", settings.backupEnabled) { checked -> vm.updateSettings { it.copy(backupEnabled = checked) } } }

        item {
            Card(
                Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.30f)),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("هوش مصنوعی مایندمپ", fontWeight = FontWeight.Bold)
                    Text(
                        "برای تبدیل عکس مایندمپ کاغذی به مایندمپ داخل برنامه، کلید OpenAI را اینجا وارد کن.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )
                    OutlinedTextField(
                        value = openAiKey,
                        onValueChange = { openAiKey = it.trim() },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = { Text("کلید OpenAI") },
                        placeholder = { Text("sk-...") }
                    )

                    ExposedDropdownMenuBox(expanded = aiModelExpanded, onExpandedChange = { aiModelExpanded = !aiModelExpanded }) {
                        OutlinedTextField(
                            value = selectedAiModel,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("نسخه هوش مصنوعی") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(aiModelExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            singleLine = true
                        )
                        ExposedDropdownMenu(expanded = aiModelExpanded, onDismissRequest = { aiModelExpanded = false }) {
                            aiModelOptions.forEach { model ->
                                DropdownMenuItem(text = { Text(model) }, onClick = {
                                    selectedAiModel = model
                                    aiModelExpanded = false
                                })
                            }
                        }
                    }
                    Text(
                        "برای دقت بیشتر روی gpt-5 بگذار؛ اگر API خطا داد، gpt-4.1 یا gpt-4o را انتخاب کن.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )

                    Button(
                        onClick = {
                            aiPrefs.edit()
                                .putString("openai_api_key", openAiKey.trim())
                                .putString("openai_model", selectedAiModel.trim())
                                .apply()
                            vm.showMessage("کلید و نسخه هوش مصنوعی ذخیره شد.")
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) { Text("ذخیره تنظیمات هوش مصنوعی") }
                }
            }
        }

        item {
            Card(
                Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.30f)),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("بکاپ و بازگردانی کامل", fontWeight = FontWeight.Bold)
                    Text(
                        "تمام تسک‌ها، قالب‌ها، مایندمپ‌ها، پروژه‌ها، مدیریت مالی و تنظیمات در یک فایل ذخیره می‌شود.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Button(
                        onClick = { fullBackupLauncher.launch("mahchin_full_backup_${System.currentTimeMillis()}.json") },
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) { Text("گرفتن بکاپ کامل") }
                    OutlinedButton(
                        onClick = { fullRestoreLauncher.launch(arrayOf("application/json", "text/*", "application/octet-stream")) },
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) { Text("بازگردانی بکاپ کامل") }
                    Text("بکاپ فقط مدیریت مالی", fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 6.dp))
                    Button(
                        onClick = { financeBackupLauncher.launch("mahchin_finance_backup_${System.currentTimeMillis()}.json") },
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) { Text("گرفتن بکاپ مالی") }
                    OutlinedButton(
                        onClick = { financeRestoreLauncher.launch(arrayOf("application/json", "text/*", "application/octet-stream")) },
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) { Text("بازگردانی بکاپ مالی") }
                }
            }
        }

        item {
            Card(
                Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.28f)),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("پاک‌سازی داده‌ها", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                    Text("برای جلوگیری از حذف اشتباهی، قبل از پاک کردن از تو تأیید گرفته می‌شود.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                    OutlinedButton(onClick = { confirmClear = "today" }, modifier = Modifier.fillMaxWidth().height(48.dp)) {
                        Text("پاک کردن تسک‌های امروز")
                    }
                    OutlinedButton(onClick = { confirmClear = "templates" }, modifier = Modifier.fillMaxWidth().height(48.dp)) {
                        Text("پاک کردن همه تسک‌های قالب")
                    }
                    Button(onClick = { confirmClear = "all" }, modifier = Modifier.fillMaxWidth().height(48.dp)) {
                        Text("پاک کردن همه تسک‌ها")
                    }
                    OutlinedButton(onClick = { confirmClear = "finance" }, modifier = Modifier.fillMaxWidth().height(48.dp)) {
                        Text("پاک کردن همه فاکتورهای مالی")
                    }
                }
            }
        }

        item {
            Spacer(Modifier.height(6.dp))
            Text(
                "برای اینکه یادآوری‌ها در بعضی گوشی‌ها بهتر کار کند، در تنظیمات گوشی Battery Optimization را برای ماه‌چین محدود نکن.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            Column(
                modifier = Modifier.fillMaxWidth().padding(top = 10.dp, bottom = 22.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    "ساخته شده توسط وب تیما",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    "نسخه ${BuildConfig.VERSION_NAME}".toPersianDigits(),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.78f),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }

    confirmClear?.let { action ->
        val title = when (action) {
            "today" -> "پاک کردن تسک‌های امروز"
            "templates" -> "پاک کردن تسک‌های قالب"
            "finance" -> "پاک کردن فاکتورهای مالی"
            else -> "پاک کردن همه تسک‌ها"
        }
        val question = when (action) {
            "today" -> "آیا می‌خواهید تسک‌های امروز پاک شود؟"
            "templates" -> "آیا می‌خواهید همه تسک‌های قالب پاک شود؟"
            "finance" -> "آیا می‌خواهید تمام فاکتورهای مالی پاک شود؟"
            else -> "آیا می‌خواهید تمام تسک‌ها پاک شود؟"
        }
        AlertDialog(
            onDismissRequest = { confirmClear = null },
            title = { Text(title) },
            text = { Text(question) },
            confirmButton = {
                Button(onClick = {
                    when (action) {
                        "today" -> vm.clearTodayTasks()
                        "templates" -> vm.clearAllTemplateTasks()
                        "finance" -> vm.clearAllFinanceTasks()
                        else -> vm.clearAllTasks()
                    }
                    confirmClear = null
                }) { Text("بله، پاک کن") }
            },
            dismissButton = { TextButton(onClick = { confirmClear = null }) { Text("انصراف") } }
        )
    }
}

@Composable
private fun SettingSwitch(title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Card(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title)
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    }
}
