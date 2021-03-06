package ua.com.cuteteam.cutetaxi.api.roads

import com.google.android.gms.maps.model.LatLng

data class Roads(
    val snappedPoints: List<RoadNode>
)

data class RoadNode(
    val location: LatLng,
    val originalIndex: Int?
)