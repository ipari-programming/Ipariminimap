package com.csakitheone.ipariminimap.mercenaries

import com.csakitheone.ipariminimap.data.Web
import org.json.JSONObject

class Merc(
    var name: String = "Iparis diák",
    var mercClass: MercClass = MercClass.classStudent,
    var level: Int = 1,
) {
    private var currentAttack = 0
    private var currentHealth = 0

    fun prepareForGame() {
        currentAttack = getMaxAttack()
        currentHealth = getMaxHealth()
    }

    fun getCurrentAttack(): Int = currentAttack
    fun getCurrentHealth(): Int = currentHealth

    fun getMaxAttack(): Int = mercClass.levelUpAttack * (level - 1) + mercClass.baseAttack
    fun getMaxHealth(): Int = mercClass.levelUpHealth * (level - 1) + mercClass.baseHealth

    fun weaken(amount: Int) {
        currentAttack -= amount
    }
    fun strengthen(amount: Int) {
        currentAttack += amount
    }

    fun damage(amount: Int) {
        currentHealth -= amount
    }
    fun heal(amount: Int) {
        currentHealth += amount
    }

    override fun toString(): String {
        return mapOf(
            "name" to name,
            "class" to mercClass.name,
            "level" to level,
            "attack" to getMaxAttack(),
            "health" to getMaxHealth(),
        ).toString()
    }

    companion object {

        fun createFromStudent(student: Web.Student): Merc {
            return Merc(student.name, MercClass.fromMajor(student.getMajor()))
        }

    }

    class MercClass(
        val name: String,
        val baseAttack: Int,
        val baseHealth: Int,
        val levelUpAttack: Int = 1,
        val levelUpHealth: Int = 2,
    ) {
        companion object {

            val classStudent = MercClass("Diák", 2, 20)

            val classVegyesz =     MercClass("Vegyész",     1, 20) // 21 caster
            val classKornyezetes = MercClass("Környezetes", 2, 15) // 17 summoner
            val classInfos =       MercClass("Infós",       1, 20) // 21 caster
            val classGepesz =      MercClass("Gépész",      4, 20, 2, 3) // 24 tank
            val classMechas =      MercClass("Mechás",      2, 15) // 17 summoner
            val classMuanyagos =   MercClass("Műanyagos",   3, 18) // 21 caster
            val classGondozo =     MercClass("Gondozó",     1, 15) // 16 healer

            val classCompanionAnimal = MercClass("Háziállat", 2, 5, 1, 1)
            val classBird = MercClass("Madár", 2, 4, 1, 1)
            val classRobot = MercClass("Robot", 2, 6, 1, 1)

            val summonPool = listOf(classCompanionAnimal, classBird, classRobot)

            fun fromMajor(major: String): MercClass {
                return when (major.toLowerCase()) {
                    "a" -> classVegyesz
                    "b" -> classKornyezetes
                    "c" -> classInfos
                    "d" -> classGepesz
                    "e" -> classMechas
                    "f" -> classMuanyagos
                    "g" -> classGondozo
                    "ny" -> classGondozo
                    else -> classStudent
                }
            }

        }
    }
}