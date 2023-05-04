package Main

import Infraestructure.VehicleTrafficZone.RoadSegmentator
import Interpreter.MLDepthEstimation.DepthEstimationModelExecutor
import Interpreter.MLSemanticSegmentation.SemanticSegmentationModelExecutor
import Interpreter.Models.ModelViewResult
import androidx.lifecycle.ViewModel
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExecutorCoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import Utils.ImageUtils

private const val TAG = "MLExecutionViewModel"

class MLExecutionViewModel : ViewModel()
{
  private val _resultingBitmap = MutableLiveData<ModelViewResult>()

  val resultingBitmap: LiveData<ModelViewResult>
    get() = _resultingBitmap

  private val viewModelJob = Job()
  private val viewModelScope = CoroutineScope(viewModelJob)

  fun onApplyModel(filePath: String, depthEstimationModel: DepthEstimationModelExecutor?,
                   semanticSegmentation: SemanticSegmentationModelExecutor?,
                   inferenceThread: ExecutorCoroutineDispatcher)
  {
    viewModelScope.launch(inferenceThread)
    {
      val contentImage = ImageUtils.decodeBitmap(File(filePath))
      val contentImage2 =  ImageUtils.decodeBitmap(File(filePath))
      try
      {
        val semanticResult = semanticSegmentation?.execute(contentImage)
        val depthResult = depthEstimationModel?.execute(contentImage2)

        val logResult = StringBuilder()
        logResult.append("DepthResult: ${depthResult?.executionLog}")
        logResult.append("SemanticResult: ${semanticResult?.executionLog}" )

        val generate = RoadSegmentator(semanticResult!!.bitmapResult, depthResult!!.bitmapResult, 7)
        val imageResult = generate.fillTraversableZone(semanticResult!!.bitmapOriginal, 0.001F, 0.001F)

        val result =  ModelViewResult(semanticResult!!.bitmapResult, depthResult!!.bitmapResult, imageResult.first, logResult.toString())
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
