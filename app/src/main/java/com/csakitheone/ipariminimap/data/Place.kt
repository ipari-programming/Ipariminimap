package com.csakitheone.ipariminimap.data

class Place() {
    var buildingName = ""
    var name: String = ""
    var level: Int = 0
    var destinations: List<String> = listOf()
    var help: String = ""

    constructor(buildingName: String, name: String, level: Int = 0, destinations: List<String> = listOf(), help: String = "") : this () {
        this.buildingName = buildingName
        this.name = name
        this.level = level
        this.destinations = destinations
        this.help = help
    }
}