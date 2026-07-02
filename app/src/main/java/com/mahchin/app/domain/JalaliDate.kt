package com.mahchin.app.domain

import java.time.LocalDate

data class JalaliDate(
    val year: Int,
    val month: Int,
    val day: Int
) {
    init {
        require(year in 1405..1500) { "سال باید بین ۱۴۰۵ تا ۱۵۰۰ باشد." }
        require(month in 1..12) { "ماه نامعتبر است." }
        require(day in 1..JalaliCalendar.monthLength(year, month)) { "روز نامعتبر است." }
    }

    val key: String get() = "%04d-%02d-%02d".format(year, month, day)
    val display: String get() = "${day.toPersianDigits()} ${JalaliCalendar.monthName(month)} ${year.toPersianDigits()}"

    fun plusDays(days: Long): JalaliDate = JalaliCalendar.fromGregorian(toGregorian().plusDays(days))
    fun toGregorian(): LocalDate = JalaliCalendar.toGregorian(year, month, day)
}

fun Int.toPersianDigits(): String = this.toString().toPersianDigits()
fun Long.toPersianDigits(): String = this.toString().toPersianDigits()
fun String.toPersianDigits(): String {
    val map = mapOf(
        '0' to '۰', '1' to '۱', '2' to '۲', '3' to '۳', '4' to '۴',
        '5' to '۵', '6' to '۶', '7' to '۷', '8' to '۸', '9' to '۹'
    )
    return buildString { this@toPersianDigits.forEach { append(map[it] ?: it) } }
}

fun String.toEnglishDigits(): String {
    val fa = "۰۱۲۳۴۵۶۷۸۹"
    val ar = "٠١٢٣٤٥٦٧٨٩"
    return map { ch ->
        when {
            ch in fa -> ('0'.code + fa.indexOf(ch)).toChar()
            ch in ar -> ('0'.code + ar.indexOf(ch)).toChar()
            else -> ch
        }
    }.joinToString("")
}
