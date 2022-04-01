package com.csakitheone.ipariminimap.mercenaries

import com.csakitheone.ipariminimap.data.Web
import org.json.JSONObject

class Merc(
    var name: String = "",
    var mercClass: MercClass = MercClass.classStudent,
    var level: Int = 1,
) {
    private var currentAttack = 0
    private var currentHealth = 0

    var selectedAbility: Ability? = null
    var abilities = mutableListOf<Ability>()
    var forceAutoAttack = false

    fun isAlive(): Boolean = currentHealth > 0

    fun refreshData() {
        selectedAbility = null
        mercClass = MercClass.getAll().first { it.id == mercClass.id }
        abilities = abilities.mapNotNull { ability -> mercClass.abilities.firstOrNull { it.name == ability.name } }.toMutableList()
    }

    fun prepareForGame() {
        currentAttack = getMaxAttack()
        currentHealth = getMaxHealth()
        abilities.map { it.prepareForGame(level) }
    }

    fun learnAllAbilities() {
        abilities = mercClass.abilities
    }

    fun getCurrentAttack(): Int = currentAttack
    fun getCurrentHealth(): Int = currentHealth

    fun getMaxAttack(): Int = mercClass.levelUpAttack * (level - 1) + mercClass.baseAttack
    fun getMaxHealth(): Int = mercClass.levelUpHealth * (level - 1) + mercClass.baseHealth

    fun weaken(amount: Int) {
        if (currentAttack - amount > -1) currentAttack -= amount
        else currentAttack = 0
    }
    fun strengthen(amount: Int) {
        currentAttack += amount
    }

    fun damage(amount: Int): Boolean {
        currentHealth -= amount
        return currentHealth <= 0
    }
    fun heal(amount: Int) {
        if (!isAlive()) return
        currentHealth += amount
        if (currentHealth > getMaxHealth()) currentHealth = getMaxHealth()
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
}