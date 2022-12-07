package com.example.stengandroid_kotlin.model

class Signal {

    var dateTime: String? = null
    var latitude: String? = null //need to change data type?
    var longitude: String? = null //need to change data type?
    var altitude: String? = null //need to change data type?
    var accuracy: String? = null
    var signalStrength: String? = null
    var ueId: String? = null
    var cellId: String? = null
    var upSpeed: String? = null
    var downSpeed: String? = null

    constructor(
        dateTime: String?,
        latitude: String?,
        longitude: String?,
        altitude: String?,
        accuracy: String?,
        signalStrength: String?,
        ueId: String?,
        cellId: String?,
        upSpeed: String?,
        downSpeed: String?
    ) {
        this.dateTime = dateTime
        this.latitude = latitude
        this.longitude = longitude
        this.altitude = altitude
        this.accuracy = accuracy
        this.signalStrength = signalStrength
        this.ueId = ueId
        this.cellId = cellId
        this.upSpeed = upSpeed
        this.downSpeed = downSpeed
    }

}