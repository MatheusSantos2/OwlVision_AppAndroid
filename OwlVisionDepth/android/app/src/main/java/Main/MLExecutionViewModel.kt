package Main

import Infraestructure.VehicleTrafficZone.TrajectoryEstimator
import Infraestructure.VehicleTrafficZone.TrajectoryValidator
import Interpreter.MLDepthEstimation.DepthEstimationModelExecutor
import Interpreter.MLSemanticSegmentation.SemanticSegmentationModelExecutor
import Interpreter.Models.ModelViewResult
import Interpreter.OpenCV.TrajectoryGenerator
import Utils.ImageHelper
import Utils.StringHelper
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import android.graphics.Bitmap
import android.graphics.PointF
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExecutorCoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.File

private const val TAG = "MLExecutionViewModel"

class MLExecutionViewModel : ViewModel()
{
  private lateinit var message: String
  private val viewModelJob = Job()
  private val viewModelScope = CoroutineScope(viewModelJob)
  private val _resultingBitmap = MutableLiveData<ModelViewResult>()
  val resultingBitmap: LiveData<ModelViewResult>
  get() = _resultingBitmap

  private var imageResult: Pair<Bitmap, MutableList<PointF>>? = null
    set(value) {
      field = value?.let { kotlin.Pair(value.first, value.second) }
    }

  fun onApplyModel(filePath: String, depthEstimationModel: DepthEstimationModelExecutor?,
                   semanticSegmentation: SemanticSegmentationModelExecutor?,
                   inferenceThread: ExecutorCoroutineDispatcher)
  {
    viewModelScope.launch(inferenceThread)
    {
      var contentImage = ImageHelper.decodeBitmap(File(filePath))
      var contentImage2 = ImageHelper.decodeBitmap(File(filePath))
      try
      {
        var semanticResult = semanticSegmentation?.execute(contentImage)
        var depthResult = depthEstimationModel?.execute(contentImage2)

        var logResult = StringBuilder()
        logResult.append("DepthResult: ${depthResult?.executionLog}")
        logResult.append("SemanticResult: ${semanticResult?.executionLog}" )

        imageResult = Pair(ImageHelper.createEmptyBitmap(100, 100),mutableListOf())!!

        imageResult = if (!TrajectoryValidator().isTraversableInCenter(depthResult!!.bitmapResult)){
          var imageResult2 = TrajectoryValidator()
                  .processTraversablePixels(depthResult.bitmapOriginal, semanticResult!!.bitmapResult, depthResult.bitmapResult)
          Pair(imageResult2.first, imageResult2.second.toMutableList())

        }else {
          var imageResult2 = TrajectoryEstimator()
                  .getTraversableZone(semanticResult!!.bitmapResult, depthResult.bitmapResult, 0)
          Pair(imageResult2.first, imageResult2.second.toMutableList())
        }

        if (imageResult!!.second.size != 0) {
          message = StringHelper().convertPointsToString(imageResult!!.second)
        }
        else {
          Log.w(TAG, "Fail in Trajectory Estimator Process")
        }

        var result =  ModelViewResult(semanticResult.bitmapResult, depthResult.bitmapResult, imageResult!!.first, message)
        _resultingBitmap.postValue(result)
      }
      catch (e: Exception) {
        Log.e(TAG, "Fail to execute ImageSegmentationModelExecutor: ${e.message}")
        _resultingBitmap.postValue(null)
      }
    }
  }
}
