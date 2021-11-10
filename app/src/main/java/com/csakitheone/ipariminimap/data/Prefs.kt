package com.csakitheone.ipariminimap.data

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import com.csakitheone.ipariminimap.Task

class Prefs {
    companion object {
        private lateinit var prefs: SharedPreferences

        fun init(context: Context) {
            prefs = PreferenceManager.getDefaultSharedPreferences(context.applicationContext)

            setNightTheme(prefs.getInt("night_mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM))
        }

        fun setNightTheme(nightMode: Int) {
            AppCompatDelegate.setDefaultNightMode(nightMode)
            prefs.edit {
                putInt("night_mode", nightMode)
                apply()
            }
        }

        fun getTasks(): MutableList<Task> {
            return prefs.getStringSet("tasks", setOf())?.map { r -> Task(r) }?.toMutableList() ?: mutableListOf()
        }

        fun setTasks(tasks: List<Task>) {
            prefs.edit().putStringSet("tasks", tasks.map { r -> r.toString() }.toSet()).apply()
        }
    }
}