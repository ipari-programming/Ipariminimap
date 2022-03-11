package com.csakitheone.ipariminimap.data

import kotlinx.coroutines.*
import java.net.URL

class Web {
    data class Student(var name: String, var gradeMajor: String) {
        override fun toString(): String {
            return "$name - $gradeMajor"
        }
    }

    companion object {

        @DelicateCoroutinesApi
        fun getNameDay(callback: (String) -> Unit) {
            GlobalScope.launch(Dispatchers.IO) {
                val name = URL("https://mai-nevnap.hu/")
                    .readText()
                    .substringAfter("<h2>")
                    .substringAfter("\">")
                    .substringBefore("</a>")
                callback(name)
            }
        }

        //#region Students
        private var students: MutableList<Student> = mutableListOf()

        @DelicateCoroutinesApi
        private fun downloadStudents(callback: (Boolean) -> Unit) {
            students.clear()
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
                        }
                        catch (ex: Exception) { }
                    }
                }
                callback(students.isNotEmpty())
            }
        }

        fun getStudentsNoDownload(): MutableList<Student> = students

        fun getStudents(callback: (MutableList<Student>) -> Unit) {
            if (students.isEmpty()) {
                downloadStudents {
                    callback(students)
                }
            }
            else callback(students)
        }
        //#endregion

    }
}