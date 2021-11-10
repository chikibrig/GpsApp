package com.android.gpsapp.ui.main.view

import android.Manifest
import android.app.Application
import android.content.Context.LOCATION_SERVICE
import android.content.pm.PackageManager
import android.location.*
import android.os.Build
import android.os.Bundle
import android.os.CancellationSignal
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.android.gpsapp.R
import com.android.gpsapp.data.model.SatelliteMetadata
import com.android.gpsapp.databinding.FragmentGpsStatusBinding
import com.android.gpsapp.ui.main.adapter.GpsStatusAdapter
import com.android.gpsapp.ui.main.viewmodel.GpsStatusViewModel
import java.security.Provider
import java.util.*

class GpsStatusFragment() : Fragment(), LocationListener {

    private lateinit var binding: FragmentGpsStatusBinding

    private lateinit var latitudeView: TextView
    private lateinit var longitudeView: TextView

    private lateinit var gnssStatusList: RecyclerView
    private lateinit var sbasStatusList: RecyclerView

    private lateinit var gnssAdapter: GpsStatusAdapter
    private lateinit var sbasAdapter: GpsStatusAdapter

    private lateinit var locationManager: LocationManager
    private lateinit var loc: Location

    private lateinit var viewModel: AndroidViewModel


    private val requestLocationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
        ::onGotLocationPermissionResult
    )


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentGpsStatusBinding.inflate(inflater, container, false)
        locationManager = requireActivity().getSystemService(LOCATION_SERVICE) as LocationManager
        viewModel = ViewModelProvider(requireActivity()).get(GpsStatusViewModel::class.java)

        with(binding) {
            latitudeView = latitude
            longitudeView = longitude
        }

        binding.btnCheckPermissions.setOnClickListener {
            checkPermissions()
        }

        return binding.root
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
        Toast.makeText(requireContext(), "Location permission is granted", Toast.LENGTH_SHORT)
            .show()
    }

    override fun onLocationChanged(location: Location) {

        loc = location
        latitudeView.text = loc.latitude.toString()
        longitudeView.text = loc.longitude.toString()

    }
}



