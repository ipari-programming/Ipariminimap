package com.csakitheone.ipariminimap.data

import android.content.Context
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import java.util.*

class Timetable {
    companion object {
        var lessons: MutableList<String> = mutableListOf()
        var appointments: MutableList<Appointment> = mutableListOf()

        fun load(context: Context) {
            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
            lessons = prefs.getStringSet("timetable_lessons", setOf())?.toMutableList() ?: mutableListOf()
            appointments = prefs.getStringSet("timetable_appointments", setOf())?.map { Appointment(it) }?.toMutableList() ?: mutableListOf()
        }

        fun save(context: Context) {
            lessons.sort()
            appointments.sortBy { it.time }
            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
            prefs.edit {
                putStringSet("timetable_lessons", lessons.toSet())
                putStringSet("timetable_appointments", appointments.map { it.getSavableString() }.toSet())
                apply()
            }
        }
    }

    data class Appointment(
        val lesson: String,
        val day: Int,
        val time: Int
    ) {
        constructor(text: String) : this(
            text.split(';')[0],
            text.split(';')[1].toInt(),
            text.split(';')[2].toInt()
        )

        fun getDayString() : String {
            return when (day) {
                0 -> "Hétfő"
                1 -> "Kedd"
                2 -> "Szerda"
                3 -> "Csütörtök"
                4 -> "Péntek"
                5 -> "Szombat"
                else -> "Vasárnap"
            }
        }

        fun getDayCalendar() : Int {
            return when (day) {
                0 -> Calendar.MONDAY
                1 -> Calendar.TUESDAY
                2 -> Calendar.WEDNESDAY
                3 -> Calendar.THURSDAY
                4 -> Calendar.FRIDAY
                5 -> Calendar.SATURDAY
                else -> Calendar.SUNDAY
            }
        }

        fun getTimeString() : String {
            val h = time / 60
            val m = time % 60
            val hs = if (h < 10) "0$h" else h.toString()
            val ms = if (m < 10) "0$m" else m.toString()
            return "$hs:$ms"
        }

        fun getSavableString() : String {
            return "$lesson;$day;$time"
        }

        fun toStringNoDay() : String {
            return "${getTimeString()} - $lesson"
        }

        override fun toString(): String {
            return "$lesson - ${getDayString()} ${getTimeString()}"
        }
    }
}