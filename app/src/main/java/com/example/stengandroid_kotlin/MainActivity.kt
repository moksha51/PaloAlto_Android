package com.example.stengandroid_kotlin

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.ConnectivityManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.telephony.*
import android.text.InputType
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.example.stengandroid_kotlin.R.*
import com.example.stengandroid_kotlin.R.color.*
import com.example.stengandroid_kotlin.model.Signal
import com.example.stengandroid_kotlin.model.VolleySingleton
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.json.JSONObject
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.concurrent.timerTask

class MainActivity : AppCompatActivity() {

    private lateinit var startButton: Button
    private lateinit var stopButton: Button
    private lateinit var editIntervalButton: Button
    private lateinit var saveIntervalButton: Button

    private lateinit var tv_dateTime: TextView
    private lateinit var tv_lat: TextView
    private lateinit var tv_long: TextView
    private lateinit var tv_alt: TextView
    private lateinit var tv_snr: TextView
    private lateinit var tv_cellID: TextView
    private lateinit var tv_ueID: TextView
    private lateinit var tv_accuracy: TextView
    private lateinit var tv_downSpeed: TextView
    private lateinit var tv_upSpeed: TextView
    private lateinit var tv_interval: TextView
    private lateinit var tv_timer: TextView

    private lateinit var et_interval: EditText

    private lateinit var locationClient: LocationClient

    private lateinit var signal: Signal

    private val mInterval = 1
    private var handler: Handler? = null
    private var timeInSeconds = 0L
    private var startButtonClicked = false

    private val simpleDateFormatter =
        java.text.SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault())

    private var locationDateTime: String? = null
    private var locationLatitude: String? = null
    private var locationLongitude: String? = null
    private var locationAltitude: String? = null
    private var locationAccuracy: String? = null
    private var settingDeviceId: String? = null
    private var signalSnr: String = "0"
    private var cellId = "iamadrone5678"
    private var upSpeed: String? = null
    private var downSpeed: String? = null
    private var interval: Long = 5000
    val MULTIPLE_PERMISSONS = 100

    private var telephonyManager: TelephonyManager? = null

    private val postUrl = "https://18.183.118.160:3000/api/post"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layout.activity_main)

        checkPermissions()

        val cm =
            applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val nc = cm.getNetworkCapabilities(cm.activeNetwork)

        var locationBackgroundJob: Job? = null

        telephonyManager = this.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

        val sharedPreferences = getSharedPreferences("Interval", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        locationClient = DefaultLocationClient(
            applicationContext,
            LocationServices.getFusedLocationProviderClient(applicationContext)
        )

        startButton = findViewById(id.button_Start)
        stopButton = findViewById(id.button_Stop)
        editIntervalButton = findViewById(id.button_EditInterval)
        saveIntervalButton = findViewById(id.button_SaveInterval)

        tv_lat = findViewById(id.textView_Latitude)
        tv_long = findViewById(id.textView_Longitude)
        tv_alt = findViewById(id.textView_Altitude)
        tv_accuracy = findViewById(id.textView_Accuracy)
        tv_dateTime = findViewById(id.textView_timeStamp)
        tv_snr = findViewById(id.textView_SNR)
        tv_cellID = findViewById(id.textView_CELLID)
        tv_ueID = findViewById(id.textView_UEID)
        tv_downSpeed = findViewById(id.textView_downSpeed)
        tv_upSpeed = findViewById(id.textView_upSpeed)
        tv_interval = findViewById(id.textView_interval)
        tv_timer = findViewById(id.textView_timer)
        tv_interval.text =
            getString(string.Interval) + sharedPreferences.getLong("interval", interval!!)
                .toString() + getString(string.ms)
        et_interval = findViewById(id.editText_interval)

        NukeSSLCerts.nuke()
        getSignalStrength()

        tv_timer.text = getString(string.timer)
        tv_ueID.text = getString(string.ueId) + getUniqueDeviceID()
        settingDeviceId = getUniqueDeviceID()

        downSpeed = nc?.linkDownstreamBandwidthKbps.toString()
        upSpeed = nc?.linkUpstreamBandwidthKbps.toString()


        startButton.setOnClickListener {
            startTimer()
            tv_cellID.text = getString(string.cellId) + getCellID().toString()
            tv_snr.text = getString(string.snr) + getSignalStrength().toString() + " " + sigType.toString()
            tv_upSpeed.text = upSpeed + getString(string.Kbps)
            tv_downSpeed.text = downSpeed + getString(string.Kbps)
            interval = sharedPreferences.getLong("interval", interval)
            tv_interval.text =
                getString(string.Interval) + interval.toString() + getString(string.ms)
            Intent(applicationContext, LocationService::class.java).apply {
                action = LocationService.ACTION_START
                startService(this)
                locationBackgroundJob = locationClient.getLocationUpdates(interval)
                    .catch { e -> e.printStackTrace() }
                    .onEach { location ->
                        tv_lat.text = getString(string.latitude) + location.latitude.toString()
                        locationLatitude = location.latitude.toString()
                        tv_long.text = getString(string.longitude) + location.longitude.toString()
                        locationLongitude = location.longitude.toString()
                        tv_alt.text =
                            getString(string.altitude) + location.altitude.toString().take(6)
                        locationAltitude = location.altitude.toString()
                        tv_accuracy.text = getString(string.accuracy) + location.accuracy.toString()
                        locationAccuracy = location.accuracy.toString()
                        val dateFormatted = Date(location.time)
                        val dateToText = simpleDateFormatter.format(dateFormatted)
                        tv_dateTime.text = dateToText
                        locationDateTime = dateToText.toString()
                        tv_snr.text = getString(string.snr) + getSignalStrength().toString() + " " + sigType.toString()
                        signalSnr = getSignalStrength().toString()
                        tv_downSpeed.text = getString(string.downSpeed) + nc?.linkDownstreamBandwidthKbps.toString() + getString(string.Kbps)
                        tv_upSpeed.text = getString(string.upSpeed) + nc?.linkUpstreamBandwidthKbps.toString() + getString(string.Kbps)
                        cellId = getCellID().toString()
                        tv_cellID.text = cellId
                        createSignalObj()
                    }.launchIn(MainScope())
            }
            startButton.setText(string.live)
            startButton.setBackgroundColor(Color.GREEN)
        }

        stopButton.setOnClickListener {
            stopTimer()
            resetTimerView()
            Intent(applicationContext, LocationService::class.java).apply {
                action = LocationService.ACTION_STOP
                startService(this)
                locationBackgroundJob?.cancel()
                stopListeners()
                startButton.text = getString(string.start)
                startButton.setBackgroundColor(resources.getColor(purple_200))
            }
        }
        editIntervalButton.setOnClickListener {
            editIntervalButton.visibility = View.INVISIBLE
            saveIntervalButton.visibility = View.VISIBLE
            tv_interval.visibility = View.INVISIBLE
            et_interval.visibility = View.VISIBLE
            et_interval.inputType = InputType.TYPE_CLASS_NUMBER
        }
        saveIntervalButton.setOnClickListener {
            tv_interval.visibility = View.VISIBLE
            et_interval.visibility = View.INVISIBLE
            editIntervalButton.visibility = View.VISIBLE
            saveIntervalButton.visibility = View.INVISIBLE
            val interval = et_interval.text.toString().toLongOrNull()
            if (interval != null) {
                editor.putLong("interval", interval)
                editor.apply()
                Toast.makeText(this, "New interval duation is: $interval", Toast.LENGTH_SHORT)
                    .show()
                tv_interval.text =
                    getString(string.Interval) + interval.toString() + getString(string.ms)
            }
        }
    }

    private fun checkPermissions() {
        if (ActivityCompat.checkSelfPermission(
                this@MainActivity,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.INTERNET
            ) != PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(
                this@MainActivity,
                android.Manifest.permission.READ_PHONE_STATE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION,
                    android.Manifest.permission.INTERNET,
                    android.Manifest.permission.READ_PHONE_STATE
                ),
                MULTIPLE_PERMISSONS
            )
        }
    }

    private fun createSignalObj() {
        signalSnr = getSignalStrength().toString()

        var signal = Signal(
            locationDateTime,
            locationLatitude,
            locationLongitude,
            locationAltitude,
            locationAccuracy,
            signalSnr,
            settingDeviceId,
            cellId,
            upSpeed,
            downSpeed
        )
        createSignalVolley(signal, interval)
        Toast.makeText(this, locationDateTime.toString(), Toast.LENGTH_SHORT).show()
    }

//--------------------------- Start of Signal Strength Retrieval

    var sigStrTelCallback: TelephonyCallback? = null
    var sigStrPSLCallback: PhoneStateListener? = null
    var sigStr: Int? = null
    var sigType: String? = null

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
            sigStr = sig.dbm
            if (sig is CellSignalStrengthNr) {
                sigType = "5G"
            } else {
                sigType = "Not 5G"
            }
        }
    }

    fun stopListeners() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            sigStrTelCallback?.let {
                telephonyManager?.unregisterTelephonyCallback(it)
            }
        } else {
            sigStrPSLCallback?.let {
                telephonyManager?.listen(
                    sigStrPSLCallback,
                    PhoneStateListener.LISTEN_NONE
                )
            }
        }
    }

//--------------------------- End of Signal Strength Retrieval


//--------------------------- Start of Device ID Retrieval
    fun getUniqueDeviceID(): String? {
        val text = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
        return text
    }

    fun getCellID(): String? {
        val text = telephonyManager?.simCarrierId.toString()
        return text
    }
//--------------------------- End of Device ID Retrieval

//--------------------------- Start of API calls handling

    private fun createSignalVolley(signal: Signal, interval: Long) {
        Timer().schedule(timerTask {
            val json = JSONObject()
            json.put("timestamp", signal.dateTime)
            json.put("lat", signal.latitude)
            json.put("long", signal.longitude)
            json.put("snr", signal.signalStrength)
            json.put("height", signal.altitude)
            json.put("ueid", signal.ueId)
            json.put("cellid", signal.cellId)

            val jsonObjectRequest =
                JsonObjectRequest(Request.Method.POST, postUrl, json, { response ->
                    val str = response.toString()
                    Toast.makeText(this@MainActivity, str, Toast.LENGTH_SHORT).show()
                }, { error ->
                    Log.d("TAG", "response: ${error.message}")
                })
            VolleySingleton.getInstance(this@MainActivity).addToRequestQueue(jsonObjectRequest)
        }, interval)

    }

    private fun resetTimerView() {
        timeInSeconds = 0
        startButtonClicked = false
    }

    private fun startTimer() {
        handler = Handler(Looper.getMainLooper())
        mStatusChecker.run()
        startButtonClicked = true
    }

    private fun stopTimer() {
        handler?.removeCallbacks(mStatusChecker)
        startButtonClicked = false
    }

    private var mStatusChecker: Runnable = object : Runnable {
        override fun run() {
            try {
                timeInSeconds += 1
                updateStopWatchView(timeInSeconds)
            } finally {
                // 100% guarantee that this always happens, even if
                // your update method throws an exception
                handler!!.postDelayed(this, mInterval.toLong())
            }
        }
    }

    private fun updateStopWatchView(timeInSeconds: Long) {
        val formattedTime = getFormattedStopWatch((timeInSeconds * 1000))
        tv_timer.text = getString(string.timer)+formattedTime
    }

    private fun getFormattedStopWatch(ms: Long): String {
        var milliseconds = ms
        val hours = TimeUnit.MILLISECONDS.toHours(milliseconds)
        milliseconds -= TimeUnit.HOURS.toMillis(hours)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds)
        milliseconds -= TimeUnit.MINUTES.toMillis(minutes)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds)

        return "${if (hours < 10) "0" else ""}$hours:" +
                "${if (minutes < 10) "0" else ""}$minutes:" +
                "${if (seconds < 10) "0" else ""}$seconds"
    }
}
