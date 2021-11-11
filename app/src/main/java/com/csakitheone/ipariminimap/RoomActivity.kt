package com.csakitheone.ipariminimap

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.csakitheone.ipariminimap.data.Data
import com.csakitheone.ipariminimap.data.Data.*
import com.google.android.material.chip.Chip
import kotlinx.android.synthetic.main.activity_room.*

class RoomActivity : AppCompatActivity() {
    lateinit var room: Room

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_room)
    }

    override fun onResume() {
        super.onResume()

        room = Data.rooms.find { r -> r.id == (intent.getStringExtra("room_sign") ?: "") } ?: Room("", placeName = "")
        loadRoom()
    }

    fun loadRoom() {
        val place = Data.places.find { r -> r.name == room.placeName }

        roomTextSign.text = room.id.toUpperCase()
        roomTextDescription.text = room.name
        roomTextDescription.visibility = if (room.name.isEmpty()) View.GONE else View.VISIBLE
        chipBuilding.text = place?.buildingName
        chipPlace.text = room.placeName
        val level = place?.level.toString()
        roomTextBuilding.text = "Épület: "
        roomTextLevel.text = " $level. emelet"

        roomTextHelp.visibility = if (place?.help.isNullOrEmpty()) View.GONE else View.VISIBLE
        roomTextHelp.text = place?.help

        roomChipGroupTags.removeAllViews()
        for (tag in room.tags) {
            val chip = Chip(this)
            chip.text = tag
            chip.setOnClickListener { chipClick(it) }
            roomChipGroupTags.addView(chip)
        }

        roomChipGroupPlace.removeAllViews()
        val roomsHere = mutableListOf<Room>()
        roomsHere.addAll(Data.rooms.filter { r -> r.placeName == room.placeName })
        for (r in roomsHere) {
            val chip = Chip(this)
            var roomText = r.id.toUpperCase()
            if (r.name.isNotEmpty()) roomText += ":${r.name}"
            if (r.tags.isNotEmpty()) roomText += ":${r.tags.joinToString()}"
            chip.text = roomText
            chip.setOnClickListener { chipClick(it) }
            roomChipGroupPlace.addView(chip)
        }

        roomChipGroupDestinations.removeAllViews()
        for (d in place!!.destinations) {
            val chip = Chip(this)
            chip.text = d
            chip.setOnClickListener { chipClick(it) }
            roomChipGroupDestinations.addView(chip)
        }
    }

    fun chipClick(view: View) {
        val query = (view as Chip).text.split(':')[0]

        if (Data.rooms.map { r -> r.id }.contains(query)) {
            val temp = Data.rooms.find { r -> r.id == query }

            if (temp != null) {
                room = temp
                loadRoom()
                return
            }
        }

        startActivity(Intent(this, SearchActivity::class.java).putExtra(SearchActivity.EXTRA_QUERY, query))
    }
}