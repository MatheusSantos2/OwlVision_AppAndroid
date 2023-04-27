package MLDepthEstimation

import Models.ModelExecutionResult
import android.content.Context
import android.graphics.Bitmap
import android.os.SystemClock
import android.util.Log
import java.io.FileInputStream
import java.io.IOException
import java.nio.channels.FileChannel
import org.tensorflow.lite.Interpreter
import Utils.ImageUtils
import org.tensorflow.lite.gpu.GpuDelegate
import java.nio.*

class DepthEstimationModelExecutor(context: Context, private var useGPU: Boolean = false)
{
  private var gpuDelegate: GpuDelegate? = null
  private var fullTimeExecutionTime = 0L
  private var numberThreads = 4
  private val depthMasks: FloatBuffer
  private val interpreter: Interpreter

  companion object
  {
    public const val TAG = "DepthInterpreter"
    private const val depthEstimationModel = "model_depth.tflite"
    private const val imageInputSizeWidth = 640
    private const val imageInputSizeHeight = 192
    private const val imageOutputSizeWidth = 160
    private const val imageOutputSizeHeight = 48
    const val NUM_CLASSES = 19
  }

  init
  {
    interpreter = getInterpreter(context, depthEstimationModel, useGPU)
    depthMasks = FloatBuffer.allocate(1 * imageOutputSizeHeight * imageOutputSizeWidth)
    depthMasks.order()
  }

  @Throws(IOException::class)
  private fun getInterpreter(context: Context, modelName: String, useGpu: Boolean = false): Interpreter
  {
      try
      {
          val tfliteOptions = Interpreter.Options()
          tfliteOptions.setNumThreads(numberThreads)

          gpuDelegate = null
          if (useGpu)
          {
              gpuDelegate = GpuDelegate()
              tfliteOptions.addDelegate(gpuDelegate)
          }
          return Interpreter(loadModelFile(context, modelName), tfliteOptions)
      }
      catch (e: Exception)
      {
          Log.e(TAG, "Fail to create Interpreter: ${e.message}")
          throw e
      }
  }

  @Throws(IOException::class)
  private fun loadModelFile(context: Context, modelFile: String): MappedByteBuffer
  {
    val fileDescriptor = context.assets.openFd(modelFile)
    val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
    val fileChannel = inputStream.channel
    val startOffset = fileDescriptor.startOffset
    val declaredLength = fileDescriptor.declaredLength
    val retFile = fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    fileDescriptor.close()
    return retFile
  }

  fun close()
  {
    interpreter.close()
    if (gpuDelegate != null)
    {
      gpuDelegate!!.close()
    }
  }

  fun execute(data: Bitmap): ModelExecutionResult
  {
    try
    {
      fullTimeExecutionTime = SystemClock.uptimeMillis()
      val scaledBitmap = ImageUtils.scaleBitmapAndKeepRatio(data, imageInputSizeHeight, imageInputSizeWidth)
      val contentArray = ImageUtils.convertBitmapToFloatBuffer(scaledBitmap,imageInputSizeWidth, imageInputSizeHeight)

      interpreter.run(contentArray, depthMasks)
      val outputBitmap = ImageUtils.convertFloatBufferToBitmap(depthMasks, imageOutputSizeWidth, imageOutputSizeHeight)
      val outputBitmapResized = ImageUtils.scaleBitmapAndKeepRatio(outputBitmap, imageInputSizeHeight, imageInputSizeWidth)

      fullTimeExecutionTime = SystemClock.uptimeMillis() - fullTimeExecutionTime
      Log.d(TAG, "Total time execution $fullTimeExecutionTime")

      return ModelExecutionResult(outputBitmapResized, scaledBitmap, formatExecutionLog())
    }
    catch (e: Exception)
    {
      val exceptionLog = "something went wrong: ${e.message}"
      Log.d(TAG, exceptionLog)

      val emptyBitmap = ImageUtils.createEmptyBitmap(imageInputSizeWidth, imageInputSizeHeight)
      return ModelExecutionResult(emptyBitmap, emptyBitmap, exceptionLog)
    }
  }

  private fun formatExecutionLog(): String
  {
    val sb = StringBuilder()
    sb.append("Input Image Size: $imageInputSizeWidth x $imageInputSizeHeight\n")
    sb.append("GPU enabled: $useGPU\n")
    sb.append("Number of threads: $numberThreads\n")
    sb.append("Full execution time: $fullTimeExecutionTime ms\n")
    return sb.toString()
  }
}
