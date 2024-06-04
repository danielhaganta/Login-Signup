package com.android.storyapp.ui.auth

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.android.storyapp.R
import com.android.storyapp.core.utils.Preferences
import com.android.storyapp.databinding.ActivityRegisterBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class RegisterActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityRegisterBinding.inflate(layoutInflater)
    }

    @Inject
    lateinit var preferences: Preferences

    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        playAnimation()

        viewModel.isLoading.observe(this) { showLoading(it) }

        binding.apply {
            btnRegister.setOnClickListener {
                errorMessage.visibility = View.GONE

                val name = edtRegisterName.text.toString().trim()
                val email = edtRegisterEmail.text.toString().trim()
                val password = edtRegisterPassword.text.toString().trim()
                val passwordConf = edtRegisterPasswordConf.text.toString().trim()
                when {
                    TextUtils.isEmpty(name) -> edtRegisterName.error =
                        getString(R.string.empty_name)
                    TextUtils.isEmpty(email) -> edtRegisterEmail.error =
                        getString(R.string.empty_email)
                    TextUtils.isEmpty(password) -> edtRegisterPassword.error =
                        getString(R.string.empty_password)
                    passwordConf != password -> edtRegisterPasswordConf.error =
                        getString(R.string.pass_not_match)
                    else -> {
                        // close keyboard
                        edtRegisterName.onEditorAction(EditorInfo.IME_ACTION_DONE)

                        // register user account
                        viewModel.registerNewUser(name, email, password)

                        viewModel.isError.observe(this@RegisterActivity) { isError ->
                            if (isError) {
                                viewModel.responseMessage.observe(this@RegisterActivity) {
                                    it.getContentIfNotHandled()?.let { msg ->
                                        Toast.makeText(this@RegisterActivity, msg, Toast.LENGTH_SHORT).show()
                                        showErrorMessage(msg)
                                    }
                                }
                            } else {
                                errorMessage.visibility = View.GONE

                                viewModel.responseMessage.observe(this@RegisterActivity) {
                                    it?.getContentIfNotHandled()?.let { text ->
                                        Toast.makeText(
                                            this@RegisterActivity,
                                            text,
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }

                                preferences.setValues(Preferences.USER_EMAIL, email)

                                startActivity(
                                    Intent(
                                        this@RegisterActivity,
                                        LoginActivity::class.java
                                    )
                                )
                                finishAffinity()
                            }
                        }
                    }
                }
            }

            btnLogin.setOnClickListener {
                errorMessage.visibility = View.GONE
                startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
                finish()
            }

            btnRetry.setOnClickListener {
                errorMessage.visibility = View.GONE
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
            if (state) loading.visibility = View.VISIBLE else loading.visibility = View.GONE
        }
    }

    private fun playAnimation() {
        binding.apply {
            val appName = ObjectAnimator.ofFloat(tvAppName, View.ALPHA, 1f).setDuration(500)
            val registerDesc =
                ObjectAnimator.ofFloat(tvRegisterDesc, View.ALPHA, 1f).setDuration(500)
            val fullName = ObjectAnimator.ofFloat(tilRegisterName, View.ALPHA, 1f).setDuration(1000)
            val email = ObjectAnimator.ofFloat(tilRegisterEmail, View.ALPHA, 1f).setDuration(1000)
            val password =
                ObjectAnimator.ofFloat(tilRegisterPassword, View.ALPHA, 1f).setDuration(1000)
            val passwordConf =
                ObjectAnimator.ofFloat(tilRegisterPasswordConf, View.ALPHA, 1f).setDuration(1000)
            val btnLogin = ObjectAnimator.ofFloat(btnLogin, View.ALPHA, 1f).setDuration(1000)
            val tvHaveAcc = ObjectAnimator.ofFloat(tvHaveAcc, View.ALPHA, 1f).setDuration(1000)
            val btnRegister = ObjectAnimator.ofFloat(btnRegister, View.ALPHA, 1f).setDuration(1000)

            val together = AnimatorSet().apply {
                playTogether(
                    fullName,
                    email,
                    password,
                    passwordConf,
                    tvHaveAcc,
                    btnRegister,
                    btnLogin
                )
            }

            AnimatorSet().apply {
                playSequentially(together, appName, registerDesc)
                start()
            }
        }

    }
}