package com.example.simplecamera

import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ImageCapture
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.core.content.ContextCompat
import com.example.simplecamera.databinding.ActivityMainBinding
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {
    // ViewBinding
    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!

    // Image Capture
    private var imageCapture: ImageCapture? = null

    // Video Capture
    private var videoCapture: VideoCapture<Recorder>? = null
    private var recording: Recording? = null

    // Camera
    private lateinit var cameraXExecutors: ExecutorService

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            if (allPermissionGranted()) {
                // カメラ起動
            } else {
                Toast.makeText(this, "アプリの使用には権限の許可が必要です", Toast.LENGTH_LONG).show()
            }
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (allPermissionGranted()) {
            // カメラ起動
        } else {
            requestPermissionLauncher.launch(REQUEST_PERMISSION)
        }

        binding.imageCaptureButton.setOnClickListener {

        }

        binding.videoCaptureButton.setOnClickListener {

        }

        cameraXExecutors = Executors.newSingleThreadExecutor()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
        cameraXExecutors.shutdown()
    }

    private fun allPermissionGranted() =
        ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.RECORD_AUDIO
                ) == PackageManager.PERMISSION_GRANTED

    companion object {
        private val REQUEST_PERMISSION = mutableListOf(
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.RECORD_AUDIO
        ).also {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                it.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }.toTypedArray()
    }
}