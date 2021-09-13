package com.csakitheone.ipariminimap.data

class Building() {
    var name: String = ""
    var sign: String = ""

    constructor(name: String, sign: String) : this() {
        this.name = name
        this.sign = sign
    }

    companion object {
        val F = "Főépület"
        val L = "Labor szárny"
        val I = "Infó szárny"
        val H = "Híradó szárny"
        val OUTSIDE = "Udvar"
    }
}