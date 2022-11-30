package com.example.stengandroid_kotlin

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
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
import androidx.compose.ui.input.key.Key.Companion.Home
import androidx.fragment.app.Fragment
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.stengandroid_kotlin.R.*
import com.example.stengandroid_kotlin.model.Signal
import com.google.android.material.bottomnavigation.BottomNavigationView
import org.json.JSONException
import org.json.JSONObject

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

    // on below line we are creating a variable for our url.
    // temporary mock api. to eventually change to http://18.183.118.160:3000/api/post
    var url = "https://6sgje9hh91.api.quickmocker.com/api/mock/test"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layout.activity_main)

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

        // temporarily hardcoded Signal properties
        var signal = Signal(
            "1600",
            38.8951,
            -77.0364,
            38.8951,
            "41",
            "89816510727414341010",
            "testcellid1234")

//        startButton.setOnClickListener {
//
//            Intent(applicationContext, LocationService::class.java).apply{
//                action = LocationService.ACTION_START
//                startService(this)
//                locationClient
//                    //interval 1000L = 1 sec, 10000L = 10 secs
//                    .getLocationUpdates(1000L)
//                    .catch {  e -> e.printStackTrace()}
//                    .onEach { location ->
//                        latData.text = getString(string.latitude) + location.latitude.toString()
//                        if (latData.text != getString(string.latitudeNA))
//                            latData.setBackgroundColor(Color.GREEN)
//
//                        longData.text = getString(string.longitude) + location.longitude.toString()
//                        if (longData.text != getString(string.longitudeNA))
//                            longData.setBackgroundColor(Color.GREEN)
//
//                        altData.text = getString(string.altitude) + location.altitude.toString().take(6)
//                        if (altData.text != getString(string.altitudeNA))
//                            altData.setBackgroundColor(Color.GREEN)
//
//                        accuracyData.text = getString(string.accuracy) + location.accuracy.toString()
//                        if (accuracyData.text != getString(string.accuracyNA))
//                            accuracyData.setBackgroundColor(Color.GREEN)
//                    }
//                    .launchIn(MainScope())
//            }
//            startButton.setBackgroundColor(Color.GREEN)
//            startButton.text = getString(string.live)
//        }
//
//        stopButton.setOnClickListener {
//            Intent(applicationContext, LocationService::class.java).apply {
//                action = LocationService.ACTION_STOP
//                startService(this)
//
//                latData.text = getString(string.latitudeNA)
//                latData.setBackgroundColor(Color.WHITE)
//
//                longData.text = getString(string.longitudeNA)
//                longData.setBackgroundColor(Color.WHITE)
//
//                altData.text = getString(string.altitudeNA)
//                altData.setBackgroundColor(Color.WHITE)
//
//                accuracyData.text = getString(string.accuracyNA)
//                accuracyData.setBackgroundColor(Color.WHITE)
//
//                stopService(this)
//                startButton.setBackgroundColor(Color.BLUE)
//                startButton.text = getString(string.start)
//            }
//            VolleyService(signal)
//        }
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

        // making a string request to update our data and
        // passing method as POST. to update our data.
        val request: StringRequest =
            object : StringRequest(Request.Method.POST, url, object : Response.Listener<String?> {
                override fun onResponse(response: String?) {
                    // on below line we are displaying a toast message as data updated.
                    Toast.makeText(this@MainActivity, "Data Updated..", Toast.LENGTH_SHORT).show()
                    try {
                        // on below line we are extracting data from our json object
                        // and passing our response to our json object.
                        val jsonObject = JSONObject(response)

                        // on below line we are getting data from our response
                        // and setting it in variables.
                        val timeStampResp: String = jsonObject.getString("timestamp")
                        val latDataResp: String = jsonObject.getString("lat")
                        val longDataResp: String = jsonObject.getString("long")
                        val altDataResp: String = jsonObject.getString("height")
                        val snrDataResp: String = jsonObject.getString("snr")
                        val ueIDResp: String = jsonObject.getString("ueid")
                        val cellIDResp: String = jsonObject.getString("cellid")

                        // on below line we are setting
                        // our string to our text view.
//                        timeStampData.text = timeStampResp
//                        latData.text = latDataResp
//                        longData.text = longDataResp
//                        altData.text = altDataResp
//                        snrData.text = snrDataResp
//                        ueID.text = ueIDResp
//                        cellID.text = cellIDResp
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }
            }, object : Response.ErrorListener {
                override fun onErrorResponse(error: VolleyError?) {
                    // displaying toast message on response failure.
                    Log.e("tag", "error is " + error!!.message)
                    Toast.makeText(this@MainActivity, "Fail to update data..", Toast.LENGTH_SHORT)
                        .show()
                }
            }) {
                override fun getParams(): Map<String, String>? {

                    // below line we are creating a map for storing
                    // our values in key and value pair.
                    val params: MutableMap<String, String> = HashMap()

                    // on below line we are passing our key
                    // and value pair to our parameters.
                    params["timestamp"] = signal.timestamp.toString()
                    params["lat"] = signal.latitude.toString()
                    params["long"] = signal.longtitude.toString()
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

    private fun replaceFragment(fragment: Fragment){

        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(id.frame_layout, fragment)
        fragmentTransaction.commit()
    }

}