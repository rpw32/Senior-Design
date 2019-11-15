package com.design.senior

import android.content.pm.PackageManager
import android.graphics.Matrix
import android.os.Bundle
import android.view.Surface
import android.view.TextureView
import android.view.ViewGroup

import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraX
import androidx.camera.core.Preview
import androidx.camera.core.PreviewConfig
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class CameraMainActivity : AppCompatActivity() {

    private val REQUEST_CODE_PERMISSIONS = 101
    private val REQUIRED_PERMISSIONS = arrayOf("android.permission.CAMERA", "android.permission.WRITE_EXTERNAL_STORAGE")
    private lateinit var textureView: TextureView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        textureView = findViewById(R.id.view_finder)

        if (allPermissionsGranted()) {
            startCamera() //start camera if permission has been granted by user
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }
    }

    private fun startCamera() {
        CameraX.unbindAll()

        val config = PreviewConfig.Builder()
                .setLensFacing(CameraX.LensFacing.BACK)
                .build()
        val preview = Preview(config)

        preview.setOnPreviewOutputUpdateListener(
                Preview.OnPreviewOutputUpdateListener { previewOutput ->
                    val parent = textureView.parent as ViewGroup
                    parent.removeView(textureView)
                    parent.addView(textureView, 0)

                    textureView.surfaceTexture = previewOutput.surfaceTexture

                    // Compute center of preview
                    val centerX = textureView.width.toFloat() / 2
                    val centerY = textureView.height.toFloat() / 2

                    val rotationDegrees: Int

                    when (textureView.rotation.toInt()) {
                        Surface.ROTATION_0 -> rotationDegrees = 0
                        Surface.ROTATION_90 -> rotationDegrees = 90
                        Surface.ROTATION_180 -> rotationDegrees = 180
                        Surface.ROTATION_270 -> rotationDegrees = 270
                        else -> return@OnPreviewOutputUpdateListener
                    }

                    val matrix = Matrix()
                    matrix.postRotate(rotationDegrees.toFloat(), centerX, centerY)

                    textureView.setTransform(matrix)
                })

        CameraX.bindToLifecycle(this, preview)
    }

    private fun allPermissionsGranted(): Boolean {

        for (permission in REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }
}
