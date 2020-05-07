package ua.com.cuteteam.cutetaxi.services

import android.app.PendingIntent
import android.content.Intent
import android.net.Uri
import androidx.core.app.NotificationCompat
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.launch
import ua.com.cuteteam.cutetaxi.R
import ua.com.cuteteam.cutetaxi.activities.PassengerActivity
import ua.com.cuteteam.cutetaxi.data.database.DbEntries
import ua.com.cuteteam.cutetaxi.data.database.PassengerDao
import ua.com.cuteteam.cutetaxi.data.entities.Order
import ua.com.cuteteam.cutetaxi.data.entities.OrderStatus

class PassengerService : BaseService() {

    private var orderId: String? = null
    private val fbDao by lazy {
        PassengerDao()
    }

    private val orderListener by lazy {
        object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                p0.toException().printStackTrace()
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.getValue(Order::class.java)?.let { processOrder(it) }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        launch {
            orderId = intent?.getStringExtra(ORDER_ID_NAME) ?: appSettingsHelper.activeOrderId
            orderId?.let { id ->
                fbDao.subscribeForChanges(DbEntries.Orders.TABLE, id, orderListener)
            } ?: stopSelf()
        }

        return START_STICKY
    }

    private fun processOrder(order: Order) {

        if (order.orderStatus == OrderStatus.ACCEPTED && !isAlreadyWillShownNotification) {
            notifyThatOrderWasAccepted(order)
            isAlreadyWillShownNotification = true
        }
        order.arrivingTime?.let { verifyIsDriverClose(it) }

    }

    private fun verifyIsDriverClose(arrivingTime: Long) = when (arrivingTime) {
        in 0..1 -> notifyThatDriverIsArrived()
        in 1..3 -> notifyThatDriverIsClose()
        else -> Unit
    }

    private fun notifyThatOrderWasAccepted(order: Order) {
        notificationUtils
            .addAction(getDialerIntent("Driver phone here"))                // TODO: Change when Order will be have a required property
            .setStartActivityIntent(PassengerActivity::class.java)
            .sendNotification(
                title = "An order is accepted!",
                text = "Wait for $order",                                           // TODO: Make a message when Order will be have a required property
                updatable = true
            )
    }

    private fun notifyThatDriverIsClose() {
        notificationUtils
            .addAction(getDialerIntent("Driver phone here"))                // TODO: Change when Order will be have a required property
            .setStartActivityIntent(PassengerActivity::class.java)
            .sendNotification(
                title = "Car status",
                text = "Less than 3 minutes left before the car arrives",
                updatable = true
            )
    }

    private fun notifyThatDriverIsArrived() {
        notificationUtils
            .addAction(getDialerIntent("Driver phone here"))                // TODO: Change when Order will be have a required property
            .sendNotification(
                title = "Car status",
                text = "Car is on the place",
                updatable = true
            )
        stopSelf()
    }

    private fun getDialerIntent(phone: String) =
        PendingIntent.getActivity(
            applicationContext,
            0,
            Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$phone")
            },
            0
        ).run {
            NotificationCompat.Action(
                R.drawable.ct_call_phone,
                applicationContext.getString(R.string.action_name_call),
                this
            )
        }

    companion object {

        private var isAlreadyWillShownNotification = false

    }

}