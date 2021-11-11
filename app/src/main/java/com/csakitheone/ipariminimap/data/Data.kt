package com.csakitheone.ipariminimap.data

import com.google.firebase.database.DataSnapshot

class Data {
    data class Building(var id: String, var name: String, var sign: String = "")
    data class Place(var name: String, var buildingName: String, var destinations: List<String> = listOf(), var level: Int = 0)
    data class Room(var id: String, var name: String = "", var placeName: String, var tags: List<String> = listOf())

    companion object {
        var buildings = mutableListOf<Building>()
        var places = mutableListOf<Place>()
        var rooms = mutableListOf<Room>()

        private var isLoaded = false

        fun getIsLoaded(): Boolean = isLoaded

        fun loadBuildingData(result: DataSnapshot) {
            for (d in result.child("buildings").children) {
                buildings.add(Building(
                    d.key ?: "",
                    d.child("name").value as String? ?: "",
                    d.child("sign").value as String? ?: ""
                ))
            }

            for (d in result.child("places").children) {
                places.add(Place(
                    d.key ?: "",
                    d.child("building").value as String? ?: "",
                    (d.child("destinations").value as String? ?: "").split(","),
                    (d.child("level").value as Long? ?: 0).toInt()
                ))
            }

            for (d in result.child("rooms").children) {
                rooms.add(Room(
                    d.key ?: "",
                    d.child("name").value as String? ?: "",
                    d.child("place").value as String? ?: "",
                    (d.child("tags").value as String? ?: "").split(",")
                ))
            }

            isLoaded = true
        }
    }
}