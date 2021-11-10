package com.android.gpsapp.utils

import kotlin.math.withSign

object MathUtils {

    fun toMhz(hertz: Double): Double {
        return hertz / 1000000.00
    }
    /**
     * Returns `true` if `a` and `b` are within `tolerance` of each other.
     */
    fun fuzzyEquals(a: Double, b: Double, tolerance: Double): Boolean {
        checkNonNegative("tolerance", tolerance)
        return ((a - b).withSign(1.0) <= tolerance
                || a == b || a.isNaN() && b.isNaN())
    }

    private fun checkNonNegative(role: String, x: Double) = require(x >= 0) {
        "$role ($x) must be >= 0"
    }
}