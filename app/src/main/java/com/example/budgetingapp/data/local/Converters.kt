package com.example.budgetingapp.data.local

import androidx.room.TypeConverter
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * Type converters for Room database.
 *
 * Room doesn't natively support LocalDate and LocalDateTime, so we need to tell it
 * how to convert these types to/from primitives that can be stored in SQLite.
 *
 * We convert:
 * - LocalDate <-> Long (epoch day)
 * - LocalDateTime <-> Long (epoch milliseconds)
 */
class Converters {

    /**
     * Convert LocalDate to Long for storage.
     * Stores the number of days since epoch (1970-01-01).
     */
    @TypeConverter
    fun fromLocalDate(date: LocalDate?): Long? {
        return date?.toEpochDay()
    }

    /**
     * Convert Long back to LocalDate.
     */
    @TypeConverter
    fun toLocalDate(epochDay: Long?): LocalDate? {
        return epochDay?.let { LocalDate.ofEpochDay(it) }
    }

    /**
     * Convert LocalDateTime to Long for storage.
     * Stores the number of milliseconds since epoch (1970-01-01T00:00:00Z).
     */
    @TypeConverter
    fun fromLocalDateTime(dateTime: LocalDateTime?): Long? {
        return dateTime?.atZone(ZoneId.systemDefault())?.toInstant()?.toEpochMilli()
    }

    /**
     * Convert Long back to LocalDateTime.
     */
    @TypeConverter
    fun toLocalDateTime(epochMilli: Long?): LocalDateTime? {
        return epochMilli?.let {
            LocalDateTime.ofInstant(Instant.ofEpochMilli(it), ZoneId.systemDefault())
        }
    }
}
