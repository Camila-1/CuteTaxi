package ua.com.cuteteam.cutetaxiproject.activities

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.AttributeSet
import android.view.View
import androidx.lifecycle.ViewModelProvider
import ua.com.cuteteam.cutetaxiproject.R
import ua.com.cuteteam.cutetaxiproject.fragments.HeadPieceFragment
import ua.com.cuteteam.cutetaxiproject.viewmodels.AuthViewModel
import ua.com.cuteteam.cutetaxiproject.viewmodels.viewmodelsfactories.AuthViewModelFactory
import java.util.*

class MainActivity : AppCompatActivity() {
    private val authViewModel by lazy {
        ViewModelProvider(this, AuthViewModelFactory())
            .get(AuthViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.head_piece_fl, HeadPieceFragment())
            .commit()

        Timer().schedule(object : TimerTask() {
            override fun run() {
                startAuthorization()
            }
        }, 1500)
    }

    private fun startAuthorization() {
        val intent = Intent(this, AuthActivity::class.java)
        startActivity(intent)
        authViewModel.verifyPhoneNumber()
    }
}
