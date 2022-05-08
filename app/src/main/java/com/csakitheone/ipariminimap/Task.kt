package com.csakitheone.ipariminimap

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.text.SpannableStringBuilder
import android.view.View
import android.widget.AdapterView
import androidx.annotation.NonNull
import androidx.core.widget.addTextChangedListener
import com.csakitheone.ipariminimap.databinding.LayoutTaskBinding
import com.csakitheone.ipariminimap.helper.Notifications
import com.csakitheone.ipariminimap.services.RingService
import com.google.gson.annotations.Expose
import kotlin.random.Random

class Task() {
    var state: Boolean = false
    var condition: String = "minden óra elején"
    var action: String = "telefon rezgőre"
    var conditionPos: Int = 0
    var actionPos: Int = 0
    var data: String = ""

    @Expose(serialize = false, deserialize = false)
    var onModified: MutableList<(task: Task) -> Unit> = mutableListOf()

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

    fun createLayout(activity: Activity): LayoutTaskBinding {
        val l = activity.layoutInflater.inflate(R.layout.layout_task, null, false)
        val binding = LayoutTaskBinding.bind(l)

        binding.taskSwitch.text = "$condition $action $data"
        binding.taskSwitch.isChecked = state
        binding.taskSpinnerCondition.setSelection(conditionPos)
        binding.taskSpinnerAction.setSelection(actionPos)
        binding.taskEditData.text = SpannableStringBuilder(data)

        binding.taskSwitch.setOnClickListener {
            state = binding.taskSwitch.isChecked
            modify()
        }
        binding.taskSpinnerCondition.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                condition = activity.resources.getStringArray(R.array.task_conditions)[position]
                conditionPos = position
                binding.taskSwitch.text = "$condition $action $data"
                modify()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) { }
        }
        binding.taskSpinnerAction.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                action = activity.resources.getStringArray(R.array.task_actions)[position]
                actionPos = position
                binding.taskSwitch.text = "$condition $action $data"
                modify()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) { }
        }
        binding.taskEditData.addTextChangedListener {
            data = it.toString()
            binding.taskSwitch.text = "$condition $action $data"
            modify()
        }

        return binding
    }

    private fun modify() {
        onModified.map { r -> r(this) }
    }

    override fun toString(): String {
        return "$state;$condition;$conditionPos;$action;$actionPos;$data"
    }
}