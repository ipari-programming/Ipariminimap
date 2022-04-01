package com.csakitheone.ipariminimap.mercenaries

import android.content.Context
import android.content.Intent
import com.csakitheone.ipariminimap.MercGameActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class Story {
    class Mission(
        val storyName: String,
        val name: String,
        val enemies: List<Merc> = listOf(),
        val welcomeMessage: String = "",
    )
    companion object {

        val missions = listOf(
            Mission("Story", "Üdv az Ipariban!", listOf(Merc("Táncsicsos"), Merc("Lovassys", level = 2))),
        )

        fun selectMission(context: Context) {
            MaterialAlertDialogBuilder(context)
                .setTitle("Válassz egy küldetést")
                .setItems(missions.map { "${it.storyName} - ${it.name}" }.toTypedArray()) { _, i ->
                    context.startActivity(Intent(context, MercGameActivity::class.java).putExtra("missionIndex", i))
                }
                .setNegativeButton("Vissza") { _, _ -> }
                .create().show()
        }

    }
}