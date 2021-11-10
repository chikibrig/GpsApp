package com.android.gpsapp.data.model

data class ConstellationFamily(
    val satellites: Map<String, Satellite>,
    val satelliteMetadata: SatelliteMetadata
)