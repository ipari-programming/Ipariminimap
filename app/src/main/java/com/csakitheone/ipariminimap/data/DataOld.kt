package com.csakitheone.ipariminimap.data

class DataOld {
    companion object {
        val buildings = mutableListOf<Building>()
        val places = mutableListOf<Place>()
        val rooms = mutableListOf<Room>()

        init {
            buildings.addAll(listOf(
                Building(Building.F, "F"),
                Building(Building.L, "L"),
                Building(Building.I, "I"),
                Building(Building.H, "H")
            ))
            places.addAll(listOf(
                Place(Building.F, "Tanári folyosó", 2, listOf("Mirelit folyosó", "Könyvtár folyosó és erkély"), "A könyvtár vagy a mirelit folyosó felől egy ajtóval elválasztva látod."),
                Place(Building.F, "Udvar", 2, listOf("Széles folyosó", "Labor szárny földszint"), "A széles folyosón egy ajtón lehet kimenni kb. az F115-ös teremnél vagy a labor földszintjén."),
                Place(Building.F, "Földszint és gépész folyosó", 0, listOf("Főlépcső (Vitrin)"), "Itt vagy először, ha bejössz a suliba."),
                Place(Building.F, "Széles folyosó", 1, listOf("Főlépcső (Vitrin)", "Könyvtár folyosó és erkély", "Udvar")),
                Place(Building.F, "Főlépcső (Vitrin)", 1, listOf("Földszint és gépész folyosó", "Széles folyosó", "Szűk folyosó", "Labor szárny földszint", "Labor szárny emelet", "Könyvtár folyosó és erkély")),
                Place(Building.F, "Szűk folyosó", 1, listOf("Főlépcső (Vitrin)", "Mirelit folyosó", "Infó első emelet", "Híradó földszint")),
                Place(Building.F, "Mirelit folyosó", 2, listOf("Szűk folyosó", "Tanári folyosó"), "A szűk folyosó végén van egy lépcső, ott kell fölmenni a mirelit folyosóra."),
                Place(Building.F, "Könyvtár folyosó és erkély", 2, listOf("Főlépcső (Vitrin)", "Tanári folyosó", "Széles folyosó"), "A főlépcsőn menj a 2. emeletre vagy a széles folyosón menj föl a matek tanárinál (F103)."),
                Place(Building.L, "Labor szárny földszint", 0, listOf("Főlépcső (Vitrin)", "Udvar", "Labor szárny emelet"), "Főlépcsőn ha elindulsz föl a 2. emeletre, félúton a folyosó vagy udvarról az üveges ajtó a labor szárny."),
                Place(Building.L, "Labor szárny emelet", 1, listOf("Főlépcső (Vitrin)", "Labor szárny földszint")),
                Place(Building.I, "Infó első emelet", 1, listOf("Szűk folyosó", "Infó tető"), "Szűk folyosón elindulsz és az első lehetőség balra."),
                Place(Building.I, "Infó tető", 2, listOf("Infó első emelet")),
                Place(Building.H, "Híradó földszint", 0, listOf("Szűk folyosó", "Híradó szakmai előadó"), "Szűk folyosón elindulsz és a második lehetőség balra."),
                Place(Building.H, "Híradó szakmai előadó", 1, listOf("Híradó földszint", "Híradó tető")),
                Place(Building.H, "Híradó tető", 2, listOf("Híradó tető"))
            ))

            // BUILDING.F special
            rooms.add(Room("Udvar", -1, "Csarnok"))
            rooms.add(Room("Udvar", -1, "Savház"))
            rooms.add(Room("Tanári folyosó", -1, "Igazgatói iroda", listOf(Room.TAG_TEACHER)))
            rooms.add(Room("Tanári folyosó", -1, "Titkárság", listOf(Room.TAG_TEACHER)))
            rooms.add(Room("Tanári folyosó", -1, "Tanári", listOf(Room.TAG_TEACHER)))

            // BUILDING.F
            rooms.addAll(generateRooms("Földszint és gépész folyosó", 7..10))
            addRoomDetails("F007", tags = listOf(Room.TAG_DRESSING))
            addRoomDetails("F008", tags = listOf(Room.TAG_DRESSING))
            addRoomDetails("F009", "Múzeum")
            addRoomDetails("F010", "Múzeum")
            rooms.add(Room("Földszint és gépész folyosó", 13, tags = listOf(Room.TAG_WC)))
            rooms.addAll(generateRooms("Földszint és gépész folyosó", 15..24))

            rooms.addAll(generateRooms("Széles folyosó", 1..3))
            rooms.add(Room("Széles folyosó", 5))
            rooms.addAll(generateRooms("Széles folyosó", 9..18))
            addRoomDetails("F101", tags = listOf(Room.TAG_WC))
            addRoomDetails("F102", tags = listOf(Room.TAG_WC))
            addRoomDetails("F103", "Matek tanári")
            addRoomDetails("F105", "Tesi tanári")
            addRoomDetails("F109", "Tesi terem", listOf(Room.TAG_DRESSING_NEAR, Room.TAG_WC_NEAR))
            addRoomDetails("F111", "Kondi")
            addRoomDetails("F117", "Irodalom tanári")

            rooms.add(Room("Főlépcső (Vitrin)", 19))
            rooms.addAll(generateRooms("Szűk folyosó", 20..35))
            addRoomDetails("F126", "DÖK szoba")
            addRoomDetails("F127", tags = listOf(Room.TAG_DRESSING))
            addRoomDetails("F134", "Töri-földrajz tanári")

            rooms.addAll(generateRooms("Mirelit folyosó", 15..21))
            addRoomDetails("F215", tags = listOf(Room.TAG_WC))
            addRoomDetails("F216", tags = listOf(Room.TAG_WC))
            addRoomDetails("F221", "Kobán László irodája", tags = listOf(Room.TAG_TEACHER))

            rooms.add(Room("Könyvtár folyosó és erkély", 1))
            rooms.add(Room("Könyvtár folyosó és erkély", 3, "Gazdasági iroda"))
            rooms.add(Room("Könyvtár folyosó és erkély", 4))
            rooms.add(Room("Könyvtár folyosó és erkély", 7, "A könyvtár"))
            rooms.addAll(generateRooms("Könyvtár folyosó és erkély", 9..11))

            // BUILDING.L
            rooms.add(Room("Labor szárny földszint", 2, tags = listOf(Room.TAG_WC)))
            rooms.add(Room("Labor szárny földszint", 3, tags = listOf(Room.TAG_WC)))
            // TODO: room numbers
            rooms.add(Room("Labor szárny földszint", 9, "szerves labor"))
            rooms.add(Room("Labor szárny földszint", -1, "labor előkészítő"))
            rooms.add(Room("Labor szárny földszint", -1, "öveges labor 1"))
            rooms.add(Room("Labor szárny földszint", -1, "öveges labor 2"))
            rooms.add(Room("Labor szárny földszint", -1, "labor tanári", listOf(Room.TAG_TEACHER)))
            rooms.add(Room("Labor szárny földszint", 13, "ruhatár (ez is egy tanterem)"))
            rooms.add(Room("Labor szárny földszint", 14))
            rooms.add(Room("Labor szárny földszint", 15))
            rooms.add(Room("Labor szárny földszint", 16, "orvosi szoba (közvetlen a főlépcső mellett)"))
            rooms.add(Room("Labor szárny emelet", 2, tags = listOf(Room.TAG_WC)))
            rooms.add(Room("Labor szárny emelet", 3, "medence"))
            rooms.add(Room("Labor szárny emelet", 6, "Sárdi Ildikó irodája", listOf(Room.TAG_TEACHER)))
            rooms.add(Room("Labor szárny emelet", 7, "labor tanári", listOf(Room.TAG_TEACHER)))
            rooms.add(Room("Labor szárny emelet", 8, "analitika labor"))
            rooms.add(Room("Labor szárny emelet", 14, "műszeres labor"))
            rooms.add(Room("Labor szárny emelet", 15, "műszeres labor"))
            rooms.add(Room("Labor szárny emelet", 16, "műszeres labor"))
            rooms.add(Room("Labor szárny emelet", -1, "mérleg szoba"))

            // BUILDING.I
            rooms.add(Room("Infó első emelet", 2, "Rajz terem (hardware terem lesz)", listOf(Room.TAG_WC_NEAR)))
            rooms.addAll(generateRooms("Infó tető", 2..4))
            rooms.add(Room("Infó tető", 1, "szerver szoba"))
            rooms.add(Room("Infó tető", 5, "infó tanári", listOf(Room.TAG_WC_NEAR)))

            // BUILDING.H
            rooms.add(Room("Híradó földszint", 1, "akvárium"))
            rooms.add(Room("Híradó földszint", 5, tags = listOf(Room.TAG_WC)))
            rooms.add(Room("Híradó földszint", 6, tags = listOf(Room.TAG_WC)))
            rooms.add(Room("Híradó földszint", 7, "rendszergazda iroda"))
            rooms.add(Room("Híradó földszint", 8, "Cisco labor"))
            rooms.add(Room("Híradó földszint", 9))
            rooms.add(Room("Híradó földszint", 10))
            rooms.add(Room("Híradó földszint", 11, tags = listOf(Room.TAG_WC_NEAR)))
            rooms.add(Room("Híradó szakmai előadó", 1, "infó tanári"))
            rooms.add(Room("Híradó szakmai előadó", 2, "szakmai előadó terem"))
            rooms.add(Room("Híradó tető", 3, tags = listOf(Room.TAG_WC_NEAR)))
            rooms.add(Room("Híradó tető", 4))
        }

        fun getBuildingByName(name: String) : Building? {
            return if (buildings.any { r -> r.name == name })
                buildings.first { r -> r.name == name }
            else null
        }

        fun getPlaceByName(name: String) : Place? {
            return if (places.any { r -> r.name == name })
                places.first { r -> r.name == name }
            else null
        }

        fun getRoomBySign(sign: String) : Room? {
            return if (rooms.any { r -> r.getSign() == sign })
                rooms.first { r -> r.getSign() == sign }
            else null
        }

        fun addRoomDetails(roomSign: String, name: String = "", tags: List<String> = listOf()) {
            if (name.isNotEmpty()) rooms[rooms.indexOf(getRoomBySign(roomSign))].name = name
            if (tags.isNotEmpty()) rooms[rooms.indexOf(getRoomBySign(roomSign))].tags.addAll(tags)
            if (name.contains("tanári") && !name.contains("tanári ")) {
                rooms[rooms.indexOf(getRoomBySign(roomSign))].tags.add(Room.TAG_TEACHER)
            }
        }

        private fun generateRooms(placeName: String, range: IntRange) : List<Room> {
            val generatedRooms = mutableListOf<Room>()
            for (i in range) {
                generatedRooms.add(Room(placeName, i))
            }
            return generatedRooms
        }
    }
}