package ua.com.cuteteam.driverclient.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.maps.SupportMapFragment
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import ua.com.cuteteam.core.activities.AuthActivity
import ua.com.cuteteam.driverclient.fragments.HeadPieceFragment
import ua.com.cuteteam.core.viewmodels.AuthViewModel
import ua.com.cuteteam.core.viewmodels.StartUpViewModel
import ua.com.cuteteam.core.viewmodels.viewmodelfactories.AuthViewModelFactory
import ua.com.cuteteam.core.viewmodels.viewmodelfactories.StartUpViewModelFactory
import ua.com.cuteteam.driverclient.R
import java.util.*

class StartActivity : AppCompatActivity() {

    companion object {
        private const val REQUEST_CODE = 100
        private const val RESULT_CODE = 101
    }

    private val authViewModel by lazy {
        ViewModelProvider(this, AuthViewModelFactory())
            .get(AuthViewModel::class.java)
    }

    private val startUpViewModel by lazy {
        ViewModelProvider(this, StartUpViewModelFactory())
            .get(StartUpViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.head_piece_fl,
                HeadPieceFragment()
            )
            .commit()

        Timer().schedule(object : TimerTask() {
            override fun run() {
                GlobalScope.launch(Dispatchers.Main) {
                    if (authViewModel.verifyCurrentUser()) {
                        authViewModel.firebaseUser?.let { updateUserAndStartActivity(it) }
                    }
                    else startAuthorization()
                }
            }
        }, 350)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE && resultCode == RESULT_CODE) {
            GlobalScope.launch(Dispatchers.Main) {
                authViewModel.firebaseUser?.let { updateUserAndStartActivity(it) }
            }
        }
    }

    private fun startAuthorization() {
        val intent = Intent(this, AuthActivity::class.java)
        startActivityForResult(intent, REQUEST_CODE)
    }

    private suspend fun updateUserAndStartActivity(firebaseUser: FirebaseUser) {
        startUpViewModel.updateOrCreateUser(firebaseUser)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.head_piece_fl, SupportMapFragment())
            .commit()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity()
    }
}
