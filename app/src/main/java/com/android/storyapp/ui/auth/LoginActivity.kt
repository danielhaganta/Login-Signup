package com.android.storyapp.ui.auth

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatDelegate
import com.android.storyapp.MainActivity
import com.android.storyapp.R
import com.android.storyapp.core.utils.Preferences
import com.android.storyapp.databinding.ActivityLoginBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityLoginBinding.inflate(layoutInflater)
    }


    @Inject
    lateinit var preferences: Preferences

    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        playAnimation()

        // check preference for dark mode
        val isDark = preferences.getBooleanValues(Preferences.DARK_MODE_PREF)
        if (isDark) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

        // check prefs for user token
        val userToken = preferences.getStringValues(Preferences.USER_TOKEN)
        if (!userToken.isNullOrEmpty()) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        viewModel.isLoading.observe(this) { showLoading(it) }

        binding.apply {
            btnLogin.setOnClickListener {
                errorMessage.visibility = View.GONE

                val email = edtLoginEmail.text.toString().trim()
                val password = edtLoginPassword.text.toString().trim()

                when {
                    TextUtils.isEmpty(email) -> edtLoginEmail.error =
                        getString(R.string.empty_email)
                    TextUtils.isEmpty(password) -> edtLoginPassword.error =
                        getString(R.string.empty_password)
                    else -> {
                        // close keyboard
                        edtLoginEmail.onEditorAction(EditorInfo.IME_ACTION_DONE)

                        loginUser(email, password)
                    }
                }
            }

            btnRegisterNow.setOnClickListener {
                startActivity(Intent(this@LoginActivity, RegisterActivity::class.java))
                errorMessage.visibility = View.GONE
            }

            btnRetry.setOnClickListener {
                errorMessage.visibility = View.GONE
            }
        }
    }

    private fun loginUser(email: String, password: String) {
        viewModel.loginUser(email, password)

        viewModel.isError.observe(this@LoginActivity) { isError ->

            if (!isError) {
                binding.errorMessage.visibility = View.GONE

                preferences.setValues(Preferences.USER_EMAIL, email)

                viewModel.loginUser(email, password)

                startActivity(
                    Intent(
                        this@LoginActivity,
                        MainActivity::class.java
                    )
                )
                finish()
            } else {
                viewModel.responseMessage.observe(this@LoginActivity) {
                    if (it != null) {
                        it.getContentIfNotHandled()?.let { msg ->
                            Toast.makeText(this@LoginActivity, msg, Toast.LENGTH_SHORT).show()
                            showErrorMessage(msg)
                        }
                    }
                }
            }
        }
    }


    private fun showErrorMessage(message: String) {
        binding.apply {
            tvErrorDesc.text = message
            errorMessage.visibility = View.VISIBLE
        }
    }

    private fun showLoading(state: Boolean) {
        binding.apply {
            if (state) {
                loading.visibility = View.VISIBLE
                errorMessage.visibility = View.GONE
            } else {
                loading.visibility = View.GONE
            }
        }
    }

    private fun playAnimation() {
        binding.apply {
            val appName = ObjectAnimator.ofFloat(tvAppName, View.ALPHA, 1f).setDuration(500)
            val loginDesc = ObjectAnimator.ofFloat(tvLoginDesc, View.ALPHA, 1f).setDuration(500)
            val email = ObjectAnimator.ofFloat(tilLoginEmail, View.ALPHA, 1f).setDuration(1000)
            val password =
                ObjectAnimator.ofFloat(tilLoginPassword, View.ALPHA, 1f).setDuration(1000)
            val btnLogin = ObjectAnimator.ofFloat(btnLogin, View.ALPHA, 1f).setDuration(1000)
            val tvNotHaveAcc =
                ObjectAnimator.ofFloat(tvNotHaveAcc, View.ALPHA, 1f).setDuration(1000)
            val btnRegister =
                ObjectAnimator.ofFloat(btnRegisterNow, View.ALPHA, 1f).setDuration(1000)

            val together = AnimatorSet().apply {
                playTogether(email, password, tvNotHaveAcc, btnLogin, btnRegister)
            }

            AnimatorSet().apply {
                playSequentially(together, appName, loginDesc)
                start()
            }
        }

    }
}