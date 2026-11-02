package ph.resq.app.core

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object LastKnownLocation {
    fun read(context: Context): Location? {
        val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (!fine && !coarse) return null
        val manager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER, LocationManager.PASSIVE_PROVIDER)
            .mapNotNull { provider ->
                try { manager.getLastKnownLocation(provider) } catch (_: SecurityException) { null } catch (_: IllegalArgumentException) { null }
            }
            .maxByOrNull { it.time }
    }

    fun format(location: Location): String {
        val date = SimpleDateFormat("yyyy-MM-dd HH:mm:ss 'UTC'", Locale.US).apply { timeZone = TimeZone.getTimeZone("UTC") }.format(Date(location.time))
        val accuracy = if (location.hasAccuracy()) {
            "%.0f m".format(Locale.US, location.accuracy)
        } else {
            "unknown"
        }
        return "GPS: %.6f, %.6f; accuracy %s; fix time %s".format(
            Locale.US, location.latitude, location.longitude, accuracy, date
        )
    }
}
