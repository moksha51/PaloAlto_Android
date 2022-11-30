package com.example.stengandroid_kotlin

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.*
import com.example.stengandroid_kotlin.model.Signal
import com.example.stengandroid_kotlin.model.VolleySingleton
import org.json.JSONException
import org.json.JSONObject

class MainActivity : AppCompatActivity() {

    private lateinit var startButton : Button
    private lateinit var stopButton : Button

    private lateinit var timeStampData : TextView
    private lateinit var latData : TextView
    private lateinit var longData : TextView
    private lateinit var altData : TextView
    private lateinit var snrData : TextView
    private lateinit var cellID : TextView
    private lateinit var ueID : TextView
    private lateinit var accuracyData : TextView

    private lateinit var locationClient: LocationClient

    // on below line we are creating a variable for our url.
    // temporary mock api. to eventually change to http://18.183.118.160:3000/api/post
    private var url = "https://18.183.118.160:3000/api/post"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        locationClient = DefaultLocationClient(applicationContext, LocationServices.getFusedLocationProviderClient(applicationContext))

        timeStampData = findViewById(R.id.textView_timeStamp)
        latData = findViewById(R.id.textView_Latitude)
        longData = findViewById(R.id.textView_Longitude)
        altData = findViewById(R.id.textView_Altitude)
        snrData = findViewById(R.id.textView_SNR)
        cellID = findViewById(R.id.textView_CELLID)
        ueID = findViewById(R.id.textView_UEID)
        accuracyData = findViewById(R.id.textView_Accuracy)

        startButton = findViewById(R.id.Button_Start)
        stopButton = findViewById(R.id.Button_Stop)

        checkPermissions()

        // temporarily hardcoded Signal properties
        var signal = Signal(
            "1600",
            38.8951,
            -77.0364,
            38.8951,
            "41",
            "89816510727414341010",
            "testcellid1234")

        startButton.setOnClickListener {

            Intent(applicationContext, LocationService::class.java).apply{
                action = LocationService.ACTION_START
                startService(this)
                locationClient
                    //interval 1000L = 1 sec, 10000L = 10 secs
                    .getLocationUpdates(1000L)
                    .catch {  e -> e.printStackTrace()}
                    .onEach { location ->
                        latData.text = getString(R.string.latitude) + location.latitude.toString()
                        if (latData.text != getString(R.string.latitudeNA))
                            latData.setBackgroundColor(Color.GREEN)

                        longData.text = getString(R.string.longitude) + location.longitude.toString()
                        if (longData.text != getString(R.string.longitudeNA))
                            longData.setBackgroundColor(Color.GREEN)

                        altData.text = getString(R.string.altitude) + location.altitude.toString().take(6)
                        if (altData.text != getString(R.string.altitudeNA))
                            altData.setBackgroundColor(Color.GREEN)

                        accuracyData.text = getString(R.string.accuracy) + location.accuracy.toString()
                        if (accuracyData.text != getString(R.string.accuracyNA))
                            accuracyData.setBackgroundColor(Color.GREEN)
                    }
                    .launchIn(MainScope())
            }
            startButton.setBackgroundColor(Color.GREEN)
            startButton.text = getString(R.string.live)
            VolleyService(signal)
        }

        stopButton.setOnClickListener {
            Intent(applicationContext, LocationService::class.java).apply {
                action = LocationService.ACTION_STOP
                startService(this)

                latData.text = getString(R.string.latitudeNA)
                latData.setBackgroundColor(Color.WHITE)

                longData.text = getString(R.string.longitudeNA)
                longData.setBackgroundColor(Color.WHITE)

                altData.text = getString(R.string.altitudeNA)
                altData.setBackgroundColor(Color.WHITE)

                accuracyData.text = getString(R.string.accuracyNA)
                accuracyData.setBackgroundColor(Color.WHITE)

                stopService(this)
                startButton.setBackgroundColor(Color.BLUE)
                startButton.text = getString(R.string.start)
            }
        }

        val cache = DiskBasedCache(cacheDir, 1024 * 1024) // 1MB cap

// Set up the network to use HttpURLConnection as the HTTP client.
        val network = BasicNetwork(HurlStack())

// Instantiate the RequestQueue with the cache and network. Start the queue.
        val requestQueue = RequestQueue(cache, network).apply {
            start()
        }
    }

    private fun checkPermissions(){
        if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, android.Manifest.permission.INTERNET)!= PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 100)
            return
        }

    }

    private fun VolleyService(signal:Signal){

        // creating a new variable for our request queue
        val queue = Volley.newRequestQueue(this@MainActivity)
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
        // below line is to make
        // a json object request.
        VolleySingleton.getInstance(this).addToRequestQueue(jsonObjectRequest)
    }

}