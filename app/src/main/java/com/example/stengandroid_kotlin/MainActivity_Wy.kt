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
import android.provider.Settings
import android.telephony.*
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.stengandroid_kotlin.R.*
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

class MainActivity_Wy : AppCompatActivity() {

    private lateinit var startButton: Button
    private lateinit var stopButton: Button

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

    private lateinit var et_interval: EditText

    private lateinit var locationClient: LocationClient

    private lateinit var signal : Signal

    val varName = listOf(
        "dateTime",
        "latitude",
        "longitude",
        "altitude",
        "accuracy",
        "snr",
        "ueId",
        "cellId",
        "upSpeed",
        "downSpeed"
    )

    private val simpleDateFormatter =
        java.text.SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault())

    private var locationDateTime: String? = null
    private var locationLatitude: String? = null
    private var locationLongitude: String? = null
    private var locationAltitude: String? = null
    private var locationAccuracy: String? = null
    private var settingDeviceId: String? = null
    private var signalSnr: String? = null
    private var signalSnr123: String? = null
    private var cellId = "iamadrone5678"
    private var upSpeed: String? = null
    private var downSpeed: String? = null
    private var interval: Long? = null

    private var telephonyManager: TelephonyManager? = null

    private var url = "https://18.183.118.160:3000/api/post"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layout.activity_main_wy)

        checkPermissions()

        var cm =
            applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val nc = cm.getNetworkCapabilities(cm.activeNetwork)

        var locationBackgroundJob: Job? = null

        telephonyManager = this.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

        locationClient = DefaultLocationClient(
            applicationContext,
            LocationServices.getFusedLocationProviderClient(applicationContext)
        )

        startButton = findViewById(id.button_Start)
        stopButton = findViewById(id.button_Stop)

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

        NukeSSLCerts.nuke()

        getSignalStrength()

        downSpeed = nc?.linkDownstreamBandwidthKbps.toString()
        upSpeed = nc?.linkUpstreamBandwidthKbps.toString()

        startButton.setOnClickListener {
            tv_ueID.text = string.ueId.toString() + getUniqueDeviceID()
            settingDeviceId = getUniqueDeviceID()
            tv_snr.text = string.dBm.toString() + getSignalStrength().toString()
            tv_upSpeed.text = upSpeed + string.Kbps
            tv_downSpeed.text = downSpeed + string.Kbps
//            getSignalVolley()
            Intent(applicationContext, LocationService::class.java).apply {
                action = LocationService.ACTION_START
                startService(this)
                locationBackgroundJob = locationClient.getLocationUpdates(10000L)
                    .catch { e -> e.printStackTrace() }
                    .onEach { location ->
                        tv_lat.text = string.latitude.toString() + location.latitude.toString()
                        locationLatitude = location.latitude.toString()
                        tv_long.text = "Long: " + location.longitude.toString()
                        locationLongitude = location.longitude.toString()
                        tv_alt.text = "Alt: " + location.altitude.toString().take(6)
                        locationAltitude = location.altitude.toString()
                        tv_accuracy.text = "Accuracy: " + location.accuracy.toString()
                        locationAccuracy = location.accuracy.toString()
                        val dateFormatted = Date(location.time)
                        val dateToText = simpleDateFormatter.format(dateFormatted)
                        tv_dateTime.text = string.currentDateTime.toString() + dateToText
                        locationDateTime = dateToText.toString()
                        signalSnr123 = getSignalStrength().toString()
                        createSignalObj()
                    }.launchIn(MainScope())
            }

//            if (tv_lat.text != getString(R.string.latitudeNA))
//                tv_lat.setBackgroundColor(Color.GREEN)
//            if (tv_long.text != getString(R.string.longitudeNA))
//                tv_long.setBackgroundColor(Color.GREEN)
//            if (tv_alt.text != getString(R.string.altitudeNA))
//                tv_alt.setBackgroundColor(Color.GREEN)
//            if (tv_accuracy.text != getString(R.string.accuracyNA))
//                tv_accuracy.setBackgroundColor(Color.GREEN)
//            if (tv_snr.text != getString(R.string.snrNA))
//                tv_snr.setBackgroundColor(Color.GREEN)
//            if (tv_ueID.text != getString(R.string.UEIDNA))
//                tv_ueID.setBackgroundColor(Color.GREEN)
//            if (tv_cellID.text != getString(R.string.CELLIDNA))
//                tv_cellID.setBackgroundColor(Color.GREEN)
//            if (tv_upSpeed.text != getString(R.string.upSpeedNA))
//                tv_upSpeed.setBackgroundColor(Color.GREEN)
//            if (tv_downSpeed.text != getString(R.string.downSpeedNA))
//                tv_downSpeed.setBackgroundColor(Color.GREEN)

            startButton.setText(string.live)
            startButton.setBackgroundColor(Color.GREEN)
            tv_downSpeed.text = "Down: " + downSpeed + " Kbps"
            tv_upSpeed.text = "Up: " + upSpeed + " Kbps"
        }

        stopButton.setOnClickListener {
            Intent(applicationContext, LocationService::class.java).apply {
                action = LocationService.ACTION_STOP
                startService(this)
                locationBackgroundJob?.cancel()
                Log.v("idempotent", "is location backgrd job cancelled? " + locationBackgroundJob?.isCancelled.toString())
                stopListeners()
                startButton.setText(string.start)
                startButton.setBackgroundColor(Color.BLUE)
                //stopHandler()
            }
        }
    }

    val MULTIPLE_PERMISSONS = 100

    private fun checkPermissions() {
        if (ActivityCompat.checkSelfPermission(
                this@MainActivity_Wy,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.INTERNET
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.INTERNET),
                MULTIPLE_PERMISSONS
            )
//            return
        }
    }

    var handler: Handler = Handler()
    var delay = 5000 // Currently, calling functions at 5 seconds interval

    var myRunnable = object : Runnable {
        override fun run() {
            createSignalObj()
            handler.postDelayed(this, delay.toLong())
        }
    }

    private fun startHandler() {
        handler.postDelayed(myRunnable, delay.toLong())
        Log.v("idempotent", "start handle")
    }

    private fun stopHandler() {
        stopListeners()
        handler.removeCallbacks(myRunnable)
        Log.v("idempotent", "stop handle")
    }

    private fun createSignalObj() {
        signalSnr = getSignalStrength().toString()

        var signal = Signal(
            locationDateTime,
            locationLatitude,
            locationLongitude,
            locationAltitude,
            locationAccuracy,
            signalSnr123,
            settingDeviceId,
            cellId,
            upSpeed,
            downSpeed
        )

        createSignalVolley(signal)
        Toast.makeText(this, locationDateTime.toString(), Toast.LENGTH_SHORT).show()
    }

//--------------------------- End of handler for getting data


//--------------------------- Start of Signal Strength Retrieval

    var sigStrTelCallback: TelephonyCallback? = null
    var sigStrPSLCallback: PhoneStateListener? = null
    var sigStr: Int? = null

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

//--------------------------- End of Signal Strength Retrieval


//--------------------------- Start of Device ID Retrieval

    fun getUniqueDeviceID(): String? {
        var uniqueDeviceId: String? = ""
        uniqueDeviceId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
        Log.v("idempotent", "i am your DEVICE ID " + uniqueDeviceId)
        return uniqueDeviceId
    }

//--------------------------- End of Device ID Retrieval


//--------------------------- Start of API calls handling

    // on below lines we are creating a variable for our url.
// temporary mock api. to eventually change to
    var post = "https://18.183.118.160:3000/api/post"

    // get: http://18.183.118.160:3000/api/getAll
    var urlPost = "https://6sgje9hh91.api.quickmocker.com/api/mock/post"
    var urlGet = "https://6sgje9hh91.api.quickmocker.com/api/mock/get"

    private fun createSignalVolley(signal: Signal) {

        val queue = Volley.newRequestQueue(this@MainActivity_Wy)
        val json = JSONObject()
        json.put("timestamp", signal.dateTime)
        json.put("lat", signal.latitude)
        json.put("long", signal.longitude)
        json.put("snr", signal.signalStrength)
        json.put("height", signal.altitude)
        json.put("ueid", signal.ueId)
        json.put("cellid", signal.cellId)


//        json.put("long", signal.longitude.toString())
//        json.put("snr", signal.snr.toString())
//        json.put("cellid", signal.cellid.toString())
//        json.put("ueid", signal.ueid.toString())
        // making a string request to update our data and
        // passing method as POST. to update our data.
        val jsonObjectRequest =
            JsonObjectRequest(Request.Method.POST, post, json, { response ->
                val str = response.toString()
                Toast.makeText(this@MainActivity_Wy, str, Toast.LENGTH_SHORT).show()
                Log.v("idempotent", "is it posting?")
                println(str)
            }, { error ->
                Log.d("TAG", "response: ${error.message}")
            })
//        queue.add(jsonObjectRequest)
        Log.v("idempotent", "post 2")
//        // making a string request to update our data and
//        // passing method as POST. to update our data.
//        val request: StringRequest =
//            object : StringRequest(
//                Request.Method.POST,
//                urlPost,
//                object : Response.Listener<String?> {
//                    override fun onResponse(response: String?) {
//                        Log.v("idempotent", "post 3")
//                        // on below line we are displaying a toast message as data updated.
//
//                        Toast.makeText(this@MainActivity_Wy, "Storing Data..", Toast.LENGTH_SHORT)
//                            .show()
//                    }
//                },
//                Response.ErrorListener { error -> // displaying toast message on response failure.
//                    Log.e("tag", "error is " + error!!.message)
//                    Toast.makeText(
//                        this,
//                        "Failed to store data",
//                        Toast.LENGTH_SHORT
//                    )
//                        .show()
//                }) {
//                override fun getParams(): Map<String, String>? {
//                    Log.v("idempotent", "post 4")
//                    // below line we are creating a map for storing
//                    // our values in key and value pair.
//                    val params: MutableMap<String, String> = HashMap()
//
//                    // on below line we are passing our key
//                    // and value pair to our parameters.
//                    params["timestamp"] = sgDateFormatter.format(
//                        ZonedDateTime.of(
//                            signal.timestamp,
//                            ZoneId.of("GMT+8")
//                        )
//                    )
//                    params["lat"] = signal.latitude.toString()
//                    params["long"] = signal.longitude.toString()
//                    params["height"] = signal.altitude.toString()
//                    params["snr"] = signal.snr.toString()
//                    params["ueid"] = signal.ueid.toString()
//                    params["cellid"] = signal.cellid.toString()
//
//                    // returning our params.
//                    return params
//                }
//            }
        VolleySingleton.getInstance(this).addToRequestQueue(jsonObjectRequest)
    }


    /* Retrieving JSONArray
     */
//    private fun getSignalsVolley() {
//        // on below line we are creating a variable for our
//        // request queue and initializing it.
//        val queue = Volley.newRequestQueue(this)
//
//        // on below line we are creating a variable for request
//        // and initializing it with json object request
//        val request =
//            JsonArrayRequest(Request.Method.GET, urlGet, null, { response ->
//                // on below line we are displaying a toast message as data is retrieved.
//                Toast.makeText(this, "Data Updated..", Toast.LENGTH_SHORT).show()
//                try {
//                    for (i in 0 until response.length()) {
//                        val responseObject = response.getJSONObject(i)
//
//                        // on below line we are getting data from our response
//                        // and setting it in variables.
//                        getJsonResponseString(responseObject)
//
//                        // <INSERT HERE> Set text to ListView using adapter to display each responseObject data
//                        // on below line we are setting
//                        // our string to our text view.
//                        // xxxxx
//
//                    }
//
//                } catch (e: JSONException) {
//                    e.printStackTrace()
//                }
//            }, { error ->
//                // displaying toast message on response failure.
//                Log.e("tag", "error is " + error!!.message)
//                Toast.makeText(this, "Failed to get response", Toast.LENGTH_SHORT)
//                    .show()
//            })
//        queue.add(request)
//    }

    /* Retrieving JSONObject
     */
//    private fun getSignalVolley() {
//
//        // on below line we are creating a variable for our
//        // request queue and initializing it.
//        val queue = Volley.newRequestQueue(this)
//
//        // on below line we are creating a variable for request
//        // and initializing it with json object request
//        val request =
//            JsonObjectRequest(Request.Method.GET, urlGet, null, { response ->
//                // on below line we are displaying a toast message as data is retrieved.
//                Toast.makeText(this, "Data Updated", Toast.LENGTH_SHORT).show()
//                try {
//                    // on below line we are getting data from our response
//                    // and setting it in variables.
//                    var signal = getJsonResponseString(response)
//
//                    // on below line we are setting
//                    // our string to our text view.
//                    //setJsonResponseStringToView(signal)
//
//                } catch (e: JSONException) {
//                    e.printStackTrace()
//                }
//            }, { error ->
//                // displaying toast message on response failure.
//                Log.e("tag", "error is " + error!!.message)
//                Toast.makeText(this, "Failed to get response", Toast.LENGTH_SHORT)
//                    .show()
//            })
//        queue.add(request)
//    }

    // To retrieve data of Signal properties from JSONObject
    /*private fun getJsonResponseString(response: JSONObject): Signal {
        val timeStampResp: String = response.getString("timestamp")
        val latDataResp: Double = response.getDouble("lat")
        val longDataResp: Double = response.getDouble("long")
        val altDataResp: Double = response.getDouble("height")
        val snrDataResp: String = response.getString("snr")
        val ueIDResp: String = response.getString("ueid")
        val cellIDResp: String = response.getString("cellid")

        val timeStampRespDT = LocalDateTime.parse(timeStampResp, sgDateFormatter)

        return Signal(
            timeStampRespDT,
            latDataResp,
            longDataResp,
            altDataResp,
            snrDataResp,
            ueIDResp,
            cellIDResp
        )
    }*/
}
