package ua.com.cuteteam.cutetaxi.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import ua.com.cuteteam.cutetaxi.livedata.LocationLiveData
import ua.com.cuteteam.cutetaxi.providers.LocationProvider
import ua.com.cuteteam.cutetaxi.helpers.NotificationUtils
import ua.com.cuteteam.cutetaxi.extentions.createNotificationChannel
import ua.com.cuteteam.cutetaxi.shPref.AppSettingsHelper
import kotlin.coroutines.CoroutineContext

const val ACCEPTED_ORDER_ID = "AcceptedOrderId"
const val ORDER_ID_NAME = "Service_active_orderId"

abstract class BaseService : Service(), CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + handler


    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    protected val notificationUtils by lazy {
        NotificationUtils(
            applicationContext
        )
    }
    protected val appSettingsHelper by lazy {
        AppSettingsHelper(applicationContext)
    }

    protected val locationLiveData by lazy {
        LocationLiveData()
    }

    protected val locationProvider by lazy {
        LocationProvider()
    }

    private val handler
        get() = CoroutineExceptionHandler { _, throwable ->
            throwable.printStackTrace()
        }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

}