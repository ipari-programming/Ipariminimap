package com.csakitheone.ipariminimap

import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.core.view.setPadding
import com.csakitheone.ipariminimap.data.DB
import com.csakitheone.ipariminimap.data.Data
import com.csakitheone.ipariminimap.data.Prefs
import com.csakitheone.ipariminimap.helper.Helper.Companion.toPx
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.activity_building_manager.*

class BuildingManagerActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_building_manager)

        if (!Prefs.getIsAdmin()) finish()
    }

    override fun onResume() {
        super.onResume()
        refreshList()
    }

    private fun refreshList() {
        DB.downloadBuildingData {
            fillList()
        }
    }

    private fun fillList() {
        buildingLayoutList.removeAllViews()

        var view: TextView
        for (building in Data.buildings) {
            view = TextView(this).apply {
                text = building.name
                tag = "building"
                setPadding(16.toPx.toInt())
                setOnClickListener { editItem("building", building.name) }
            }
            buildingLayoutList.addView(view)

            for (place in Data.places.filter { r -> r.buildingName == building.name }) {
                view = TextView(this).apply {
                    text = place.name
                    tag = "place"
                    setPadding(48.toPx.toInt(), 16.toPx.toInt(), 16.toPx.toInt(), 16.toPx.toInt())
                    setOnClickListener { editItem("place", place.name) }
                }
                buildingLayoutList.addView(view)

                for (room in Data.rooms.filter { r -> r.placeName == place.name }) {
                    view = TextView(this).apply {
                        text = room.id
                        tag = "room"
                        setPadding(80.toPx.toInt(), 16.toPx.toInt(), 16.toPx.toInt(), 16.toPx.toInt())
                        setOnClickListener { editItem("room", room.id) }
                    }
                    buildingLayoutList.addView(view)
                }

            }

        }
    }

    private fun editItem(type: String, name: String) {
        MaterialAlertDialogBuilder(this)
            .setTitle("$name szerkesztése")
            .setPositiveButton("Mentés") { _: DialogInterface, _: Int -> }
            .setNeutralButton("Mégsem") { _: DialogInterface, _: Int -> }
            .setNegativeButton("Törlés") { _: DialogInterface, _: Int -> }
            .create().show()
    }

    fun onFabNewClick(view: View) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Új elem")
            .setPositiveButton("Hozzáadás") { _: DialogInterface, _: Int -> }
            .setNegativeButton("Mégsem") { _: DialogInterface, _: Int -> }
            .create().show()
    }
}