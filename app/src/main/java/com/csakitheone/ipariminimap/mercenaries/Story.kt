package com.csakitheone.ipariminimap.mercenaries

import android.content.Context
import android.content.Intent
import com.csakitheone.ipariminimap.MercGameActivity
import com.csakitheone.ipariminimap.data.Web
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class Story {
    data class Mission(
        var storyName: String,
        var name: String,
        var enemies: List<Merc> = listOf(),
        var welcomeMessage: String = "",
    ) {
        companion object {
            const val MERC_REPLACE_WITH_STUDENT_ONE_ABILITY = "{replaceWithStudent}"
        }
    }
    companion object {

        private val missions = listOf(
            Mission(
                "Story",
                "Üdv az Ipariban!",
                listOf(
                    Merc("Felvételi felügyelő", level = 2)
                ),
                "Minden kör 2 részből áll: felkészülés és harc." +
                        "A felkészülés szakaszban állítsd be, hogy mit tegyenek a csapatod tagjai. " +
                        "Támadjanak? Gyógyítsanak? Ki legyen a célpont? " +
                        "Miután megvannak az utasítások kezdődhet a harc. " +
                        "Alul a naplóban láthatod a csata történéseit."
            ),
            Mission(
                "Story",
                "Beilleszkedés",
                listOf(
                    Merc(Mission.MERC_REPLACE_WITH_STUDENT_ONE_ABILITY),
                    Merc(Mission.MERC_REPLACE_WITH_STUDENT_ONE_ABILITY),
                ),
                "Vannak esetek, amikor veled szemben sincsenek egyedül."
            ),
            Mission(
                "Story",
                "Emelt szint",
                listOf(
                    Merc(Mission.MERC_REPLACE_WITH_STUDENT_ONE_ABILITY, level = 2),
                    Merc(Mission.MERC_REPLACE_WITH_STUDENT_ONE_ABILITY),
                ),
            ),
            Mission(
                "Story",
                "Kisérettségi",
                listOf(
                    Merc("Érettségiztető tanár", level = 3),
                    Merc("Felügyelő tanár", level = 2),
                ),
            ),
            Mission(
                "Story",
                "Nyakunkon az érettségi",
                listOf(
                    Merc("Érettségiztető tanár", level = 3),
                    Merc("Vizsgabiztos", level = 4),
                    Merc("Felügyelő tanár", level = 2),
                ),
            ),
            Mission(
                "Story",
                "Technikás technikum",
                listOf(
                    Merc("Szakdolgozat", level = 5),
                    Merc("Dokumentáció", level = 5),
                ),
            ),
            Mission(
                "Gyakorlás",
                "Egyenrangú PvE",
                listOf(
                    Merc(Mission.MERC_REPLACE_WITH_STUDENT_ONE_ABILITY),
                    Merc(Mission.MERC_REPLACE_WITH_STUDENT_ONE_ABILITY),
                    Merc(Mission.MERC_REPLACE_WITH_STUDENT_ONE_ABILITY),
                ),
            ),
            Mission(
                "Gyakorlás",
                "Igazságtalan PvE",
                listOf(
                    Merc(Mission.MERC_REPLACE_WITH_STUDENT_ONE_ABILITY, level = 3),
                    Merc(Mission.MERC_REPLACE_WITH_STUDENT_ONE_ABILITY, level = 4),
                    Merc(Mission.MERC_REPLACE_WITH_STUDENT_ONE_ABILITY, level = 3),
                ),
            ),
        )

        fun getMissionByIndex(index: Int): Mission {
            val mission = missions[index].copy()

            mission.enemies = mission.enemies.map {
                val level = it.level
                if (it.name == Mission.MERC_REPLACE_WITH_STUDENT_ONE_ABILITY) {
                    Merc.createFromStudent(Web.getStudentsNoDownload().random()).apply {
                        abilities = mutableListOf(this.mercClass.abilities.random())
                        this.level = level
                    }
                }
                else it
            }

            return mission
        }

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