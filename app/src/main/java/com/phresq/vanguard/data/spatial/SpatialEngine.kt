package com.phresq.vanguard.data.spatial

import kotlin.math.*

data class BarangayLocation(
    val id: String,
    val name: String,
    val municipality: String,
    val province: String,
    val latitude: Double,
    val longitude: Double
)

class SpatialEngine {

    private val barangayIndex = mutableListOf(
        BarangayLocation("097204001", "Aguada", "Naga", "Zamboanga Sibugay", 7.8234, 122.7211),
        BarangayLocation("097204002", "Banguingui", "Naga", "Zamboanga Sibugay", 7.8412, 122.7350),
        BarangayLocation("097204003", "Baliwasan", "Naga", "Zamboanga Sibugay", 7.8105, 122.7098),
        BarangayLocation("097204004", "Guintoloan", "Naga", "Zamboanga Sibugay", 7.8550, 122.7482),
        BarangayLocation("097204005", "Cabongbongan", "Naga", "Zamboanga Sibugay", 7.8320, 122.7150),
        BarangayLocation("097204006", "Gasi", "Naga", "Zamboanga Sibugay", 7.8190, 122.7290),
        BarangayLocation("097204007", "Lower Sulitan", "Naga", "Zamboanga Sibugay", 7.8400, 122.7010),
        BarangayLocation("097204008", "Mamagon", "Naga", "Zamboanga Sibugay", 7.8610, 122.7520),
        BarangayLocation("097204009", "Poblacion", "Naga", "Zamboanga Sibugay", 7.8250, 122.7200),
        BarangayLocation("097204010", "San Isidro", "Naga", "Zamboanga Sibugay", 7.8050, 122.6950)
    )

    fun calculateHaversineDistanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val rEarth = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return rEarth * c
    }

    fun findNearestBarangay(currentLat: Double, currentLon: Double): BarangayLocation? {
        if (barangayIndex.isEmpty()) return null
        return barangayIndex.minByOrNull {
            calculateHaversineDistanceKm(currentLat, currentLon, it.latitude, it.longitude)
        }
    }

    fun searchBarangay(query: String): List<BarangayLocation> {
        return barangayIndex.filter { 
            it.name.contains(query, ignoreCase = true) || 
            it.municipality.contains(query, ignoreCase = true) 
        }
    }
}
