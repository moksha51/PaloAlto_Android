package com.example.stengandroid_kotlin

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.telephony.*
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi

class SignalService : Service() {

    private lateinit var signalClient: SignalClient
    private var sigStrTelCallback: TelephonyCallback? = null
    private var sigStrPSLCallback: PhoneStateListener? = null
    private var sigStr: Int? = null
    private var telephonyManager: TelephonyManager? = null

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        //note the "this", may need to change to applicationContext???
        telephonyManager = this.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> start()
            ACTION_STOP -> stop()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun start() {
        getSignalStrength()
        cbTelephony()
        cbPhoneStateListener()
    }

    private fun stop() {
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    private fun getSignalStrength(): Int? {
        stopListeners()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            telephonyManager?.registerTelephonyCallback(this.mainExecutor, cbTelephony())
        } else {
            telephonyManager?.listen(
                cbPhoneStateListener(),
                PhoneStateListener.LISTEN_SIGNAL_STRENGTHS
            )
        }
        return sigStr
    }

    // For phones on SDK API Ver 31 and newer
    @RequiresApi(Build.VERSION_CODES.S)
    fun cbTelephony(): TelephonyCallback {
        var cb = object : TelephonyCallback(), TelephonyCallback.SignalStrengthsListener {
            override fun onSignalStrengthsChanged(signalStrength: SignalStrength) {
                processSignalStrengthData(signalStrength)
            }
        }
        sigStrTelCallback = cb;
        Log.v("idempotent", "sigStrCallbackThingy assigned")
        return cb
    }

    // For phones on SDK API Ver older than 31
    fun cbPhoneStateListener(): PhoneStateListener {
        var cb = object : PhoneStateListener() {
            @Deprecated("Deprecated in Java")
            override fun onSignalStrengthsChanged(signalStrength: SignalStrength?) {
                if (signalStrength != null) {
                    processSignalStrengthData(signalStrength)
                }
            }
        }
        sigStrPSLCallback = cb
        return cb
    }

    // Retrieve signal strength in dbm
    fun processSignalStrengthData(signalStrength: SignalStrength) {
        var listSig: List<CellSignalStrength> = signalStrength.cellSignalStrengths
        for (sig in listSig) {
            if (sig is CellSignalStrengthNr) {
                sigStr = sig.dbm
                Log.v("idempotent", "Nr found " + sig.dbm)
            } else {
                sigStr = sig.dbm
                Log.v("idempotent", "Other sig found " + sig.dbm)
            }
        }
    }

    fun stopListeners() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            sigStrTelCallback?.let {
                telephonyManager?.unregisterTelephonyCallback(it)
                Log.v("idempotent", "sigStrCallbackThingy unregistered")
            }
        } else {
            Log.v("idempotent", "old stuff")
            sigStrPSLCallback?.let {
                telephonyManager?.listen(
                    sigStrPSLCallback,
                    PhoneStateListener.LISTEN_NONE
                )
                Log.v("idempotent", "sigStrCallbackThingy unregistered")
            }
        }
    }

    companion object {
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
    }
}