package com.android.gpsapplication.model

data class Satellite (
    val id: String,
    val status: Map<String, SatelliteStatus>)