package com.android.gpsapp.ui.main.view

import android.Manifest
import android.content.pm.PackageManager
import android.location.GnssMeasurementsEvent
import android.location.GnssStatus
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.android.gpsapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding


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

}

