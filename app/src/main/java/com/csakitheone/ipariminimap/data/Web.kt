package com.csakitheone.ipariminimap.data

import kotlinx.coroutines.*
import java.net.URL

class Web {
    data class Student(var name: String, var gradeMajor: String)

    companion object {
        private var students: List<Student> = listOf()

        @DelicateCoroutinesApi
        private fun downloadStudents(callback: (Boolean) -> Unit) {
            val temp = mutableListOf<Student>()
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val raw = URL("https://www.ipariszakkozep.hu/09A")
                        .readText()
                        .split("<ol class=\"tk2\">")[1]
                        .split("</ol>")[0]
                        .replace("<li>|</li>".toRegex(), "")
                        .split("\n")
                        .filter { r -> r.trim().isNotBlank() }
                        .map { r -> temp.add(Student(r.trim(), "9.A")) }
                    students = temp
                    callback(true)
                }
                catch (ex: Exception) {
                    callback(false)
                }
            }
        }

        fun getStudents(callback: (List<Student>) -> Unit) {
            if (students.isEmpty()) {
                downloadStudents {
                    callback(students)
                }
            }
            else callback(students)
        }
    }
}