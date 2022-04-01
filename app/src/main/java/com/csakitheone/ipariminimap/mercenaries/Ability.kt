package com.csakitheone.ipariminimap.mercenaries

class Ability(
    var name: String,
    var speed: Int = 5,
    var description: String = "",
    var baseVariables: Map<String, Int> = mapOf(),
) {
    private var requireChoice = false

    var variables = mutableMapOf<String, Int>()
    var code = ""
    var target: Merc? = null

    fun doRequireChoice(): Boolean = requireChoice

    fun prepareForGame(level: Int) {
        variables = mutableMapOf()
        variables.putAll(baseVariables)
        for (v in variables.keys) {
            variables[v] = (variables[v] ?: 0) + ((level - 1) * 2)
        }
    }

    fun attack(target: String, amount: String = varMercAttack()): Ability {
        if (target == Target.CHOICE) requireChoice = true
        code += "$ACTION_ATTACK $target $amount;"
        return this
    }
    fun heal(target: String, amount: String): Ability {
        if (target == Target.CHOICE) requireChoice = true
        code += "$ACTION_HEAL $target $amount;"
        return this
    }
    fun weaken(target: String, amount: String): Ability {
        if (target == Target.CHOICE) requireChoice = true
        code += "$ACTION_WEAKEN $target $amount;"
        return this
    }
    fun strengthen(target: String, amount: String): Ability {
        if (target == Target.CHOICE) requireChoice = true
        code += "$ACTION_STRENGTHEN $target $amount;"
        return this
    }
    fun stun(target: String): Ability {
        if (target == Target.CHOICE) requireChoice = true
        code += "$ACTION_STUN $target;"
        return this
    }
    fun summon(mercClass: MercClass, vararg tags: String = arrayOf()): Ability {
        code += "$ACTION_SUMMON ${mercClass.id} ${tags.joinToString()};"
        return this
    }
    fun loop(): Ability {
        code += "$ACTION_LOOP;"
        return this
    }

    override fun toString(): String {
        return "(⏳$speed) $name: $description $variables"
    }

    companion object {
        fun varMercAttack(): String = "{merc.attack}"
        fun varAmount(): String = "{amount}"

        const val ACTION_ATTACK = "attack"
        const val ACTION_HEAL = "heal"
        const val ACTION_WEAKEN = "weaken"
        const val ACTION_STRENGTHEN = "strengthen"
        const val ACTION_STUN = "stun"
        const val ACTION_SUMMON = "summon"
        const val ACTION_LOOP = "loop"

        const val SUMMON_TAG_FORCA_AUTO_ATTACK = "force_auto_attack"
        const val SUMMON_LEARN_ABILITY_1 = "learn_ability_1"
        const val SUMMON_LEARN_ABILITY_2 = "learn_ability_2"
        const val SUMMON_LEARN_ABILITY_3 = "learn_ability_3"
    }

    class Target {
        companion object {
            const val SELF = "self"
            const val CHOICE = "choice"
        }
        class Enemy {
            companion object {
                const val RANDOM = "enemy.random"
                const val ALL = "enemy.all"
            }
        }
        class Friend {
            companion object {
                const val RANDOM = "friend.random"
                const val ALL = "friend.all"
            }
        }
    }
}