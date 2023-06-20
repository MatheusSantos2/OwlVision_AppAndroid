package Main

import Infraestructure.VehicleTrafficZone.BufferListHelper
import Infraestructure.VehicleTrafficZone.TrafficableTrajectoryEstimator
import Infraestructure.VehicleTrafficZone.TrajectoryEstimationValidator
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

        var imageResult = TrafficableTrajectoryEstimator(semanticResult!!.bitmapResult, depthResult!!.bitmapResult, 7)
                .getTraversableZone(semanticResult.bitmapOriginal, 0.001F, 0.001F)

        if (!TrajectoryEstimationValidator().isTraversableInCenter(imageResult.first)){
          imageResult = TrajectoryEstimationValidator().processTraversablePixels(depthResult.bitmapOriginal, semanticResult.bitmapResult, depthResult.bitmapResult,0.001F, 0.001F)
        }

        var trajectoryList = TrajectoryGenerator().generateTrajectory(imageResult.second)
        var bufferList =  BufferListHelper().getBufferedPoint(trajectoryList)
        var message = StringHelper().convertPointsToString(bufferList)

        var result =  ModelViewResult(semanticResult.bitmapResult, depthResult.bitmapResult, imageResult.first, message)
        _resultingBitmap.postValue(result)
      }
      catch (e: Exception)
      {
        Log.e(TAG, "Fail to execute ImageSegmentationModelExecutor: ${e.message}")
        _resultingBitmap.postValue(null)
      }
    }
  }
}
