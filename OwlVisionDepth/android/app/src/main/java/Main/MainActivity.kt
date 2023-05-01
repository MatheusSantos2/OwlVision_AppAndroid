package Main

import Interpreter.MLDepthEstimation.DepthEstimationModelExecutor
import Interpreter.MLSemanticSegmentation.SemanticSegmentationModelExecutor
import Interpreter.Models.ModelViewResult
import Main.Camera.CameraFragment
import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.hardware.camera2.CameraCharacteristics
import android.os.Bundle
import android.os.Process
import android.util.Log
import android.view.animation.AnimationUtils
import android.view.animation.BounceInterpolator
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
import com.bumptech.glide.Glide
import kotlinx.coroutines.asCoroutineDispatcher
import org.opencv.android.OpenCVLoader
import java.io.File
import java.util.concurrent.Executors

private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)

private const val REQUEST_CODE_PERMISSIONS = 10
private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity(), CameraFragment.OnCaptureFinished
{
  private lateinit var cameraFragment: CameraFragment
  private lateinit var viewModel: MLExecutionViewModel
  private lateinit var viewFinder: FrameLayout
  private lateinit var resultImageViewDepth: ImageView
  private lateinit var resultImageViewSegmentation: ImageView
  private lateinit var originalImageView: ImageView
  private lateinit var captureButton: ImageButton

  private var lastSavedFile = ""
  private var useGPU = false
  private var depthEstimationExecutor: DepthEstimationModelExecutor? = null
  private var semanticSegmentationExecutor: SemanticSegmentationModelExecutor? = null
  private val inferenceThread = Executors.newSingleThreadExecutor().asCoroutineDispatcher()

  private var lensFacing = CameraCharacteristics.LENS_FACING_FRONT

  override fun onCreate(savedInstanceState: Bundle?)
  {
    super.onCreate(savedInstanceState)

    setContentView(R.layout.tfe_is_activity_main)

    supportActionBar?.setDisplayShowTitleEnabled(false)

    if (OpenCVLoader.initDebug()) Log.d(TAG, "OpenCV - Sucess")
    else Log.d(TAG, "OpenCV - Fail")

    viewFinder = findViewById(R.id.view_finder)
    resultImageViewDepth = findViewById(R.id.result_imageview_depth)
    resultImageViewSegmentation = findViewById(R.id.result_imageview_segmentation)
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
          updateUIWithResults(resultImage as ModelViewResult)
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

  private fun updateUIWithResults(modelViewResult: ModelViewResult)
  {
    setImageView(resultImageViewDepth, modelViewResult.bitmapResult)
    setImageView(resultImageViewSegmentation, modelViewResult.bitmapResult2)
    setImageView(originalImageView, modelViewResult.bitmapOriginal)

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
    if (depthEstimationExecutor != null && semanticSegmentationExecutor != null)
    {
      depthEstimationExecutor!!.close()
      depthEstimationExecutor = null

      semanticSegmentationExecutor!!.close()
      semanticSegmentationExecutor = null
    }
    try
    {
      depthEstimationExecutor = DepthEstimationModelExecutor(this, useGPU)
      semanticSegmentationExecutor = SemanticSegmentationModelExecutor(this, useGPU)
    }
    catch (e: Exception)
    {
      Log.e(TAG, "Fail to create Executors depthEstimation and semanticSegmentation - Exception: ${e.message}")
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
    viewModel.onApplyModel(file.absolutePath, depthEstimationExecutor, semanticSegmentationExecutor, inferenceThread)
  }
}
