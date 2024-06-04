package com.android.storyapp.ui.profile

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.android.storyapp.R
import com.android.storyapp.core.utils.Preferences
import com.android.storyapp.databinding.ActivityProfileBinding
import com.android.storyapp.ui.auth.LoginActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ProfileActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityProfileBinding.inflate(layoutInflater)
    }

    @Inject
    lateinit var preferences: Preferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.apply {

            val isDark = preferences.getBooleanValues(Preferences.DARK_MODE_PREF)
            switchDarkMode.isChecked = isDark == true

            val name = preferences.getStringValues(Preferences.USER_NAME)
            tvUserName.text = name

            val email = preferences.getStringValues(Preferences.USER_EMAIL)
            tvUserEmail.text = email

            switchDarkMode.setOnCheckedChangeListener { _, checked ->
                preferences.setValues(Preferences.DARK_MODE_PREF, checked)
                if (checked) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                }
            }

            btnLogout.setOnClickListener {
                val builder = MaterialAlertDialogBuilder(this@ProfileActivity)
                builder.setTitle(getString(R.string.logout))
                builder.setMessage(getString(R.string.are_you_sure_want_to_logout))
                    .setCancelable(false)
                    .setPositiveButton(getString(R.string.sure)) { _, _ ->
                        // clear the user data
                        preferences.setValues(Preferences.USER_ID, "")
                        preferences.setValues(Preferences.USER_NAME, "")
                        preferences.setValues(Preferences.USER_TOKEN, "")
                        preferences.setValues(Preferences.USER_EMAIL, "")
                        startActivity(Intent(this@ProfileActivity, LoginActivity::class.java))
                        finishAffinity()
                    }
                    .setNegativeButton(getString(R.string.no)) { dialogInterface, _ ->
                        dialogInterface.cancel()
                    }

                val alertDialog: AlertDialog = builder.create()
                alertDialog.show()
            }

            btnBack.setOnClickListener { onBackPressed() }

            btnChangeLanguage.setOnClickListener { startActivity(Intent(Settings.ACTION_LOCALE_SETTINGS)) }
        }
    }
}