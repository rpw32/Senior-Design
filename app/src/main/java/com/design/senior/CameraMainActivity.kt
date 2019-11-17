package com.design.senior

import android.content.pm.PackageManager
import android.graphics.Matrix
import android.os.Bundle
import android.view.Surface
import android.view.TextureView
import android.view.ViewGroup
import android.Manifest
import android.util.Log
import android.util.Size
import android.widget.ImageButton
import android.widget.Toast

import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File
import java.util.concurrent.Executors

class CameraMainActivity : AppCompatActivity() {

    private val REQUEST_CODE_PERMISSIONS = 10
    private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    private lateinit var textureView: TextureView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.camera_main)
        textureView = findViewById(R.id.view_finder)

        if (allPermissionsGranted()) {
            textureView.post{startCamera()} //start camera if permission has been granted by user
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }

        // Every time the provided texture view changes, recompute layout
        textureView.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            updateTransform()
        }
    }

    private val executor = Executors.newSingleThreadExecutor()

    private fun startCamera() {

        val config = PreviewConfig.Builder().apply {
            setLensFacing(CameraX.LensFacing.BACK)
        }.build()
        val preview = Preview(config)

        preview.setOnPreviewOutputUpdateListener{
                    val parent = textureView.parent as ViewGroup
                    parent.removeView(textureView)
                    textureView.surfaceTexture = it.surfaceTexture
                    parent.addView(textureView, 0)

                    updateTransform()
                }

        // Create configuration object for the image capture use case
        val imageCaptureConfig = ImageCaptureConfig.Builder()
                .apply {
                    // We don't set a resolution for image capture; instead, we
                    // select a capture mode which will infer the appropriate
                    // resolution based on aspect ration and requested mode
                    setCaptureMode(ImageCapture.CaptureMode.MIN_LATENCY)
                }.build()

        // Build the image capture use case and attach button click listener
        val imageCapture = ImageCapture(imageCaptureConfig)
        findViewById<ImageButton>(R.id.imageButton).setOnClickListener {
            val file = File(externalMediaDirs.first(),
                    "${System.currentTimeMillis()}.jpg")

            imageCapture.takePicture(file, executor,
                    object : ImageCapture.OnImageSavedListener {
                        override fun onError(
                                imageCaptureError: ImageCapture.ImageCaptureError,
                                message: String,
                                exc: Throwable?
                        ) {
                            val msg = "Photo capture failed: $message"
                            Log.e("CameraXApp", msg, exc)
                            textureView.post {
                                Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
                            }
                        }

                        override fun onImageSaved(file: File) {
                            val msg = "Photo capture succeeded: ${file.absolutePath}"
                            Log.d("CameraXApp", msg)
                            textureView.post {
                                Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
                            }
                        }
                    })
        }

        CameraX.bindToLifecycle(this, preview, imageCapture)
    }
    private fun updateTransform(){

        // Compute center of preview
        val centerX = textureView.width.toFloat() / 2
        val centerY = textureView.height.toFloat() / 2

        val rotationDegrees: Int

        when (textureView.rotation.toInt()) {
            Surface.ROTATION_0 -> rotationDegrees = 0
            Surface.ROTATION_90 -> rotationDegrees = 90
            Surface.ROTATION_180 -> rotationDegrees = 180
            Surface.ROTATION_270 -> rotationDegrees = 270
            else -> return
        }

        val matrix = Matrix()
        matrix.postRotate(rotationDegrees.toFloat(), centerX, centerY)

        textureView.setTransform(matrix)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == REQUEST_CODE_PERMISSIONS){
            if (allPermissionsGranted()){
                textureView.post{startCamera()}
            } else{
                Toast.makeText(this,
                        "Permissions not granted by the user.",
                        Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
                baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    }
