package ua.com.cuteteam.cutetaxiproject.viewmodels

import android.content.Context
import androidx.lifecycle.*
import com.google.android.gms.maps.model.*
import ua.com.cuteteam.cutetaxiproject.api.RouteProvider
import ua.com.cuteteam.cutetaxiproject.api.geocoding.GeocodeRequest
import ua.com.cuteteam.cutetaxiproject.application.AppClass
import ua.com.cuteteam.cutetaxiproject.helpers.PhoneNumberHelper
import ua.com.cuteteam.cutetaxiproject.livedata.LocationLiveData
import ua.com.cuteteam.cutetaxiproject.helpers.network.NetStatus
import ua.com.cuteteam.cutetaxiproject.livedata.MapAction
import ua.com.cuteteam.cutetaxiproject.livedata.SingleLiveEvent
import ua.com.cuteteam.cutetaxiproject.providers.LocationProvider
import ua.com.cuteteam.cutetaxiproject.repositories.Repository
import ua.com.cuteteam.cutetaxiproject.shPref.AppSettingsHelper
import java.util.*

open class BaseViewModel(
    private val repository: Repository,
    private val context: Context = AppClass.appContext()
) : ViewModel() {

    var currentRoute: RouteProvider.RouteSummary? = null

    var cameraPosition: CameraPosition? = null

    var polylineOptions: PolylineOptions? = null

    val locationProvider: LocationProvider
        get() = repository.locationProvider

    var markers = MutableLiveData(mutableMapOf<Int, Marker?>())

    fun replaceMarkers(newMarkers: Map<Int, Marker?>) {
        markers.value?.clear()
        markers.value?.plusAssign(newMarkers)
    }

    suspend fun currentCameraPosition(): CameraPosition {
        return cameraPosition ?: countryCameraPosition()
    }

    fun findMarkerPositionByTag(tag: String): LatLng? {
        return findMarkerByTag(tag)?.position
    }

    fun findMarkerByTag(tag: String): Marker? {
        return markers.value?.findBy { it.value?.tag == tag }?.value
    }

    fun setMarkers(newMarkers: Map<Int, Marker?>) {
        markers.value = newMarkers.toMutableMap()
    }

    fun setMarker(key: Int, value: Marker?) {
        markers.value = markers.value?.plus(key to value)?.toMutableMap()
    }

    fun markerPositions(): List<LatLng> {
        return markers.value?.map {
            it.value?.position
        }?.filterNotNull() ?: emptyList()
    }

    private suspend fun countryCameraPosition(): CameraPosition {
        val phone = AppSettingsHelper(context).phone
        val country = countryNameByRegionCode(
            PhoneNumberHelper().regionCode(phone!!)
        )
        val countryCoordinates = coordinatesByCountryName(country)
        return CameraPosition.builder().target(countryCoordinates).zoom(6f).build()
    }

    private suspend fun coordinatesByCountryName(countryName: String): LatLng {
        return GeocodeRequest.Builder().build()
            .requestCoordinatesByName(countryName).toLatLng()
    }

    private fun countryNameByRegionCode(regionCode: String): String {
        val local = Locale("", regionCode)
        return local.displayCountry
    }

    private var dialogShowed = false

    fun shouldShowGPSRationale(): Boolean {
        if (dialogShowed || repository.locationProvider.isGPSEnabled()) return false

        dialogShowed = true
        return true
    }

    fun addMarkers() {
        mapAction.value = MapAction.AddMarkers()
    }

    fun moveCamera(latLng: LatLng) {
        mapAction.value = MapAction.MoveCamera(latLng)
    }

    fun buildRoute(from: LatLng?, to: LatLng?) {
        if (from == null || to == null) return
        mapAction.value = MapAction.BuildRoute(from, to)
    }

    fun updateCameraForRoute() {
        mapAction.value = MapAction.UpdateCameraForRoute()
    }

    var shouldShowPermissionPermanentlyDeniedDialog = true

    val mapAction = SingleLiveEvent<MapAction>()

    init {
        repository.netHelper.registerNetworkListener()
    }

    val isGPSEnabled get() = repository.locationProvider.isGPSEnabled()

    val shouldStartService: Boolean get() = repository.spHelper.isServiceEnabled

    val netStatus: LiveData<NetStatus> = repository.netHelper.netStatus

    val activeOrderId: LiveData<String?> = SingleLiveEvent<String?>().apply {
        value = repository.spHelper.activeOrderId
    }

    private val _currentLocation =
        LocationLiveData()
    val currentLocation
        get() = Transformations.map(_currentLocation) {
            LatLng(it.latitude, it.longitude)
        }

    private val _role = MutableLiveData(repository.spHelper.role)

    fun changeRole(role: Boolean) {
        _role.value = role
        repository.spHelper.role = role
    }

    val isChecked = _role.value ?: false

    override fun onCleared() {
        super.onCleared()
        repository.netHelper.unregisterNetworkListener()
    }

    companion object {

        @Suppress("UNCHECKED_CAST")
        fun getViewModelFactory(repository: Repository) = object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T =
                when {
                    modelClass.isAssignableFrom(BaseViewModel::class.java) -> {
                        BaseViewModel(
                            repository
                        ) as T
                    }
                    modelClass.isAssignableFrom(PassengerViewModel::class.java) -> {
                        PassengerViewModel(
                            repository
                        ) as T
                    }
                    modelClass.isAssignableFrom(DriverViewModel::class.java) -> {
                        DriverViewModel(
                            repository
                        ) as T
                    }
                    else -> throw IllegalArgumentException("Wrong class name")
                }
        }
    }
}