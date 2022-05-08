package com.csakitheone.ipariminimap.data

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.csakitheone.ipariminimap.Task
import com.csakitheone.ipariminimap.helper.Rings
import com.csakitheone.ipariminimap.mercenaries.SaveData
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken

class Prefs {
    companion object {
        private lateinit var prefs: SharedPreferences

        fun init(context: Context) {
            prefs = PreferenceManager.getDefaultSharedPreferences(context.applicationContext)
        }

        fun getIsUsingDynamicColors(): Boolean = prefs.getBoolean("is_using_dynamic_colors", true)
        fun setIsUsingDynamicColors(value: Boolean) = prefs.edit()
            .putBoolean("is_using_dynamic_colors", value).apply()

        fun getIsServiceAllowed(): Boolean = prefs.getBoolean("is_service_allowed", false)
        fun setIsServiceAllowed(value: Boolean) = prefs.edit()
            .putBoolean("is_service_allowed", value).apply()

        fun getMercenariesSaveData(): SaveData = Gson().fromJson(
            prefs.getString("mercenaries_save_data", null) ?: Gson().toJson(SaveData()), SaveData::class.java
        )
        fun setMercenariesSaveData(saveData: SaveData) = prefs.edit()
            .putString("mercenaries_save_data", Gson().toJson(saveData)).apply()

        fun getTasks(): MutableList<Task> {
            return try {
                GsonBuilder()
                    .excludeFieldsWithoutExposeAnnotation()
                    .create()
                    .fromJson(
                    prefs.getString("tasks", "[]") ?: "[]",
                    object : TypeToken<MutableList<Task>>() {}.type
                )
            }
            catch (ex: Exception) {
                prefs.getStringSet("tasks", setOf())?.map { Task(it) }?.toMutableList() ?: mutableListOf()
            }
        }
        fun setTasks(tasks: List<Task>) = prefs.edit()
            .putString("tasks", Gson().toJson(tasks)).apply()

        fun getIsAdmin(): Boolean = prefs.getBoolean("is_admin", false)
        fun setIsAdmin(value: Boolean, password: String = ""): Boolean {
            if (value && password != Rings.getCurrentLesson()) return false
            prefs.edit().putBoolean("is_admin", value).apply()
            return true
        }

        fun getStudentsCache(): List<Web.Student> = Gson().fromJson(
            prefs.getString("students_cache", null) ?: "[]",
            object : TypeToken<List<Web.Student>>() {}.type
        )
        fun setStudentsCache(students: List<Web.Student>) = prefs.edit()
            .putString("students_cache", Gson().toJson(students)).apply()
    }
}