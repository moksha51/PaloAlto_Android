package com.example.stengandroid_kotlin

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.compose.setContent

class MainActivity : AppCompatActivity() {

    lateinit var startButton : Button
    lateinit var endButton : Button

    //lateinit var timeStamp : TextView
    lateinit var latData : TextView
    lateinit var longData : TextView
    lateinit var altData : TextView
    lateinit var snrData : TextView
    lateinit var cellID : TextView
    lateinit var ueID : TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //timeStamp code

        //
        latData = findViewById(R.id.textView_Latitude_Data)
        longData = findViewById(R.id.textView_Longitude_Data)
        altData = findViewById(R.id.textView_Altitude_Data)
        snrData = findViewById(R.id.textView_SNR_Data)
        cellID = findViewById(R.id.textView_CELLID_Data)
        ueID = findViewById(R.id.textView_UEID_Data)
        startButton = findViewById(R.id.Button_Start)
        endButton = findViewById(R.id.Button_End)

        startButton.setOnClickListener {
            /*
            basically below code is will be changed to the corresponding data, i.e. once latData is pulled, change latData to 100,028.812398
            or however it's supposed to look like
            */
            latData.text = "latData is pulled"
            longData.text = "longData is pulled"
            altData.text = "altData is pulled"
            snrData.text = "snrData is pulled"
            cellID.text = "hardcoded cell ID"
            ueID.text = "hardcoded UEID"
            Toast.makeText(this, "to test whether Start Button works", Toast.LENGTH_SHORT).show()
        }

        endButton.setOnClickListener {
            Toast.makeText(this, "to test whether END button works", Toast.LENGTH_SHORT).show()
        }
    }
}