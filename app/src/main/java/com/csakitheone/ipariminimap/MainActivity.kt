package com.csakitheone.ipariminimap

import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.children
import androidx.core.view.setMargins
import androidx.core.view.setPadding
import com.csakitheone.ipariminimap.data.DataOld
import com.csakitheone.ipariminimap.data.Prefs
import com.csakitheone.ipariminimap.helper.Helper.Companion.toPx
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main_badges.*
import kotlinx.android.synthetic.main.activity_main_bell.*
import kotlinx.android.synthetic.main.activity_main_map.*
import kotlinx.android.synthetic.main.activity_main_old.*
import kotlinx.android.synthetic.main.layout_get_badges_dialog.view.*
import kotlinx.android.synthetic.main.layout_task.view.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Prefs.init(this)

        initAds()

        mainNav.setOnItemSelectedListener {
            for (subActivity in mainFrame.children) {
                subActivity.visibility = View.GONE
            }

            when (it.title) {
                "Főoldal" -> mainActivityHome.visibility = View.VISIBLE
                "Térkép" -> mainActivityMap.visibility = View.VISIBLE
                "Csengő" -> mainActivityBell.visibility = View.VISIBLE
                "Kitűzők" -> mainActivityBadges.visibility = View.VISIBLE
                else -> return@setOnItemSelectedListener false
            }
            true
        }

        initBellTable()
    }

    private fun initAds() {
        MobileAds.setRequestConfiguration(
            RequestConfiguration.Builder()
                .setTestDeviceIds(listOf("24E9E518AB9DBE2924B9B93F22361702"))
                .build()
        )
        MobileAds.initialize(this)

        mainBannerAd.loadAd(AdRequest.Builder().build())
    }

    fun onBtnSearchClick(view: View) {
        startActivity(Intent(this, SearchActivity::class.java))
    }

    fun onBtnMenuClick(view: View) {
        PopupMenu(this, mainBtnMenu).apply {
            setOnMenuItemClickListener {
                when (it.title) {
                    "Rendszer követése" -> Prefs.setNightTheme(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                    "Világos téma" -> Prefs.setNightTheme(AppCompatDelegate.MODE_NIGHT_NO)
                    "Sötét téma" -> Prefs.setNightTheme(AppCompatDelegate.MODE_NIGHT_YES)
                    "Régi felület használata" -> startActivity(Intent(this@MainActivity, MainOldActivity::class.java))
                    else -> return@setOnMenuItemClickListener true
                }
                false
            }
            inflate(R.menu.menu_main)
            show()
        }
    }

    //#region Home
    //#endregion

    //#region Map

    fun onBtnEnteranceClick(view: View) {
        Snackbar.make(mainFrame, "Üdv az Ipariban!", Snackbar.LENGTH_SHORT).show()
    }

    fun onSearchBtnClick(view: View) {
        startActivity(Intent(this, SearchActivity::class.java).apply {
            putExtra(SearchActivity.EXTRA_QUERY, (view as MaterialButton).text)
        })
    }

    //#endregion

    //#region Bell

    private fun initBellTable() {
        mainBellTable.removeAllViews()
        val timetable = """
            0. óra | 07:00 | 07:40
            1. óra | 07:45 | 08:30
            2. óra | 08:40 | 09:25
            3. óra | 09:35 | 10:20
            4. óra | 10:30 | 11:15
            Nagyszünet | 11:15 | 11:30
            5. óra | 11:30 | 12:15
            6. óra | 12:25 | 13:10
            7. óra | 13:20 | 14:05
            8. óra | 14:15 | 15:00
        """.trimIndent()

        fun createCell(row: TableRow, content: String) {
            TextView(this).apply {
                text = content
                setPadding(8.toPx.toInt())
                gravity = Gravity.CENTER_HORIZONTAL or Gravity.CENTER_VERTICAL
                row.addView(this)
                (layoutParams as TableRow.LayoutParams).apply {
                    width = TableRow.LayoutParams.MATCH_PARENT
                    weight = 1f
                }
            }
        }

        for (line in timetable.lines()) {
            val row = TableRow(this)
            mainBellTable.addView(row)

            val data = line.split("|").map { r -> r.trim() }
            createCell(row, data[0])
            createCell(row, data[1])
            createCell(row, data[2])
        }
    }

    private var tasks = mutableListOf<Task>()

    private fun refreshTasks() {
        tasks = Prefs.getTasks()
        mainLayoutTasks.removeAllViews()
        tasks.map {
            val v = it.createLayout(this)
            v.taskBtnRemove.setOnClickListener { _ ->
                tasks.remove(it)
                saveTasks()
                refreshTasks()
            }
            mainLayoutTasks.addView(v)
            (v.layoutParams as LinearLayout.LayoutParams).apply { setMargins(4.toPx.toInt()) }
            it.onModified.add { saveTasks() }
        }
        saveTasks()
    }

    private fun saveTasks() {
        Prefs.setTasks(tasks)
        if (tasks.any { r -> r.state && r.condition == "minden óra elején" && r.action == "telefon rezgőre" })
        {
            Badge.userAdd(this, Badge.BADGE_JOTANULO.toString())
            refreshBadges()
        }
    }

    fun btnNewTaskClick(view: View) {
        tasks.add(Task())
        saveTasks()
        refreshTasks()
    }

    //#endregion

    //#region Badges

    private fun refreshBadges() {
        mainLayoutBadges.removeAllViews()
        Badge.userGet(this).map { r -> mainLayoutBadges.addView(r.createLayout(this, true) { refreshBadges() }) }
    }

    fun btnGetBadgeClick(view: View) {
        val getBadges = layoutInflater.inflate(R.layout.layout_get_badges_dialog, null, false)
        Badge.all.filter { r -> r.isVisible && !Badge.userContains(this, r.id) }.map {
            getBadges.getbadgesLayout.addView(it.createLayout(this, true))
        }
        MaterialAlertDialogBuilder(this)
            .setTitle("Kitűzők")
            .setView(getBadges)
            .setPositiveButton("Kód beváltása") { _: DialogInterface, _: Int ->
                if (getBadges.getbadgesEdit.text.toString().isNotEmpty() && Badge.all.any { r -> !r.code.isNullOrEmpty() && r.code!!.contains(getBadges.getbadgesEdit.text.toString().toLowerCase()) }) {
                    Badge.all
                        .filter { r -> !r.code.isNullOrEmpty() && r.code!!.split('|').contains(getBadges.getbadgesEdit.text.toString().toLowerCase()) }
                        .map { r -> Badge.userAdd(this, r.toString()) }
                    Toast.makeText(this, "Kitűzőt szereztél!", Toast.LENGTH_SHORT).show()
                    refreshBadges()
                }
                else {
                    Toast.makeText(this, "Nincs ilyen kód.", Toast.LENGTH_SHORT).show()
                }
            }
            .create().show()
        refreshBadges()
    }

    //#endregion
}