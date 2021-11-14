package com.android.gpsapplication.utils

import android.location.GnssAntennaInfo
import android.os.Build
import androidx.annotation.RequiresApi
import com.android.gpsapplication.model.GnssType
import com.android.gpsapplication.model.SatelliteStatus

class CarrierFreqUtils {
    companion object {

        const val CF_TOLERANCE_MHZ = 1.0
        const val CF_UNSUPPORTED = "unsupported"
        const val CF_UNKNOWN = "unknown"

        fun getCarrierFrequencyLabel(status: SatelliteStatus): String {
            if (!SatelliteUtils.isGnssCarrierFrequenciesSupported || !status.hasCarrierFrequency) {
                return CF_UNSUPPORTED
            }
            val cfMhz = MathUtils.toMhz(status.carrierFrequencyHz)
            val svid = status.svid
            when (status.gnssType) {
                GnssType.NAVSTAR -> return getNavstarCF(cfMhz)
                GnssType.GLONASS -> return getGlonassCf(cfMhz)
                GnssType.BEIDOU -> return getBeidoucCf(cfMhz)
                GnssType.QZSS -> return getQzssCf(cfMhz)
                GnssType.GALILEO -> return getGalileoCf(cfMhz)
                GnssType.IRNSS -> return getIrnssCf(cfMhz)
                GnssType.SBAS -> return getSbasCf(svid, cfMhz)
                GnssType.UNKNOWN -> {
                }
            }
            // Unknown carrier frequency for given constellation and svid
            return CF_UNKNOWN
        }

        private fun getNavstarCF(carrierFrequencyMhz: Double): String {
            return if (MathUtils.fuzzyEquals(carrierFrequencyMhz, 1575.42, CF_TOLERANCE_MHZ)) {
                "L1"
            } else if (MathUtils.fuzzyEquals(carrierFrequencyMhz, 1227.6, CF_TOLERANCE_MHZ)) {
                "L2"
            } else if (MathUtils.fuzzyEquals(carrierFrequencyMhz, 1381.05, CF_TOLERANCE_MHZ)) {
                "L3"
            } else if (MathUtils.fuzzyEquals(carrierFrequencyMhz, 1379.913, CF_TOLERANCE_MHZ)) {
                "L4"
            } else if (MathUtils.fuzzyEquals(carrierFrequencyMhz, 1176.45, CF_TOLERANCE_MHZ)) {
                "L5"
            } else {
                CF_UNKNOWN
            }
        }

        fun getGlonassCf(carrierFrequencyMhz: Double): String {
            return if (carrierFrequencyMhz in 1598.0..1606.0) {
                // Actual range is 1598.0625 MHz to 1605.375, but allow padding for float comparisons - #103
                "L1"
            } else if (carrierFrequencyMhz in 1242.0..1249.0) {
                // Actual range is 1242.9375 MHz to 1248.625, but allow padding for float comparisons - #103
                "L2"
            } else if (MathUtils.fuzzyEquals(carrierFrequencyMhz, 1207.14, CF_TOLERANCE_MHZ)) {
                // Exact range is unclear - appears to be 1202.025 - 1207.14 - #103
                "L3"
            } else if (MathUtils.fuzzyEquals(carrierFrequencyMhz, 1176.45, CF_TOLERANCE_MHZ)) {
                "L5"
            } else if (MathUtils.fuzzyEquals(carrierFrequencyMhz, 1575.42, CF_TOLERANCE_MHZ)) {
                "L1-C"
            } else {
                CF_UNKNOWN
            }
        }

        fun getBeidoucCf(carrierFrequencyMhz: Double): String {
            return if (MathUtils.fuzzyEquals(carrierFrequencyMhz, 1561.098, CF_TOLERANCE_MHZ)) {
                "B1"
            } else if (MathUtils.fuzzyEquals(carrierFrequencyMhz, 1589.742, CF_TOLERANCE_MHZ)) {
                "B1-2"
            } else if (MathUtils.fuzzyEquals(carrierFrequencyMhz, 1575.42, CF_TOLERANCE_MHZ)) {
                "B1C"
            } else if (MathUtils.fuzzyEquals(carrierFrequencyMhz, 1207.14, CF_TOLERANCE_MHZ)) {
                "B2"
            } else if (MathUtils.fuzzyEquals(carrierFrequencyMhz, 1176.45, CF_TOLERANCE_MHZ)) {
                "B2a"
            } else if (MathUtils.fuzzyEquals(carrierFrequencyMhz, 1268.52, CF_TOLERANCE_MHZ)) {
                "B3"
            } else {
                CF_UNKNOWN
            }
        }

        fun getQzssCf(carrierFrequencyMhz: Double): String {
            return if (MathUtils.fuzzyEquals(carrierFrequencyMhz, 1575.42, CF_TOLERANCE_MHZ)) {
                "L1"
            } else if (MathUtils.fuzzyEquals(carrierFrequencyMhz, 1227.6, CF_TOLERANCE_MHZ)) {
                "L2"
            } else if (MathUtils.fuzzyEquals(carrierFrequencyMhz, 1176.45, CF_TOLERANCE_MHZ)) {
                "L5"
            } else if (MathUtils.fuzzyEquals(carrierFrequencyMhz, 1278.75, CF_TOLERANCE_MHZ)) {
                "L6"
            } else {
                CF_UNKNOWN
            }
        }

        fun getGalileoCf(carrierFrequencyMhz: Double): String {
            return if (MathUtils.fuzzyEquals(carrierFrequencyMhz, 1575.42, CF_TOLERANCE_MHZ)) {
                "E1"
            } else if (MathUtils.fuzzyEquals(carrierFrequencyMhz, 1191.795, CF_TOLERANCE_MHZ)) {
                "E5"
            } else if (MathUtils.fuzzyEquals(carrierFrequencyMhz, 1176.45, CF_TOLERANCE_MHZ)) {
                "E5a"
            } else if (MathUtils.fuzzyEquals(carrierFrequencyMhz, 1207.14, CF_TOLERANCE_MHZ)) {
                "E5b"
            } else if (MathUtils.fuzzyEquals(carrierFrequencyMhz, 1278.75, CF_TOLERANCE_MHZ)) {
                "E6"
            } else {
                CF_UNKNOWN
            }
        }

        fun getIrnssCf(carrierFrequencyMhz: Double): String {
            return if (MathUtils.fuzzyEquals(carrierFrequencyMhz, 1176.45, CF_TOLERANCE_MHZ)) {
                "L5"
            } else if (MathUtils.fuzzyEquals(carrierFrequencyMhz, 2492.028, CF_TOLERANCE_MHZ)) {
                "S"
            } else {
                CF_UNKNOWN
            }
        }

        fun getSbasCf(svid: Int, carrierFrequencyMhz: Double): String {
            if (svid == 120 || svid == 123 || svid == 126 || svid == 136) {
                // EGNOS - https://gssc.esa.int/navipedia/index.php/EGNOS_Space_Segment
                if (MathUtils.fuzzyEquals(carrierFrequencyMhz, 1575.42, CF_TOLERANCE_MHZ)) {
                    return "L1"
                } else if (MathUtils.fuzzyEquals(
                        carrierFrequencyMhz, 1176.45,
                        CF_TOLERANCE_MHZ
                    )
                ) {
                    return "L5"
                }
            } else if (svid == 129 || svid == 137) {
                // MSAS (Japan) - https://gssc.esa.int/navipedia/index.php/MSAS_Space_Segment
                if (MathUtils.fuzzyEquals(carrierFrequencyMhz, 1575.42, CF_TOLERANCE_MHZ)) {
                    return "L1"
                } else if (MathUtils.fuzzyEquals(
                        carrierFrequencyMhz, 1176.45,
                        CF_TOLERANCE_MHZ
                    )
                ) {
                    return "L5"
                }
            } else if (svid == 127 || svid == 128 || svid == 139) {
                // GAGAN (India)
                if (MathUtils.fuzzyEquals(carrierFrequencyMhz, 1575.42, CF_TOLERANCE_MHZ)) {
                    return "L1"
                }
            } else if (svid == 131 || svid == 133 || svid == 135 || svid == 138) {
                // WAAS (US)
                if (MathUtils.fuzzyEquals(carrierFrequencyMhz, 1575.42, CF_TOLERANCE_MHZ)) {
                    return "L1"
                } else if (MathUtils.fuzzyEquals(
                        carrierFrequencyMhz, 1176.45,
                        CF_TOLERANCE_MHZ
                    )
                ) {
                    return "L5"
                }
            }
            return CF_UNKNOWN
        }

        @RequiresApi(api = Build.VERSION_CODES.R)
        fun getCarrierFrequencyLabel(gnssAntennaInfo: GnssAntennaInfo): String {
            val cfMHz = gnssAntennaInfo.carrierFrequencyMHz
            // Try each GNSS until we find a valid label
            var label = getNavstarCF(cfMHz)
            if (label == CF_UNKNOWN) {
                label = getGalileoCf(cfMHz)
            }
            if (label == CF_UNKNOWN) {
                label = getGlonassCf(cfMHz)
            }
            if (label == CF_UNKNOWN) {
                label = getBeidoucCf(cfMHz)
            }
            if (label == CF_UNKNOWN) {
                label = getQzssCf(cfMHz)
            }
            if (label == CF_UNKNOWN) {
                label = getIrnssCf(cfMHz)
            }
            return label
        }

        fun isPrimaryCarrier(label: String): Boolean {
            return label == "L1" || label == "E1" || label == "L1-C" || label == "B1" || label == "B1C"
        }

    }
}