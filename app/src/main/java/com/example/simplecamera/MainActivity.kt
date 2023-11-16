package com.example.simplecamera

import android.content.ContentValues
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.FallbackStrategy
import androidx.camera.video.MediaStoreOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.core.content.ContextCompat
import androidx.core.view.GestureDetectorCompat
import com.example.simplecamera.databinding.ActivityMainBinding
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.Locale
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

    // Mode
    private var selectedCameraMode: CameraMode = CameraMode.Photo

    // ジェスチャー検出
    private lateinit var gestureDetector: GestureDetectorCompat

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                // カメラの権限が許可が取れている場合、カメラ機能を開始
                startCamera()
            } else {
                // TODO: ダイアログで表示する
                Toast.makeText(this, "アプリの使用にはカメラの権限の許可が必要です", Toast.LENGTH_LONG).show()
                finish()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        gestureDetector =
            GestureDetectorCompat(this, object : GestureDetector.SimpleOnGestureListener() {
                override fun onFling(
                    e1: MotionEvent?, // e1 ： フリック開始座標（起点）
                    e2: MotionEvent,  // e2 ： 現在のフリック座標
                    velocityX: Float, // velocityX ： 横方向の加速度
                    velocityY: Float  // velocityY ： 縦方向の加速度
                ): Boolean {
                    val distance = e1?.x?.minus(e2.x)?.toInt() ?: 0
                    Log.d("DEBUG_TAG", distance.toString())
                    Log.d("DEBUG_TAG", Math.abs(distance).toString())
                    if (Math.abs(distance) <= SWIPE_EVENT_MIN_DISTANCE) {
                        return true
                    }

                    selectedCameraMode = if (distance > 0) {
                        Log.d("DEBUG_TAG", "左へスワイプ：Photoモード")
                        CameraMode.Photo
                    } else {
                        Log.d("DEBUG_TAG", "右へスワイプ：Videoモード")
                        CameraMode.Video
                    }

                    return super.onFling(e1, e2, velocityX, velocityY)
                }
            })

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            requestPermissionLauncher.launch(REQUEST_PERMISSIONS)
        }

        binding.executeButton.setOnClickListener {
            if (selectedCameraMode == CameraMode.Photo) {
                takePicture()
            } else {
                captureVideo()
            }
        }
        cameraXExecutors = Executors.newSingleThreadExecutor()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        gestureDetector.onTouchEvent(event)
        return super.onTouchEvent(event)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
        cameraXExecutors.shutdown()
    }

    private fun allPermissionsGranted() = REQUEST_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            this@MainActivity,
            it
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.preview.surfaceProvider)
                }

            // TODO: 撮影時のオプションを検討
            imageCapture = ImageCapture.Builder()
                .build()

            val recorder = Recorder.Builder()
                .setQualitySelector(
                    QualitySelector.from(
                        Quality.HIGHEST,
                        FallbackStrategy.higherQualityOrLowerThan(Quality.SD)
                    )
                )
                .build()
            videoCapture = VideoCapture.withOutput(recorder)

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // カメラがバインドされている場合、バインドを解除
                cameraProvider.unbindAll()

                // ライフサイクルにカメラをバインド
                cameraProvider.bindToLifecycle(
                    this,
                    cameraSelector,
                    preview,
                    imageCapture,
                    videoCapture
                )
            } catch (exception: Exception) {
                Log.e("SimpleCamera", "Use case binding failed", exception)

            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun takePicture() {
        val imageCapture = this.imageCapture ?: return
        val outputOptions = createPhotoOutputOptions()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exception: ImageCaptureException) {
                    Log.e("SimpleCamera", "Photo capture failed: ${exception.message}", exception)
                }

                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    val message = "Photo capture succeeded: ${outputFileResults.savedUri}"
                    Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT).show()
                    Log.e("SimpleCamera", message)
                }
            }
        )
    }

    private fun createPhotoOutputOptions(): ImageCapture.OutputFileOptions {
        val name = SimpleDateFormat(
            FILENAME_FORMAT,
            Locale.getDefault()
        ).format(System.currentTimeMillis())

        val contentValue = ContentValues().also {
            it.put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            it.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                it.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/SimpleCamera-Image")
            }
        }

        return ImageCapture.OutputFileOptions.Builder(
            contentResolver,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValue
        ).build()
    }

    private fun captureVideo() {
        val videoCapture = this.videoCapture ?: return

        val curRecording = recording

        if (curRecording != null) {
            curRecording.stop()
            recording = null
            return
        }

//        binding.videoCaptureButton.isEnabled = false

        val mediaStoreOutputOptions = createVideoOutputOptions()

        recording = videoCapture.output.prepareRecording(this, mediaStoreOutputOptions).also {
            if (ContextCompat.checkSelfPermission(
                    this@MainActivity,
                    android.Manifest.permission.RECORD_AUDIO
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                // 録音を有効化
                it.withAudioEnabled()
            }
        }.start(ContextCompat.getMainExecutor(this)) { recordEvent ->
            when (recordEvent) {
                is VideoRecordEvent.Start -> {
//                    binding.videoCaptureButton.also {
//                        it.text = getString(R.string.end_video)
//                        it.isEnabled = true
//                    }
                }

                is VideoRecordEvent.Finalize -> {
                    if (recordEvent.hasError()) {
                        recording?.close()
                        recording = null
                        Log.e(
                            "SimpleCamera", "Video capture ends with error: " +
                                    "${recordEvent.error}"
                        )
                    } else {
                        val message = "Video capture succeeded: " +
                                "${recordEvent.outputResults.outputUri}"
                        Toast.makeText(this, message, Toast.LENGTH_SHORT)
                            .show()
                        Log.e("SimpleCamera", message)
                    }
//                    binding.videoCaptureButton.also {
//                        it.text = getString(R.string.start_video)
//                        it.isEnabled = true
//                    }
                }
            }
        }
    }

    private fun createVideoOutputOptions(): MediaStoreOutputOptions {
        val name =
            SimpleDateFormat(FILENAME_FORMAT, Locale.JAPAN).format(System.currentTimeMillis())
        val contentValues = ContentValues().also {
            it.put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            it.put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                it.put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/SimpleCamera-Video")
            }
        }

        return MediaStoreOutputOptions.Builder(
            contentResolver,
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        )
            .setContentValues(contentValues)
            .build()
    }

    companion object {
        private const val SWIPE_EVENT_MIN_DISTANCE = 100
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private val REQUEST_PERMISSIONS = mutableListOf(
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.RECORD_AUDIO
        ).also {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                it.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }.toTypedArray()
    }
}
