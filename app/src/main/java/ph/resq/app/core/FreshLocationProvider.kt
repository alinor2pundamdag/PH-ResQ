package ph.resq.app.core

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.core.content.ContextCompat

/**
 * Bounded, one-shot location request. It does not silently run in the background.
 * Call from a visible UI after the user grants location permission; always stop on completion.
 */
class FreshLocationProvider(context: Context) {
    private val appContext = context.applicationContext
    private val manager = appContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    private val handler = Handler(Looper.getMainLooper())
    private var listener: LocationListener? = null
    private var timeout: Runnable? = null

    fun request(timeoutMs: Long = 15_000L, onResult: (Location?, String) -> Unit) {
        cancel()
        val fine = ContextCompat.checkSelfPermission(appContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(appContext, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (!fine && !coarse) {
            onResult(null, "Location permission is required.")
            return
        }
        val providers = try {
            manager.getProviders(true).filter {
                it == LocationManager.GPS_PROVIDER || it == LocationManager.NETWORK_PROVIDER
            }
        } catch (_: Exception) { emptyList() }
        if (providers.isEmpty()) {
            onResult(null, "Location providers are disabled.")
            return
        }
        var best: Location? = null
        var finished = false
        fun complete(location: Location?, message: String) {
            if (finished) return
            finished = true
            cancel()
            onResult(location, message)
        }
        val active = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                if (best == null || (location.hasAccuracy() && (!best!!.hasAccuracy() || location.accuracy < best!!.accuracy)) ||
                    (!location.hasAccuracy() && location.time > best!!.time)) best = Location(location)
                // Prefer a reasonably accurate fresh fix; otherwise wait for the bounded timeout.
                if (best != null && best!!.hasAccuracy() && best!!.accuracy <= 100f &&
                    System.currentTimeMillis() - best!!.time < 20_000L) {
                    complete(best, "Fresh location acquired.")
                }
            }
            @Deprecated("Deprecated by Android; retained for API 26 compatibility")
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) = Unit
            override fun onProviderEnabled(provider: String) = Unit
            override fun onProviderDisabled(provider: String) = Unit
        }
        listener = active
        try {
            providers.forEach { provider ->
                try { manager.requestLocationUpdates(provider, 0L, 0f, active, Looper.getMainLooper()) }
                catch (_: SecurityException) { }
            }
            timeout = Runnable {
                val fix = best
                if (fix != null && System.currentTimeMillis() - fix.time <= 120_000L) {
                    complete(fix, "Location request timed out; using the best recent fix.")
                } else {
                    complete(null, "No sufficiently recent location fix was obtained.")
                }
            }
            handler.postDelayed(timeout!!, timeoutMs.coerceIn(1_000L, 30_000L))
        } catch (_: SecurityException) {
            complete(null, "Location permission was revoked.")
        } catch (error: Exception) {
            complete(null, "Location unavailable: " + (error.message ?: "provider error"))
        }
    }

    fun cancel() {
        timeout?.let { handler.removeCallbacks(it) }
        timeout = null
        listener?.let { runCatching { manager.removeUpdates(it) } }
        listener = null
    }
}
