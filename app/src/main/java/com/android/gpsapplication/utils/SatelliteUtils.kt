package com.android.gpsapplication.utils


import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import android.location.GnssMeasurement
import android.location.GnssStatus
import android.location.Location
import android.location.LocationManager
import android.os.Build
import androidx.annotation.RequiresApi
import com.android.gpsapplication.model.GnssType
import com.android.gpsapplication.model.SatelliteStatus
import com.android.gpsapplication.model.SbasType

class SatelliteUtils {
    companion object {
        @RequiresApi(api = Build.VERSION_CODES.N)
        fun getGnssConstellationType(gnssConstellationType: Int): GnssType {
            return when (gnssConstellationType) {
                GnssStatus.CONSTELLATION_GPS -> GnssType.NAVSTAR
                GnssStatus.CONSTELLATION_BEIDOU -> GnssType.BEIDOU
                GnssStatus.CONSTELLATION_QZSS -> GnssType.QZSS
                GnssStatus.CONSTELLATION_GALILEO -> GnssType.GALILEO
                GnssStatus.CONSTELLATION_IRNSS -> GnssType.IRNSS
                GnssStatus.CONSTELLATION_SBAS -> GnssType.SBAS
                GnssStatus.CONSTELLATION_UNKNOWN -> GnssType.UNKNOWN
                else -> GnssType.UNKNOWN
            }
        }

        @RequiresApi(api = Build.VERSION_CODES.N)
        fun getSbasConstellationType(svid: Int): SbasType {
            if (svid == 120 || svid == 123 || svid == 126 || svid == 136) {
                return SbasType.EGNOS
            } else if (svid == 125 || svid == 140 || svid == 141) {
                return SbasType.SDCM
            } else if (svid == 130 || svid == 143 || svid == 144) {
                // Also referred to as BDSBAS
                return SbasType.SNAS
            } else if (svid == 131 || svid == 133 || svid == 135 || svid == 138) {
                return SbasType.WAAS
            } else if (svid == 127 || svid == 128 || svid == 139) {
                return SbasType.GAGAN
            } else if (svid == 129 || svid == 137) {
                return SbasType.MSAS
            }
            return SbasType.UNKNOWN
        }

        fun isRotationVectorSensorSupported(context: Context): Boolean {
            val sensorManager = context
                .getSystemService(Context.SENSOR_SERVICE) as SensorManager
            return sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR) != null
        }

        val isGnssStatusListenerSupported: Boolean
            get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N


        val isGnssCarrierFrequenciesSupported: Boolean
            get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O

        fun isVerticalAccuracySupported(location: Location): Boolean {
            return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && location.hasVerticalAccuracy()
        }

        val isSpeedAndBearingAccuracySupported: Boolean
            get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O

        fun isAutomaticGainControlSupported(gnssMeasurement: GnssMeasurement): Boolean {
            return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && gnssMeasurement.hasAutomaticGainControlLevelDb()
        }

        fun isAccumulatedDeltaRangeStateValid(accumulatedDeltaRangeState: Int): Boolean {
            return GnssMeasurement.ADR_STATE_VALID and accumulatedDeltaRangeState == GnssMeasurement.ADR_STATE_VALID
        }

        fun isGnssAntennaInfoSupported(manager: LocationManager?): Boolean {
            if (manager == null) {
                return false
            }
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                manager.gnssCapabilities.hasAntennaInfo()
            } else {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && manager.gnssCapabilities.hasGnssAntennaInfo()
            }
        }

        val isForceFullGnssMeasurementsSupported: Boolean
            get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

        @RequiresApi(api = Build.VERSION_CODES.S)
        fun isGnssMeasurementsSupported(manager: LocationManager?): Boolean {
            return manager != null && manager.gnssCapabilities.hasMeasurements()
        }

        @RequiresApi(api = Build.VERSION_CODES.S)
        fun isNavigationMessagesSupported(manager: LocationManager?): Boolean {
            return manager != null && manager.gnssCapabilities.hasNavigationMessages()
        }

        fun createGnssSatelliteKey(status: SatelliteStatus): String {
            return if (status.gnssType === GnssType.SBAS) {
                "${status.svid}; ${status.gnssType}; ${status.sbasType}"
            } else {
                // GNSS
                "${status.svid}; ${status.gnssType}"
            }
        }

        fun createGnssStatusKey(status: SatelliteStatus): String {
            val carrierLabel = CarrierFreqUtils.getCarrierFrequencyLabel(status)
            return if (status.gnssType === GnssType.SBAS) {
                "${status.svid}; ${status.gnssType}; ${status.sbasType}; $carrierLabel"
            } else {
                // GNSS
                "${status.svid}; ${status.gnssType}; $carrierLabel"
            }
        }
    }
}