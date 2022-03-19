package com.csakitheone.ipariminimap

import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.csakitheone.ipariminimap.databinding.ActivityKreszBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class KreszActivity : AppCompatActivity() {

    lateinit var binding: ActivityKreszBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityKreszBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    fun kreszBtnStartExam(view: View) {
        binding.kreszLayoutRules.visibility = View.GONE
        binding.kreszLayoutExam.visibility = View.VISIBLE
    }

    fun kreszBtnScoreClick(view: View) {
        var score = 0
        if (binding.kreszRadio1.isChecked) score++
        if (binding.kreszRadio2.isChecked) score++
        if (binding.kreszRadio3.isChecked) score++
        if (binding.kreszRadio4.isChecked) score++
        if (binding.kreszRadio5.isChecked) score++

        MaterialAlertDialogBuilder(this)
            .setTitle("KRESZ teszt eredménye")
            .setMessage("$score/5 pont")
            .setPositiveButton("Szabályok") { _: DialogInterface, _: Int ->
                binding.kreszLayoutRules.visibility = View.VISIBLE
                binding.kreszLayoutExam.visibility = View.GONE
            }
            .setNegativeButton("Kilépés") { _: DialogInterface, _: Int ->
                finish()
            }
            .create().show()
    }
}