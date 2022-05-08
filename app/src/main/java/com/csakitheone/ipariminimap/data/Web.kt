package com.csakitheone.ipariminimap.data

import kotlinx.coroutines.*
import org.json.JSONObject
import java.net.URL

class Web {
    data class Student(var name: String, var gradeMajor: String) {
        fun getGrade(): Int = gradeMajor.substringBefore('.').toIntOrNull() ?: 9

        fun getMajor(): String = gradeMajor.substringAfter('.')

        override fun toString(): String {
            return "$name - $gradeMajor"
        }
    }

    companion object {

        @DelicateCoroutinesApi
        fun getNameDay(callback: (List<String>) -> Unit) {
            GlobalScope.launch(Dispatchers.IO) {
                val response = JSONObject(URL("https://api.nevnapok.eu/ma").readText())
                val jsonArray = response.getJSONArray(response.keys().next())
                val names = mutableListOf<String>()
                for (i in 0 until jsonArray.length()) {
                    names.add(jsonArray.getString(i))
                }
                callback(names)
            }
        }

        //#region Students
        @DelicateCoroutinesApi
        private fun downloadStudents(callback: (Boolean) -> Unit, progressCallback: (Int) -> Unit) {
            val students = mutableListOf<Student>()
            GlobalScope.launch(Dispatchers.IO) {
                for (grade in 9..13) {
                    for (major in listOf("A", "B", "C", "D", "E", "F", "G", "NY")) {
                        val temp = mutableListOf<Student>()
                        val gradeMajor = "${if (grade == 9) "09" else grade}$major"
                        try {
                            URL("https://www.ipariszakkozep.hu/$gradeMajor")
                                .readText()
                                .substringAfter("<ol class=\"tk2\">")
                                .substringBefore("</ol>")
                                .replace("<li>|</li>".toRegex(), "")
                                .split("\n")
                                .filter { r -> r.trim().isNotBlank() }
                                .map { r -> temp.add(Student(r.trim(), "$grade.$major")) }
                            students.addAll(temp)
                            progressCallback(students.size)
                        }
                        catch (ex: Exception) { }
                    }
                }
                Prefs.setStudentsCache(students)
                callback(students.isNotEmpty())
            }
        }

        fun getStudents(forceDownload: Boolean = false, callback: (List<Student>) -> Unit) {
            getStudents(forceDownload, callback, {})
        }

        fun getStudents(forceDownload: Boolean = false, callback: (List<Student>) -> Unit, progressCallback: (Int) -> Unit) {
            if (Prefs.getStudentsCache().isEmpty() || forceDownload) {
                downloadStudents({ callback(Prefs.getStudentsCache()) }, progressCallback)
            }
            else callback(Prefs.getStudentsCache())
        }
        //#endregion

    }
}