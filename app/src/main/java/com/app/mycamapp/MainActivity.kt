package com.app.mycamapp

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import android.widget.Toast
import androidx.camera.lifecycle.ProcessCameraProvider
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.TextView
import androidx.camera.core.*
import androidx.camera.video.FallbackStrategy
import androidx.camera.video.MediaStoreOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.VideoRecordEvent
import androidx.core.content.PermissionChecker
import androidx.core.view.isVisible
import com.app.mycamapp.databinding.ActivityMainBinding
import com.app.mycamapp.databinding.ActivityMainBinding.inflate
import java.nio.ByteBuffer
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {
    private lateinit var viewBinding: ActivityMainBinding
    lateinit var textView: TextView

    private var videoCapture:VideoCapture<Recorder>? = null
    private var recording: Recording? = null

    private lateinit var cameraExecutor: ExecutorService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
     viewBinding= inflate(layoutInflater)
        viewBinding.pause.isVisible=false
        viewBinding.stopbtn.isVisible=false
        setContentView(viewBinding.root)
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }

         viewBinding.button.setOnClickListener {
             captureVideo()
         }
        viewBinding.stopbtn.setOnClickListener{
             stopVideo()
        }
        cameraExecutor = Executors.newSingleThreadExecutor()

     }

    private fun stopVideo() {
        val curRecording = recording
        if (curRecording != null) {
            // Stop the current recording session.
            curRecording.stop()
            recording = null
            viewBinding.stopbtn.isVisible=false
            viewBinding.pause.isVisible=false
            return
        }
        val videoCapture = this.videoCapture ?: return

        viewBinding.button.isEnabled=false
        viewBinding.pause.isVisible=true
        viewBinding.stopbtn.isVisible=true




        // create and start a new recording session
        val name = SimpleDateFormat(FILENAME_FORMAT, Locale.US)
            .format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/CameraX-Video")
            }
        }

        val mediaStoreOutputOptions = MediaStoreOutputOptions
            .Builder(contentResolver, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
            .setContentValues(contentValues)
            .build()
        recording = videoCapture.output
            .prepareRecording(this, mediaStoreOutputOptions)

            .start(ContextCompat.getMainExecutor(this)) { recordEvent ->
                when(recordEvent) {
                    is VideoRecordEvent.Start -> {
                        viewBinding.button.apply {

                            isEnabled = false
                        }
                        viewBinding.pause.apply { isEnabled=true }
                        viewBinding.stopbtn.apply { isEnabled=true }
                    }
                    is VideoRecordEvent.Finalize -> {
                        if (!recordEvent.hasError()) {
                            val msg = "Video capture succeeded: " +
                                    "${recordEvent.outputResults.outputUri}"
                            Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT)
                                .show()
                            Log.d(TAG, msg)
                        } else {
                            recording?.close()
                            recording = null
                            Log.e(TAG, "Video capture ends with error: " +
                                    "${recordEvent.error}")
                        }
                        viewBinding.button.apply {

                            isEnabled = true
                        }
                        viewBinding.pause.apply { isEnabled=false}
                        viewBinding.stopbtn.apply { isEnabled=false }
                    }
                }
            }



    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it) == PackageManager.PERMISSION_GRANTED
    }


    private fun captureVideo() {

            val videoCapture = this.videoCapture ?: return

            viewBinding.button.isEnabled=false
        viewBinding.pause.isVisible=true
        viewBinding.stopbtn.isVisible=true




            // create and start a new recording session
            val name = SimpleDateFormat(FILENAME_FORMAT, Locale.US)
                .format(System.currentTimeMillis())
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, name)
                put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                    put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/CameraX-Video")
                }
            }

            val mediaStoreOutputOptions = MediaStoreOutputOptions
                .Builder(contentResolver, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
                .setContentValues(contentValues)
                .build()
            recording = videoCapture.output
                .prepareRecording(this, mediaStoreOutputOptions)

                .start(ContextCompat.getMainExecutor(this)) { recordEvent ->
                    when(recordEvent) {
                        is VideoRecordEvent.Start -> {
                            viewBinding.button.apply {

                                isEnabled = false
                            }
                            viewBinding.pause.apply { isEnabled=true }
                            viewBinding.stopbtn.apply { isEnabled=true }
                            //start counter
                            textView = findViewById(R.id.textView)

                            // time count down for 30 seconds,
                            // with 1 second as countDown interval
                            object : CountDownTimer(15000, 1000) {

                                // Callback function, fired on regular interval
                                override fun onTick(millisUntilFinished: Long) {
                                    textView.setText("seconds remaining: " + millisUntilFinished / 1000)
                                }

                                // Callback function, fired
                                // when the time is up
                                override fun onFinish() {
                                    textView.setText("")
                                   stopVideo()
                                }
                            }.start()
                        }
                        is VideoRecordEvent.Finalize -> {
                            if (!recordEvent.hasError()) {
                                val msg = "Video capture succeeded: " +
                                        "${recordEvent.outputResults.outputUri}"
                                Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT)
                                    .show()
                                Log.d(TAG, msg)
                            } else {
                                recording?.close()
                                recording = null
                                Log.e(TAG, "Video capture ends with error: " +
                                        "${recordEvent.error}")
                            }
                            viewBinding.button.apply {

                                isEnabled = true
                            }
                            viewBinding.pause.apply { isEnabled=false}
                            viewBinding.stopbtn.apply { isEnabled=false }
                        }
                    }
                }






    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener(Runnable {
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(viewBinding.viewFinder.surfaceProvider)
                }
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            val recorder = Recorder.Builder()
                .setQualitySelector(QualitySelector.from(Quality.HIGHEST))
                .build()
            videoCapture = VideoCapture.withOutput(recorder)


// For querying information and states.
            try {
                cameraProvider.unbindAll()
                val camera = cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview,videoCapture)
                val cameraControl = camera.cameraControl
// For querying information and states.
                val cameraInfo = camera.cameraInfo
                cameraControl.enableTorch(true);

            }
            catch(exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)}
        }, ContextCompat.getMainExecutor(this))

    }
override fun onDestroy() {
    super.onDestroy()
    cameraExecutor.shutdown()
}



companion object {
    private const val TAG = "CamApp"
    private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
    private const val REQUEST_CODE_PERMISSIONS = 10
    private val REQUIRED_PERMISSIONS =
        mutableListOf (
            Manifest.permission.CAMERA

        ).apply {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }.toTypedArray()
}

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults:
        IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(this,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
}