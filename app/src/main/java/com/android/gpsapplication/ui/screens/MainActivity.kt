package com.android.gpsapplication.ui.screens

import android.Manifest
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.android.gpsapplication.databinding.ActivityMainBinding
import com.android.gpsapplication.ui.screens.statusscreen.GpsStatusFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val requestLocationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
        ::onGotLocationPermissionResult
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        checkPermissions()
        setGpsStatusFragment()
    }

    private fun setGpsStatusFragment() {
        supportFragmentManager
            .beginTransaction()
            .replace(binding.container.id, GpsStatusFragment())
            .commit()
    }

    private fun checkPermissions() {
        requestLocationPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_LOCATION_EXTRA_COMMANDS
            )
        )
    }

    private fun onGotLocationPermissionResult(grantResults: Map<String, Boolean>) {
        if (grantResults.entries.all { it.value }) {
            onLocationPermissionsGranted()
        }
    }

    private fun onLocationPermissionsGranted() {
        Toast.makeText(this, "Location permission is granted", Toast.LENGTH_SHORT)
            .show()
    }
}