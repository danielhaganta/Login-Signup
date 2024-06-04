package com.android.storyapp.ui.addstory

import android.Manifest
import android.content.Intent
import android.content.Intent.ACTION_GET_CONTENT
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.android.storyapp.MainActivity
import com.android.storyapp.R
import com.android.storyapp.core.utils.Preferences
import com.android.storyapp.core.utils.reduceFileImage
import com.android.storyapp.core.utils.rotateBitmap
import com.android.storyapp.core.utils.uriToFile
import com.android.storyapp.databinding.ActivityAddStoryBinding
import com.android.storyapp.ui.main.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class AddStoryActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityAddStoryBinding.inflate(layoutInflater)
    }

    @Inject
    lateinit var preferences: Preferences

    private val mainViewModel: MainViewModel by viewModels()

    private var getFile: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        showDialog()

        binding.apply {

            btnBack.setOnClickListener { onBackPressed() }

            btnUpload.setOnClickListener { uploadImage() }

            btnAddPhotoCenter.setOnClickListener { showDialog() }

            btnAddPhoto.setOnClickListener { showDialog() }
        }
    }

    private fun showDialog() {
        // check if permission not granted, then request for permission else show picture dialog
        if (!allPermissionsGranted()) {
            ActivityCompat.requestPermissions(
                this@AddStoryActivity,
                REQUIRED_PERMISSIONS,
                REQUEST_CODE_PERMISSIONS
            )
        } else {
            val pictureDialog = AlertDialog.Builder(this)
            pictureDialog.setTitle(getString(R.string.choose_action))
            val pictureDialogItem = arrayOf(
                getString(R.string.from_gallery),
                getString(R.string.using_camera)
            )
            pictureDialog.setItems(pictureDialogItem) { _, which ->
                when (which) {
                    0 -> openGallery()
                    1 -> openCamera()
                }
            }
            pictureDialog.show()
        }
    }

    private fun openGallery() {
        val intent = Intent()
        intent.action = ACTION_GET_CONTENT
        intent.type = "image/*"
        val chooser = Intent.createChooser(intent, getString(R.string.choose_picture))
        launcherIntentGallery.launch(chooser)
    }

    private val launcherIntentGallery = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val selectedImg: Uri = result.data?.data as Uri
            val myFile = uriToFile(selectedImg, this)
            getFile = myFile

            binding.apply {
                if (getFile == null) btnAddPhotoCenter.visibility =
                    View.VISIBLE else btnAddPhotoCenter.visibility = View.GONE
                ivStoryPhoto.setImageURI(selectedImg)
            }
        }
    }

    // Open CameraX
    private fun openCamera() {
        val intent = Intent(this, CameraActivity::class.java)
        launcherIntentCameraX.launch(intent)
    }

    // resultForActivity to get returned file from CameraActivity
    private val launcherIntentCameraX = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == CAMERA_X_RESULT) {
            val myFile = it.data?.getSerializableExtra("picture") as File
            val isBackCamera = it.data?.getBooleanExtra("isBackCamera", true) as Boolean
            getFile = myFile
            val result = rotateBitmap(BitmapFactory.decodeFile(myFile.path), isBackCamera)

            binding.apply {
                if (getFile == null) btnAddPhotoCenter.visibility =
                    View.VISIBLE else btnAddPhotoCenter.visibility = View.GONE
                ivStoryPhoto.setImageBitmap(result)
            }
        }
    }

    private fun uploadImage() {
        binding.apply {
            val token = preferences.getStringValues(Preferences.USER_TOKEN)

            val inputDesc = edtStoryCaption.text.toString().trim()
            if (inputDesc.isEmpty()) {
                edtStoryCaption.error = getString(R.string.empty_desc)
            }

            if (inputDesc.isNotEmpty()) {
                if (getFile != null) {
                    val file = reduceFileImage(getFile as File)

                    val description = inputDesc.toRequestBody("text/plain".toMediaType())
                    val requestImageFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
                    val imageMultipart: MultipartBody.Part = MultipartBody.Part.createFormData(
                        "photo",
                        file.name,
                        requestImageFile
                    )

                    if (token != null) {
                        mainViewModel.addNewStory(token, imageMultipart, description)

                        mainViewModel.responseMessage.observe(this@AddStoryActivity) {
                            if (it != null){
                                it.getContentIfNotHandled()?.let { msg ->
                                    Toast.makeText(
                                        this@AddStoryActivity,
                                        msg,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }

                        mainViewModel.isError.observe(this@AddStoryActivity) { isError ->
                            if (!isError) {
                                startActivity(
                                    Intent(
                                        this@AddStoryActivity,
                                        MainActivity::class.java
                                    )
                                )
                                finishAffinity()
                            }
                        }
                    }
                } else {
                    Toast.makeText(
                        this@AddStoryActivity,
                        getString(R.string.select_img),
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
            }

        }
    }

    // check permission for Camera
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (!allPermissionsGranted()) {
                Toast.makeText(
                    this,
                    getString(R.string.permission_not_granted),
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        const val CAMERA_X_RESULT = 200
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
        private const val REQUEST_CODE_PERMISSIONS = 10
    }
}