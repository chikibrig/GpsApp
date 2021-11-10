package com.android.gpsapp.data.model

data class Satellite(
    val id: String,
    // Individual signals are stored in a map with the carrier frequency label as the key so we can
    // see if there are duplicate frequencies.
    val status: Map<String, SatelliteStatus>)