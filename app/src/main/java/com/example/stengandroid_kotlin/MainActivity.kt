package com.example.stengandroid_kotlin

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle

import android.os.Handler
import android.provider.Settings
import android.telephony.*
import android.os.CountDownTimer
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.compose.ui.input.key.Key.Companion.Home
import androidx.fragment.app.Fragment
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response

import com.android.volley.VolleyError
import com.android.volley.toolbox.*
import com.example.stengandroid_kotlin.model.Signal
import com.example.stengandroid_kotlin.model.VolleySingleton
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.stengandroid_kotlin.R.*
import com.example.stengandroid_kotlin.model.Signal
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import com.google.android.material.bottomnavigation.BottomNavigationView
import org.json.JSONException
import org.json.JSONObject
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter


class MainActivity : AppCompatActivity() {

//    private lateinit var startButton : Button
//    private lateinit var stopButton : Button
//
//    private lateinit var timeStampData : TextView
//    private lateinit var latData : TextView
//    private lateinit var longData : TextView
//    private lateinit var altData : TextView
//    private lateinit var snrData : TextView
//    private lateinit var cellID : TextView
//    private lateinit var ueID : TextView
//    private lateinit var accuracyData : TextView

    private lateinit var bottomNavigationView : BottomNavigationView
    private lateinit var locationClient: LocationClient


    var locationLatitude: Double? = null
    var locationLongitude: Double? = null
    var locationAltitude: Double? = null

    var deviceId = ""

    val sgDateTimePattern = "dd/MM/yyyy HH:mm z"
    val sgDateFormatter = DateTimeFormatter.ofPattern(sgDateTimePattern)

    var telephonyManager: TelephonyManager? = null


    // on below line we are creating a variable for our url.
    // temporary mock api. to eventually change to http://18.183.118.160:3000/api/post
    private var url = "https://18.183.118.160:3000/api/post"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layout.activity_main)


        telephonyManager = this.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

        locationClient = DefaultLocationClient(applicationContext, LocationServices.getFusedLocationProviderClient(applicationContext))
        bottomNavigationView = findViewById(R.id.bottomNavigationView)

        replaceFragment(HomeFragment())

        bottomNavigationView.setOnItemSelectedListener {

            when (it.itemId){
                id.Button_Home -> replaceFragment(HomeFragment())
                id.Button_Log -> replaceFragment(LogFragment())
                id.Button_Map -> replaceFragment(MapFragment())

                else -> {}
            }
            true
        }

        locationClient = DefaultLocationClient(applicationContext, LocationServices.getFusedLocationProviderClient(applicationContext))


        checkPermissions()

/*leon's code

        val cache = DiskBasedCache(cacheDir, 1024 * 1024) // 1MB cap

// Set up the network to use HttpURLConnection as the HTTP client.
        val network = BasicNetwork(HurlStack())

// Instantiate the RequestQueue with the cache and network. Start the queue.
        val requestQueue = RequestQueue(cache, network).apply {
            start()
        }
        // Retrieve Unique User Equipment ID
        deviceId = getUniqueDeviceID().toString()
*/
    }


    private fun checkPermissions(){
        if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, android.Manifest.permission.INTERNET)!= PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 100)
            return
        }

    }


    //--------------------------- Start of Handler for getting data

    var handler: Handler = Handler()
    var delay = 5000 // Currently, calling functions at 5 seconds interval

    var myRunnable = object : Runnable {
        override fun run() {
            createSignalObj()
            Log.v("idempotent", "i am RUNNING")
            Toast.makeText(
                this@MainActivity, "i am RUNNING",
                Toast.LENGTH_SHORT
            ).show()
            Log.v("idempotent", "i am POSTING")

            handler.postDelayed(this, delay.toLong())
        }
    }

    private fun startHandler(){
        handler.postDelayed(myRunnable, delay.toLong())
        Log.v("idempotent", "start handle")
    }

    private fun stopHandler(){
        stopListeners()
        handler.removeCallbacks(myRunnable)
        Log.v("idempotent", "stop handle")
    }

    private fun createSignalObj(){
        getSignalStrength().toString()
        var snr = sigStr.toString()

        var currentDateTime = LocalDateTime.now()

        var locationLatitudeSig = locationLatitude
        var locationLongitudeSig = locationLongitude
        var locationAltitudeSig = locationAltitude

        var cellId = "iamadrone5678"

        var signal = Signal(
            currentDateTime,
            locationLatitudeSig,
            locationLongitudeSig,
            locationAltitudeSig,
            snr,
            deviceId,
            cellId)

        createSignalVolley(signal)
    }

    //--------------------------- End of handler for getting data


    //--------------------------- Start of Signal Strength Retrieval

    var sigStrTelCallback:TelephonyCallback? = null
    var sigStrPSLCallback:PhoneStateListener? = null
    var sigStr: Int? = null

    private fun getSignalStrength(): Int?{
        stopListeners()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            telephonyManager?.registerTelephonyCallback(this.mainExecutor, cbTelephony())
        } else {
            telephonyManager?.listen(cbPhoneStateListener(),PhoneStateListener.LISTEN_SIGNAL_STRENGTHS)
        }
        return sigStr
    }

    // For phones on SDK API Ver 31 and newer
    @RequiresApi(Build.VERSION_CODES.S)
    fun cbTelephony():TelephonyCallback{
       var cb = object : TelephonyCallback(), TelephonyCallback.SignalStrengthsListener {
            override fun onSignalStrengthsChanged(signalStrength: SignalStrength) {
                processSignalStrengthData(signalStrength)
            }
        }
        sigStrTelCallback = cb;
        Log.v("idempotent","sigStrCallbackThingy assigned")
        return cb
    }

    // For phones on SDK API Ver older than 31
    fun cbPhoneStateListener():PhoneStateListener{
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
    fun processSignalStrengthData(signalStrength:SignalStrength){
        var listSig: List<CellSignalStrength> = signalStrength.cellSignalStrengths
        for (sig in listSig){
            if (sig is CellSignalStrengthNr){
                sigStr = sig.dbm
                Log.v("idempotent","Nr found " + sig.dbm)
            } else {
                sigStr = sig.dbm
                Log.v("idempotent","Other sig found " + sig.dbm)
            }
        }
    }

    fun stopListeners(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            sigStrTelCallback?.let {
                telephonyManager?.unregisterTelephonyCallback(it)
                Log.v("idempotent","sigStrCallbackThingy unregistered")
            }}
        else {
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
    // post: http://18.183.118.160:3000/api/post
    // get: http://18.183.118.160:3000/api/getAll
    var urlPost = "https://6sgje9hh91.api.quickmocker.com/api/mock/post"
    var urlGet = "https://6sgje9hh91.api.quickmocker.com/api/mock/get"

    private fun createSignalVolley(signal:Signal){
        Log.v("idempotent", "post 1")
        // creating a new variable for our request queue
        val queue = Volley.newRequestQueue(this@MainActivity)
/*leon's code
        val json = JSONObject()
        json.put("timestamp", signal.timestamp)
        json.put("lat", signal.latitude.toString())
        json.put("long", signal.longtitude.toString())
        json.put("height", signal.altitude.toString())
        json.put("snr", signal.snr)
        json.put("cellid", signal.cellid)
        json.put("ueid", signal.ueid)
        // making a string request to update our data and
        // passing method as POST. to update our data.
        val jsonObjectRequest =
                JsonObjectRequest(Request.Method.POST, url, json, { response ->
                    val str = response.toString()
                    Toast.makeText(this, str, Toast.LENGTH_SHORT)
                    println(str)
                }, {
                        error ->
                    Log.d("TAG","response: ${error.message}")
                })
*/
        Log.v("idempotent", "post 2")
        // making a string request to update our data and
        // passing method as POST. to update our data.
        val request: StringRequest =
            object : StringRequest(Request.Method.POST, urlPost, object : Response.Listener<String?> {
                override fun onResponse(response: String?) {
                    Log.v("idempotent", "post 3")
                    // on below line we are displaying a toast message as data updated.

                    Toast.makeText(this@MainActivity, "Storing Data..", Toast.LENGTH_SHORT).show()
                }
            }, Response.ErrorListener { error -> // displaying toast message on response failure.
                Log.e("tag", "error is " + error!!.message)
                Toast.makeText(this@MainActivity, "Failed to store data", Toast.LENGTH_SHORT)
                    .show()
            }) {
                override fun getParams(): Map<String, String>? {
                    Log.v("idempotent", "post 4")
                    // below line we are creating a map for storing
                    // our values in key and value pair.
                    val params: MutableMap<String, String> = HashMap()

                    // on below line we are passing our key
                    // and value pair to our parameters.
                    params["timestamp"] = sgDateFormatter.format(ZonedDateTime.of(signal.timestamp, ZoneId.of("GMT+8")))
                    params["lat"] = signal.latitude.toString()
                    params["long"] = signal.longitude.toString()
                    params["height"] = signal.altitude.toString()
                    params["snr"] = signal.snr.toString()
                    params["ueid"] = signal.ueid.toString()
                    params["cellid"] = signal.cellid.toString()

                    // returning our params.
                    return params
                }
            }
        VolleySingleton.getInstance(this).addToRequestQueue(jsonObjectRequest)
    }


    /* Retrieving JSONArray
     */
    private fun getSignalsVolley()
    {
        // on below line we are creating a variable for our
        // request queue and initializing it.
        val queue = Volley.newRequestQueue(this@MainActivity)

        // on below line we are creating a variable for request
        // and initializing it with json object request
        val request =
            JsonArrayRequest(Request.Method.GET, urlGet, null,  { response ->
                // on below line we are displaying a toast message as data is retrieved.
                Toast.makeText(this@MainActivity, "Data Updated..", Toast.LENGTH_SHORT).show()
                try {
                    for (i in 0 until response.length()){
                        val responseObject = response.getJSONObject(i)

                        // on below line we are getting data from our response
                        // and setting it in variables.
                        getJsonResponseString(responseObject)

                        // <INSERT HERE> Set text to ListView using adapter to display each responseObject data
                        // on below line we are setting
                        // our string to our text view.
                        // xxxxx

                    }

                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }, { error ->
                // displaying toast message on response failure.
                Log.e("tag", "error is " + error!!.message)
                Toast.makeText(this@MainActivity, "Failed to get response", Toast.LENGTH_SHORT)
                    .show()
            })
        queue.add(request)
    }

    /* Retrieving JSONObject
     */
    private fun getSignalVolley()
    {

        // on below line we are creating a variable for our
        // request queue and initializing it.
        val queue = Volley.newRequestQueue(this@MainActivity)

        // on below line we are creating a variable for request
        // and initializing it with json object request
        val request =
            JsonObjectRequest(Request.Method.GET, urlGet, null,  { response ->
                    // on below line we are displaying a toast message as data is retrieved.
                    Toast.makeText(this@MainActivity, "Data Updated", Toast.LENGTH_SHORT).show()
                    try {
                         // on below line we are getting data from our response
                        // and setting it in variables.
                        var signal = getJsonResponseString(response)

                        // on below line we are setting
                        // our string to our text view.
                        setJsonResponseStringToView(signal)

                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
            }, { error ->
                    // displaying toast message on response failure.
                    Log.e("tag", "error is " + error!!.message)
                    Toast.makeText(this@MainActivity, "Failed to get response", Toast.LENGTH_SHORT)
                        .show()
            })
        queue.add(request)
    }

    // To retrieve data of Signal properties from JSONObject
    private fun getJsonResponseString(response: JSONObject): Signal {
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
    }

    // To display data of Signal properties on layout view
    private fun setJsonResponseStringToView(signal: Signal){
        timeStampData.text = signal.timestamp.toString()
        latData.text = signal.latitude.toString()
        longData.text = signal.longitude.toString()
        altData.text = signal.altitude.toString()
        snrData.text = signal.snr.toString()
        ueID.text = signal.ueid.toString()
        cellID.text = signal.cellid.toString()
    }

    //--------------------------- End of API calls handling

    private fun replaceFragment(fragment: Fragment){

        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(id.frame_layout, fragment)
        fragmentTransaction.commit()
    }
}