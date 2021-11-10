package com.android.gpsapp.ui.main.view

import android.Manifest
import android.content.pm.PackageManager
import android.location.GnssMeasurementsEvent
import android.location.GnssStatus
import android.location.LocationManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.android.gpsapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var statusGnss: GnssStatus

    private lateinit var gnssStatusListener: GnssStatus.Callback
    private lateinit var gnssMeasurementsListener: GnssMeasurementsEvent
    private lateinit var locationManager: LocationManager

    init {
        locationManager = this.getSystemService(LOCATION_SERVICE) as LocationManager
        addStatusListener()

        addGnssMeasurementsListener()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setGpsStatusFragment()
    }

    private fun setGpsStatusFragment() {
        supportFragmentManager
            .beginTransaction()
            .replace(binding.container.id, GpsStatusFragment())
            .commit()
    }

    private fun addStatusListener() {
        gnssStatusListener = object : GnssStatus.Callback() {

            override fun onSatelliteStatusChanged(status: GnssStatus) {
                super.onSatelliteStatusChanged(status)
                statusGnss = status
            }
        }
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        locationManager.registerGnssStatusCallback(gnssStatusListener)
    }
}

