package com.csakitheone.ipariminimap

import android.content.DialogInterface
import android.content.Intent
import android.content.res.TypedArray
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.core.view.setMargins
import androidx.core.view.setPadding
import com.csakitheone.ipariminimap.data.DB
import com.csakitheone.ipariminimap.data.Data
import com.csakitheone.ipariminimap.data.Prefs
import com.csakitheone.ipariminimap.helper.Helper.Companion.toPx
import com.csakitheone.ipariminimap.helper.Rings
import com.csakitheone.ipariminimap.services.RingService
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.material.button.MaterialButton
import com.google.android.material.color.DynamicColors
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main_badges.*
import kotlinx.android.synthetic.main.activity_main_bell.*
import kotlinx.android.synthetic.main.activity_main_database.*
import kotlinx.android.synthetic.main.activity_main_home.*
import kotlinx.android.synthetic.main.activity_main_map.*
import kotlinx.android.synthetic.main.layout_get_badges_dialog.view.*
import kotlinx.android.synthetic.main.layout_task.view.*
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initDatabase()
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
                "Adatbázis" -> {
                    mainActivityDatabase.visibility = View.VISIBLE
                    updateDBStats()
                }
                else -> return@setOnItemSelectedListener false
            }
            true
        }
    }

    override fun onResume() {
        super.onResume()

        mainSwitchService.isChecked = Prefs.getIsServiceAllowed()
        runServiceIfAllowed()

        initBellTable()

        refreshTasks()
        refreshBadges()

        mainBtnAdminLock.visibility = if (Prefs.getIsAdmin()) View.VISIBLE else View.GONE
    }

    private fun initAds() {
        MobileAds.setRequestConfiguration(
            RequestConfiguration.Builder()
                .setTestDeviceIds(listOf(
                    "A95A3A512D1FE5693AE2EF06BAFC5E42",
                    "1B0B102828598F5DC553C280FE8A3020"
                ))
                .build()
        )
        MobileAds.initialize(this)

        mainBannerAd.loadAd(AdRequest.Builder().build())
    }

    private fun initDatabase() {
        DB.connect {
            if (DB.getIsConnected()) {
                refreshLinks()
            }
            else if (DB.databaseVersion != it) {
                MaterialAlertDialogBuilder(this)
                    .setTitle("Nem sikerült csatlakozni az adatbázishoz")
                    .setMessage("""
                        Helyi adatbázis verzió: ${DB.databaseVersion}
                        Szerver adatbázis verzió: $it
                        Lehet, hogy az alkalmazást frissíteni kell.
                    """.trimIndent())
                    .setPositiveButton("Play Áruház megnyitása") { _: DialogInterface, _: Int ->
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.csakitheone.ipariminimap")))
                    }
                    .create().show()
            }
            else {
                MaterialAlertDialogBuilder(this)
                    .setTitle("Nem sikerült csatlakozni az adatbázishoz")
                    .setMessage("Nincs internet? Véletlen drop-oltam az adatbázist?")
                    .create().show()
            }
        }
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
                    "Dinamikus színek" -> {
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

    private fun runServiceIfAllowed() {
        if (mainSwitchService.isChecked) {
            ContextCompat.startForegroundService(this, Intent(this, RingService::class.java))
        }
        else {
            stopService(Intent(this, RingService::class.java))
        }
    }

    private fun refreshLinks() {
        DB.getLinks { links ->
            mainLayoutLinks.removeAllViews()
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

    fun onSwitchServiceClick(view: View) {
        Prefs.setIsServiceAllowed(mainSwitchService.isChecked)
        runServiceIfAllowed()
    }

    fun onBtnFAQClick(view: View) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Gyakori kérdések")
            .setMessage(
                "Barátomnak iPhone-ja van. Ő le tudja tölteni az appot?\n\n" +
                        "Sajnos nem. 😕 Egy Apple fejlesztői fiók elég drága havidíjjal rendelkezik. " +
                        "Ezen kívül iOS fejlesztésben sincs még tapasztalatom.\n\n" +
                        "Barátomnak Huawei telefonja van. Ő honnan tudja megszerezni az appot?\n\n" +
                        "Ha nála nem elérhető a Play áruház akkor APK formájában tudja beszerezni valakitől." +
                        " Írjon nekem vagy valaki csomagoljon egy APK-t és küldje el neki, hogy tudja " +
                        "sideload-olni. Nyilván így nem fog frissülni, de legalább meglesz.\n\n" +
                        "Miért van reklám az appban? Kapsz érte valamit?\n\n" +
                        "Nagyon sok munka van az app fejlesztéssel és nem szeretnék senkitől pénzt kérni. " +
                        "Ezért döntöttem a reklámok mellett. Nyilván nem akarlak idegesíteni titeket, én " +
                        "sem szeretem a reklámokat, de szerintem így a legjobb mindenkinek.Viszont ne " +
                        "gondoljatok nagy dolgokra, jobb napokon kb. 20Ft-ot kapok maximum."
            )
            .create().show()
    }

    fun onBtnSupportClick(view: View) {
        startActivity(Intent(this, RewardAdActivity::class.java))
    }

    fun onBtnExploreKRESZ(view: View) {
        startActivity(Intent(this, KreszActivity::class.java))
    }

    fun onBtnOpenLinkClick(view: View) {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(view.tag.toString())))
    }

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

            if (data[0][0].digitToIntOrNull() ?: -2 == Rings.getCurrentLessonValue().roundToInt()) {
                val colorAttribute = TypedValue()
                theme.resolveAttribute(R.attr.colorPrimaryDark, colorAttribute, true)
                row.setBackgroundColor(getColor(colorAttribute.resourceId))
            }
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

    fun onBtnNewTaskClick(view: View) {
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

    fun onBtnGetBadgeClick(view: View) {
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

    //#region Database

    private fun updateDBStats() {
        if (!DB.getIsConnected()) {
            mainTextDatabaseStats.text = "Nem sikerült csatlakozni az adatbázishoz."
            return
        }

        DB.downloadBuildingData {
            mainTextDatabaseStats.text = """
                            Helyi adatbázis verzió: ${DB.databaseVersion}
                            Szerver adatbázis verzió: ${DB.remoteDatabaseVersion}
                            Linkek: ${Data.links.size}
                            Helyadatok: ${Data.buildings.size} épület, ${Data.places.size} hely és ${Data.rooms.size} terem
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
        if (!DB.getIsConnected()) {
            Toast.makeText(this, "Nem vagy csatlakozva az adatbázishoz!", Toast.LENGTH_SHORT).show()
            return
        }

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

    fun onBtnAdminLockClick(view: View) {
        Prefs.setIsAdmin(false)
        openAdminUI()
    }

    //#endregion
}