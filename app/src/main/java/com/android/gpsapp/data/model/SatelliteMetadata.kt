package com.android.gpsapp.data.model

data class SatelliteMetadata(
    val numSignalsInView: Int,
    val numSignalsUsed: Int,
    val numSignalsTotal: Int,
    val numSatsInView: Int,
    val numSatsUsed: Int,
    val numSatsTotal: Int)