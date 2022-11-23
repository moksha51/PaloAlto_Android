package com.example.stengandroid_kotlin

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.stengandroid_kotlin.model.Signal
import org.json.JSONException
import org.json.JSONObject

class MainActivity : AppCompatActivity() {

    lateinit var startButton : Button
    lateinit var endButton : Button

    lateinit var timeStamp : TextView
    lateinit var latData : TextView
    lateinit var longData : TextView
    lateinit var altData : TextView
    lateinit var snrData : TextView
    lateinit var cellID : TextView
    lateinit var ueID : TextView

    // on below line we are creating a variable for our url.
    // temporary mock api. to eventually change to http://18.183.118.160:3000/api/post
    var url = "https://6sgje9hh91.api.quickmocker.com/api/mock/test"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //timeStamp code

        //
        timeStamp = findViewById(R.id.timeStamp)
        latData = findViewById(R.id.textView_Latitude_Data)
        longData = findViewById(R.id.textView_Longitude_Data)
        altData = findViewById(R.id.textView_Altitude_Data)
        snrData = findViewById(R.id.textView_SNR_Data)
        cellID = findViewById(R.id.textView_CELLID_Data)
        ueID = findViewById(R.id.textView_UEID_Data)
        startButton = findViewById(R.id.Button_Start)
        endButton = findViewById(R.id.Button_End)

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

            VolleyService(signal)

            /*
            basically below code is will be changed to the corresponding data, i.e. once latData is pulled, change latData to 100,028.812398
            or however it's supposed to look like
            */
            /*
            latData.text = "latData is pulled"
            longData.text = "longData is pulled"
            altData.text = "altData is pulled"
            snrData.text = "snrData is pulled"
            cellID.text = "hardcoded cell ID"
            ueID.text = "hardcoded UEID"
             */
            Toast.makeText(this, "to test whether Start Button works", Toast.LENGTH_SHORT).show()

        }

        endButton.setOnClickListener {
            Toast.makeText(this, "to test whether END button works", Toast.LENGTH_SHORT).show()
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
                        timeStamp.text = timeStampResp
                        latData.text = latDataResp
                        longData.text = longDataResp
                        altData.text = altDataResp
                        snrData.text = snrDataResp
                        ueID.text = ueIDResp
                        cellID.text = cellIDResp
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

}