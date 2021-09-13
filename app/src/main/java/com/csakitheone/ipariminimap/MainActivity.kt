package com.csakitheone.ipariminimap

import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate.*
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.preference.PreferenceManager
import com.csakitheone.ipariminimap.data.Timetable
import com.csakitheone.ipariminimap.helper.Rings
import com.csakitheone.ipariminimap.services.RingService
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.layout_get_badges_dialog.view.*
import kotlinx.android.synthetic.main.layout_task.view.*
import java.util.*
import kotlin.concurrent.timerTask

class MainActivity : AppCompatActivity() {
    lateinit var prefs: SharedPreferences
    lateinit var tasks: MutableList<Task>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        prefs = PreferenceManager.getDefaultSharedPreferences(applicationContext)

        setNightTheme(prefs.getInt("night_mode", MODE_NIGHT_FOLLOW_SYSTEM))

        mainToolbar.setOnMenuItemClickListener {
            when (it.title) {
                "Suli weboldal" -> startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.ipariszakkozep.hu/")))
                "Suli újság" -> startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://news.ipariszakkozep.hu/")))
                "@ipariselet" -> startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.instagram.com/ipariselet/")))
                "Ipari Discord" -> startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://discord.gg/TQXKTm47t4")))
                "Helyettesítés letöltése" -> startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.ipariszakkozep.hu/content/helyettesites/aktualis.pdf")))
                "Távoktatás infók" -> startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.ipariszakkozep.hu/tavoktatas")))
                "Rendszer követése" -> setNightTheme(MODE_NIGHT_FOLLOW_SYSTEM)
                "Világos téma" -> setNightTheme(MODE_NIGHT_NO)
                "Sötét téma" -> setNightTheme(MODE_NIGHT_YES)
                else -> return@setOnMenuItemClickListener false
            }
            true
        }

        Timer().schedule(timerTask {
            runOnUiThread {
                mainTextBell.text = Rings.getCurrentLesson()
                if (Rings.getTimeUntilNext() != null) {
                    mainTextBell.text = "${mainTextBell.text} - ${Rings.getTimeUntilNext()}"
                }
            }
        }, 0, 1000L)

        mainTextSupport.text = listOf(
            "Támogatás", "Írj, ha hibát találsz!", "Írj, ha van ötleted!", "Nézd meg a többi appom!",
            "Legyen szép napod!", "Szerdánként infó szakkörön ott vagyok.", "Írj, ha szeretnél!",
            "Minden segítség jól jön <3", "Mit rakjak még ebbe az appba?",
            "Játékokat is programozok.", "Próbáld ki a Random Bot appom!",
            "Mogyorós latte a kedvencem a büféből."
        ).random()

        mainEditNotes.addTextChangedListener {
            prefs.edit {
                putString("main_notes", it.toString())
                apply()
            }
        }

        MobileAds.setRequestConfiguration(
            RequestConfiguration.Builder()
                .setTestDeviceIds(listOf("24E9E518AB9DBE2924B9B93F22361702"))
                .build()
        )
        MobileAds.initialize(this)

        mainBannerAd.loadAd(AdRequest.Builder().build())
    }

    override fun onResume() {
        super.onResume()
        refreshTimetable()
        refreshTasks()
        refreshBadges()
        mainBtnSupport.text = "Támogatás videóval (${prefs.getInt("mainRewardedAd.watchCount", 0)})"

        mainEditNotes.text = SpannableStringBuilder(prefs.getString("main_notes", ""))

        mainTextSecretGame.visibility = if (Badge.userGet(this).size >= 5) View.VISIBLE else View.GONE
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 0 && resultCode == 2 && data != null) {
            startActivity(Intent(this, RoomActivity::class.java).putExtra("room_sign", data.getStringExtra("room_sign")!!))
        }
    }

    fun setNightTheme(nightMode: Int) {
        setDefaultNightMode(nightMode)
        prefs.edit {
            putInt("night_mode", nightMode)
            apply()
        }
    }

    // LESSONS

    fun refreshTimetable() {
        Timetable.load(this)
        if (Timetable.appointments.any { it.getDayCalendar() == Calendar.getInstance().get(Calendar.DAY_OF_WEEK) }) {
            mainTextLessons.text = "Mai órák:\n"
            for (a in Timetable.appointments.filter { it.getDayCalendar() == Calendar.getInstance().get(Calendar.DAY_OF_WEEK) }) {
                mainTextLessons.text = "${mainTextLessons.text}${a.toStringNoDay()}\n"
            }
        }
    }

    fun btnEditLessonsClick(view: View) {
        startActivity(Intent(this, TimetableActivity::class.java))
    }

    // TASKS

    fun refreshTasks() {
        tasks = prefs.getStringSet("tasks", setOf())?.map { r -> Task(r) }?.toMutableList() ?: mutableListOf()
        mainLayoutTasks.removeAllViews()
        tasks.map {
            val v = it.createLayout(this)
            v.taskBtnRemove.setOnClickListener { vi ->
                tasks.remove(it)
                saveTasks()
                refreshTasks()
            }
            mainLayoutTasks.addView(v)
            it.onModified.add { saveTasks() }
        }
        saveTasks()
    }

    fun saveTasks() {
        prefs.edit().putStringSet("tasks", tasks.map { r -> r.toString() }.toSet()).apply()
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

    // BADGES

    fun refreshBadges() {
        mainLayoutBadges.removeAllViews()
        Badge.userGet(this).map { r -> mainLayoutBadges.addView(r.createLayout(this, true) { refreshBadges() }) }
    }

    fun btnGetBadgeClick(view: View) {
        val getBadges = layoutInflater.inflate(R.layout.layout_get_badges_dialog, null, false)
        Badge.all.filter { r -> r.isVisible && !Badge.userContains(this, r.id) }.map {
            getBadges.getbadgesLayout.addView(it.createLayout(this, true))
        }
        AlertDialog.Builder(this)
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

    // BUTTONS

    fun btnStartBackgroundClick(view: View) {
        ContextCompat.startForegroundService(this, Intent(this, RingService::class.java))
    }

    fun btnSupportUnfoldClick(view: View) {
        mainLayoutSupport.visibility = if (mainLayoutSupport.isVisible) View.GONE else View.VISIBLE
    }

    fun btnFAQClick(view: View) {
        AlertDialog.Builder(this)
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

    fun btnSupportClick(view: View) {
        startActivity(Intent(this, RewardAdActivity::class.java))
    }

    fun btnRingOrderClick(view: View) {
        mainImageBellOrder.visibility = if (mainImageBellOrder.visibility == View.GONE) View.VISIBLE else View.GONE
    }

    fun btnExploreKRESZ(view: View) {
        startActivity(Intent(this, KreszActivity::class.java))
    }

    fun btnSawClick(view: View) {
        AlertDialog.Builder(this)
            .setTitle("Sawház")
            .setMessage("Ha a széles folyosóról mész ki az udvarra, rögtön balra.\n" +
                    "\nTilos cigizni! Pozíció küldése Kobán László igazgatóhelyettesnek...")
            .setView(ProgressBar(this))
            .setPositiveButton("Nem cigizni megyek!") { _: DialogInterface, _: Int -> }
            .create().show()
    }

    fun btnEntranceClick(view: View) {
        Toast.makeText(this, "Üdv az Ipariban! 😀", Toast.LENGTH_SHORT).show()
    }

    fun btnOpenLinkClick(view: View) {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(view.tag.toString())))
    }

    fun btnSearchForTextClick(view: View) {
        startActivityForResult(Intent(this, SearchActivity::class.java).putExtra("query", (view as Button).text.split('(')[0].trim()), 0)
    }

    fun fabSearchButtonClick(view: View) {
        startActivityForResult(Intent(this, SearchActivity::class.java), 0)
    }

    fun btnSecretGameClick(view: View) {
        startActivity(Intent(this, SecretGameActivity::class.java))
    }
}