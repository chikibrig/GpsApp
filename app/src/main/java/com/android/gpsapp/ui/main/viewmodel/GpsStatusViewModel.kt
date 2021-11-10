package com.android.gpsapp.ui.main.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.android.gpsapp.data.model.*
import com.android.gpsapp.data.model.SatelliteStatus.Companion.NO_DATA
import com.android.gpsapp.utils.CarrierFreqUtils
import com.android.gpsapp.utils.CarrierFreqUtils.CF_UNKNOWN
import com.android.gpsapp.utils.CarrierFreqUtils.CF_UNSUPPORTED
import com.android.gpsapp.utils.SatelliteUtils
import kotlin.collections.HashSet

class GpsStatusViewModel(application: Application) : AndroidViewModel(application) {

    private var gnssSatellitesLd = MutableLiveData<Map<String, Satellite>>()
    private var sbasSatellitesLd = MutableLiveData<Map<String, Satellite>>()

    private var isDualFrequencyPerSatInView = false

    private var isDualFrequencyPerSatInUse = false

    private var isNonPrimaryCarrierFreqInView = false

    private var isNonPrimaryCarrierFreqInUse = false

    private var gotFirstFix = false

    private val satelliteMetadata = MutableLiveData<SatelliteMetadata>()

    private var duplicateCarrierStatuses: MutableMap<String, SatelliteStatus> = HashMap()

    private var unknownCarrierStatuses: MutableMap<String, SatelliteStatus> = HashMap()

    private var supportedGnss: Set<GnssType> = HashSet()

    private var supportedSbas: Set<SbasType> = HashSet()

    private var supportedGnssCfs: HashSet<String> = HashSet()

    private var supportedSbasCfs: HashSet<String> = HashSet()

    fun setStatuses(gnssStatuses: List<SatelliteStatus>, sbasStatuses: List<SatelliteStatus>) {

        val gnssSatellites: ConstellationFamily = getSatellitesFromStatuses(gnssStatuses)
        val sbasSatellites: ConstellationFamily = getSatellitesFromStatuses(sbasStatuses)

        gnssSatellitesLd.setValue(gnssSatellites.satellites)
        sbasSatellitesLd.setValue(sbasSatellites.satellites)

        val numSignalsUsed = gnssSatellites.satelliteMetadata.numSignalsUsed +
                sbasSatellites.satelliteMetadata.numSignalsUsed

        val numSignalsInView = gnssSatellites.satelliteMetadata.numSignalsInView +
                sbasSatellites.satelliteMetadata.numSignalsInView

        val numSignalsTotal = gnssSatellites.satelliteMetadata.numSignalsInView +
                sbasSatellites.satelliteMetadata.numSignalsTotal

        val numSatsUsed = gnssSatellites.satelliteMetadata.numSignalsInView +
                sbasSatellites.satelliteMetadata.numSatsUsed

        val numSatsInView = gnssSatellites.satelliteMetadata.numSignalsInView +
                sbasSatellites.satelliteMetadata.numSatsInView

        val numSatsTotal = gnssSatellites.satelliteMetadata.numSignalsInView +
                sbasSatellites.satelliteMetadata.numSatsTotal

        satelliteMetadata.postValue(
            SatelliteMetadata(
                numSignalsInView,
                numSignalsUsed,
                numSignalsTotal,
                numSatsInView,
                numSatsUsed,
                numSatsTotal
            )
        )
    }

    private fun getSatellitesFromStatuses(allStatuses: List<SatelliteStatus>?): ConstellationFamily {
        val satellites: MutableMap<String, Satellite> = HashMap()
        var numSignalsUsed = 0
        var numSignalsInView = 0
        var numSatsUsed = 0
        var numSatsInView = 0
        if (allStatuses == null) {
            return ConstellationFamily(satellites, SatelliteMetadata(0, 0, 0, 0, 0, 0))
        }
        for (status in allStatuses) {
            if (status.usedInFix) {
                numSignalsUsed++
            }
            if (status.cn0DbHz.toDouble() !== NO_DATA) {
                numSignalsInView++
            }

            // Save the supported GNSS or SBAS type
            val key: String = SatelliteUtils.createGnssSatelliteKey(status)
            if (status.gnssType !== GnssType.UNKNOWN) {
                if (status.gnssType !== GnssType.SBAS) {
                    supportedGnss.plus(status.gnssType)
                } else {
                    if (status.sbasType !== SbasType.UNKNOWN) {
                        supportedSbas.plus(status.sbasType)
                    }
                }
            }

            // Get carrier label
            val carrierLabel: String = CarrierFreqUtils.getCarrierFrequencyLabel(status)
            if (carrierLabel == CF_UNKNOWN) {
                unknownCarrierStatuses.put(SatelliteUtils.createGnssStatusKey(status), status)
            }
            if (carrierLabel != CF_UNKNOWN && carrierLabel != CF_UNSUPPORTED) {
                // Save the supported GNSS or SBAS CF
                if (status.gnssType !== GnssType.UNKNOWN) {
                    if (status.gnssType !== GnssType.SBAS) {
                        supportedGnssCfs.add(carrierLabel)
                    } else {
                        if (status.sbasType !== SbasType.UNKNOWN) {
                            supportedSbasCfs.add(carrierLabel)
                        }
                    }
                }
                // Check if this is a non-primary carrier frequency
                if (!CarrierFreqUtils.isPrimaryCarrier(carrierLabel)) {
                    isNonPrimaryCarrierFreqInView = true
                    if (status.usedInFix) {
                        isNonPrimaryCarrierFreqInUse = true
                    }
                }
            }

            var satStatuses: MutableMap<String, SatelliteStatus>
            if (!satellites.containsKey(key)) {
                // Create new satellite and add signal
                satStatuses = HashMap()
                satStatuses[carrierLabel] = status
                val sat = Satellite(key, satStatuses)
                satellites[key] = sat
                if (status.usedInFix) {
                    numSatsUsed++
                }
                if (status.cn0DbHz.toDouble() !== NO_DATA) {
                    numSatsInView++
                }
            } else {
                // Add signal to existing satellite
                val sat = satellites[key]
                satStatuses = sat?.status as MutableMap<String, SatelliteStatus>
                if (!satStatuses.containsKey(carrierLabel)) {
                    // We found another frequency for this satellite
                    satStatuses[carrierLabel] = status
                    var frequenciesInUse = 0
                    var frequenciesInView = 0
                    for (satelliteStatus in satStatuses.values) {
                        if (satelliteStatus.usedInFix) {
                            frequenciesInUse++
                        }
                        if (satelliteStatus.cn0DbHz.toDouble() !== NO_DATA) {
                            frequenciesInView++
                        }
                    }
                    if (frequenciesInUse > 1) {
                        isDualFrequencyPerSatInUse = true
                    }
                    if (frequenciesInUse == 1 && status.usedInFix) {
                        // The new frequency we just added was the first in use for this satellite
                        numSatsUsed++
                    }
                    if (frequenciesInView > 1) {
                        isDualFrequencyPerSatInView = true
                    }
                    if (frequenciesInView == 1 && status.cn0DbHz.toDouble() !== NO_DATA) {
                        // The new frequency we just added was the first in view for this satellite
                        numSatsInView++
                    }
                } else {
                    // This shouldn't happen - we found a satellite signal with the same constellation,
                    // sat ID, and carrier frequency
                    // (including multiple "unknown" or "unsupported" frequencies) as an existing one
                    duplicateCarrierStatuses.put(SatelliteUtils.createGnssStatusKey(status), status)
                }
            }
        }
        return ConstellationFamily(
            satellites,
            SatelliteMetadata(
                numSignalsInView,
                numSignalsUsed,
                allStatuses.size,
                numSatsInView,
                numSatsUsed,
                satellites.size
            )
        )
    }

    private fun resetAll() {
        gnssSatellitesLd.setValue(null)
        sbasSatellitesLd.setValue(null)
        satelliteMetadata.setValue(null)
        duplicateCarrierStatuses = HashMap<String, SatelliteStatus>()
        unknownCarrierStatuses = HashMap<String, SatelliteStatus>()
        supportedGnss = HashSet()
        supportedSbas = HashSet()
        supportedGnssCfs = HashSet()
        supportedSbasCfs = HashSet()
        isDualFrequencyPerSatInView = false
        isDualFrequencyPerSatInUse = false
        isNonPrimaryCarrierFreqInView = false
        isNonPrimaryCarrierFreqInUse = false
        gotFirstFix = false
    }

    override fun onCleared() {
        super.onCleared()
        resetAll()
    }

}