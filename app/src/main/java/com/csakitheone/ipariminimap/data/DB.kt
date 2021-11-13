package com.csakitheone.ipariminimap.data

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase

class DB {
    companion object {
        var databaseVersion: String = "1"

        private val rtdb = FirebaseDatabase.getInstance().reference
        private val db = rtdb.child("databases/$databaseVersion")

        fun getLinks(callback: (Map<String, String>) -> Unit) {
            db.child("links").get().addOnCompleteListener {
                if (!it.isSuccessful || it.result == null) {
                    callback(mapOf())
                    Data.links = mutableMapOf()
                    return@addOnCompleteListener
                }

                val links = mutableMapOf<String, String>()
                for (linkData in it.result!!.children) {
                    if (!linkData.key.isNullOrBlank())
                        links[linkData.key!!] = linkData.value.toString()
                }
                Data.links = links
                callback(links)
            }
        }

        fun downloadBuildingData(callback: (Boolean) -> Unit) {
            db.get().addOnCompleteListener {
                if (!it.isSuccessful || it.result == null) {
                    callback(false)
                    return@addOnCompleteListener
                }

                loadBuildingData(it.result!!)
                callback(true)
            }
        }

        private fun loadBuildingData(result: DataSnapshot) {
            Data.buildings.clear()
            for (d in result.child("buildings").children) {
                Data.buildings.add(
                    Data.Building(
                        d.key ?: "",
                        d.child("name").value as String? ?: "",
                        d.child("places").children.map { place ->
                            Data.Place(
                                place.key ?: "",
                                (place.child("destinations").value as String? ?: "").split(",")
                                    .map { r -> r.trim() }.filter { r -> r.isNotBlank() },
                                (place.child("level").value as Long? ?: 0).toInt(),
                                place.child("help").value as String? ?: "",
                                place.child("rooms").children.map { room ->
                                    Data.Room(
                                        room.key ?: "",
                                        room.child("name").value as String? ?: "",
                                        (room.child("tags").value as String? ?: "").split(",")
                                            .map { r -> r.trim() }.filter { r -> r.isNotBlank() }
                                    )
                                }.toMutableList()
                            )
                        }.toMutableList()
                    )
                )
            }

            Data.isLoaded = true
        }

    }

    class Admin {
        companion object {
            fun setLinks(links: Map<String, String>, callback: ((Boolean) -> Unit)? = null) {
                if (!Prefs.getIsAdmin()) {
                    callback?.invoke(false)
                    return
                }
                db.child("links").setValue(links).addOnCompleteListener {
                    callback?.invoke(it.isSuccessful)
                }
            }

            fun uploadBuildingData(callback: (Boolean) -> Unit) {
                if (!Prefs.getIsAdmin()) {
                    callback(false)
                    return
                }
                db.updateChildren(Data.getBuildingData()).addOnCompleteListener {
                    callback(it.isSuccessful)
                }
            }
        }
    }
}