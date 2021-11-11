package com.csakitheone.ipariminimap

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.csakitheone.ipariminimap.data.DataOld
import com.csakitheone.ipariminimap.data.Room
import com.google.android.material.chip.Chip
import kotlinx.android.synthetic.main.activity_room.*

class RoomActivity : AppCompatActivity() {
    lateinit var room: Room

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_room)

        room = DataOld.getRoomBySign(intent.getStringExtra("room_sign") ?: "") ?: Room("Az alkalmazás kódja", Int.MAX_VALUE)
        loadRoom()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 0 && resultCode == 2 && data != null) {
            room = DataOld.getRoomBySign(data.getStringExtra("room_sign")!!)!!
            loadRoom()
        }
    }

    fun loadRoom() {
        roomTextSign.text = room.getSign()
        roomTextDescription.text = room.getRoomName()
        roomTextDescription.visibility = if (room.getRoomName().isEmpty()) View.GONE else View.VISIBLE
        chipBuilding.text = room.getBuildingName()
        chipPlace.text = room.placeName
        val level = DataOld.getPlaceByName(room.placeName)?.level.toString()
        roomTextBuilding.text = "Épület: "
        roomTextLevel.text = " $level. emelet"

        roomTextHelp.visibility = if (DataOld.getPlaceByName(room.placeName)?.help.isNullOrEmpty()) View.GONE else View.VISIBLE
        roomTextHelp.text = DataOld.getPlaceByName(room.placeName)?.help

        roomChipGroupTags.removeAllViews()
        for (tag in room.tags) {
            val chip = Chip(this)
            chip.text = tag
            chip.setOnClickListener { chipClick(it) }
            roomChipGroupTags.addView(chip)
        }

        roomChipGroupPlace.removeAllViews()
        val roomsHere = mutableListOf<Room>()
        roomsHere.addAll(DataOld.rooms.filter { r -> r.placeName == room.placeName })
        for (r in roomsHere) {
            val chip = Chip(this)
            var roomText = r.getSign()
            if (r.getRoomName().isNotEmpty()) roomText += ":${r.getRoomName()}"
            if (r.tags.isNotEmpty()) roomText += ":${r.tags.joinToString()}"
            chip.text = roomText
            chip.setOnClickListener { chipClick(it) }
            roomChipGroupPlace.addView(chip)
        }

        roomChipGroupDestinations.removeAllViews()
        for (d in DataOld.getPlaceByName(room.placeName)!!.destinations) {
            val chip = Chip(this)
            chip.text = d
            chip.setOnClickListener { chipClick(it) }
            roomChipGroupDestinations.addView(chip)
        }
    }

    fun chipClick(view: View) {
        val query = (view as Chip).text.split(':')[0]

        if (DataOld.rooms.map { r -> r.getSign() }.contains(query)) {
            val temp = DataOld.getRoomBySign(query)

            if (temp != null) {
                room = temp
                loadRoom()
                return
            }
        }

        startActivity(Intent(this, SearchActivity::class.java).putExtra(SearchActivity.EXTRA_QUERY, query))
    }
}