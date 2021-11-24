package com.csakitheone.ipariminimap.data

import kotlinx.coroutines.*
import java.net.URL

class Web {
    data class Event(var text: String, var date: String) {
        override fun toString(): String {
            return "$text\n\n- $date"
        }
    }

    data class Student(var name: String, var gradeMajor: String) {
        override fun toString(): String {
            return "$name - $gradeMajor"
        }
    }

    companion object {
        //#region Calendar
        private var events: MutableList<Event> = mutableListOf()

        @DelicateCoroutinesApi
        private fun downloadCalendar(callback: (Boolean) -> Unit) {
            events.clear()
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val html = URL("https://www.ipariszakkozep.hu/Esemenyek").readText()
                    val currentText = html.split("<div class=\"esemenyek-jelen\">")[1]
                        .split("</h3>")[1]
                        .split("</div>")[0]
                        .replace("<br>", "\n")
                    val currentDate = html.split("<div class=\"esemenyek-jelen\">")[1]
                        .split("<div class=\"idop\">")[1]
                        .split("</div>")[0]
                    events.add(Event(currentText, currentDate))
                    html.split("<h2>Következik:</h2>")[1]
                        .split("<tbody>")[1]
                        .split("</tbody>")[0]
                        .split("<tr>")
                        .map { r ->
                            val content = r//.replace("""<tr>|<td>|</td>|<div class="cim">|</div>""".toRegex(), "").trim()
                            events.add(Event(
                                content.split("<div class=\"idop\">")[0].replace("<br>", "\n"),
                                content.split("<div class=\"idop\">")[1]
                            ))
                        }
                }
                catch (ex: Exception) { }
                callback(events.isNotEmpty())
            }
        }

        fun getCalendar(callback: (MutableList<Event>) -> Unit) {
            if (events.isEmpty()) {
                downloadCalendar {
                    callback(events)
                }
            }
            else callback(events)
        }
        //#endregion

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
                                .split("<ol class=\"tk2\">")[1]
                                .split("</ol>")[0]
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