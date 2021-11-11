package com.csakitheone.ipariminimap.data

import com.google.firebase.database.FirebaseDatabase

class DB {
    companion object {
        val databaseVersion: Int = 1

        private var isConnected = false
        private var db = FirebaseDatabase.getInstance().reference

        fun getIsConnected(): Boolean = isConnected

        fun connect(callback: ((Int) -> Unit)? = null) {
            db.child("meta/database-version").get().addOnCompleteListener {
                val remoteDatabaseVersion: Int = if (it.isSuccessful) {
                    (it.result?.value as Long? ?: -1).toInt()
                }
                else -1
                isConnected = databaseVersion == remoteDatabaseVersion
                callback?.invoke(remoteDatabaseVersion)
            }
        }

        fun getLinks(callback: (Map<String, String>) -> Unit) {
            if (!isConnected) {
                callback(mapOf())
                return
            }

            db.child("links").get().addOnCompleteListener {
                if (!it.isSuccessful || it.result == null) {
                    callback(mapOf())
                    return@addOnCompleteListener
                }

                val links = mutableMapOf<String, String>()
                for (linkData in it.result!!.children) {
                    if (!linkData.key.isNullOrBlank())
                        links[linkData.key!!] = linkData.value.toString()
                }
                callback(links)
            }
        }
    }
}