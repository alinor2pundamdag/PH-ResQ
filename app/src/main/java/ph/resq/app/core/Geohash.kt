package ph.resq.app.core

object Geohash {
    private const val ALPHABET = "0123456789bcdefghjkmnpqrstuvwxyz"

    fun encode(latitude: Double, longitude: Double, precision: Int = 8): String {
        require(latitude in -90.0..90.0) { "Latitude out of range" }
        require(longitude in -180.0..180.0) { "Longitude out of range" }
        require(precision in 1..12) { "Precision must be between 1 and 12" }
        var latMin = -90.0
        var latMax = 90.0
        var lonMin = -180.0
        var lonMax = 180.0
        var even = true
        var bit = 0
        var value = 0
        val output = StringBuilder(precision)
        while (output.length < precision) {
            if (even) {
                val mid = (lonMin + lonMax) / 2.0
                if (longitude >= mid) { value = (value shl 1) or 1; lonMin = mid } else { value = value shl 1; lonMax = mid }
            } else {
                val mid = (latMin + latMax) / 2.0
                if (latitude >= mid) { value = (value shl 1) or 1; latMin = mid } else { value = value shl 1; latMax = mid }
            }
            even = !even
            bit++
            if (bit == 5) {
                output.append(ALPHABET[value])
                bit = 0
                value = 0
            }
        }
        return output.toString()
    }
}
