package com.csakitheone.ipariminimap

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.csakitheone.ipariminimap.data.Data
import com.csakitheone.ipariminimap.data.Data.*
import com.csakitheone.ipariminimap.databinding.ActivityRoomBinding
import com.google.android.material.chip.Chip

class RoomActivity : AppCompatActivity() {

    lateinit var binding: ActivityRoomBinding

    lateinit var room: Room

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRoomBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun onResume() {
        super.onResume()

        room = Data.getAllRooms().find { r -> r.id == (intent.getStringExtra("room_sign") ?: "") } ?: Room("")
        loadRoom()
    }

    private fun loadRoom() {
        val place = Data.getAllPlaces().find { r -> r.rooms.contains(room) } ?: Data.getAllPlaces().first()
        val building = Data.buildings.find { r -> r.places.contains(place) } ?: Data.buildings.first()

        binding.roomTextSign.text = room.id.toUpperCase()
        binding.roomTextDescription.text = room.name
        binding.roomTextDescription.visibility = if (room.name.isEmpty()) View.GONE else View.VISIBLE
        binding.chipBuilding.text = building.name
        binding.chipPlace.text = place.name
        val level = place.level.toString()
        binding.roomTextBuilding.text = "Épület: "
        binding.roomTextLevel.text = " $level. emelet"

        binding.roomTextHelp.visibility = if (place.help.isEmpty()) View.GONE else View.VISIBLE
        binding.roomTextHelp.text = place.help

        binding.roomChipGroupTags.removeAllViews()
        for (tag in room.tags) {
            val chip = Chip(this)
            chip.text = tag
            chip.setOnClickListener { chipClick(it) }
            binding.roomChipGroupTags.addView(chip)
        }

        binding.roomChipGroupPlace.removeAllViews()
        for (r in place.rooms) {
            val chip = Chip(this)
            var roomText = r.id.toUpperCase()
            if (r.name.isNotEmpty()) roomText += ":${r.name}"
            if (r.tags.isNotEmpty()) roomText += ":${r.tags.joinToString()}"
            chip.text = roomText
            chip.setOnClickListener { chipClick(it) }
            binding.roomChipGroupPlace.addView(chip)
        }

        binding.roomChipGroupDestinations.removeAllViews()
        for (d in place.destinations) {
            val chip = Chip(this)
            chip.text = d
            chip.setOnClickListener { chipClick(it) }
            binding.roomChipGroupDestinations.addView(chip)
        }
    }

    fun chipClick(view: View) {
        val query = (view as Chip).text.split(':')[0]

        if (Data.getAllRooms().map { r -> r.id }.contains(query)) {
            val temp = Data.getAllRooms().find { r -> r.id == query }

            if (temp != null) {
                room = temp
                loadRoom()
                return
            }
        }

        startActivity(Intent(this, SearchActivity::class.java).putExtra(SearchActivity.EXTRA_QUERY, query))
    }
}