package com.csakitheone.ipariminimap.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.csakitheone.ipariminimap.R
import com.csakitheone.ipariminimap.mercenaries.Merc
import com.csakitheone.ipariminimap.mercenaries.SaveData
import com.csakitheone.ipariminimap.mercenaries.SaveData.Companion.isInTeam
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_mercenary.*
import kotlinx.android.synthetic.main.fragment_mercenary.view.*

class MercenaryFragment : Fragment() {

    var onTeamChanged: (Boolean) -> Unit = { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_mercenary, container, false)
        val merc = Gson().fromJson(arguments?.getString("mercenary"), Merc::class.java)

        v.mercFragmentTextName.text = "${merc.level}\n${merc.name}\n${merc.mercClass.name}"
        v.mercFragmentTextAttack.text = "⚔️${merc.getCurrentAttack()}"
        v.mercFragmentTextHealth.text = "${merc.getCurrentHealth()}🩸"

        v.mercFragmentCard.setOnClickListener {
            when (arguments?.getString("mode")) {
                MODE_COMMAND -> {
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle("${merc.name}: Mit tegyek?")
                        .setSingleChoiceItems(arrayOf(
                            "Marás: Egy maró vegyületet dob az ellenségre",
                            "Gyengítés: Csökkenti a választott karakter sebzését",
                            "Összezavarás: Csökkenti a választott karakter sebzését és a célpontát egy másik ellenségre állítja",
                        ), 0) { _, _ -> }
                        .setPositiveButton("Beállít") { _, i ->
                            Toast.makeText(requireContext(), i.toString(), Toast.LENGTH_SHORT).show()
                        }
                        .create().show()
                }
                MODE_EDIT -> {
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle("${merc.name} (${merc.level})")
                        .setMessage("${merc.mercClass.name}\nTámadás: ${merc.getCurrentAttack()} Élet: ${merc.getMaxHealth()}")
                        .setPositiveButton("Ok") { _, _ -> }
                        .setNegativeButton(if (merc.isInTeam()) "Kidobás csapatból" else "Felvétel a csapatba") { _, _ ->
                            if (merc.isInTeam()) SaveData.instance.team.removeAll { it.name == merc.name && it.mercClass.name == merc.mercClass.name }
                            else {
                                if (SaveData.isTeamReady()) Toast.makeText(requireContext(), "Maximum 3 ember lehet egy csapatban!", Toast.LENGTH_SHORT).show()
                                else SaveData.instance.team.add(merc)
                            }
                            onTeamChanged(merc.isInTeam())
                        }
                        .create().show()
                }
            }
        }

        return v
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