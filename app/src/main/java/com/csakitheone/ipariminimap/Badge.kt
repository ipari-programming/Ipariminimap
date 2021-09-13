package com.csakitheone.ipariminimap

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.core.graphics.createBitmap
import androidx.preference.PreferenceManager
import kotlinx.android.synthetic.main.layout_badge.view.*
import java.lang.Exception

class Badge() {
    lateinit var id: String
    var name: String = ""
    var isVisible: Boolean = false
    var description: String = ""
    var icon: Int = R.drawable.badge_null
    var code: String? = null
    var helpFunction: ((context: Context) -> Unit)? = null

    private constructor(id: String, name: String, isVisible: Boolean = false, description: String = "", icon: Int, code: String? = null, helpFunction: ((context: Context) -> Unit)? = null) : this() {
        this.id = id
        this.name = name
        this.isVisible = isVisible
        this.description = description
        this.icon = if (icon != 0) icon else R.drawable.badge_null
        this.code = code
        this.helpFunction = helpFunction
    }

    fun createLayout(activity: Activity, isInteractable: Boolean = false, removedListener: (() -> Unit)? = null) : View {
        val v = activity.layoutInflater.inflate(R.layout.layout_badge, null, false)

        if (icon != 0) v.badgeIcon.setImageDrawable(ContextCompat.getDrawable(activity, icon))
        v.badgeIcon.alpha = if (userContains(activity, id)) 1F else .5F
        v.badgeName.text = name
        v.badgeDesctiption.text = description
        v.badgeDesctiption.visibility = if (description.isEmpty()) View.GONE else View.VISIBLE

        v.isClickable = isInteractable
        v.isFocusable = isInteractable

        v.setOnClickListener {
            val builder = AlertDialog.Builder(activity)
                .setTitle(name)
                .setMessage(description)
            if (icon != 0) {
                val iconView = ImageView(activity)
                iconView.setImageDrawable(ContextCompat.getDrawable(activity, icon))
                builder.setView(iconView)
            }
            if (userContains(activity, id)) {
                if (icon != 0) {
                    builder.setPositiveButton("Megosztás") { _: DialogInterface, _: Int ->
                        val url = Uri.parse("android.resource://" + activity.resources.getResourceName(icon).replace(":", "/"))
                        val intent = Intent(Intent.ACTION_SEND)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        intent.putExtra(Intent.EXTRA_STREAM, url)
                        intent.type = "image/png"
                        startActivity(activity, Intent.createChooser(intent, "Kitűző megosztása"), null)
                    }
                }
                builder.setNegativeButton("Eldobás") { _: DialogInterface, _: Int ->
                    AlertDialog.Builder(activity)
                        .setTitle("Kitűző eldobása")
                        .setMessage("Biztos eldobod ezt a kitűzőt?\nÚjra teljesítened kell a feltételét a visszaszerzéshez.")
                        .setPositiveButton("Igen") { _: DialogInterface, _: Int ->
                            userRemove(activity, id)
                            if (removedListener != null) removedListener()
                        }
                        .setNegativeButton("Nem") { _: DialogInterface, _: Int -> }
                        .create().show()
                }
            }
            else if (helpFunction != null) {
                builder.setPositiveButton("Megszerzés") { _: DialogInterface, _: Int -> helpFunction?.invoke(activity) }
            }
            builder.create().show()
        }

        return v
    }

    override fun toString(): String {
        return id
    }

    companion object {
        val BADGE_IPARI = Badge("ipari", "Iparis", true, "Igazolt iparis tanuló", R.drawable.badge_ipari, "ipari2020a|ipari2020b|ipari2020c|ipari2020d|ipari2020e|ipari2020f|ipari2020g")
        val BADGE_VEGYESZ = Badge("szak_a", "Vegyész", false, "Ezt a kódot továbbadhatod CSAK az osztálytársaknak: ipari2020a (más szakokon más a kód)", R.drawable.badge_vegyesz, "ipari2020a")
        val BADGE_KORNYEZET = Badge("szak_b", "Környezetvédő", false, "Ezt a kódot továbbadhatod CSAK az osztálytársaknak: ipari2020b (más szakokon más a kód)", R.drawable.badge_kornyezet, "ipari2020b")
        val BADGE_PROG = Badge("szak_c", "Programozó", false, "Ezt a kódot továbbadhatod CSAK az osztálytársaknak: ipari2020c (más szakokon más a kód)", R.drawable.badge_info, "ipari2020c")
        val BADGE_GEPESZ = Badge("szak_d", "Gépész", false, "Ezt a kódot továbbadhatod CSAK az osztálytársaknak: ipari2020d (más szakokon más a kód)", R.drawable.badge_gepesz, "ipari2020d")
        val BADGE_MECHA = Badge("szak_e", "Mechás", false, "Ezt a kódot továbbadhatod CSAK az osztálytársaknak: ipari2020e (más szakokon más a kód)", R.drawable.badge_mecha, "ipari2020e")
        val BADGE_MUANYAG = Badge("szak_f", "Műanyagos", false, "Ezt a kódot továbbadhatod CSAK az osztálytársaknak: ipari2020f (más szakokon más a kód)", R.drawable.badge_muanyag, "ipari2020f")
        val BADGE_KISGYERMEK = Badge("szak_g", "Kisgyermekgondozó", false, "Ezt a kódot továbbadhatod CSAK az osztálytársaknak: ipari2020g (más szakokon más a kód)", R.drawable.badge_kisgyermekgondozo, "ipari2020g")
        val BADGE_DOK = Badge("dök", "DÖK", false, "", R.drawable.badge_dok, "dok181207")
        val BADGE_TAMOGATO = Badge("reklám_10", "Támogató", true, "10 reklám videó", R.drawable.badge_tamogato) { context -> startActivity(context, Intent(context, RewardAdActivity::class.java), null) }
        val BADGE_BEFEKTETO = Badge("reklám_100", "Befektető", true, "100 reklám videó", R.drawable.badge_befekteto, "csakitheone100") { context -> startActivity(context, Intent(context, RewardAdActivity::class.java), null) }
        val BADGE_OSZTONDIJ = Badge("reklám_1000", "Ösztöndíj", false, "1000 reklám videó", 0, "csakitheone1000") { context -> startActivity(context, Intent(context, RewardAdActivity::class.java), null) }
        val BADGE_KRESZ = Badge("kresz", "Iskolai jogosítvány", true, "Hibátlan KRESZ teszt", R.drawable.badge_kresz) { context -> startActivity(context, Intent(context, KreszActivity::class.java), null) }
        val BADGE_TORZSVENDEG = Badge("büfé", "Törzsvendég", true, "Kevesen tudják a büfés nő nevét...", R.drawable.badge_torzsvendeg, "kata|kati")
        val BADGE_JOTANULO = Badge("feladat_némítás", "Jó tanuló", true, "Csinálj egy feladatot, ami rezgőre rakja a telód minden óra előtt!", R.drawable.badge_jotanulo)
        val BADGE_SZIVARVANY = Badge("dísz_lgbt", "Szivárvány", true, "🌈", R.drawable.badge_szivarvany, "lgbt|lgbtq")
        val BADGE_GAMER = Badge("dísz_gamer", "Gamer", true, "C418-Sweeden", R.drawable.badge_gamer, "c418-sweeden|minecraft") { context -> startActivity(context, Intent(Intent.ACTION_VIEW, Uri.parse("https://youtu.be/_3ngiSxVCBs")), null) }

        val all = listOf(
            BADGE_IPARI, BADGE_VEGYESZ, BADGE_KORNYEZET, BADGE_PROG, BADGE_GEPESZ, BADGE_MECHA,
            BADGE_MUANYAG, BADGE_KISGYERMEK, BADGE_DOK, BADGE_TAMOGATO, BADGE_BEFEKTETO,
            BADGE_OSZTONDIJ, BADGE_KRESZ, BADGE_TORZSVENDEG, BADGE_JOTANULO, BADGE_SZIVARVANY,
            BADGE_GAMER
        )

        fun userGet(context: Context) : List<Badge> {
            try {
                return PreferenceManager.getDefaultSharedPreferences(context).getStringSet("badges", setOf())
                    ?.toList()
                    ?.map { r -> all.find { s -> s.id == r }!! }
                    ?: listOf()
            } catch (e: Exception) {
                PreferenceManager.getDefaultSharedPreferences(context).edit().apply {
                    remove("badges")
                    apply()
                }
                Toast.makeText(context, "Nem sikerült betölteni a kitűzőket :(", Toast.LENGTH_SHORT).show()
            }
            return listOf()
        }

        fun userContains(context: Context, id: String) : Boolean {
            return PreferenceManager.getDefaultSharedPreferences(context).getStringSet("badges", setOf())
                ?.contains(id)
                ?: false
        }

        fun userAdd(context: Context, id: String) {
            if (userContains(context, id)) return
            val list = userGet(context).toMutableList()
            list.add(all.find { r -> r.id == id }!!)
            save(context, list)
        }

        fun userRemove(context: Context, id: String) {
            if (!userContains(context, id)) return
            val list = userGet(context).toMutableList()
            list.remove(all.find { r -> r.id == id }!!)
            save(context, list)
        }

        private fun save(context: Context, list: List<Badge>) {
            PreferenceManager.getDefaultSharedPreferences(context).edit().apply {
                putStringSet("badges", list.map { r -> r.id }.toSet())
                apply()
            }
        }
    }
}