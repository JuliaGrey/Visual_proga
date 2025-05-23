package com.example.calculator

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Environment
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

class GPS : AppCompatActivity(), LocationListener {

    private lateinit var tv: TextView
    private lateinit var lm: LocationManager
    private var tracking = false
    private val gpsFile = "gps2.json"
    private val df = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gps)

        tv = findViewById(R.id.tvCoordinates)
        lm = getSystemService(LOCATION_SERVICE) as LocationManager

        findViewById<Button>(R.id.btnStart).setOnClickListener { startTracking() }
        findViewById<Button>(R.id.btnStop).setOnClickListener { stopTracking() }
    }

    private fun startTracking() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1
            )
            return
        }

        lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)?.let { update(it) }
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0f, this)
        tracking = true
        saveToJson("""{"start","time":"${df.format(Date())}"}""")
    }

    private fun stopTracking() {
        lm.removeUpdates(this)
        tracking = false
        saveToJson("""{"stop","time":"${df.format(Date())}"}""")
    }

    override fun onLocationChanged(l: Location) {
        update(l)
        if (tracking) saveToJson(
            """{"lat":${l.latitude},"lon":${l.longitude},"alt":${l.altitude},"time":"${
                df.format(
                    Date(l.time)
                )
            }"}"""
        )
    }

    private fun update(l: Location) {
        tv.text =
            "Lat: ${l.latitude}\nLon: ${l.longitude}\nAlt: ${l.altitude}\nTime: ${df.format(Date(l.time))}"
    }

    private fun saveToJson(json: String) {
        try {
            File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
                gpsFile
            ).let {
                FileWriter(it, true).use { fw -> fw.write("$json\n") }
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(rq: Int, p: Array<String>, g: IntArray) {
        super.onRequestPermissionsResult(rq, p, g)
        if (rq == 1 && g.isNotEmpty() && g[0] == PackageManager.PERMISSION_GRANTED) startTracking()
    }

    override fun onProviderEnabled(p: String) {}
    override fun onProviderDisabled(p: String) {}
    override fun onStatusChanged(p: String?, s: Int, e: Bundle?) {}

    override fun onDestroy() {
        super.onDestroy()
        if (tracking) stopTracking()
    }
}