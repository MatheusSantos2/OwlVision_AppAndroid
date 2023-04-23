package Main

import android.Manifest
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.hardware.camera2.CameraCharacteristics
import android.os.Bundle
import android.os.Process
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.view.animation.AnimationUtils
import android.view.animation.BounceInterpolator
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import java.io.File
import java.util.concurrent.Executors
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.asCoroutineDispatcher
import Main.Camera.CameraFragment
import DepthEstimation.ImageSegmentationModelExecutor
import DepthEstimation.ModelExecutionResult

private const val REQUEST_CODE_PERMISSIONS = 10

private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity(), CameraFragment.OnCaptureFinished
{

  private lateinit var cameraFragment: CameraFragment
  private lateinit var viewModel: MLExecutionViewModel
  private lateinit var viewFinder: FrameLayout
  private lateinit var resultImageView: ImageView
  private lateinit var originalImageView: ImageView
  private lateinit var captureButton: ImageButton

  private var lastSavedFile = ""
  private var useGPU = false
  private var imageSegmentationModel: ImageSegmentationModelExecutor? = null
  private val inferenceThread = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
  private val mainScope = MainScope()

  private var lensFacing = CameraCharacteristics.LENS_FACING_FRONT

  override fun onCreate(savedInstanceState: Bundle?)
  {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.tfe_is_activity_main)

    supportActionBar?.setDisplayShowTitleEnabled(false)

    viewFinder = findViewById(R.id.view_finder)
    resultImageView = findViewById(R.id.result_imageview)
    originalImageView = findViewById(R.id.original_imageview)
    captureButton = findViewById(R.id.capture_button)

    if (allPermissionsGranted())
    {
      addCameraFragment()
    }
    else
    {
      ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
    }

    viewModel = AndroidViewModelFactory(application).create(MLExecutionViewModel::class.java)
    viewModel.resultingBitmap.observe(
      this,
      Observer { resultImage ->
        if (resultImage != null) {
          updateUIWithResults(resultImage as ModelExecutionResult)
        }
        enableControls(true)
      }
    )

    createModelExecutor(useGPU)

    animateCameraButton()

    setupControls()
    enableControls(true)
  }

  private fun setupControls()
  {
    captureButton.setOnClickListener {
      it.clearAnimation()
      cameraFragment.takePicture()
    }

    findViewById<ImageButton>(R.id.toggle_button).setOnClickListener {
      lensFacing =
              if (lensFacing == CameraCharacteristics.LENS_FACING_BACK) {
                CameraCharacteristics.LENS_FACING_FRONT
              } else {
                CameraCharacteristics.LENS_FACING_BACK
              }
      cameraFragment.setFacingCamera(lensFacing)
      addCameraFragment()
    }
  }

  private fun allPermissionsGranted() =
          REQUIRED_PERMISSIONS.all {
            checkPermission(it, Process.myPid(), Process.myUid()) == PackageManager.PERMISSION_GRANTED
          }

  private fun addCameraFragment()
  {
    cameraFragment = CameraFragment.newInstance()
    cameraFragment.setFacingCamera(lensFacing)
    supportFragmentManager.popBackStack()
    supportFragmentManager.beginTransaction().replace(R.id.view_finder, cameraFragment).commit()
  }

  private fun updateUIWithResults(modelExecutionResult: ModelExecutionResult)
  {
    setImageView(resultImageView, modelExecutionResult.bitmapResult)
    setImageView(originalImageView, modelExecutionResult.bitmapOriginal)

    enableControls(true)
  }

  private fun enableControls(enable: Boolean)
  {
    captureButton.isEnabled = enable
  }

  private fun setImageView(imageView: ImageView, image: Bitmap)
  {
    Glide.with(baseContext).load(image).override(512, 512).fitCenter().into(imageView)
  }

  private fun createModelExecutor(useGPU: Boolean)
  {
    if (imageSegmentationModel != null)
    {
      imageSegmentationModel!!.close()
      imageSegmentationModel = null
    }
    try
    {
      imageSegmentationModel = ImageSegmentationModelExecutor(this, useGPU)
    }
    catch (e: Exception)
    {
      Log.e(TAG, "Fail to create ImageSegmentationModelExecutor: ${e.message}")
    }
  }

  private fun animateCameraButton()
  {
    val animation = AnimationUtils.loadAnimation(this, R.anim.scale_anim)
    animation.interpolator = BounceInterpolator()
    captureButton.animation = animation
    captureButton.animation.start()
  }

  override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray)
  {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    if (requestCode == REQUEST_CODE_PERMISSIONS)
    {
      if (allPermissionsGranted())
      {
        addCameraFragment()
        viewFinder.post { setupControls() }
      }
      else
      {
        Toast.makeText(this, "Permissions not granted by the user.", Toast.LENGTH_SHORT).show()
        finish()
      }
    }
  }

  override fun onCaptureFinished(file: File)
  {
    val msg = "Photo capture succeeded: ${file.absolutePath}"
    Log.d(TAG, msg)

    lastSavedFile = file.absolutePath
    enableControls(false)
    viewModel.onApplyModel(file.absolutePath, imageSegmentationModel, inferenceThread)
  }
}
