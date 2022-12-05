package com.example.stengandroid_kotlin.model

import java.time.LocalDateTime

class Signal {

    var timestamp:LocalDateTime? = null
    var latitude:String? = null //need to change data type?
    var longitude:String? = null //need to change data type?
    var altitude:String? = null //need to change data type?
    var accuracy: String? = null
    var snr:String? = null
    var ueid:String? = null
    var cellid:String? = null
    var upSpeed: String? = null
    var downSpeed: String? = null


    constructor(
        timestamp: LocalDateTime?,
        latitude: String?,
        longitude: String?,
        altitude: String?,
        accuracy: String?,
        snr: String?,
        ueid: String?,
        cellid: String?,
        upSpeed: String?,
        downSpeed: String?
    ) {
        this.timestamp = timestamp
        this.latitude = latitude
        this.longitude = longitude
        this.altitude = altitude
        this.accuracy = accuracy
        this.snr = snr
        this.ueid = ueid
        this.cellid = cellid
        this.upSpeed = upSpeed
        this.downSpeed = downSpeed
    }

}