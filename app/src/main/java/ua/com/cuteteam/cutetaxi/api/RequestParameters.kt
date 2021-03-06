package ua.com.cuteteam.cutetaxi.api


/**
 * Constants are used by Google API
 */
object RequestParameters {
    const val RADIUS = "radius"
    const val LOCATION = "location"
    const val ORIGIN_PLACE = "origin"
    const val DESTINATION_PLACE = "destination"
    const val WAY_POINTS = "waypoints"
    const val IS_ALTERNATIVE_WAYS = "alternatives"
    const val AVOID = "avoid"
    const val LANG = "language"
    const val UNITS = "units"
    const val REGION = "region"

    object Units {
        const val METRIC = "metric"
        const val IMPERIAL = "imperial"
    }

    object Avoid {
        const val TOLLS = "tolls"
        const val HIGHWAYS = "highways"
        const val FERRIES = "ferries"
    }

}