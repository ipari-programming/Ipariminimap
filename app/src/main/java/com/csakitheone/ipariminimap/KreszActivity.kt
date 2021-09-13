package com.csakitheone.ipariminimap

import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import kotlinx.android.synthetic.main.activity_kresz.*

class KreszActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_kresz)
    }

    fun kreszBtnStartExam(view: View) {
        kreszLayoutRules.visibility = View.GONE
        kreszLayoutExam.visibility = View.VISIBLE
    }

    fun kreszBtnScoreClick(view: View) {
        var score = 0
        if (kreszRadio1.isChecked) score++
        if (kreszRadio2.isChecked) score++
        if (kreszRadio3.isChecked) score++
        if (kreszRadio4.isChecked) score++
        if (kreszRadio5.isChecked) score++

        if (score >= 5) Badge.userAdd(this, Badge.BADGE_KRESZ.toString())

        AlertDialog.Builder(this)
            .setTitle("KRESZ teszt eredménye")
            .setMessage("$score/5 pont")
            .setPositiveButton("Szabályok") { _: DialogInterface, _: Int ->
                kreszLayoutRules.visibility = View.VISIBLE
                kreszLayoutExam.visibility = View.GONE
            }
            .setNegativeButton("Kilépés") { _: DialogInterface, _: Int ->
                finish()
            }
            .create().show()
    }
}