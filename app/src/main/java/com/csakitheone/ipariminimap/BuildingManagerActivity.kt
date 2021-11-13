package com.csakitheone.ipariminimap

import android.content.DialogInterface
import android.graphics.drawable.Icon
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.View
import android.widget.*
import androidx.core.view.setPadding
import com.csakitheone.ipariminimap.data.DB
import com.csakitheone.ipariminimap.data.Data
import com.csakitheone.ipariminimap.data.Prefs
import com.csakitheone.ipariminimap.helper.Helper.Companion.toPx
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.activity_building_manager.*

class BuildingManagerActivity : AppCompatActivity() {
    var selectedItemType = ""
    var selectedItemName = ""

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
        buildingFabAdd.setImageIcon(Icon.createWithResource(this, R.drawable.ic_add))

        buildingLayoutList.visibility = View.VISIBLE
        buildingCardBuilding.visibility = View.GONE
        buildingCardPlace.visibility = View.GONE
        buildingCardRoom.visibility = View.GONE

        DB.downloadBuildingData {
            fillList()

            buildingGroupPlaceBuilding.removeAllViews()
            for (building in Data.buildings) {
                buildingGroupPlaceBuilding.addView(RadioButton(this).apply {
                    text = building.name
                })
            }

            buildingGroupPlaceDestinations.removeAllViews()
            for (place in Data.places) {
                buildingGroupPlaceDestinations.addView(Chip(this).apply {
                    text = place.name
                    isCheckable = true
                })
            }

            buildingGroupRoomPlace.removeAllViews()
            for (place in Data.places) {
                buildingGroupRoomPlace.addView(RadioButton(this).apply {
                    text = place.name
                })
            }

            buildingGroupRoomTags.removeAllViews()
            for (tag in Data.tags) {
                buildingGroupRoomTags.addView(Chip(this).apply {
                    text = tag
                    isCheckable = true
                })
            }
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

    private fun checkRadio(group: RadioGroup, index: Int) {
        (group.getChildAt(index) as RadioButton).isChecked = true
    }

    private fun checkChip(group: ChipGroup, index: Int) {
        (group.getChildAt(index) as Chip).isChecked = true
    }

    private fun editItem(type: String, name: String) {
        buildingFabAdd.setImageIcon(Icon.createWithResource(this, R.drawable.ic_more_vert))
        buildingLayoutList.visibility = View.GONE

        selectedItemType = type
        selectedItemName = name

        when (type) {
            "building" -> {
                val building = Data.buildings.find { r -> r.name == name } ?: Data.Building("building-${System.currentTimeMillis()}", "")
                buildingTextBuildingId.text = building.id
                buildingEditBuildingName.text = SpannableStringBuilder(building.name)
                buildingCardBuilding.visibility = View.VISIBLE
            }
            "place" -> {
                val place = Data.places.find { r -> r.name == name } ?: Data.Place("", Data.buildings.first().name)
                buildingEditPlaceName.text = SpannableStringBuilder(place.name)
                checkRadio(buildingGroupPlaceBuilding, Data.buildings.indexOfFirst { r -> r.name == place.buildingName })
                place.destinations.map { checkChip(buildingGroupPlaceDestinations, Data.places.indexOfFirst { r -> r.name == it }) }
                buildingEditPlaceHelp.text = SpannableStringBuilder(place.help)
                buildingCardPlace.visibility = View.VISIBLE
            }
            "room" -> {
                val room = Data.rooms.find { r -> r.id == name } ?: Data.Room("", placeName = Data.places.first().name)
                buildingEditRoomId.text = SpannableStringBuilder(room.id)
                buildingEditRoomName.text = SpannableStringBuilder(room.name)
                checkRadio(buildingGroupRoomPlace, Data.places.indexOfFirst { r -> r.name == room.placeName })
                room.tags.map { checkChip(buildingGroupRoomTags, Data.tags.indexOfFirst { r -> r == it }) }
                buildingCardRoom.visibility = View.VISIBLE
            }
        }
    }

    private fun saveItem() {
        when(selectedItemType) {
            "building" -> {

            }
            "place" -> {

            }
            "room" -> {
                val room = Data.rooms.find { r -> r.id == selectedItemName } ?: Data.Room("", placeName = Data.places.first().name)
                room.id = buildingEditRoomId.text.toString()
                room.name = buildingEditRoomName.text.toString()
                room.placeName = Data.places[buildingGroupRoomPlace.checkedRadioButtonId].name
                room.tags = buildingGroupRoomTags.checkedChipIds.map { r -> Data.tags[r] }
            }
        }
        DB.Admin.uploadBuildingData {
            Toast.makeText(this, if (it) "Mentve" else "Nem sikerült menteni", Toast.LENGTH_SHORT).show()

            refreshList()
        }
    }

    private fun deleteItem() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Elem törlése")
            .setMessage("Még nem lehet appon belül elemet törölni. Kérd meg a fejlesztőt a művelethez!")
            .create().show()
    }

    fun onFabNewClick(view: View) {
        if (buildingLayoutList.visibility == View.GONE) {
            PopupMenu(this, buildingFabAdd).apply {
                inflate(R.menu.menu_building_manager)
                setOnMenuItemClickListener {
                    when (it.title) {
                        "Mentés" -> saveItem()
                        "Visszalépés mentés nélkül" -> refreshList()
                        "Törlés" -> deleteItem()
                        else -> return@setOnMenuItemClickListener false
                    }
                    true
                }
                show()
            }
            return
        }

        MaterialAlertDialogBuilder(this)
            .setTitle("Új elem hozzáadása")
            .setMessage("Válaszd ki, hogy mit szeretnél hozzáadni! Visszalépéshez koppints a dialóguson kívülre.")
            .setPositiveButton("Terem") { _: DialogInterface, _: Int -> editItem("room", "") }
            .setNeutralButton("Épület") { _: DialogInterface, _: Int -> editItem("building", "") }
            .setNegativeButton("Hely") { _: DialogInterface, _: Int -> editItem("place", "") }
            .create().show()
    }
}