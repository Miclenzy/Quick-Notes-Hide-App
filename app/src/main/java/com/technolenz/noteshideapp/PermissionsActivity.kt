package com.technolenz.noteshideapp

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat

class PermissionsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_permissions)

        val grantButton: Button = findViewById(R.id.btn_grant_permissions)
        grantButton.setOnClickListener {
            when {
                Build.VERSION.SDK_INT < Build.VERSION_CODES.R -> {
                    PermissionsHandlerBelowR(this).requestPermissions()
                }
                Build.VERSION.SDK_INT in Build.VERSION_CODES.R..Build.VERSION_CODES.S_V2 -> {
                    PermissionsHandlerRtoS(this).requestPermissions()
                }
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                    PermissionsHandlerTAndAbove(this).requestPermissions()
                }
            }
        }
    }


    private fun navigateToHome() {
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        finish()
    }
}

class PermissionsHandlerBelowR(private val activity: AppCompatActivity) {

    fun requestPermissions() {
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            navigateToMainActivity()
        } else {
            ActivityCompat.requestPermissions(activity, arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ), 1)
        }
    }

    private fun navigateToMainActivity() {
        val intent = Intent(activity, HomeActivity::class.java)
        activity.startActivity(intent)
        activity.finish()
    }
}

class PermissionsHandlerRtoS(private val activity: AppCompatActivity) {

    @SuppressLint("NewApi")
    fun requestPermissions() {
        if (Environment.isExternalStorageManager()) {
            navigateToMainActivity()
        } else {
            val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
            intent.data = Uri.parse("package:" + activity.packageName)
            activity.startActivity(intent)
        }
    }

    private fun navigateToMainActivity() {
        val intent = Intent(activity, HomeActivity::class.java)
        activity.startActivity(intent)
        activity.finish()
    }
}

class PermissionsHandlerTAndAbove(private val activity: AppCompatActivity) {

    private val requestPermissionLauncher = activity.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        val allGranted = permissions.all { it.value }
        if (allGranted) {
            navigateToMainActivity()
        } else {
            showReeconsiderationDialog()
        }
    }

    fun requestPermissions() {
        val permissionsToRequest = mutableListOf<String>()
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.READ_MEDIA_IMAGES)
        }
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_MEDIA_VIDEO) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.READ_MEDIA_VIDEO)
        }

        if (permissionsToRequest.isNotEmpty()) {
            requestPermissionLauncher.launch(permissionsToRequest.toTypedArray())
        } else {
            navigateToMainActivity()
        }
    }

    private fun showReeconsiderationDialog() {
        val dialogBuilder = AlertDialog.Builder(activity)
        dialogBuilder.setTitle("Permissions are Required")
        dialogBuilder.setMessage("For this app to work at all, you need to grant permissions.")
        dialogBuilder.setPositiveButton("Grant") { _, _ ->
            requestPermissions()
        }
        dialogBuilder.setNegativeButton("Go to Settings") { _, _ ->
            navigateToAppSettings()
        }
        dialogBuilder.setCancelable(false)
        dialogBuilder.create().show()
    }

    private fun navigateToMainActivity() {
        val intent = Intent(activity, HomeActivity::class.java)
        activity.startActivity(intent)
        activity.finish()
    }

    private fun navigateToAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        intent.data = Uri.parse("package:" + activity.packageName)
        activity.startActivity(intent)
    }
}
