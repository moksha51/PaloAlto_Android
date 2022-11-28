package com.example.stengandroid_kotlin.model

import java.time.LocalDateTime

class Signal {

    var timestamp:LocalDateTime? = null
    var latitude:Double? = null //need to change data type?
    var longitude:Double? = null //need to change data type?
    var altitude:Double? = null //need to change data type?
    var snr:String? = null
    var ueid:String? = null
    var cellid:String? = null

    constructor(
        timestamp: LocalDateTime?,
        latitude: Double?,
        longitude: Double?,
        altitude: Double?,
        snr: String?,
        ueid: String?,
        cellid: String?
    ) {
        this.timestamp = timestamp
        this.latitude = latitude
        this.longitude = longitude
        this.altitude = altitude
        this.snr = snr
        this.ueid = ueid
        this.cellid = cellid
    }
}