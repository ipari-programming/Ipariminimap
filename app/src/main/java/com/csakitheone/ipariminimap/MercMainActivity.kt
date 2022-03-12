package com.csakitheone.ipariminimap

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class MercMainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_merc_main)
    }

    fun onBtnStartClick(view: View) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Készülj fel a harcra!")
            .setMessage("Válassz nehézségi szintet:")
            .setPositiveButton("Könnyed") { _, _ -> }
            .setNegativeButton("Kihívás") { _, _ -> }
            .setNeutralButton("Mégsem") { _, _ -> }
            .create().show()
    }

    fun onBtnPvpClick(view: View) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Szeretnél más emberek ellen játszani?")
            .setMessage("Ez a mód még nincs kész, de lesz rá lehetőség.")
            .setPositiveButton("Kösz Csáki") { _, _ -> }
            .create().show()
    }
}