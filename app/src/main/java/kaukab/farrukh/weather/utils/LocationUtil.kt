package kaukab.farrukh.weather.utils

import android.Manifest
import android.content.Context
import android.location.Geocoder
import android.location.Location
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.FusedLocationProviderClient
import kotlinx.coroutines.tasks.await
import java.util.*

object LocationUtil {

    @Suppress("MissingPermission", "DEPRECATION")
    suspend fun getCityFromLocation(context: Context): String? {
        // ✅ Check location permission
        val permissionGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED

        if (!permissionGranted) return null

        // ✅ Get last known location
        val fusedLocationClient: FusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(context)

        val location: Location? = try {
            fusedLocationClient.lastLocation.await()
        } catch (e: Exception) {
            null
        }

        // ✅ Convert location to city using Geocoder
        return location?.let {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses = geocoder.getFromLocation(it.latitude, it.longitude, 1)
            addresses?.firstOrNull()?.locality
        }
    }
}
