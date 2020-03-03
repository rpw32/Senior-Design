package com.design.senior

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Matrix
import android.os.Bundle
import android.util.Log
import android.view.Surface
import android.view.TextureView
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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

    override fun onResume() {
        super.onResume()

        flag = 0
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

        val analysisConfig = ImageAnalysisConfig.Builder().apply{
            setImageReaderMode(ImageAnalysis.ImageReaderMode.ACQUIRE_LATEST_IMAGE)
        }.build()

        val analysis = ImageAnalysis(analysisConfig)
        val intent = Intent(this, DetailResultActivity::class.java)


        val barCodeAnalyzer = BarCodeScanner { barCodes ->
            barCodes.forEach() {
                Log.d("CameraMainActivity", "Barcode Detected: ${it.rawValue}.")
                setMessage(intent, it.rawValue.toString())
                startDetail(intent)
                finish()
            }
        }


        analysis.setAnalyzer(executor, barCodeAnalyzer)

        CameraX.bindToLifecycle(this, preview, analysis)

    }
    var flag = 0

    private fun startDetail(intent: Intent){
        if(flag == 0)
        {
            flag += 1
            startActivity(intent)
        }

    }

    companion object{
        private const val EXTRA_MESSAGE  = ""

        fun getMessage(intent: Intent): String? {
            return intent.getStringExtra(EXTRA_MESSAGE)
        }

        fun setMessage(intent: Intent, message: String?){
            intent.putExtra(EXTRA_MESSAGE, message)
        }

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


