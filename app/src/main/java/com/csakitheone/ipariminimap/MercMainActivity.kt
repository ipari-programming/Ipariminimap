package com.csakitheone.ipariminimap

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import androidx.fragment.app.FragmentTransaction
import com.csakitheone.ipariminimap.data.Web
import com.csakitheone.ipariminimap.fragments.MercenaryFragment
import com.csakitheone.ipariminimap.mercenaries.Merc
import com.csakitheone.ipariminimap.mercenaries.SaveData
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.activity_merc_main.*

class MercMainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_merc_main)

        SaveData.load()
        SaveData.setup(this) {
            SaveData.save()
            refreshUI()
        }

        val loadingDialog = MaterialAlertDialogBuilder(this)
            .setTitle("Adatok betöltése...")
            .setView(ProgressBar(this))
            .setCancelable(false)
            .create()
        loadingDialog.show()

        Web.getStudents {
            runOnUiThread {
                loadingDialog.dismiss()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        refreshUI()
    }

    private fun refreshUI() {
        mercBtnPlay.isEnabled = SaveData.isTeamReady()
        mercBtnPvp.isEnabled = SaveData.isTeamReady()

        val fragmentTransaction = supportFragmentManager.beginTransaction()

        mercLayoutTeam.removeAllViews()
        SaveData.instance.team.map {
            it.prepareForGame()
            val fragment = MercenaryFragment.newInstance(it, MercenaryFragment.MODE_EDIT)
            fragment.onTeamChanged = {
                refreshUI()
            }
            fragmentTransaction.add(R.id.mercLayoutTeam, fragment)
        }

        mercLayoutCollection.removeAllViews()
        SaveData.instance.collection.map {
            it.prepareForGame()
            val fragment = MercenaryFragment.newInstance(it, MercenaryFragment.MODE_EDIT)
            fragment.onTeamChanged = {
                refreshUI()
            }
            fragmentTransaction.add(R.id.mercLayoutCollection, fragment)
        }

        fragmentTransaction.commit()
    }

    fun onBtnStartClick(view: View) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Készülj fel a harcra!")
            .setMessage("Válassz nehézségi szintet:")
            .setPositiveButton("Könnyed") { _, _ -> }
            .setNegativeButton("Kihívás") { _, _ -> }
            .setNeutralButton("Mégsem") { _, _ -> }
            .create().show()
    }

    fun onBtnPvpClick(view: View) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Szeretnél más emberek ellen játszani?")
            .setMessage("Ez a mód még nincs kész, de lesz rá lehetőség.")
            .setPositiveButton("Kösz Csáki") { _, _ -> }
            .create().show()
    }

    fun onBtnDeleteDataClick(view: View) {
        SaveData.instance = SaveData()
        SaveData.save()
        finish()
    }

}