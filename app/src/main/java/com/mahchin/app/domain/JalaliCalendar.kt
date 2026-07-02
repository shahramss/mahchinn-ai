package com.mahchin.app.domain

import java.time.DayOfWeek
import java.time.LocalDate

object JalaliCalendar {
    private val breaks = intArrayOf(
        -61, 9, 38, 199, 426, 686, 756, 818, 1111, 1181,
        1210, 1635, 2060, 2097, 2192, 2262, 2324, 2394,
        2456, 3178
    )

    private data class JalCalResult(val leap: Int, val gy: Int, val march: Int)

    fun today(): JalaliDate {
        val now = LocalDate.now()
        var j = fromGregorian(now)
        if (j.year < 1405) j = JalaliDate(1405, 1, 1)
        if (j.year > 1500) j = JalaliDate(1500, 12, monthLength(1500, 12))
        return j
    }

    fun monthName(month: Int): String = listOf(
        "فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور",
        "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند"
    )[month - 1]

    fun weekdayName(date: JalaliDate): String = when (date.toGregorian().dayOfWeek) {
        DayOfWeek.SATURDAY -> "شنبه"
        DayOfWeek.SUNDAY -> "یکشنبه"
        DayOfWeek.MONDAY -> "دوشنبه"
        DayOfWeek.TUESDAY -> "سه‌شنبه"
        DayOfWeek.WEDNESDAY -> "چهارشنبه"
        DayOfWeek.THURSDAY -> "پنجشنبه"
        DayOfWeek.FRIDAY -> "جمعه"
    }

    fun firstDayOffsetSaturdayBased(year: Int, month: Int): Int {
        return when (JalaliDate(year, month, 1).toGregorian().dayOfWeek) {
            DayOfWeek.SATURDAY -> 0
            DayOfWeek.SUNDAY -> 1
            DayOfWeek.MONDAY -> 2
            DayOfWeek.TUESDAY -> 3
            DayOfWeek.WEDNESDAY -> 4
            DayOfWeek.THURSDAY -> 5
            DayOfWeek.FRIDAY -> 6
        }
    }

    fun monthLength(year: Int, month: Int): Int = when (month) {
        in 1..6 -> 31
        in 7..11 -> 30
        12 -> if (isLeapJalaliYear(year)) 30 else 29
        else -> error("ماه نامعتبر است")
    }

    fun normalizeDayForMonth(dayOfMonth: Int, year: Int, month: Int): Int {
        return dayOfMonth.coerceIn(1, monthLength(year, month))
    }

    fun isLeapJalaliYear(jy: Int): Boolean = jalCal(jy).leap == 0

    fun toGregorian(jy: Int, jm: Int, jd: Int): LocalDate {
        val g = d2g(j2d(jy, jm, jd))
        return LocalDate.of(g[0], g[1], g[2])
    }

    fun fromGregorian(date: LocalDate): JalaliDate {
        val j = d2j(g2d(date.year, date.monthValue, date.dayOfMonth))
        return JalaliDate(j[0], j[1], j[2])
    }

    fun nextMonth(date: JalaliDate): JalaliDate {
        val m = if (date.month == 12) 1 else date.month + 1
        val y = if (date.month == 12) date.year + 1 else date.year
        return JalaliDate(y.coerceAtMost(1500), m, 1)
    }

    fun previousMonth(date: JalaliDate): JalaliDate {
        val m = if (date.month == 1) 12 else date.month - 1
        val y = if (date.month == 1) date.year - 1 else date.year
        return JalaliDate(y.coerceAtLeast(1405), m, 1)
    }

    private fun jalCal(jy: Int): JalCalResult {
        var gy = jy + 621
        var leapJ = -14
        var jp = breaks[0]
        var jm = 0
        var jump = 0
        for (i in 1 until breaks.size) {
            jm = breaks[i]
            jump = jm - jp
            if (jy < jm) break
            leapJ += (jump / 33) * 8 + (jump % 33) / 4
            jp = jm
        }
        var n = jy - jp
        leapJ += (n / 33) * 8 + ((n % 33) + 3) / 4
        if ((jump % 33) == 4 && jump - n == 4) leapJ += 1
        val leapG = gy / 4 - ((gy / 100 + 1) * 3) / 4 - 150
        val march = 20 + leapJ - leapG
        if (jump - n < 6) n = n - jump + ((jump + 4) / 33) * 33
        var leap = (((n + 1) % 33) - 1) % 4
        if (leap == -1) leap = 4
        return JalCalResult(leap, gy, march)
    }

    private fun j2d(jy: Int, jm: Int, jd: Int): Int {
        val r = jalCal(jy)
        return g2d(r.gy, 3, r.march) + (jm - 1) * 31 - (jm / 7) * (jm - 7) + jd - 1
    }

    private fun d2j(jdn: Int): IntArray {
        val gy = d2g(jdn)[0]
        var jy = gy - 621
        val r = jalCal(jy)
        val jdn1f = g2d(gy, 3, r.march)
        var k = jdn - jdn1f
        val jm: Int
        val jd: Int
        if (k >= 0) {
            if (k <= 185) {
                jm = 1 + k / 31
                jd = (k % 31) + 1
                return intArrayOf(jy, jm, jd)
            } else {
                k -= 186
            }
        } else {
            jy -= 1
            k += 179
            if (r.leap == 1) k += 1
        }
        jm = 7 + k / 30
        jd = (k % 30) + 1
        return intArrayOf(jy, jm, jd)
    }

    private fun g2d(gy: Int, gm: Int, gd: Int): Int {
        var d = ((gy + ((gm - 8) / 6) + 100100) * 1461) / 4
        d += (153 * ((gm + 9) % 12) + 2) / 5
        d += gd - 34840408
        d -= (((gy + 100100 + ((gm - 8) / 6)) / 100) * 3) / 4
        d += 752
        return d
    }

    private fun d2g(jdn: Int): IntArray {
        var j = 4 * jdn + 139361631
        j += (((4 * jdn + 183187720) / 146097) * 3 / 4) * 4 - 3908
        val i = ((j % 1461) / 4) * 5 + 308
        val gd = (i % 153) / 5 + 1
        val gm = (i / 153) % 12 + 1
        val gy = j / 1461 - 100100 + (8 - gm) / 6
        return intArrayOf(gy, gm, gd)
    }
}
