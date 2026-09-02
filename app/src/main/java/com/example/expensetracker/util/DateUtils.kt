package com.example.expensetracker.util

import java.util.Calendar

object DateUtils {

    fun startOfDay(
        timestamp: Long = System.currentTimeMillis()
    ): Long {

        return Calendar.getInstance().apply {

            timeInMillis = timestamp

            set(
                Calendar.HOUR_OF_DAY,
                0
            )

            set(
                Calendar.MINUTE,
                0
            )

            set(
                Calendar.SECOND,
                0
            )

            set(
                Calendar.MILLISECOND,
                0
            )

        }.timeInMillis
    }

    fun startOfWeek(
        timestamp: Long = System.currentTimeMillis()
    ): Long {

        return Calendar.getInstance().apply {

            timeInMillis = timestamp

            firstDayOfWeek =
                Calendar.MONDAY

            set(
                Calendar.DAY_OF_WEEK,
                Calendar.MONDAY
            )

            set(
                Calendar.HOUR_OF_DAY,
                0
            )

            set(
                Calendar.MINUTE,
                0
            )

            set(
                Calendar.SECOND,
                0
            )

            set(
                Calendar.MILLISECOND,
                0
            )

        }.timeInMillis
    }

    fun startOfMonth(
        timestamp: Long = System.currentTimeMillis()
    ): Long {

        return Calendar.getInstance().apply {

            timeInMillis = timestamp

            set(
                Calendar.DAY_OF_MONTH,
                1
            )

            set(
                Calendar.HOUR_OF_DAY,
                0
            )

            set(
                Calendar.MINUTE,
                0
            )

            set(
                Calendar.SECOND,
                0
            )

            set(
                Calendar.MILLISECOND,
                0
            )

        }.timeInMillis
    }

    fun startOfYear(
        timestamp: Long = System.currentTimeMillis()
    ): Long {

        return Calendar.getInstance().apply {

            timeInMillis = timestamp

            set(
                Calendar.MONTH,
                Calendar.JANUARY
            )

            set(
                Calendar.DAY_OF_MONTH,
                1
            )

            set(
                Calendar.HOUR_OF_DAY,
                0
            )

            set(
                Calendar.MINUTE,
                0
            )

            set(
                Calendar.SECOND,
                0
            )

            set(
                Calendar.MILLISECOND,
                0
            )

        }.timeInMillis
    }

    fun endOfDay(
        timestamp: Long = System.currentTimeMillis()
    ): Long {

        val calendar = Calendar.getInstance()

        calendar.timeInMillis =
            startOfDay(timestamp)

        calendar.add(
            Calendar.DAY_OF_YEAR,
            1
        )

        return calendar.timeInMillis
    }

    fun endOfWeek(
        timestamp: Long = System.currentTimeMillis()
    ): Long {

        val calendar = Calendar.getInstance()

        calendar.timeInMillis =
            startOfWeek(timestamp)

        calendar.add(
            Calendar.WEEK_OF_YEAR,
            1
        )

        return calendar.timeInMillis
    }

    fun endOfMonth(
        timestamp: Long = System.currentTimeMillis()
    ): Long {

        val calendar = Calendar.getInstance()

        calendar.timeInMillis =
            startOfMonth(timestamp)

        calendar.add(
            Calendar.MONTH,
            1
        )

        return calendar.timeInMillis
    }

    fun endOfYear(
        timestamp: Long = System.currentTimeMillis()
    ): Long {

        val calendar = Calendar.getInstance()

        calendar.timeInMillis =
            startOfYear(timestamp)

        calendar.add(
            Calendar.YEAR,
            1
        )

        return calendar.timeInMillis
    }

    fun startOfPrevMonth(
        timestamp: Long = System.currentTimeMillis()
    ): Long {

        return Calendar.getInstance().apply {

            timeInMillis = timestamp

            add(Calendar.MONTH, -1)

            set(Calendar.DAY_OF_MONTH, 1)

            set(Calendar.HOUR_OF_DAY, 0)

            set(Calendar.MINUTE, 0)

            set(Calendar.SECOND, 0)

            set(Calendar.MILLISECOND, 0)

        }.timeInMillis
    }

    fun endOfPrevMonth(
        timestamp: Long = System.currentTimeMillis()
    ): Long {

        val calendar = Calendar.getInstance()

        calendar.timeInMillis =
            startOfPrevMonth(timestamp)

        calendar.add(Calendar.MONTH, 1)

        return calendar.timeInMillis
    }
}
