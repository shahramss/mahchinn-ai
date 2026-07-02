package com.mahchin.app.data.model

enum class TaskStatus(val fa: String) {
    NOT_STARTED("انجام نشده"),
    IN_PROGRESS("در حال انجام"),
    DONE("انجام شد"),
    MOVED_TO_TOMORROW("موکول شد به فردا"),
    MOVED_TO_CUSTOM_DATE("موکول شد به تاریخ دلخواه"),
    CANCELED("لغو شد");

    fun isClosed(): Boolean = this == DONE || this == MOVED_TO_TOMORROW ||
        this == MOVED_TO_CUSTOM_DATE || this == CANCELED
}

enum class TaskType(val fa: String) {
    MONTHLY_TEMPLATE("قالب ماهانه"),
    DAILY_FROM_TEMPLATE("تکرارشونده امروز"),
    ONE_TIME("اختصاصی")
}

enum class TaskPriority(val fa: String, val weight: Int) {
    NORMAL("عادی", 0),
    IMPORTANT("مهم", 1),
    URGENT("فوری", 2)
}

enum class ReminderIntensity(val fa: String, val hours: Long) {
    CALM("آرام؛ فقط یک بار صبح", 24),
    NORMAL("معمولی؛ هر ۳ ساعت", 3),
    SERIOUS("جدی؛ هر ۱ ساعت", 1),
    VERY_SERIOUS("خیلی جدی؛ هر ۱ ساعت با صدا و ویبره", 1)
}

enum class TaskOrigin {
    DAILY_INSTANCE,
    ONE_TIME,
    TEMPLATE
}
