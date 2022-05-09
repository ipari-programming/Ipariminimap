package com.csakitheone.ipariminimap

import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.text.SpannableStringBuilder
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.setPadding
import androidx.viewpager.widget.ViewPager
import com.csakitheone.ipariminimap.data.*
import com.csakitheone.ipariminimap.databinding.ActivityMainBinding
import com.csakitheone.ipariminimap.helper.Helper.Companion.toPx
import com.csakitheone.ipariminimap.helper.Rings
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.ChipGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.skydoves.transformationlayout.TransformationCompat
import com.skydoves.transformationlayout.onTransformationStartContainer
import java.util.*
import kotlin.concurrent.timerTask
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding

    private var timerBell = Timer()

    override fun onCreate(savedInstanceState: Bundle?) {
        onTransformationStartContainer()
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initAds()

        binding.mainNav.setOnItemSelectedListener {
            when (it.title) {
                "Főoldal" -> binding.mainViewPager.currentItem = 0
                "Diákok" -> {
                    binding.mainViewPager.currentItem = 1
                    downloadStudents(true)
                }
                "Adatbázis" -> {
                    binding.mainViewPager.currentItem = 2
                    updateDBStats()
                }
                else -> return@setOnItemSelectedListener false
            }
            true
        }

        binding.mainViewPager.addOnPageChangeListener(object: ViewPager.OnPageChangeListener{
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) { }
            override fun onPageSelected(position: Int) {
                binding.mainNav.selectedItemId = when (position) {
                    1 -> R.id.menuMainNavStudents
                    2 -> R.id.menuMainNavDatabase
                    else -> R.id.menuMainNavHome
                }
            }
            override fun onPageScrollStateChanged(state: Int) { }
        })

        refreshLinks()
    }

    override fun onResume() {
        super.onResume()

        timerBell = Timer("timerBell").apply {
            schedule(timerTask {
                runOnUiThread { refreshBell() }
            }, 0L, 1000L)
        }

        binding.mainBannerAd.visibility = if (Temp.isAdWatched) View.GONE else View.VISIBLE
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

        binding.mainBannerAd.loadAd(AdRequest.Builder().build())
    }

    fun onCardSearchClick(view: View) {
        startActivity(Intent(this, SearchActivity::class.java))
    }

    //#region Home

    private fun refreshLinks() {
        DB.getLinks { links ->
            binding.mainLayoutLinks.removeAllViews()

            if (links.isEmpty()) {
                binding.mainLayoutLinks.addView(TextView(this).apply {
                    text = "Nem sikerült betölteni a linkeket. Ha nincs net akkor amúgy sem tudnád használni."
                    setPadding(16.toPx.toInt())
                })
                return@getLinks
            }

            for (pair in links) {
                binding.mainLayoutLinks.addView(
                    MaterialButton(this, null, R.attr.styleTextButton).apply {
                        text = pair.key
                        setOnClickListener {
                            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(pair.value)))
                        }
                        setOnLongClickListener {
                            if (!binding.mainSwitchAdminLinks.isChecked) return@setOnLongClickListener false

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
        binding.mainTextBellTitle.text = "${Rings.getCurrentLesson()} • ${Rings.getTimeUntilNext()}"

        binding.mainBellTable.removeAllViews()
        val timetable = """
            0. óra | 07:00 - 07:40
            1. óra | 07:45 - 08:30
            2. óra | 08:40 - 09:25
            3. óra | 09:35 - 10:20
            4. óra | 10:30 - 11:15
            5. óra | 11:30 - 12:15
            6. óra | 12:25 - 13:10
            7. óra | 13:20 - 14:05
            8. óra | 14:15 - 15:00
        """.trimIndent()

        fun createCell(row: TableRow, content: String, doHighlight: Boolean = false) {
            TextView(this).apply {
                text = content
                setPadding(4.toPx.toInt())
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
            binding.mainBellTable.addView(row)

            val data = line.split("|").map { r -> r.trim() }
            val isRowCurrentTime = data[0][0].digitToIntOrNull() ?: -2 == Rings.getCurrentLessonValue().roundToInt()

            createCell(row, data[0], isRowCurrentTime)
            createCell(row, data[1], isRowCurrentTime)

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
                TransformationCompat.startActivity(
                    binding.mainTransformationLayoutMercenaries,
                    Intent(this, MercMainActivity::class.java)
                )
            }
            .setNegativeButton("Vissza") { _, _ -> }
            .create().show()
    }

    fun onBtnExploreKRESZClick(view: View) {
        TransformationCompat.startActivity(
            binding.mainTransformationLayoutKresz,
            Intent(this, KreszActivity::class.java)
        )
    }

    fun onBtnSupportClick(view: View) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Támogatás")
            .setItems(arrayOf(
                "Videó nézése (elrejti a szalaghírdetést következő indításig)",
                "Üzenet a fejlesztőnek",
                "További appok",
            )) { _, i ->
                when (i) {
                    0 -> {
                        TransformationCompat.startActivity(
                            binding.mainTransformationLayoutSupport,
                            Intent(this, RewardAdActivity::class.java)
                        )
                    }
                    1 -> startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://m.me/CsakiTheOne")))
                    2 -> startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/dev?id=5554124272482096869")))
                }
            }
            .create().show()
    }

    fun onBtnAutomateClick(view: View) {
        TransformationCompat.startActivity(
            binding.mainTransformationLayoutAutomate,
            Intent(this, TasksActivity::class.java)
        )
    }

    //#endregion

    //#region Students

    private fun initStudents() {
        binding.mainTextStudentsInfo.text = "${Prefs.getStudentsCache().size} diák összesen"
        binding.mainLayoutClasses.removeAllViews()

        Web.getNameDay { names ->
            val nameCount = Prefs.getStudentsCache().count { student ->
                val studentName = student.name.split(" ")
                names.any { studentName.contains(it) }
            }
            runOnUiThread {
                binding.mainTextNameday.text = "Mai névnap(ok): ${names.joinToString()}\n$nameCount diáknak van ma névnapja."
            }
        }

        Prefs.getStudentsCache().groupBy { student -> student.gradeMajor }.keys.map { gradeMajor ->
            val btnClass = MaterialButton(this, null, R.attr.styleTextButton).apply {
                text = gradeMajor
                setOnClickListener {
                    startActivity(Intent(this@MainActivity, SearchActivity::class.java).apply {
                        putExtra(SearchActivity.EXTRA_QUERY, gradeMajor)
                    })
                }
            }
            binding.mainLayoutClasses.addView(btnClass)
            btnClass.layoutParams.width = ChipGroup.LayoutParams.WRAP_CONTENT
        }
    }

    private fun downloadStudents(skipIfCached: Boolean = false) {
        if (skipIfCached && Prefs.getStudentsCache().isNotEmpty()) {
            initStudents()
            return
        }

        val loadingDialog = MaterialAlertDialogBuilder(this)
            .setTitle("Diákok letöltése...")
            .setMessage("Keresés...")
            .setCancelable(false)
            .create()
        loadingDialog.show()

        Web.getStudents(true, {
            runOnUiThread {
                initStudents()
                loadingDialog.dismiss()
            }
        }, {
            runOnUiThread {
                loadingDialog.setMessage(it.toString())
            }
        })
    }

    fun onBtnRedownloadStudentsClick(view: View) {
        downloadStudents()
    }

    //#endregion

    //#region Database

    private fun updateDBStats() {
        DB.downloadBuildingData {
            binding.mainTextDatabaseStats.text = """
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
            binding.mainLayoutAdminLocked.visibility = View.VISIBLE
            binding.mainLayoutAdminUnlocked.visibility = View.GONE
            binding.mainSwitchAdminLinks.isChecked = false
            return
        }
        binding.mainLayoutAdminLocked.visibility = View.GONE
        binding.mainLayoutAdminUnlocked.visibility = View.VISIBLE
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