package Main

import Infraestructure.VehicleTrafficZone.BufferListHelper
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExecutorCoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.File

private const val TAG = "MLExecutionViewModel"

class MLExecutionViewModel : ViewModel()
{
  private val viewModelJob = Job()
  private val viewModelScope = CoroutineScope(viewModelJob)
  private val _resultingBitmap = MutableLiveData<ModelViewResult>()
  val resultingBitmap: LiveData<ModelViewResult>
  get() = _resultingBitmap

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

        var imageResult = TrajectoryEstimator()
                .getTraversableZone(semanticResult!!.bitmapResult, depthResult!!.bitmapResult)

        if(imageResult != null)
        {
          if (!TrajectoryValidator().isTraversableInCenter(imageResult.first)){
            imageResult = TrajectoryValidator().processTraversablePixels(depthResult.bitmapOriginal, semanticResult.bitmapResult, depthResult.bitmapResult)
          }

          var trajectoryList = TrajectoryGenerator().generateTrajectory(imageResult.second)
          var message = StringHelper().convertPointsToString(trajectoryList)

          var result =  ModelViewResult(semanticResult.bitmapResult, depthResult.bitmapResult, imageResult.first, message)
          _resultingBitmap.postValue(result)
        }
        else{
          Log.w(TAG, "Fail in Trajectory Estimator Process")
          _resultingBitmap.postValue(null)
        }
      }
      catch (e: Exception)
      {
        Log.e(TAG, "Fail to execute ImageSegmentationModelExecutor: ${e.message}")
        _resultingBitmap.postValue(null)
      }
    }
  }
}
