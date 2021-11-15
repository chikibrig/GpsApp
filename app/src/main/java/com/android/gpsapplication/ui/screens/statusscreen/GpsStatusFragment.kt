package com.android.gpsapplication.ui.screens.statusscreen

import android.annotation.SuppressLint
import android.location.*
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.gpsapplication.R
import com.android.gpsapplication.databinding.FragmentGpsStatusBinding
import com.android.gpsapplication.model.GnssType
import com.android.gpsapplication.model.SatelliteStatus
import com.android.gpsapplication.utils.SatelliteUtils
import java.text.SimpleDateFormat
import java.util.*

class GpsStatusFragment : Fragment() {

    private lateinit var binding: FragmentGpsStatusBinding
    private lateinit var viewModel: GpsStatusViewModel
    private val statusAdapter by lazy { SatelliteStatusListAdapter() }

    private lateinit var locationManager: LocationManager

    private lateinit var latitudeView: TextView
    private lateinit var longitudeView: TextView
    private lateinit var altitudeView: TextView
    private lateinit var satelliteNum: TextView
    private lateinit var timeView: TextView
    private lateinit var startButton: Button
    private lateinit var stopButton: Button

    private lateinit var locationUpd: Location

    private val gnssStatus = mutableListOf<SatelliteStatus>()
    private var sbasStatus = mutableListOf<SatelliteStatus>()

    private val timeAndDateFormat = SimpleDateFormat("hh:mm:ss", Locale.ROOT)

    private val handler = Handler(Looper.getMainLooper())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_gps_status, container, false)
        viewModel = ViewModelProvider(requireActivity()).get(GpsStatusViewModel::class.java)
        locationManager =
            requireActivity().getSystemService(AppCompatActivity.LOCATION_SERVICE) as LocationManager
        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        with(binding) {
            latitudeView = latitudeValue
            longitudeView = longitudeValue
            altitudeView = altitudeValue
            satelliteNum = numSatsValue
            timeView = timeValue
            startButton = btnStart
            stopButton = btnStop
        }

        setSatelliteListListener()
        setSatelliteListAdapter()

        startButton.setOnClickListener {
            registerAll()
        }

        stopButton.setOnClickListener {
            unregisterAll()
        }

        return binding.root
    }

    private fun setSatelliteListAdapter() {
        binding.gnssSatsStatusList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = statusAdapter
        }
    }

    private fun setSatelliteListListener() {
        viewModel.satelliteLiveData.observe(viewLifecycleOwner, {
            statusAdapter.setSatelliteList(it)
        })
    }

    private val locationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            locationUpd = location

            latitudeView.text = location.latitude.toString()
            longitudeView.text = location.longitude.toString()
            altitudeView.text = location.altitude.toString()
            timeView.text = timeAndDateFormat.format(location.time)

        }

        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {

        }

        override fun onProviderEnabled(provider: String) {
            if (startButton.isEnabled) {
                registerAll()
            }
            Toast.makeText(requireContext(), "Gps enabled", Toast.LENGTH_SHORT).show()
        }

        override fun onProviderDisabled(provider: String) {
            unregisterAll()
            Toast.makeText(requireContext(), "Gps disabled", Toast.LENGTH_SHORT).show()
        }
    }

    private val gnssNavigationMessageListener = object : GnssNavigationMessage.Callback() {
        override fun onGnssNavigationMessageReceived(event: GnssNavigationMessage?) {
            super.onGnssNavigationMessageReceived(event)

        }
    }

    private val gnssStatusListener = object : GnssStatus.Callback() {

        override fun onSatelliteStatusChanged(status: GnssStatus) {
            super.onSatelliteStatusChanged(status)
            satelliteNum.text = status.satelliteCount.toString()
            val length = status.satelliteCount
            var cvCount = 0
            gnssStatus.clear()
            sbasStatus.clear()
            while (cvCount < length) {
                val satStatus = SatelliteStatus(
                    status.getSvid(cvCount),
                    SatelliteUtils.getGnssConstellationType(status.getConstellationType(cvCount)),
                    status.getCn0DbHz(cvCount),
                    status.hasAlmanacData(cvCount),
                    status.hasEphemerisData(cvCount),
                    status.usedInFix(cvCount),
                    status.getElevationDegrees(cvCount),
                    status.getAzimuthDegrees(cvCount)
                )
                if (SatelliteUtils.isGnssCarrierFrequenciesSupported) {
                    if (status.hasCarrierFrequencyHz(cvCount)) {
                        satStatus.hasCarrierFrequency = true
                        satStatus.carrierFrequencyHz =
                            status.getCarrierFrequencyHz(cvCount).toDouble()
                    }
                    if (satStatus.gnssType === GnssType.SBAS) {
                        satStatus.sbasType = SatelliteUtils.getSbasConstellationType(satStatus.svid)
                        sbasStatus.add(satStatus)
                    } else {
                        gnssStatus.add(satStatus)
                    }
                }
                cvCount++
            }
            viewModel.setStatuses(gnssStatus, sbasStatus)
            statusAdapter.notifyDataSetChanged()
        }
    }

    private val nmeaMessageListener = OnNmeaMessageListener { message, timestamp ->
    }

    @SuppressLint("MissingPermission")
    private fun registerLocation() {
        locationManager.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            3000,
            0f,
            locationListener
        )
    }

    private fun unregisterLocation() {
        locationManager.removeUpdates(locationListener)
    }

    @SuppressLint("MissingPermission")
    private fun registerNavigation() {
        locationManager.registerGnssNavigationMessageCallback(
            gnssNavigationMessageListener,
            handler
        )
    }

    private fun unregisterNavigation() {
        locationManager.unregisterGnssNavigationMessageCallback(gnssNavigationMessageListener)
    }

    @SuppressLint("MissingPermission")
    private fun registerGnssStatus() {
        locationManager.registerGnssStatusCallback(gnssStatusListener, handler)
    }

    private fun unregisterGnssStatus() {
        locationManager.unregisterGnssStatusCallback(gnssStatusListener)
    }

    @SuppressLint("MissingPermission")
    private fun registerNmea() {
        locationManager.addNmeaListener(nmeaMessageListener, handler)
    }

    private fun unregisterNmea() {
        locationManager.removeNmeaListener(nmeaMessageListener)
    }

    private fun registerAll() {
        registerLocation();
        registerNavigation();
        registerGnssStatus();
        registerNmea();
    }

    private fun unregisterAll() {
        unregisterLocation();
        unregisterNavigation();
        unregisterGnssStatus();
        unregisterNmea();
    }

}