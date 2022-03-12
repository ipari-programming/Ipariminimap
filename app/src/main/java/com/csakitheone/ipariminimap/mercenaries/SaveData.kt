package com.csakitheone.ipariminimap.mercenaries

import android.app.Activity
import com.csakitheone.ipariminimap.data.Prefs
import com.csakitheone.ipariminimap.data.Web
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class SaveData {

    var collection = mutableListOf<Merc>()
    var team = mutableListOf<Merc>()

    companion object {

        var instance = SaveData()

        fun Merc.isInTeam(): Boolean {
            return instance.team.any { it.name == this.name && it.mercClass.name == this.mercClass.name }
        }

        fun isTeamReady(): Boolean = instance.team.size == 3

        fun save() {
            Prefs.setMercenariesSaveData(instance)
        }

        fun load() {
            instance = Prefs.getMercenariesSaveData()
        }

        fun setup(activity: Activity, onSuccess: () -> Unit) {
            if (!instance.collection.isNullOrEmpty()) return

            MaterialAlertDialogBuilder(activity)
                .setTitle("Üdv a Mercenaries játékban!")
                .setMessage("Mindent elmagyarázok, de először kell toboroznunk neked egy csapatot.")
                .setPositiveButton("Kezdjük!") { _, _ ->

                    val selectedMercs = mutableListOf<Merc>()

                    fun showSelectMercDialog() {
                        MaterialAlertDialogBuilder(activity)
                            .setTitle("Válassz szakot! (${selectedMercs.size}/3)")
                            .setItems(arrayOf(
                                "A: gyengítik az ellenséget",
                                "B: állatokat idéznek, akiket szintén irányíthatsz",
                                "C: erősítik a barátokat",
                                "D: erősek + gyorsak vagy védenek",
                                "E: robotokat építenek, amik maguktól küzdenek",
                                "F: A és D egyben, csak kicsit gyengébben",
                                "G/Ny: gyógyítanak",
                            )) { _, majorIndex ->
                                val major = listOf("a", "b", "c", "d", "e", "f", "gny")[majorIndex]
                                MaterialAlertDialogBuilder(activity)
                                    .setTitle("Válassz egy diákot! (${selectedMercs.size}/3)")
                                    .setItems(Web.getStudentsNoDownload().filter { major.contains(it.getMajor().toLowerCase()) }.map { it.toString() }.toTypedArray()) { _, i ->
                                        selectedMercs.add(Merc.createFromStudent(Web.getStudentsNoDownload().filter { major.contains(it.getMajor().toLowerCase()) }[i]))
                                        if (selectedMercs.size < 3) showSelectMercDialog()
                                        else {
                                            instance.collection.addAll(selectedMercs)
                                            instance.team.addAll(selectedMercs)
                                            onSuccess()
                                        }
                                    }
                                    .setOnCancelListener {
                                        setup(activity) { onSuccess() }
                                    }
                                    .create().show()
                            }
                            .setOnCancelListener {
                                setup(activity) { onSuccess() }
                            }
                            .create().show()
                    }

                    showSelectMercDialog()
                }
                .setNegativeButton("Mégsem") { _, _ ->
                    activity.finish()
                }
                .setCancelable(false)
                .create().show()
        }

    }

}