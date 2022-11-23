package com.example.stengandroid_kotlin.model

class Signal {

    var timestamp:String? = null //to change to DateTime data type
    var latitude:Double? = null //need to change data type?
    var longtitude:Double? = null //need to change data type?
    var altitude:Double? = null //need to change data type?
    var snr:String? = null
    var ueid:String? = null
    var cellid:String? = null

    constructor(
        timestamp: String?,
        latitude: Double?,
        longtitude: Double?,
        altitude: Double?,
        snr: String?,
        ueid: String?,
        cellid: String?
    ) {
        this.timestamp = timestamp
        this.latitude = latitude
        this.longtitude = longtitude
        this.altitude = altitude
        this.snr = snr
        this.ueid = ueid
        this.cellid = cellid
    }
}