package com.csakitheone.ipariminimap

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.csakitheone.ipariminimap.databinding.ActivityMercGameBinding
import com.csakitheone.ipariminimap.fragments.MercenaryFragment
import com.csakitheone.ipariminimap.mercenaries.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.util.*
import kotlin.concurrent.timerTask

class MercGameActivity : AppCompatActivity() {

    lateinit var binding: ActivityMercGameBinding

    var enemies = mutableListOf<Merc>()
    var mercs = mutableListOf<Merc>()

    var roundCount = 1
    var abilityQueue = mutableListOf<Merc>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMercGameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mission = Story.getMissionByIndex(intent.getIntExtra("missionIndex", 0))
        if (mission.welcomeMessage.isNotBlank()) {
            MaterialAlertDialogBuilder(this)
                .setTitle(mission.name)
                .setMessage(mission.welcomeMessage)
                .setPositiveButton("Kezdjük!") { _, _ -> }
                .create().show()
        }

        enemies.addAll(mission.enemies)
        enemies.map { enemy ->
            if (enemy.abilities.isEmpty()) enemy.abilities.addAll(enemy.mercClass.abilities)
            enemy.prepareForGame()
            enemy.selectedAbility = enemy.abilities.first()
        }
        mercs.addAll(SaveData.instance.team)
        mercs.map {
            it.prepareForGame()
        }

        binding.mercGameTextLog.text = "Kezdődjön a harc!\n"
        refreshUI()
    }

    val fragments = mutableListOf<MercenaryFragment>()
    private fun refreshUI() {
        val fragmentTransaction = supportFragmentManager.beginTransaction()

        fragments.map {
            fragmentTransaction.remove(it)
        }
        fragments.clear()

        enemies.map {
            val fragment = MercenaryFragment.newInstance(it, MercenaryFragment.MODE_VIEW)
            fragments.add(fragment)
            fragmentTransaction.add(R.id.mercGameLayoutEnemies, fragment)
        }

        for (i in mercs.indices) {
            val fragment = MercenaryFragment.newInstance(mercs[i], if (binding.mercGameBtnFight.isEnabled) MercenaryFragment.MODE_COMMAND else MercenaryFragment.MODE_VIEW)
            fragments.add(fragment)
            fragment.onMercChanged = {
                mercs[i] = it
                refreshUI()
            }
            fragment.onRequestTargets = { listOf(enemies, mercs).flatten() }
            fragmentTransaction.add(R.id.mercGameLayoutMercs, fragment)
        }

        fragmentTransaction.commit()
    }

    private fun log(message: String) {
        runOnUiThread {
            binding.mercGameTextLog.text = "${binding.mercGameTextLog.text}\n$message\n"
            binding.mercGameScrollLog.post {
                binding.mercGameScrollLog.fullScroll(View.FOCUS_DOWN)
            }
        }
    }

    private fun gameOver(isWin: Boolean) {
        MaterialAlertDialogBuilder(this)
            .setTitle(if (isWin) "Győzelem!" else "A játéknak vége")
            .setPositiveButton("Vissza a menübe") { _, _ ->
                finish()
            }
            .setOnCancelListener {
                finish()
            }
            .create().show()
    }

    private fun endRound() {
        enemies.removeAll { !it.isAlive() }
        mercs.removeAll { !it.isAlive() }
        enemies.map { it.selectedAbility = it.mercClass.abilities.random() }
        mercs.map { if (it.forceAutoAttack) it.selectedAbility = it.abilities.random() }

        binding.mercGameBtnFight.isEnabled = true
        roundCount++
        refreshUI()

        if (enemies.isEmpty()) gameOver(true)
        else if (mercs.isEmpty()) gameOver(false)
    }

    private fun executeAbilities() {
        if (abilityQueue.isEmpty()) {
            runOnUiThread { endRound() }
            return
        }
        val currentMerc = abilityQueue.removeFirst()
        val currentFragment = fragments.firstOrNull { it.getMerc().name == currentMerc.name }

        runOnUiThread {
            if (currentMerc.isAlive()) {
                var logMessage = "${currentMerc.name}: ${currentMerc.selectedAbility?.name}"
                if (currentMerc.selectedAbility?.target != null) logMessage += " ➡️ ${currentMerc.selectedAbility?.target?.name}"
                log(logMessage)

                currentFragment?.elevate()
                runActions(currentMerc)
                refreshUI()
            }
        }

        Timer().schedule(timerTask {
            executeAbilities()
        }, 1000L)
    }

    private fun runActions(merc: Merc) {
        if (!merc.isAlive() || merc.selectedAbility == null) return
        val code = merc.selectedAbility?.code ?: return

        fun locateTargets(targetString: String): List<Merc> {
            return when (targetString) {
                Ability.Target.SELF -> listOf(merc)
                Ability.Target.CHOICE -> {
                    if (merc.selectedAbility?.target == null) listOf()
                    else listOf(merc.selectedAbility?.target!!)
                }
                Ability.Target.Enemy.RANDOM -> {
                    if (enemies.contains(merc)) listOf(mercs.random())
                    else listOf(enemies.random())
                }
                Ability.Target.Friend.RANDOM -> {
                    if (mercs.contains(merc)) listOf(mercs.random())
                    else listOf(enemies.random())
                }
                Ability.Target.Enemy.ALL -> enemies
                Ability.Target.Friend.ALL -> mercs
                else -> listOf()
            }
        }

        fun resolveVariable(text: String, default: Int = 0): Int {
            val intValue = text.toIntOrNull()
            if (intValue != null) return intValue
            return when {
                merc.selectedAbility?.variables?.containsKey(text) == true -> merc.selectedAbility?.variables!![text] ?: default
                text == Ability.varMercAttack() -> merc.getCurrentAttack()
                else -> default
            }
        }

        println("CODE FOR ABILITY:\n${code.replace(";", ";\n")}")
        val actions = code.split(";")

        for (action in actions) {
            val keywords = action.split(" ")
            when (keywords[0]) {
                Ability.ACTION_ATTACK -> {
                    val isPhysical = keywords[2] == Ability.varMercAttack()
                    locateTargets(keywords[1]).map {
                        val isKillingBlow = it.damage(resolveVariable(keywords[2]))
                        val isDeadByBackfire = if (isPhysical) merc.damage(it.getCurrentAttack()) else false
                        if (isKillingBlow) log("${it.name} meghalt ${merc.name} által")
                        if (isDeadByBackfire) log("${merc.name} belehalt a támadásba")
                    }
                }
                Ability.ACTION_HEAL -> {
                    locateTargets(keywords[1]).map { it.heal(resolveVariable(keywords[2])) }
                }
                Ability.ACTION_WEAKEN -> {
                    locateTargets(keywords[1]).map { it.weaken(resolveVariable(keywords[2])) }
                }
                Ability.ACTION_STRENGTHEN -> {
                    locateTargets(keywords[1]).map { it.strengthen(resolveVariable(keywords[2])) }
                }
                Ability.ACTION_STUN -> {
                    locateTargets(keywords[1]).map { it.selectedAbility = null }
                }
                Ability.ACTION_SUMMON -> {
                    val mercClassToSummon = MercClass.getAll().first { it.id == keywords[1] }
                    val mercToSummon = Merc(mercClassToSummon.name, mercClassToSummon, merc.level)
                    if (action.contains(Ability.SUMMON_TAG_FORCA_AUTO_ATTACK)) mercToSummon.forceAutoAttack = true
                    mercToSummon.prepareForGame()
                    if (action.contains(Ability.SUMMON_LEARN_ABILITY_1)) mercToSummon.abilities.add(mercClassToSummon.abilities[0])
                    else if (action.contains(Ability.SUMMON_LEARN_ABILITY_2)) mercToSummon.abilities.add(mercClassToSummon.abilities[1])
                    else if (action.contains(Ability.SUMMON_LEARN_ABILITY_3)) mercToSummon.abilities.add(mercClassToSummon.abilities[2])
                    else mercToSummon.learnAllAbilities()
                    if (enemies.contains(merc)) {
                        enemies.add(mercToSummon)
                    }
                    else if (mercs.contains(merc)) {
                        mercs.add(mercToSummon)
                    }
                }
                Ability.ACTION_LOOP -> {
                    abilityQueue.add(merc)
                    return
                }
            }
        }
        merc.selectedAbility = null
    }

    fun onBtnFightClick(view: View) {
        binding.mercGameBtnFight.isEnabled = false

        // 0. prepare
        log("--- $roundCount. kör ---")
        // 1. sort abilities
        val queue = mutableListOf<Merc>()
        queue.addAll(enemies.filter { it.selectedAbility != null })
        queue.addAll(mercs.filter { it.selectedAbility != null })
        queue.shuffle()
        abilityQueue = queue.sortedBy { it.selectedAbility!!.speed }.toMutableList()
        // 2. execute ability actions
        executeAbilities()

    }

}