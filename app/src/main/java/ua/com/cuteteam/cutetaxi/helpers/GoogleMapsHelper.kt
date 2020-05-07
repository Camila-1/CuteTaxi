package ua.com.cuteteam.cutetaxi.helpers

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.animation.ValueAnimator
import android.graphics.Color
import android.view.animation.LinearInterpolator
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ua.com.cuteteam.cutetaxi.R
import ua.com.cuteteam.cutetaxi.api.RouteProvider
import ua.com.cuteteam.cutetaxi.application.AppClass
import ua.com.cuteteam.cutetaxi.data.MarkerData


class GoogleMapsHelper(private val googleMap: GoogleMap) {

    init {
        googleMap.isMyLocationEnabled = true
        googleMap.isBuildingsEnabled = false
    }

    private var currentRoute: RouteProvider.RouteSummary? = null

    fun updateMarkers(markers: MutableCollection<MarkerData>?) {
        googleMap.clear()
        markers?.forEach { createMarker(it) }
    }

    fun onCameraMove(callback: ((CameraPosition) -> Unit)) {
        googleMap.setOnCameraMoveListener { callback.invoke(googleMap.cameraPosition) }
    }

    fun moveCameraToLocation(latLng: LatLng) {
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17f), 1000, null)
    }

    fun createMarker(markerData: MarkerData): Marker? {
        return googleMap.addMarker(
            MarkerOptions()
                .position(markerData.position)
                .icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons(markerData.icon, 150, 150)))

        )
    }

    fun removeMarker(marker: Marker?) {
        marker?.remove()
    }

    fun addOnMapClickListener(callback: ((LatLng) -> Unit)) {
        googleMap.setOnMapClickListener { latLng ->
            callback.invoke(latLng)
        }
    }

    suspend fun routeSummary(
        from: LatLng,
        to: LatLng,
        wayPoints: List<LatLng>
    ): RouteProvider.RouteSummary {
        val routeProvider = buildRouteProvider(from, to, wayPoints)
        return withContext(Dispatchers.Main) { return@withContext routeProvider.routes()[0] }
    }

    fun buildRoute(routeSummary: RouteProvider.RouteSummary): PolylineOptions? {
        val customCap = CustomCap(
            BitmapDescriptorFactory.fromResource(R.drawable.circular_shape_silhouette),
            300f
        )
        val polylineOptions = PolylineOptions()
            .clickable(true)
            .add(*routeSummary.polyline)
            .color(Color.parseColor("#0288d1"))
            .width(15f)
            .startCap(customCap)
            .endCap(customCap)

        addPolyline(polylineOptions)
        return polylineOptions
    }

    fun addPolyline(polylineOptions: PolylineOptions) {
        googleMap.addPolyline(polylineOptions)
    }

    fun removeOnMapClickListener() {
        googleMap.setOnMapClickListener(null)
    }

    fun updateCameraForCurrentRoute(routeSummary: RouteProvider.RouteSummary?) {
        routeSummary?.let {
            googleMap.animateCamera(
                CameraUpdateFactory.newLatLngBounds(
                    buildBoundaryForRoute(it),
                    50
                )
            )
        }
    }

    private fun buildBoundaryForRoute(routeSummary: RouteProvider.RouteSummary): LatLngBounds {
        return LatLngBounds.Builder()
            .include(routeSummary.polyline.minBy { it.longitude })
            .include(routeSummary.polyline.minBy { it.latitude })
            .include(routeSummary.polyline.maxBy { it.latitude })
            .include(routeSummary.polyline.maxBy { it.longitude })
            .build()
    }

    private fun buildRouteProvider(from: LatLng, to: LatLng, wayPoints: List<LatLng>)
            : RouteProvider {
        val routeProviderBuilder = RouteProvider.Builder()
        wayPoints.forEach {
            routeProviderBuilder.addWayPoint(it)
        }
        return routeProviderBuilder
            .addOrigin(from)
            .addDestination(to)
            .build()
    }

    private fun resizeMapIcons(iconId: Int, width: Int, height: Int): Bitmap? {
        val b = BitmapFactory.decodeResource(AppClass.appContext().resources, iconId)
        return Bitmap.createScaledBitmap(b, width, height, false)

    }

    fun animateCarOnMap(bearing: Float, markerData: MarkerData, from: LatLng, to: LatLng) {
        val carMarker = googleMap.addMarker(
            MarkerOptions()
                .position(markerData.position)
                .icon(BitmapDescriptorFactory.fromResource(markerData.icon))
        )
        val valueAnimator = ValueAnimator.ofFloat(0f, 1f)
        valueAnimator.duration = 1000
        valueAnimator.interpolator = LinearInterpolator()
        valueAnimator.addUpdateListener ( object : ValueAnimator.AnimatorUpdateListener {
            override fun onAnimationUpdate(animation: ValueAnimator?) {
                val animatedFraction = valueAnimator.animatedFraction
                val lat = (animatedFraction * to.latitude) + (1 - animatedFraction) * from.latitude
                val lng = (animatedFraction * to.longitude) + (1 - animatedFraction) * from.longitude

                val newPosition = LatLng(lat, lng)

                carMarker.position = newPosition
                carMarker.setAnchor(0.5f, 0.5f)
                carMarker.rotation = bearing
            }
        })

        valueAnimator.start()
    }
}
