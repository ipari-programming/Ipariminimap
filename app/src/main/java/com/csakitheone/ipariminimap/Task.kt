package com.csakitheone.ipariminimap

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.text.SpannableStringBuilder
import android.view.View
import android.widget.AdapterView
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import com.csakitheone.ipariminimap.helper.Notifications
import com.csakitheone.ipariminimap.services.RingService
import kotlinx.android.synthetic.main.layout_task.view.*

class Task() {
    var state: Boolean = false
    var condition: String = "minden óra elején"
    var action: String = "telefon rezgőre"
    var conditionPos: Int = 0
    var actionPos: Int = 0
    var data: String = ""

    val onModified: MutableList<(task: Task) -> Unit> = mutableListOf()

    constructor(text: String) : this() {
        val textParts = text.split(';')
        state = textParts[0] == "true"
        condition = textParts[1]
        conditionPos = textParts[2].toInt()
        action = textParts[3]
        actionPos = textParts[4].toInt()
        data = textParts[5]
    }

    fun execute(context: Context) {
        val audio = context.getSystemService(AudioManager::class.java)
        when(action) {
            "telefon rezgőre" -> audio.ringerMode = AudioManager.RINGER_MODE_VIBRATE
            "telefon normál hangerőre" -> audio.ringerMode = AudioManager.RINGER_MODE_NORMAL
            "app leállítása" -> context.stopService(Intent(context, RingService::class.java))
        }
        if (data.isNotEmpty()) Notifications.sendTaskNotification(context, data)
    }

    fun createLayout(activity: Activity) : View {
        val l = activity.layoutInflater.inflate(R.layout.layout_task, null, false)

        l.taskSwitch.text = "$condition $action $data"
        l.taskSwitch.isChecked = state
        l.taskSpinnerCondition.setSelection(conditionPos)
        l.taskSpinnerAction.setSelection(actionPos)
        l.taskEditData.text = SpannableStringBuilder(data)

        l.taskSwitch.setOnClickListener {
            state = l.taskSwitch.isChecked
            modify()
        }
        l.taskBtnEdit.setOnClickListener {
            l.taskLayoutEdit.visibility = if (l.taskLayoutEdit.visibility == View.GONE) View.VISIBLE else View.GONE
            l.taskBtnRemove.visibility = l.taskLayoutEdit.visibility
            l.taskBtnEdit.setImageDrawable(ContextCompat.getDrawable(activity, if (l.taskLayoutEdit.visibility == View.VISIBLE) R.drawable.ic_done else R.drawable.ic_edit))
            modify()
        }
        l.taskSpinnerCondition.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                condition = activity.resources.getStringArray(R.array.task_conditions)[position]
                conditionPos = position
                l.taskSwitch.text = "$condition $action $data"
            }
            override fun onNothingSelected(parent: AdapterView<*>?) { }
        }
        l.taskSpinnerAction.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                action = activity.resources.getStringArray(R.array.task_actions)[position]
                actionPos = position
                l.taskSwitch.text = "$condition $action $data"
            }
            override fun onNothingSelected(parent: AdapterView<*>?) { }
        }
        l.taskEditData.addTextChangedListener {
            data = it.toString()
            l.taskSwitch.text = "$condition $action $data"
        }

        return l
    }

    private fun modify() {
        onModified.map { r -> r(this) }
    }

    override fun toString(): String {
        return "$state;$condition;$conditionPos;$action;$actionPos;$data"
    }
}