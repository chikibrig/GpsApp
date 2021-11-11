package com.android.gpsapp.ui.main.view

import android.Manifest
import android.app.Application
import android.content.Context.LOCATION_SERVICE
import android.content.pm.PackageManager
import android.location.*
import android.os.*
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

class GpsStatusFragment() : Fragment() {

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

        binding.btnStartGps.setOnClickListener {
            registerLocation()
            registerMeasurements()
            registerGnssStatus()
            registerNavigation()
            //registerNmea()
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

    //Location listener
    private val locationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            latitudeView.text = location.latitude.toString()
            longitudeView.text = location.longitude.toString()
            Log.d(
                "locListener",
                "called ${location.provider} ${location.extras} ${location.accuracy}"
            )
        }

        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {

        }
    }

    //Measurements listener
    private val gnssMeasurementsEventListener = object : GnssMeasurementsEvent.Callback() {
        override fun onGnssMeasurementsReceived(eventArgs: GnssMeasurementsEvent?) {
            Log.d("before meas", "size: ${eventArgs?.measurements?.size ?: "null"}")
            super.onGnssMeasurementsReceived(eventArgs)
            Log.d("meas after", "size: ${eventArgs?.measurements?.size ?: "null"}")
        }

        override fun onStatusChanged(status: Int) {
            super.onStatusChanged(status)
            Log.d("meas after", "size: ${status}")
        }
    }

    //Navigation listener
    private val gnssNavigationMessageListener = object : GnssNavigationMessage.Callback() {
        override fun onGnssNavigationMessageReceived(event: GnssNavigationMessage?) {
            super.onGnssNavigationMessageReceived(event)
            Log.d("navReceived", "size: ${event.toString()}")
        }

        override fun onStatusChanged(status: Int) {
            super.onStatusChanged(status)
            Log.d("navStatusChanged", "size: ${status}")
        }
    }

    //Status Listener
    private val gnssStatusListener = object : GnssStatus.Callback() {
        override fun onStarted() {
            super.onStarted()
        }

        override fun onStopped() {
            super.onStopped()
        }

        override fun onFirstFix(ttffMillis: Int) {
            super.onFirstFix(ttffMillis)
        }

        override fun onSatelliteStatusChanged(status: GnssStatus) {
            super.onSatelliteStatusChanged(status)
            Log.d("satelliteStatus", "${status.satelliteCount}")
        }

    }

    //Nmea Listener
    private val nmeaMessageListener = object : OnNmeaMessageListener {
        override fun onNmeaMessage(message: String?, l: Long) {
            Log.d("nmeaMessageListener", " m: $message; l: $l")
        }
    }

    fun registerLocation() {
        locationManager.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            5000,
            0f,
            locationListener
        )
        Toast.makeText(requireContext(), "request", Toast.LENGTH_SHORT).show()
        Log.d("updates", "${locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)}")
    }

    fun unregisterLocation() {
        locationManager.removeUpdates(locationListener)
    }

    fun registerMeasurements() {
        Log.d(
            "regMeas", "${
                locationManager.registerGnssMeasurementsCallback(
                    gnssMeasurementsEventListener,
                    Handler(Looper.getMainLooper())
                )
            }"
        )
        Log.d("registerMeasurement", "called")
    }

    fun unregisterMeasurements() {
        locationManager.unregisterGnssMeasurementsCallback(gnssMeasurementsEventListener)
    }

    fun registerNavigation() {
        locationManager.registerGnssNavigationMessageCallback(
            gnssNavigationMessageListener,
            Handler(Looper.getMainLooper())
        )
    }

    fun unregisterNavigation() {
        locationManager.unregisterGnssNavigationMessageCallback(gnssNavigationMessageListener)
    }

    fun registerGnssStatus() {
        locationManager.registerGnssStatusCallback(
            gnssStatusListener,
            Handler(Looper.getMainLooper())
        )
    }

    fun unregisterGnssStatus() {
        locationManager.unregisterGnssStatusCallback(gnssStatusListener)
    }

    fun registerNmea() {
        locationManager.addNmeaListener(nmeaMessageListener, Handler(Looper.getMainLooper()))
    }

    fun unregisterNmea() {
        locationManager.removeNmeaListener(nmeaMessageListener)
    }

    fun unregisterAll() {
        unregisterLocation();
        unregisterMeasurements();
        unregisterNavigation();
        unregisterGnssStatus();
        unregisterNmea();
    }

}



