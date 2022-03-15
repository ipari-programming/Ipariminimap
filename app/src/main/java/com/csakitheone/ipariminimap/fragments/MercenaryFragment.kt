package com.csakitheone.ipariminimap.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.csakitheone.ipariminimap.R
import com.csakitheone.ipariminimap.mercenaries.Ability
import com.csakitheone.ipariminimap.mercenaries.Merc
import com.csakitheone.ipariminimap.mercenaries.SaveData
import com.csakitheone.ipariminimap.mercenaries.SaveData.Companion.isInTeam
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_mercenary.view.*

class MercenaryFragment : Fragment() {

    private lateinit var merc: Merc

    var onMercChanged: (merc: Merc) -> Unit = { }
    var onRequestTargets: () -> List<Merc> = { listOf() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_mercenary, container, false)
        merc = Gson().fromJson(arguments?.getString("mercenary"), Merc::class.java)

        v.mercFragmentTextName.text = if (merc.selectedAbility == null) "${merc.level}\n${merc.name}\n${merc.mercClass.name}"
        else "${merc.level}\n${merc.name}\n${merc.mercClass.name}\n✅\n${merc.selectedAbility?.name}(⏳${merc.selectedAbility?.speed})"
        v.mercFragmentTextAttack.text = "⚔️${merc.getCurrentAttack()}"
        v.mercFragmentTextHealth.text = "${merc.getCurrentHealth()}🩸"

        v.mercFragmentCard.setOnClickListener {
            when (arguments?.getString("mode")) {
                MODE_VIEW -> showDetails()
                MODE_COMMAND -> command()
                MODE_EDIT -> edit()
            }
        }

        return v
    }

    fun showDetails() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("${merc.name} (${merc.level})")
            .setMessage("${merc.mercClass.name}\nTámadás: ${merc.getCurrentAttack()} Élet: ${merc.getMaxHealth()}\nKépességek:\n${merc.abilities.joinToString("\n")}")
            .setPositiveButton("Ok") { _, _ -> }
            .create().show()
    }

    fun command() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("${merc.name}: Mit tegyek?")
            .setItems(merc.abilities.map { it.toString() }.toTypedArray()) { _, i ->
                val ability = merc.abilities[i]
                if (ability.doRequireChoice()) {
                    chooseTarget(ability)
                    return@setItems
                }
                merc.selectedAbility = merc.abilities[i]
                onMercChanged(merc)
            }
            .setPositiveButton("Ne csinálj semmit") { _, _ ->
                merc.selectedAbility = null
                onMercChanged(merc)
            }
            .create().show()
    }

    fun chooseTarget(ability: Ability) {
        val targets = onRequestTargets()
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(ability.toString())
            .setItems(targets.map { "${it.name} (${it.getCurrentAttack()}/${it.getCurrentHealth()})" }.toTypedArray()) { _, i ->
                ability.target = targets[i]
                merc.selectedAbility = ability
                onMercChanged(merc)
            }
            .setNegativeButton("Vissza") { _, _ ->
                command()
            }
            .setPositiveButton("Ne csinálj semmit") { _, _ ->
                merc.selectedAbility = null
                onMercChanged(merc)
            }
            .create().show()
    }

    fun edit() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("${merc.name} (${merc.level})")
            .setMessage("${merc.mercClass.id} ${merc.mercClass.name}\nTámadás: ${merc.getCurrentAttack()} Élet: ${merc.getMaxHealth()}\nKépességek:\n${merc.abilities.joinToString("\n")}")
            .setPositiveButton("Ok") { _, _ -> }
            .setNegativeButton(if (merc.isInTeam()) "Kidobás csapatból" else "Felvétel a csapatba") { _, _ ->
                if (merc.isInTeam()) SaveData.instance.team.removeAll { it.name == merc.name && it.mercClass.name == merc.mercClass.name }
                else {
                    if (SaveData.isTeamReady()) Toast.makeText(requireContext(), "Maximum 3 ember lehet egy csapatban!", Toast.LENGTH_SHORT).show()
                    else SaveData.instance.team.add(merc)
                }
                onMercChanged(merc)
            }
            .create().show()
    }

    companion object {
        const val MODE_VIEW = "view"
        const val MODE_COMMAND = "command"
        const val MODE_EDIT = "edit"

        @JvmStatic
        fun newInstance(mercenary: Merc, mode: String) =
            MercenaryFragment().apply {
                arguments = Bundle().apply {
                    putString("mercenary", Gson().toJson(mercenary))
                    putString("mode", mode)
                }
            }
    }
}