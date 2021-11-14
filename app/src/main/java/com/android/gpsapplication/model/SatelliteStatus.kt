package com.android.gpsapplication.model

data class SatelliteStatus (
    val svid: Int,
    val gnssType: GnssType,
    var cn0DbHz: Float,
    val hasAlmanac: Boolean,
    val hasEphemeris: Boolean,
    val usedInFix: Boolean,
    var elevationDegrees: Float,
    var azimuthDegrees: Float) {
    var sbasType: SbasType = SbasType.UNKNOWN
    var hasCarrierFrequency: Boolean = false
    var carrierFrequencyHz: Double = NO_DATA

    companion object {
        const val NO_DATA = 0.0
    }
}