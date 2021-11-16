package com.android.gpsapplication.ui.screens.statusscreen

import android.annotation.SuppressLint
import android.location.*
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.gpsapplication.R
import com.android.gpsapplication.databinding.FragmentGpsStatusBinding
import com.android.gpsapplication.model.GnssType
import com.android.gpsapplication.model.SatelliteStatus
import com.android.gpsapplication.utils.SatelliteUtils
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import java.text.SimpleDateFormat
import java.util.*

class GpsStatusFragment : Fragment() {

    private lateinit var binding: FragmentGpsStatusBinding
    private lateinit var viewModel: GpsStatusViewModel
    private val statusAdapter by lazy { SatelliteStatusListAdapter() }

    private lateinit var locationManager: LocationManager

    private lateinit var latitudeView: TextView
    private lateinit var latitudeAccView: TextView
    private lateinit var longitudeView: TextView
    private lateinit var longitudeAccView: TextView
    private lateinit var altitudeView: TextView
    private lateinit var altitudeAccView: TextView
    private lateinit var satelliteNum: TextView
    private lateinit var timeView: TextView
    private lateinit var chart: BarChart

    private lateinit var switch: SwitchCompat

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
            chart = signalChart
            latitudeView = latitudeValue
            latitudeAccView = latitudeAccuracy
            longitudeView = longitudeValue
            longitudeAccView = longitudeAccuracy
            altitudeView = altitudeValue
            altitudeAccView = altitudeAccuracy
            satelliteNum = numSatsValue
            timeView = timeValue
            switch = searchingLocationStatus
        }

        setSatelliteListListener()
        setSatelliteListAdapter()

        switch.setOnClickListener {
            if (switch.isChecked) {
                registerAll()
            } else {
                unregisterAll()
            }
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
            setBarChartValues(it)
        })
    }

    private fun setBarChartValues(list: List<SatelliteStatus>) {
        val barEntries = mutableListOf<BarEntry>()
        var i = 0
        while (i < list.size) {
            barEntries.add(i, BarEntry(list[i].svid.toFloat(), list[i].cn0DbHz))
            i++
        }
        chart.data = BarData(BarDataSet(barEntries, "Signals"))
    }

    private val locationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            locationUpd = location

            latitudeView.text = location.latitude.toString()
            longitudeView.text = location.longitude.toString()
            altitudeView.text = location.altitude.toString()
            timeView.text = timeAndDateFormat.format(location.time)
            altitudeAccView.text = location.verticalAccuracyMeters.toString()
            latitudeAccView.text = location.accuracy.toString()
            longitudeAccView.text = location.accuracy.toString()

        }

        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}

        override fun onProviderEnabled(provider: String) {
            Toast.makeText(requireContext(), "Gps enabled", Toast.LENGTH_SHORT).show()
            if (switch.isChecked) {
                registerAll()
            }
        }

        override fun onProviderDisabled(provider: String) {
            Toast.makeText(requireContext(), "Gps disabled", Toast.LENGTH_SHORT).show()
            if (switch.isChecked) {
                unregisterAll()
            }
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
                        satStatus.sbasType =
                            SatelliteUtils.getSbasConstellationType(satStatus.svid)
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
            1000,
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