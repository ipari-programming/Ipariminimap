package com.csakitheone.ipariminimap.mercenaries

import android.app.Activity
import android.app.SharedElementCallback
import android.content.Context
import com.csakitheone.ipariminimap.data.Prefs
import com.csakitheone.ipariminimap.data.Web
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class SaveData {

    var collection = mutableListOf<Merc>()
    var team = mutableListOf<Merc>()

    companion object {

        var instance = SaveData()

        fun save() {
            Prefs.setMercenariesSaveData(instance)
        }

        fun load() {
            instance = Prefs.getMercenariesSaveData()
            instance.collection.map { it.refreshData() }
            instance.team.map { it.refreshData() }
        }

        fun Merc.isInTeam(): Boolean {
            return instance.team.any { it.name == this.name && it.mercClass.name == this.mercClass.name }
        }

        fun isTeamReady(): Boolean = instance.team.size == 3

        fun addToCollection(context: Context, merc: Merc, callback: () -> Unit = {}) {
            MaterialAlertDialogBuilder(context)
                .setTitle("Válassz első képességet ${merc.name} számára!")
                .setCancelable(false)
                .setItems(merc.mercClass.abilities.map { it.toString() }.toTypedArray()) { _, i ->
                    merc.abilities.add(merc.mercClass.abilities[i])
                    instance.collection.add(merc)
                    callback()
                }
                .create().show()
        }

        fun setup(activity: Activity, onSuccess: () -> Unit) {
            if (!instance.collection.isNullOrEmpty()) return

            MaterialAlertDialogBuilder(activity)
                .setTitle("Üdv a Mercenaries játékban!")
                .setMessage("Mindent elmagyarázok, de először kell toboroznunk neked embereket.")
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
                                    .setItems(Prefs.getStudentsCache().filter { major.contains(it.getMajor().toLowerCase()) }.map { it.toString() }.toTypedArray()) { _, i ->
                                        selectedMercs.add(Merc.createFromStudent(Prefs.getStudentsCache().filter { major.contains(it.getMajor().toLowerCase()) }[i]))
                                        if (selectedMercs.size < 3) showSelectMercDialog()
                                        else {
                                            selectedMercs.map {
                                                addToCollection(activity, it) { onSuccess() }
                                            }
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