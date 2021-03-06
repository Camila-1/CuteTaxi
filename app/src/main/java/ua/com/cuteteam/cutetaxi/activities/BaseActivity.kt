package ua.com.cuteteam.cutetaxi.activities

import android.app.Service
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI.setupWithNavController
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.onNavDestinationSelected
import androidx.preference.PreferenceManager
import com.google.android.material.navigation.NavigationView
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textview.MaterialTextView
import kotlinx.android.synthetic.main.navigation_header.view.*
import ua.com.cuteteam.cutetaxi.R
import ua.com.cuteteam.cutetaxi.data.database.DbEntries
import ua.com.cuteteam.cutetaxi.data.database.DriverDao
import ua.com.cuteteam.cutetaxi.data.database.PassengerDao
import ua.com.cuteteam.cutetaxi.data.entities.ComfortLevel
import ua.com.cuteteam.cutetaxi.dialogs.InfoDialog
import ua.com.cuteteam.cutetaxi.extentions.createNotificationChannel
import ua.com.cuteteam.cutetaxi.helpers.NotificationUtils
import ua.com.cuteteam.cutetaxi.helpers.network.NetStatus
import ua.com.cuteteam.cutetaxi.permissions.AccessFineLocationPermission
import ua.com.cuteteam.cutetaxi.permissions.PermissionProvider
import ua.com.cuteteam.cutetaxi.services.DriverService
import ua.com.cuteteam.cutetaxi.services.ORDER_ID_NAME
import ua.com.cuteteam.cutetaxi.services.PassengerService
import ua.com.cuteteam.cutetaxi.shPref.AppSettingsHelper
import ua.com.cuteteam.cutetaxi.viewmodels.BaseViewModel

abstract class BaseActivity :
    AppCompatActivity(),
    SharedPreferences.OnSharedPreferenceChangeListener {

    protected abstract val menuResId: Int
    protected abstract val layoutResId: Int
    protected abstract val service: Class<out Service>
    protected abstract fun onHasActiveOrder(orderId: String?)
    protected abstract fun onNoActiveOrder()
    protected abstract fun onNetworkAvailable()
    protected abstract fun onNetworkLost()

    protected val permissionProvider
        get() = PermissionProvider(this).apply {
            onDenied = { permission, isPermanentlyDenied ->
                if (isPermanentlyDenied && model.shouldShowPermissionPermanentlyDeniedDialog) {
                    InfoDialog.show(
                        supportFragmentManager,
                        permission.requiredPermissionDialogTitle,
                        permission.requiredPermissionDialogMessage
                    ) { model.shouldShowPermissionPermanentlyDeniedDialog = false }
                }
            }
        }

    protected lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var navigationView: NavigationView
    private lateinit var drawerLayout: DrawerLayout
    protected lateinit var header: View

    abstract val model: BaseViewModel

    private val appSettingsHelper by lazy { AppSettingsHelper(this) }

    private val onNavigationListener = NavigationView.OnNavigationItemSelectedListener { item ->
        item.isChecked = true
        when (item.itemId) {
            R.id.settings -> model.rememberMenu(false)
            R.id.backToHome -> model.rememberMenu(true).run { navController.navigateUp() }
            R.id.sign_out -> {
                model.signOut()
                startActivity(Intent(this, MainActivity::class.java))
            }
            else -> drawerLayout.closeDrawers()
        }
        item.onNavDestinationSelected(navController) || super.onOptionsItemSelected(item)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        initTheme()
        super.onCreate(savedInstanceState)
        NotificationUtils(this).cancelAll()
        stopService()
        setContentView(layoutResId)
        createNotificationChannel()
        initNavigation()
        initUI()
        setObservers()
    }

    override fun onResume() {
        super.onResume()
        PreferenceManager
            .getDefaultSharedPreferences(this)
            .registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        super.onPause()
        PreferenceManager
            .getDefaultSharedPreferences(this)
            .unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        startService()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionProvider.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onSupportNavigateUp(): Boolean =
        navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()


    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        val paths = mapOf(
            DbEntries.Passengers.Fields.NAME to DbEntries.Passengers.Fields.NAME,
            DbEntries.Passengers.Fields.PHONE to DbEntries.Passengers.Fields.PHONE,
            DbEntries.Passengers.Fields.COMFORT_LEVEL to DbEntries.Passengers.Fields.COMFORT_LEVEL,
            DbEntries.Car.BRAND to "${DbEntries.Drivers.Fields.CAR}/${DbEntries.Car.BRAND}",
            DbEntries.Car.MODEL to "${DbEntries.Drivers.Fields.CAR}/${DbEntries.Car.MODEL}",
            DbEntries.Car.NUMBER to "${DbEntries.Drivers.Fields.CAR}/${DbEntries.Car.NUMBER}",
            DbEntries.Car.COLOR to "${DbEntries.Drivers.Fields.CAR}/${DbEntries.Car.COLOR}",
            DbEntries.Car.CAR_CLASS to "${DbEntries.Drivers.Fields.CAR}/${DbEntries.Car.CAR_CLASS}"
        )
        paths[key]?.let {
            val value = when (key) {
                DbEntries.Passengers.Fields.COMFORT_LEVEL,
                DbEntries.Car.CAR_CLASS -> sharedPreferences?.getString(key, null)?.toInt()
                    ?.let { ordinal ->
                        ComfortLevel.values()[ordinal].name
                    }
                else -> sharedPreferences?.getString(key, null)
            }

            if (model.isChecked) {
                DriverDao().writeField(it, value)
            } else {
                PassengerDao().writeField(it, value)
            }
        }

        when (key) {
            getString(R.string.key_user_name_preference) -> {
                val name = sharedPreferences?.getString(key, null)
                header.tv_nav_header_name.text = name
            }
            getString(R.string.key_user_phone_number_preference) -> {
                val phone = sharedPreferences?.getString(key, null)
                header.tv_nav_header_phone.text = phone
            }
            getString(R.string.key_app_theme_preference) -> {
                model.rememberMenu(false)
                recreate()
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        model.rememberMenu(navigationView.menu.findItem(R.id.home) == null)
    }

    private fun initNavigation() {

        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.navigation)
        navController = findNavController(R.id.nav_host_fragment)
        appBarConfiguration = AppBarConfiguration(navController.graph, drawerLayout)
        header = navigationView.getHeaderView(0)

        setupWithNavController(navigationView, navController)

        navigationView.setNavigationItemSelectedListener(onNavigationListener)

    }

    private fun initUI() {
        val headerName = header.findViewById<MaterialTextView>(R.id.tv_nav_header_name)
        val headerPhone = header.findViewById<MaterialTextView>(R.id.tv_nav_header_phone)
        val roleChooser = header.findViewById<SwitchMaterial>(R.id.role_chooser)

        with(appSettingsHelper) {
            headerName.text = name
            headerPhone.text = phone
        }

        with(roleChooser) {
            isChecked = model.isChecked
            setOnCheckedChangeListener { _, isChecked ->
                onRoleChanged(isChecked)
            }
        }
    }

    private fun setObservers() {

        model.isMainMenu.observe(this, Observer {
            when (it) {
                true -> inflateMainMenu()
                false -> inflateSettingsSubMenu()
            }
        })

        permissionProvider.withPermission(AccessFineLocationPermission()) {
            if (!model.isGPSEnabled) {
                InfoDialog.show(
                    supportFragmentManager,
                    getString(R.string.enable_gps_recommended_dialog_title),
                    getString(R.string.enable_gps_recommended_dialog_message)
                )
            }
        }

        model.netStatus.observe(this, Observer {
            when (it) {
                NetStatus.AVAILABLE -> onNetworkAvailable()
                NetStatus.LOST, NetStatus.UNAVAILABLE -> onNetworkLost()
                else -> {
                }
            }
        })

        model.activeOrderId.observe(this, Observer {
            if (it != null) onHasActiveOrder(it)
            else onNoActiveOrder()
        })

    }

    private fun onRoleChanged(isDriver: Boolean) {
        model.changeRole(isDriver)
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun initTheme() {
        when (appSettingsHelper.appTheme) {
            getString(R.string.value_item_light_theme) -> setTheme(R.style.AppTheme_NoActionBar)
            getString(R.string.value_item_dark_theme) -> setTheme(R.style.AppTheme_NoActionBar_Dark)
        }
    }

    private fun inflateMainMenu() {
        navigationView.menu.clear()
        navigationView.inflateMenu(R.menu.main_menu)
    }

    private fun inflateSettingsSubMenu() {
        navigationView.menu.clear()
        navigationView.inflateMenu(menuResId)
    }

    private fun stopService() {
        stopService(Intent(this, PassengerService::class.java))
        stopService(Intent(this, DriverService::class.java))
    }

    private fun startService() {
        if (model.shouldStartService && model.getSignInUser() != null) {
            val orderId = model.activeOrderId.value
            startService(Intent(this, service).apply {
                putExtra(ORDER_ID_NAME, orderId)
            })
        }
    }

}