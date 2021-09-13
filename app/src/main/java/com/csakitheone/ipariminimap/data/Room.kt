package com.csakitheone.ipariminimap.data

class Room() {
    var placeName: String = ""
    var number: Int = -1

    var name: String = ""
    var tags: MutableList<String> = mutableListOf()
    var userTags: MutableList<String> = mutableListOf()

    constructor(placeName: String, number: Int, name: String = "", tags: List<String> = listOf()) : this () {
        this.placeName = placeName
        this.number = number
        this.name = name
        this.tags = tags.toMutableList()
    }

    fun getRoomName() : String {
        return if (number == -1) "" else name
    }

    fun getBuildingName() : String {
        return Data.getPlaceByName(placeName)?.buildingName ?: ""
    }

    fun getSign() : String {
        if (number == -1) return name
        val numString = if (number < 10) "0$number" else number.toString()
        return Data.getBuildingByName(getBuildingName())?.sign + Data.getPlaceByName(placeName)?.level.toString() + numString
    }

    fun containsQuery(query: String) : Boolean {
        return (getSign() + name + getBuildingName() + placeName + tags.joinToString() + userTags.joinToString() + "*")
            .toLowerCase().contains(query.toLowerCase())
    }

    companion object {
        val TAG_DRESSING = "öltöző"
        val TAG_DRESSING_NEAR = "öltöző közelben"
        val TAG_TEACHER = "tanári"
        val TAG_WC = "mosdó"
        val TAG_WC_NEAR = "mosdó közelben"
    }
}