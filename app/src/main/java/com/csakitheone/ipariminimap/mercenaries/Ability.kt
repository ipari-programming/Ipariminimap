package com.csakitheone.ipariminimap.mercenaries

class Ability(
    var name: String = "",
    var speed: Int = 5,
    var description: String = ""
) {

    var actions = mutableListOf<String>()

    fun actionSay(message: String): Ability {
        actions.add("say $message")
        return this
    }

    fun actionAttack(target: String, amount: Int = -1): Ability {
        actions.add("attack $target $amount")
        return this
    }

    fun actionHeal(target: String, amount: Int): Ability {
        actions.add("heal $target $amount")
        return this
    }

    fun actionSummon(mercName: String, extra: String = "") {
        actions.add("summon $mercName $extra")
    }

    class ActionTarget {
        companion object {
            const val SELF = "self"
        }
        class Friend {
            companion object {
                const val CHOOSE = "friend_choose"
                const val RANDOM = "friend_random"
                const val ALL = "friend_all"
            }
        }
        class Enemy {
            companion object {
                const val CHOOSE = "enemy_choose"
                const val RANDOM = "enemy_random"
                const val ALL = "enemy_all"
            }
        }
    }

    companion object {

        const val SUMMON_EXTRA_USE_ABILITY = "use_ability"

    }

}