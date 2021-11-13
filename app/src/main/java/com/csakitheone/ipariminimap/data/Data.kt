package com.csakitheone.ipariminimap.data

import com.google.firebase.database.DataSnapshot

class Data {
    data class Building(var id: String, var name: String, var places: MutableList<Place> = mutableListOf()) {
        fun toData(): Pair<String, Any> {
            return Pair(id, mapOf("name" to name, "places" to places.map { r -> r.toData() }.toMap()))
        }
    }
    data class Place(var name: String, var destinations: List<String> = listOf(), var level: Int = 0, var help: String = "", var rooms: MutableList<Room> = mutableListOf()) {
        fun toData(): Pair<String, Any> {
            return Pair(name, mapOf("destinations" to destinations.joinToString(), "level" to level, "help" to help, "rooms" to rooms.map { r -> r.toData() }.toMap()))
        }
    }
    data class Room(var id: String, var name: String = "", var tags: List<String> = listOf()) {
        fun toData(): Pair<String, Any> {
            return Pair(id, mapOf("name" to name, "tags" to tags.joinToString()))
        }

        override fun toString(): String {
            val place = getAllPlaces().find { r -> r.rooms.contains(this) }
            val building = buildings.find { r -> r.places.contains(place) }
            var text = "${building?.name} > ${place?.name} > $id"
            if (name.isNotBlank()) text += "\n$name"
            return text
        }
    }

    companion object {
        var links = mutableMapOf<String, String>()
        var buildings = mutableListOf<Building>()
        val tags = listOf("mosdó", "mosdó közelben", "öltöző", "öltöző közelben", "tanári")

        var isLoaded = false
        fun getIsLoaded(): Boolean = isLoaded

        fun getBuildingData(): Map<String, Any> {
            return mapOf("buildings" to buildings.map { r -> r.toData() }.toMap())
        }

        fun getAllPlaces(): List<Place> = buildings.map { r -> r.places }.flatten()

        fun getAllRooms(): List<Room> = getAllPlaces().map { r -> r.rooms }.flatten()

        fun clear() {
            links.clear()
            buildings.clear()
            isLoaded = false
        }
    }
}