package com.android.gpsapplication.utils


import android.location.GnssStatus
import android.os.Build
import androidx.annotation.RequiresApi
import com.android.gpsapplication.model.GnssType
import com.android.gpsapplication.model.SatelliteStatus
import com.android.gpsapplication.model.SbasType

class SatelliteUtils {
    companion object {

        val isGnssCarrierFrequenciesSupported: Boolean
            get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O

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