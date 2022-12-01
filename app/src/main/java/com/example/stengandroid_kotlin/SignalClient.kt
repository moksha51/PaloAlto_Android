package com.example.stengandroid_kotlin

interface SignalClient {

    fun getSignalStrength():Int?

    fun stopListeners()

}