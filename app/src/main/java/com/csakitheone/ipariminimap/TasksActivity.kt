package com.csakitheone.ipariminimap

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.view.setMargins
import com.csakitheone.ipariminimap.data.Prefs
import com.csakitheone.ipariminimap.helper.Helper.Companion.toPx
import com.csakitheone.ipariminimap.services.RingService
import kotlinx.android.synthetic.main.activity_tasks.*
import kotlinx.android.synthetic.main.layout_task.view.*

class TasksActivity : AppCompatActivity() {

    private var tasks = mutableListOf<Task>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tasks)
    }

    override fun onResume() {
        super.onResume()

        runServiceIfAllowed()
        tasksSwitch.isChecked = Prefs.getIsServiceAllowed()
        refreshTasks()
    }

    private fun runServiceIfAllowed() {
        if (Prefs.getIsServiceAllowed()) {
            ContextCompat.startForegroundService(this, Intent(this, RingService::class.java))
        }
        else {
            stopService(Intent(this, RingService::class.java))
        }
    }

    private fun refreshTasks() {
        tasks = Prefs.getTasks()
        tasksLayout.removeAllViews()
        tasks.map {
            val v = it.createLayout(this)
            v.taskBtnRemove.setOnClickListener { _ ->
                tasks.remove(it)
                saveTasks()
                refreshTasks()
            }
            tasksLayout.addView(v)
            (v.layoutParams as LinearLayout.LayoutParams).apply { setMargins(8.toPx.toInt()) }
            it.onModified.add { saveTasks() }
        }
        saveTasks()
    }

    private fun saveTasks() {
        Prefs.setTasks(tasks)
    }

    fun onSwitchServiceClick(view: View) {
        Prefs.setIsServiceAllowed(tasksSwitch.isChecked)
        runServiceIfAllowed()
    }

    fun onBtnNewTaskClick(view: View) {
        tasks.add(Task())
        saveTasks()
        refreshTasks()
    }
}