package com.example.stengandroid_kotlin

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.stengandroid_kotlin.model.Signal
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.json.JSONException
import org.json.JSONObject
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle


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

    val sgDateTimePattern = "dd/MM/yyyy HH:mm z"
    val sgDateFormatter = DateTimeFormatter.ofPattern(sgDateTimePattern)

    // on below lines we are creating a variable for our url.
    // temporary mock api. to eventually change to
    // post: http://18.183.118.160:3000/api/post
    // get: http://18.183.118.160:3000/api/getAll
    var urlPost = "https://6sgje9hh91.api.quickmocker.com/api/mock/post"
    var urlGet = "https://6sgje9hh91.api.quickmocker.com/api/mock/get"

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

        // temporarily declared variables for testing
        var locationLatitude = "0"
        var locationLongitude = "0"
        var locationAltitude = "0"

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

                        locationLatitude = location.latitude.toString()
                        locationLongitude = location.longitude.toString()
                        locationAltitude = location.altitude.toString().take(6)
                    }
                    .launchIn(MainScope())
            }
            startButton.setBackgroundColor(Color.GREEN)
            startButton.text = getString(R.string.live)
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

            // temporarily hardcoded Signal properties for testing
            var currentDateTime = LocalDateTime.now()
            var signal = Signal(
                currentDateTime,
                locationLatitude.toDouble(),
                locationLongitude.toDouble(),
                locationAltitude.toDouble(),
                "41",
                "89816510727414341010",
                "testcellid1234")

            createSignalVolley(signal)
            getSignalVolley()
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

    //--------------------------- Start of API calls handling

    private fun createSignalVolley(signal:Signal){

        // creating a new variable for our request queue
        val queue = Volley.newRequestQueue(this@MainActivity)

        // making a string request to update our data and
        // passing method as POST. to update our data.
        val request: StringRequest =
            object : StringRequest(Request.Method.POST, urlPost, object : Response.Listener<String?> {
                override fun onResponse(response: String?) {
                    // on below line we are displaying a toast message as data updated.
                    Toast.makeText(this@MainActivity, "Updating Data..", Toast.LENGTH_SHORT).show()
                }
            }, Response.ErrorListener { error -> // displaying toast message on response failure.
                Log.e("tag", "error is " + error!!.message)
                Toast.makeText(this@MainActivity, "Failed to update data", Toast.LENGTH_SHORT)
                    .show()
            }) {
                override fun getParams(): Map<String, String>? {

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
        // below line is to make
        // a json object request.
        queue.add(request)
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

}