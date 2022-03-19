package com.csakitheone.ipariminimap

import android.content.DialogInterface
import android.graphics.drawable.Icon
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.View
import android.widget.*
import androidx.core.view.children
import com.csakitheone.ipariminimap.data.DB
import com.csakitheone.ipariminimap.data.Data
import com.csakitheone.ipariminimap.data.Prefs
import com.csakitheone.ipariminimap.databinding.ActivityBuildingManagerBinding
import com.csakitheone.ipariminimap.helper.Helper.Companion.toPx
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class BuildingManagerActivity : AppCompatActivity() {

    lateinit var binding: ActivityBuildingManagerBinding

    var selectedItemType = ""
    var selectedItemName = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBuildingManagerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (!Prefs.getIsAdmin()) finish()

        binding.buildingSeekPlaceLevel.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) { }
            override fun onStartTrackingTouch(p0: SeekBar?) { }
            override fun onStopTrackingTouch(p0: SeekBar?) {
                Toast.makeText(this@BuildingManagerActivity, p0?.progress.toString(), Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onResume() {
        super.onResume()
        refreshList()
    }

    private fun refreshList() {
        binding.buildingFabAdd.setImageIcon(Icon.createWithResource(this, R.drawable.ic_add))

        binding.buildingLayoutList.visibility = View.VISIBLE
        binding.buildingCardBuilding.visibility = View.GONE
        binding.buildingCardPlace.visibility = View.GONE
        binding.buildingCardRoom.visibility = View.GONE

        DB.downloadBuildingData {
            fillList()

            binding.buildingGroupPlaceBuilding.removeAllViews()
            for (building in Data.buildings) {
                binding.buildingGroupPlaceBuilding.addView(RadioButton(this).apply {
                    text = building.name
                })
            }

            binding.buildingGroupPlaceDestinations.removeAllViews()
            for (place in Data.getAllPlaces()) {
                binding.buildingGroupPlaceDestinations.addView(Chip(this).apply {
                    text = place.name
                    isCheckable = true
                })
            }

            binding.buildingGroupRoomPlace.removeAllViews()
            for (place in Data.getAllPlaces()) {
                binding.buildingGroupRoomPlace.addView(RadioButton(this).apply {
                    text = place.name
                })
            }

            binding.buildingGroupRoomTags.removeAllViews()
            for (tag in Data.tags) {
                binding.buildingGroupRoomTags.addView(Chip(this).apply {
                    text = tag
                    isCheckable = true
                })
            }
        }
    }

    private fun fillList() {
        binding.buildingLayoutList.removeAllViews()

        var view: TextView
        for (building in Data.buildings) {
            view = TextView(this).apply {
                text = building.name
                tag = "building"
                setPadding(16.toPx.toInt(), 8.toPx.toInt(), 8.toPx.toInt(), 8.toPx.toInt())
                setOnClickListener { editItem("building", building.name) }
            }
            binding.buildingLayoutList.addView(view)

            for (place in building.places) {
                view = TextView(this).apply {
                    text = place.name
                    tag = "place"
                    setPadding(48.toPx.toInt(), 8.toPx.toInt(), 8.toPx.toInt(), 8.toPx.toInt())
                    setOnClickListener { editItem("place", place.name) }
                }
                binding.buildingLayoutList.addView(view)

                for (room in place.rooms) {
                    view = TextView(this).apply {
                        text = room.id
                        tag = "room"
                        setPadding(80.toPx.toInt(), 8.toPx.toInt(), 8.toPx.toInt(), 8.toPx.toInt())
                        setOnClickListener { editItem("room", room.id) }
                    }
                    binding.buildingLayoutList.addView(view)
                }

            }

        }
    }

    private fun checkRadio(group: RadioGroup, index: Int) {
        if (index < 0) return
        (group.getChildAt(index) as RadioButton).isChecked = true
    }

    private fun checkChip(group: ChipGroup, index: Int) {
        if (index < 0) return
        (group.getChildAt(index) as Chip).isChecked = true
    }

    private fun editItem(type: String, name: String) {
        binding.buildingFabAdd.setImageIcon(Icon.createWithResource(this, R.drawable.ic_more_vert))
        binding.buildingLayoutList.visibility = View.GONE

        selectedItemType = type
        selectedItemName = name

        when (type) {
            "building" -> {
                val building = Data.buildings.find { r -> r.name == name } ?: Data.Building("building-${System.currentTimeMillis()}", "")
                binding.buildingTextBuildingId.text = building.id
                binding.buildingEditBuildingName.text = SpannableStringBuilder(building.name)
                binding.buildingCardBuilding.visibility = View.VISIBLE
            }
            "place" -> {
                val place = Data.getAllPlaces().find { r -> r.name == name } ?: Data.Place("")
                binding.buildingEditPlaceName.text = SpannableStringBuilder(place.name)
                binding.buildingSeekPlaceLevel.progress = place.level
                checkRadio(binding.buildingGroupPlaceBuilding, Data.buildings.indexOfFirst { r -> r.places.contains(place) })
                place.destinations.map { checkChip(binding.buildingGroupPlaceDestinations, Data.getAllPlaces().indexOfFirst { r -> r.name == it }) }
                binding.buildingEditPlaceHelp.text = SpannableStringBuilder(place.help)
                binding.buildingCardPlace.visibility = View.VISIBLE
            }
            "room" -> {
                val room = Data.getAllRooms().find { r -> r.id == name } ?: Data.Room("")
                binding.buildingEditRoomId.text = SpannableStringBuilder(room.id)
                binding.buildingEditRoomName.text = SpannableStringBuilder(room.name)
                checkRadio(binding.buildingGroupRoomPlace, Data.getAllPlaces().indexOfFirst { r -> r.rooms.contains(room) })
                room.tags.map { checkChip(binding.buildingGroupRoomTags, Data.tags.indexOfFirst { r -> r == it }) }
                binding.buildingCardRoom.visibility = View.VISIBLE
            }
        }
    }

    private fun saveItem() {
        when(selectedItemType) {
            "building" -> {
                val building = Data.buildings.find { r -> r.id == binding.buildingTextBuildingId.text } ?:
                    Data.Building(binding.buildingTextBuildingId.text.toString(), "")
                building.name = binding.buildingEditBuildingName.text.toString()
                Data.buildings.removeAll { r -> r.id == binding.buildingTextBuildingId.text }
                Data.buildings.add(building)
            }
            "place" -> {
                val buildingOld = Data.buildings.find { r -> r.places.any { place -> place.name == selectedItemName } }
                val building = Data.buildings.find { r -> r.name == binding.buildingGroupPlaceBuilding.children.map { radio -> radio as RadioButton }.find { radio -> radio.isChecked }?.text }

                val place = Data.getAllPlaces().find { r -> r.name == selectedItemName } ?: Data.Place("")

                val destinations = mutableListOf<String>()
                for (chip in binding.buildingGroupPlaceDestinations.children.map { r -> r as Chip }.filter { r -> r.isChecked }) {
                    destinations.add(chip.text.toString())
                }

                place.apply {
                    name = binding.buildingEditPlaceName.text.toString()
                    this.destinations = destinations
                    level = binding.buildingSeekPlaceLevel.progress
                    help = binding.buildingEditPlaceHelp.text.toString()
                }
                buildingOld?.places?.removeAll { r -> r.name == selectedItemName }
                building?.places?.add(place)
            }
            "room" -> {
                val placeOld = Data.getAllPlaces().find { r -> r.rooms.any { room -> room.id == selectedItemName } }
                val place = Data.getAllPlaces().find { r -> r.name == binding.buildingGroupRoomPlace.children.map { radio -> radio as RadioButton }.find { radio -> radio.isChecked }?.text }

                val tags = mutableListOf<String>()
                for (chip in binding.buildingGroupRoomTags.children.map { r -> r as Chip }.filter { r -> r.isChecked }) {
                    tags.add(chip.text.toString())
                }
                val room = Data.Room(
                    binding.buildingEditRoomId.text.toString(),
                    binding.buildingEditRoomName.text.toString(),
                    tags
                )
                placeOld?.rooms?.removeAll { r -> r.id == selectedItemName }
                place?.rooms?.add(room)
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
        if (binding.buildingLayoutList.visibility == View.GONE) {
            PopupMenu(this, binding.buildingFabAdd).apply {
                inflate(R.menu.menu_building_manager)
                setOnMenuItemClickListener {
                    when (it.title) {
                        "Mentés" -> saveItem()
                        "Elvetés" -> refreshList()
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