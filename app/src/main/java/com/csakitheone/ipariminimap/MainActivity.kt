package com.csakitheone.ipariminimap

import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.text.SpannableStringBuilder
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.core.view.children
import androidx.core.view.setPadding
import com.csakitheone.ipariminimap.data.*
import com.csakitheone.ipariminimap.helper.Helper.Companion.toPx
import com.csakitheone.ipariminimap.helper.Rings
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main_database.*
import kotlinx.android.synthetic.main.activity_main_students.*
import kotlinx.android.synthetic.main.layout_task.view.*
import java.util.*
import kotlin.concurrent.timerTask
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {

    private var timerBell = Timer()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initAds()

        mainNav.setOnItemSelectedListener {
            for (subActivity in mainFrame.children) {
                subActivity.visibility = View.GONE
            }

            when (it.title) {
                "Főoldal" -> mainActivityHome.visibility = View.VISIBLE
                "Diákok" -> {
                    mainActivityStudents.visibility = View.VISIBLE
                    initStudents()
                }
                "Adatbázis" -> {
                    mainActivityDatabase.visibility = View.VISIBLE
                    updateDBStats()
                    Web.getStudents {
                        runOnUiThread {
                            mainTextWebStats.text = "Diákok: ${it.size}"
                        }
                    }
                }
                else -> return@setOnItemSelectedListener false
            }
            true
        }

        refreshLinks()
    }

    override fun onResume() {
        super.onResume()

        timerBell = Timer("timerBell").apply {
            schedule(timerTask {
                runOnUiThread { refreshBell() }
            }, 0L, 1000L)
        }

        mainBannerAd.visibility = if (Temp.isAdWatched) View.GONE else View.VISIBLE
    }

    override fun onPause() {
        super.onPause()

        timerBell.cancel()
    }

    private fun initAds() {
        MobileAds.setRequestConfiguration(
            RequestConfiguration.Builder()
                .setTestDeviceIds(listOf(
                    "1B0B102828598F5DC553C280FE8A3020"
                ))
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
                    "Dinamikus színek beállítása" -> {
                        MaterialAlertDialogBuilder(this@MainActivity)
                            .setTitle("Dinamikus színek")
                            .setMessage("Android 12-től kezve az alkalmazások követhetik a háttered színeit. A változtatások csak újraindítás után lépnek életbe.")
                            .setPositiveButton("Bekapcsolás") { _: DialogInterface, _: Int ->
                                Prefs.setIsUsingDynamicColors(true)
                            }
                            .setNegativeButton("Kikapcsolás") { _: DialogInterface, _: Int ->
                                Prefs.setIsUsingDynamicColors(false)
                            }
                            .create().show()
                    }
                    else -> return@setOnMenuItemClickListener true
                }
                false
            }
            inflate(R.menu.menu_main)
            show()
        }
    }

    //#region Home

    private fun refreshLinks() {
        DB.getLinks { links ->
            mainLayoutLinks.removeAllViews()

            if (links.isEmpty()) {
                mainLayoutLinks.addView(TextView(this).apply {
                    text = "Nem sikerült betölteni a linkeket. Ha nincs net akkor amúgy sem tudnád használni."
                    setPadding(16.toPx.toInt())
                })
                return@getLinks
            }

            for (pair in links) {
                mainLayoutLinks.addView(
                    MaterialButton(this, null, R.attr.styleTextButton).apply {
                        text = pair.key
                        setOnClickListener {
                            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(pair.value)))
                        }
                        setOnLongClickListener {
                            if (!mainSwitchAdminLinks.isChecked) return@setOnLongClickListener false

                            val editURL = EditText(this@MainActivity)
                            editURL.text = SpannableStringBuilder(pair.value)
                            MaterialAlertDialogBuilder(this@MainActivity)
                                .setTitle(pair.key)
                                .setView(editURL)
                                .setPositiveButton("Mentés") { _: DialogInterface, _: Int ->
                                    DB.Admin.setLinks(links.toMutableMap().apply { set(pair.key, editURL.text.toString()) }) {
                                        refreshLinks()
                                    }
                                }
                                .setNeutralButton("Mégsem") { _: DialogInterface, _: Int -> }
                                .setNegativeButton("Törlés") { _: DialogInterface, _: Int ->
                                    DB.Admin.setLinks(links.toMutableMap().apply { remove(pair.key) }) {
                                        refreshLinks()
                                    }
                                }
                                .create().show()

                            return@setOnLongClickListener true
                        }
                    }
                )
            }
        }
    }

    private fun refreshBell() {
        mainTextBellTitle.text = "${Rings.getCurrentLesson()} • ${Rings.getTimeUntilNext()}"

        mainBellTable.removeAllViews()
        val timetable = """
            0. óra | 07:00 | 07:40
            1. óra | 07:45 | 08:30
            2. óra | 08:40 | 09:25
            3. óra | 09:35 | 10:20
            4. óra | 10:30 | 11:15
            5. óra | 11:30 | 12:15
            6. óra | 12:25 | 13:10
            7. óra | 13:20 | 14:05
            8. óra | 14:15 | 15:00
        """.trimIndent()

        fun createCell(row: TableRow, content: String, doHighlight: Boolean = false) {
            TextView(this).apply {
                text = content
                setPadding(8.toPx.toInt())
                gravity = Gravity.CENTER
                row.addView(this)
                (layoutParams as TableRow.LayoutParams).apply {
                    width = TableRow.LayoutParams.MATCH_PARENT
                    weight = 1f
                }
                if (doHighlight) setTextColor(Color.WHITE)
            }
        }

        for (line in timetable.lines()) {
            val row = TableRow(this)
            mainBellTable.addView(row)

            val data = line.split("|").map { r -> r.trim() }
            val isRowCurrentTime = data[0][0].digitToIntOrNull() ?: -2 == Rings.getCurrentLessonValue().roundToInt()

            createCell(row, data[0], isRowCurrentTime)
            createCell(row, data[1], isRowCurrentTime)
            createCell(row, data[2], isRowCurrentTime)

            if (isRowCurrentTime) {
                val colorAttribute = TypedValue()
                theme.resolveAttribute(android.R.attr.colorPrimaryDark, colorAttribute, true)
                row.setBackgroundColor(getColor(colorAttribute.resourceId))
            }
        }
    }

    fun onBtnMercenariesClick(view: View) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Figyelem! Korai alpha tesztelés!")
            .setMessage("Ez a funkció még nagyon korai állapotban van, még tele van hibákkal, hiányosságokkal!")
            .setPositiveButton("Megértettem") { _, _ ->
                startActivity(Intent(this, MercMainActivity::class.java))
            }
            .setNegativeButton("Vissza") { _, _ -> }
            .create().show()
    }

    fun onBtnExploreKRESZClick(view: View) {
        startActivity(Intent(this, KreszActivity::class.java))
    }

    fun onBtnSupportClick(view: View) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Támogatás")
            .setItems(arrayOf("Videó nézése (elrejti a szalaghírdetést következő indításig)")) { _, i ->
                when (i) {
                    0 -> startActivity(Intent(this, RewardAdActivity::class.java))
                }
            }
            .create().show()
    }

    fun onBtnAutomateClick(view: View) {
        startActivity(Intent(this, TasksActivity::class.java))
    }

    //#endregion

    //#region Students

    private fun initStudents() {
        Web.getStudents { students ->
            runOnUiThread {

                mainProgressStudents.visibility = View.GONE

                Web.getNameDay { names ->
                    runOnUiThread {

                        val nameCount = students.count { student ->
                            val studentName = student.name.split(" ")
                            names.any { studentName.contains(it) }
                        }
                        mainTextNameday.text = "Mai névnap(ok): ${names.joinToString()}\n$nameCount diáknak van ma névnapja."

                    }
                }

                mainLayoutClasses.removeAllViews()
                students.groupBy { student -> student.gradeMajor }.keys.map { gradeMajor ->
                    val btnClass = Chip(this).apply {
                        text = gradeMajor
                        setOnClickListener {
                            startActivity(Intent(this@MainActivity, SearchActivity::class.java).apply {
                                putExtra(SearchActivity.EXTRA_QUERY, gradeMajor)
                            })
                        }
                    }
                    mainLayoutClasses.addView(btnClass)
                    btnClass.layoutParams.width = ChipGroup.LayoutParams.WRAP_CONTENT
                }

            }
        }
    }

    //#endregion

    //#region Database

    private fun updateDBStats() {
        DB.downloadBuildingData {
            mainTextDatabaseStats.text = """
                            Adatbázis verzió: ${DB.databaseVersion}
                            Linkek: ${Data.links.size}
                            Helyadatok: ${Data.buildings.size} épület, ${Data.buildings.sumOf { r -> r.places.size }} hely és ${Data.buildings.sumOf { r -> r.places.sumOf { s -> s.rooms.size } }} terem
                        """.trimIndent()
        }
    }

    private fun showAdminDialog() {
        val editAdmin = EditText(this)
        editAdmin.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD

        MaterialAlertDialogBuilder(this)
            .setTitle("Admin bejelentkezés")
            .setView(editAdmin)
            .setPositiveButton("Belépés") { _: DialogInterface, _: Int ->
                val isSuccess = Prefs.setIsAdmin(true, editAdmin.text.toString())

                if (!isSuccess) {
                    Toast.makeText(this, "A jelszó helytelen", Toast.LENGTH_SHORT).show()
                }
                openAdminUI()
            }
            .setNegativeButton("Mégsem") { _: DialogInterface, _: Int -> }
            .create().show()
    }

    private fun openAdminUI() {
        if (!Prefs.getIsAdmin()) {
            mainLayoutAdminLocked.visibility = View.VISIBLE
            mainLayoutAdminUnlocked.visibility = View.GONE
            mainSwitchAdminLinks.isChecked = false
            return
        }
        mainLayoutAdminLocked.visibility = View.GONE
        mainLayoutAdminUnlocked.visibility = View.VISIBLE
    }

    fun onBtnAdminClick(view: View) {
        if (Prefs.getIsAdmin()) openAdminUI()
        else showAdminDialog()
    }

    fun onBtnAdminLinkAddClick(view: View) {
        val editTitle = EditText(this).apply { hint = "Link neve" }
        val editURL = EditText(this).apply { hint = "URL" }
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            addView(editTitle)
            addView(editURL)
        }

        MaterialAlertDialogBuilder(this@MainActivity)
            .setTitle("Új link")
            .setView(layout)
            .setPositiveButton("Mentés") { _: DialogInterface, _: Int ->
                DB.Admin.setLinks(Data.links.toMutableMap().apply { put(editTitle.text.toString(), editURL.text.toString()) }) {
                    Toast.makeText(this, if (it) "Hozzáadva" else "Nem sikerült a link hozzáadása", Toast.LENGTH_SHORT).show()
                    refreshLinks()
                }
            }
            .setNegativeButton("Mégsem") { _: DialogInterface, _: Int -> }
            .create().show()
    }

    fun onBtnAdminOpenBuildingManagerClick(view: View) {
        if (!Prefs.getIsAdmin()) return
        startActivity(Intent(this, BuildingManagerActivity::class.java))
    }

    fun onBtnAdminLockClick(view: View) {
        Prefs.setIsAdmin(false)
        openAdminUI()
    }

    //#endregion
}