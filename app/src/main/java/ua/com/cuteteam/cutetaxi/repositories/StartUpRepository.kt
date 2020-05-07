package ua.com.cuteteam.cutetaxi.repositories

import android.content.Context
import com.google.firebase.auth.FirebaseUser
import ua.com.cuteteam.cutetaxi.application.AppClass
import ua.com.cuteteam.cutetaxi.data.User
import ua.com.cuteteam.cutetaxi.data.database.BaseDao
import ua.com.cuteteam.cutetaxi.data.database.DriverDao
import ua.com.cuteteam.cutetaxi.data.database.PassengerDao
import ua.com.cuteteam.cutetaxi.data.entities.Driver
import ua.com.cuteteam.cutetaxi.data.entities.Passenger
import ua.com.cuteteam.cutetaxi.shPref.AppSettingsHelper

class StartUpRepository(context: Context = AppClass.appContext()) {
    private val passengerDAO = PassengerDao()
    private val driverDAO = DriverDao()

    private val appSettingsHelper = AppSettingsHelper(context)

    val isDriver: Boolean
        get() = appSettingsHelper.role

    suspend fun updateOrCreateUser(firebaseUser: FirebaseUser) {
        val name = appSettingsHelper.name
        if (appSettingsHelper.role) {
            updateOrCreateUser(firebaseUser, driverDAO) {
                Driver(name, firebaseUser.phoneNumber)
            }
        } else {
            updateOrCreateUser(firebaseUser, passengerDAO) {
                Passenger(name, firebaseUser.phoneNumber)
            }
        }
    }

    private suspend fun updateOrCreateUser(
        firebaseUser: FirebaseUser,
        dao: BaseDao,
        buildUser: () -> User
    ) {
        dao.getUser(firebaseUser.uid)?.also {
            appSettingsHelper.initUser(it)
            return@updateOrCreateUser
        }
        val user = buildUser()
        dao.writeUser(user)
        appSettingsHelper.initUser(user)
    }
}
