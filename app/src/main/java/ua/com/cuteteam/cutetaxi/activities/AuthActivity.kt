package ua.com.cuteteam.cutetaxi.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import ua.com.cuteteam.cutetaxi.R
import ua.com.cuteteam.cutetaxi.fragments.PhoneNumberFragment
import ua.com.cuteteam.cutetaxi.fragments.VerificationCodeFragment
import ua.com.cuteteam.cutetaxi.viewmodels.AuthViewModel
import ua.com.cuteteam.cutetaxi.viewmodels.AuthViewModel.State
import ua.com.cuteteam.cutetaxi.viewmodels.viewmodelsfactories.AuthViewModelFactory

class AuthActivity : AppCompatActivity() {

    companion object {
        private const val VERIFICATION_CODE_FRAGMENT = "VERIFICATION_CODE_FRAGMENT"
        private const val PHONE_NUMBER_FRAGMENT = "PHONE_NUMBER_FRAGMENT"
    }

    private val authViewModel by lazy {
        ViewModelProvider(this, AuthViewModelFactory())
            .get(AuthViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)


        authViewModel.state.observe(this, Observer {
            val transaction = supportFragmentManager.beginTransaction()
            when (it) {
                State.ENTERING_PHONE_NUMBER -> transaction.replace(
                    R.id.auth_fl,
                    PhoneNumberFragment(),
                    PHONE_NUMBER_FRAGMENT
                )
                    .addToBackStack(PHONE_NUMBER_FRAGMENT)
                    .commit()
                State.ENTERING_VERIFICATION_CODE -> transaction.replace(
                    R.id.auth_fl,
                    VerificationCodeFragment(),
                    VERIFICATION_CODE_FRAGMENT
                )
                    .addToBackStack(VERIFICATION_CODE_FRAGMENT)
                    .commit()
                State.LOGGED_IN -> returnToStartUpActivity()
                State.RESEND_CODE -> authViewModel.resendVerificationCode()
                else -> {
                }
            }
        })
    }

    private fun returnToStartUpActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onBackPressed() {
        val verificationCodeFragment =
            supportFragmentManager.findFragmentByTag(VERIFICATION_CODE_FRAGMENT)
        if (verificationCodeFragment?.isVisible == true) authViewModel.backToEnteringPhoneNumber()
        else finishAffinity()
    }
}
