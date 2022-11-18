package com.example.stengandroid_kotlin

import java.text.SimpleDateFormat
import java.util.*
import android.content.Context
import android.content.SharedPreferences
import java.util.prefs.Preferences

class DataHelper(context: Context) {
    private var sharedPref :SharedPreferences = context.getSharedPreferences(PREFERENCES,Context.MODE_PRIVATE)
    public var timeStamp = SimpleDateFormat("dd/mm/yyyy HH:mm:ss", Locale.getDefault())

    private var timerCounting = false
    private var startTime: Date? = null
    private var endTime: Date? = null
    private var altitude = 0;
    private var longitude = 0;
    private var latitude = 0;
    private var ueId = "ABC";
    private var cellId = "DEF";

    init
    {
        timerCounting = sharedPref.getBoolean(COUNTING_KEY, false)

        val startString = sharedPref.getString(START_TIME_KEY, null)
        if (startString != null)
            startTime = timeStamp.parse(startString)

        val stopString = sharedPref.getString(STOP_TIME_KEY, null)
        if (stopString != null)
            endTime = timeStamp.parse(stopString)
    }


    fun startTime(): Date? = startTime

    fun setStartTime(date: Date?)
    {
        startTime = date
        with(sharedPref.edit())
        {
            val stringDate = if (date == null) null else timeStamp.format(date)
            putString(START_TIME_KEY,stringDate)
            apply()
        }
    }

    fun stopTime(): Date? = endTime

    fun setStopTime(date: Date?)
    {
        endTime = date
        with(sharedPref.edit())
        {
            val stringDate = if (date == null) null else timeStamp.format(date)
            putString(STOP_TIME_KEY,stringDate)
            apply()
        }
    }

    fun timerCounting(): Boolean = timerCounting

    fun setTimerCounting(value: Boolean)
    {
        timerCounting = value
        with(sharedPref.edit())
        {
            putBoolean(COUNTING_KEY,value)
            apply()
        }
    }
    companion object
    {
        const val PREFERENCES = "prefs"
        const val START_TIME_KEY = "startKey"
        const val STOP_TIME_KEY = "stopKey"
        const val COUNTING_KEY = "countingKey"
    }
}