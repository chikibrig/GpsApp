package com.android.gpsapp.data.model

enum class GnssType {
    NAVSTAR, GLONASS, GALILEO, QZSS, BEIDOU, IRNSS, SBAS, UNKNOWN;

     fun fromString(gnssType: String): GnssType {
        return when (gnssType) {
            "NAVSTAR" -> NAVSTAR
            "GLONASS" -> GLONASS
            "GALILEO" -> GALILEO
            "QZSS" -> QZSS
            "BEIDOU" -> BEIDOU
            "IRNSS" -> IRNSS
            "SBAS" -> SBAS
            "UNKNOWN" -> UNKNOWN
            else -> UNKNOWN
        }
    }
}