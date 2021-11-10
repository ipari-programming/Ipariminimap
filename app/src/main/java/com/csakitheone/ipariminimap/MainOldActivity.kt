package com.csakitheone.ipariminimap

import android.content.*
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate.*
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.view.isVisible
import androidx.preference.PreferenceManager
import com.csakitheone.ipariminimap.helper.Rings
import com.csakitheone.ipariminimap.services.RingService
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main_old.*
import kotlinx.android.synthetic.main.layout_get_badges_dialog.view.*
import kotlinx.android.synthetic.main.layout_task.view.*
import java.util.*
import kotlin.concurrent.timerTask

class MainOldActivity : AppCompatActivity() {
    lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_old)

        prefs = PreferenceManager.getDefaultSharedPreferences(applicationContext)

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
    }

    override fun onResume() {
        super.onResume()
        mainBtnSupport.text = "Támogatás videóval (${prefs.getInt("mainRewardedAd.watchCount", 0)})"
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

    fun btnExploreKRESZ(view: View) {
        startActivity(Intent(this, KreszActivity::class.java))
    }

    fun btnOpenLinkClick(view: View) {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(view.tag.toString())))
    }

    fun fabSearchButtonClick(view: View) {
        startActivityForResult(Intent(this, SearchOldActivity::class.java), 0)
    }
}