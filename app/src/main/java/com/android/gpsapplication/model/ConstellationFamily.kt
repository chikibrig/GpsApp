package com.android.gpsapplication.model

data class ConstellationFamily(
    val satellites: Map<String, Satellite>,
    val satelliteMetadata: SatelliteMetadata
)