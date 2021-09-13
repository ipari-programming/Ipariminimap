package com.csakitheone.ipariminimap

import android.app.TimePickerDialog
import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.view.setPadding
import com.csakitheone.ipariminimap.data.Timetable
import kotlinx.android.synthetic.main.activity_timetable.*
import org.w3c.dom.Text
import java.sql.Time

class TimetableActivity : AppCompatActivity() {
    var selectedLesson = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timetable)
    }

    override fun onResume() {
        super.onResume()
        Timetable.load(this)
        refreshLessons()
    }

    fun refreshLessons() {
        timetableLayoutLessons.removeAllViews()
        for (l in Timetable.lessons) {
            val text = TextView(this)
            text.text = if (selectedLesson == l) "* $l" else l
            text.setPadding(20)
            text.setOnClickListener {
                selectedLesson = (it as TextView).text.toString()
                refreshAppointments()
                refreshLessons()
            }
            text.setOnLongClickListener {
                if (selectedLesson == (it as TextView).text.toString()) {
                    selectedLesson = ""
                    refreshAppointments()
                }
                Timetable.appointments.removeAll { r -> r.lesson == it.text.toString() }
                Timetable.lessons.remove(it.text.toString())
                refreshLessons()
                true
            }
            timetableLayoutLessons.addView(text)
        }
        Timetable.save(this)
    }

    fun refreshAppointments() {
        timetableCardAppointments.visibility = if (Timetable.lessons.any { it == selectedLesson }) View.VISIBLE else View.GONE
        timetableTextAppointment.text = "$selectedLesson óra időpontjai"
        timetableLayoutAppointments.removeAllViews()
        for (a in Timetable.appointments.filter { r -> r.lesson == selectedLesson }) {
            val text = TextView(this)
            text.text = a.toString()
            text.setPadding(20)
            text.setOnLongClickListener {
                Timetable.appointments.removeAll { r -> r.toString() == (it as TextView).text.toString().replace("*", "").trim() }
                refreshAppointments()
                true
            }
            timetableLayoutAppointments.addView(text)
        }
        Timetable.save(this)
    }

    fun btnAddLessonClick(view: View) {
        val edit = EditText(this)
        edit.hint = "Óra neve"

        AlertDialog.Builder(this)
            .setTitle("Új óra")
            .setView(edit)
            .setPositiveButton("Hozzáadás") { _: DialogInterface, _: Int ->
                Timetable.lessons.add(edit.text.toString())
                refreshLessons()
            }
            .setNegativeButton("Mégsem") { _: DialogInterface, _: Int -> }
            .create().show()
    }

    fun btnAddAppointmentClick(view: View) {
        var timeInt = 8 * 60

        val layout = LinearLayout(this)
        val spinner = Spinner(this)
        layout.addView(spinner)
        spinner.adapter = ArrayAdapter(this, R.layout.support_simple_spinner_dropdown_item, resources.getStringArray(R.array.day_of_week))
        (spinner.layoutParams as LinearLayout.LayoutParams).apply {
            weight = 1f
        }
        val button = Button(this)
        layout.addView(button)
        button.text = "8:00"
        (button.layoutParams as LinearLayout.LayoutParams).apply {
            weight = 1f
        }
        button.setOnClickListener {
            TimePickerDialog(this, { timePicker: TimePicker, h: Int, m: Int ->
                timeInt = h * 60 + m
                val hs = if (h < 10) "0$h" else h.toString()
                val ms = if (m < 10) "0$m" else m.toString()
                button.text = "$hs:$ms"
            }, 8, 0, true).show()
        }

        AlertDialog.Builder(this)
            .setTitle("Új időpont $selectedLesson órához")
            .setView(layout)
            .setPositiveButton("Hozzáadás") { _: DialogInterface, _: Int ->
                Timetable.appointments.add(Timetable.Appointment(selectedLesson, spinner.selectedItemPosition, timeInt))
                refreshAppointments()
            }
            .setNegativeButton("Mégsem") { _: DialogInterface, _: Int -> }
            .create().show()
    }
}