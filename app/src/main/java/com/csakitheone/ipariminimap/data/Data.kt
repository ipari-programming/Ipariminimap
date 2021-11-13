package com.csakitheone.ipariminimap.data

import com.google.firebase.database.DataSnapshot

class Data {
    data class Building(var id: String, var name: String) {
        fun toPair(): Pair<String, Any> {
            return Pair(id, mapOf("name" to name))
        }
    }
    data class Place(var name: String, var buildingName: String, var destinations: List<String> = listOf(), var level: Int = 0, var help: String = "") {
        fun toPair(): Pair<String, Any> {
            return Pair(name, mapOf("building" to buildingName, "destinations" to destinations.joinToString(), "level" to level, "help" to help))
        }
    }
    data class Room(var id: String, var name: String = "", var placeName: String, var tags: List<String> = listOf()) {
        fun toPair(): Pair<String, Any> {
            return Pair(id, mapOf("name" to name, "place" to placeName, "tags" to tags.joinToString()))
        }
    }

    companion object {
        var links = mutableMapOf<String, String>()
        var buildings = mutableListOf<Building>()
        var places = mutableListOf<Place>()
        var rooms = mutableListOf<Room>()

        val tags = listOf("mosdó", "mosdó közelben", "öltöző", "öltöző közelben", "tanári")

        private var isLoaded = false

        fun getIsLoaded(): Boolean = isLoaded

        fun getBuildingData(): Map<String, Any> {
            val map = mutableMapOf<String, Any>()

            map.put("buildings", buildings.map { r -> r.toPair() }.toMap())
            map.put("places", places.map { r -> r.toPair() }.toMap())
            map.put("rooms", rooms.map { r -> r.toPair() }.toMap())

            return map
        }

        fun loadBuildingData(result: DataSnapshot) {
            buildings.clear()
            for (d in result.child("buildings").children) {
                buildings.add(Building(
                    d.key ?: "",
                    d.child("name").value as String? ?: ""
                ))
            }
            places.clear()
            for (d in result.child("places").children) {
                places.add(Place(
                    d.key ?: "",
                    d.child("building").value as String? ?: "",
                    (d.child("destinations").value as String? ?: "").split(",").map { r -> r.trim() }.filter { r -> r.isNotBlank() },
                    (d.child("level").value as Long? ?: 0).toInt(),
                    d.child("help").value as String? ?: ""
                ))
            }
            rooms.clear()
            for (d in result.child("rooms").children) {
                rooms.add(Room(
                    d.key ?: "",
                    d.child("name").value as String? ?: "",
                    d.child("place").value as String? ?: "",
                    (d.child("tags").value as String? ?: "").split(",").map { r -> r.trim() }.filter { r -> r.isNotBlank() }
                ))
            }

            isLoaded = true
        }

        fun clear() {
            links.clear()
            buildings.clear()
            places.clear()
            rooms.clear()
            isLoaded = false
        }
    }
}