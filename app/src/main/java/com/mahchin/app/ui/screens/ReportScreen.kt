package com.mahchin.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mahchin.app.domain.JalaliCalendar
import com.mahchin.app.domain.toPersianDigits
import com.mahchin.app.ui.viewmodel.MainViewModel

@Composable
fun ReportScreen(vm: MainViewModel) {
    val month by vm.calendarMonth.collectAsState()
    val report by vm.report.collectAsState()
    val progress = report.completionPercent / 100f

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("گزارش ماه", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            OutlinedButton(onClick = vm::previousMonth, enabled = !(month.year == 1405 && month.month == 1)) { Text("ماه قبل") }
            Text("${JalaliCalendar.monthName(month.month)} ${month.year.toPersianDigits()}", style = MaterialTheme.typography.titleLarge)
            OutlinedButton(onClick = vm::nextMonth, enabled = !(month.year == 1500 && month.month == 12)) { Text("ماه بعد") }
        }
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("درصد تکمیل برنامه ماهانه", fontWeight = FontWeight.Bold)
                LinearProgressIndicator(progress = { progress.coerceIn(0f, 1f) }, modifier = Modifier.fillMaxWidth())
                Text("${report.completionPercent.toPersianDigits()}٪ از کارهای ثبت‌شده انجام شده‌اند.")
            }
        }
        ReportItem("کل تسک‌های ماه", report.total)
        ReportItem("تعداد تسک‌های انجام‌شده", report.done)
        ReportItem("تعداد تسک‌های موکول‌شده", report.moved)
        ReportItem("تعداد تسک‌های لغوشده", report.canceled)
        Spacer(Modifier.height(8.dp))
        Text("نکته: گزارش بر اساس تسک‌هایی است که برای روزهای ماه ساخته یا باز شده‌اند. ماه‌چین هنگام ورود به گزارش، برنامه ماه جاری را می‌سازد تا گزارش دقیق‌تر شود.", color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun ReportItem(title: String, value: Int) {
    Card(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(title)
            Text(value.toPersianDigits(), fontWeight = FontWeight.Bold)
        }
    }
}
