package Interpreter.MLSemanticSegmentation

import Interpreter.Models.ModelExecutionResult
import Utils.ImageHelper
import Utils.SegmentColors
import android.content.Context
import android.graphics.Bitmap
import android.os.SystemClock
import android.util.Log
import java.io.FileInputStream
import java.io.IOException
import java.nio.channels.FileChannel
import org.tensorflow.lite.Interpreter
import androidx.core.graphics.ColorUtils
import java.nio.*
import java.util.*
import kotlin.collections.HashMap


class SemanticSegmentationModelExecutor(context: Context)
{
  private var outputData: FloatBuffer
  private var inputData: FloatBuffer

  private val interpreter: Interpreter

  private var fullTimeExecutionTime = 0L
  private var numberThreads = 4

  companion object
  {
    public const val TAG = "SegmentationInterpreter"
    private const val imageSegmentationModel = "model_semantic.tflite"
    private const val imageWidthSize = 128
    private const val imageHeightSize = 96
    const val NUM_CLASSES = 23
    private const val IMAGE_MEAN = 127.5f
    private const val IMAGE_STD = 127.5f

    val segmentColors = SegmentColors().getColors()
    val labelsArrays = SegmentColors().getLabels()
  }

  init
  {
    interpreter = getInterpreter(context, imageSegmentationModel)

    outputData = ByteBuffer.allocateDirect(1 * imageHeightSize * imageWidthSize * NUM_CLASSES * 4).apply {
      order(ByteOrder.nativeOrder())
    }.asFloatBuffer()

    inputData = ByteBuffer.allocateDirect(1 * imageHeightSize * imageWidthSize * 3 * 4).apply {
      order(ByteOrder.nativeOrder())
    }.asFloatBuffer()
  }

  @Throws(IOException::class)
  private fun getInterpreter(context: Context, modelName: String): Interpreter
  {
    val tfliteOptions = Interpreter.Options()
    tfliteOptions.setNumThreads(numberThreads)

    return Interpreter(loadModelFile(context, modelName), tfliteOptions)
  }

  private fun formatExecutionLog(): String {
    val sb = StringBuilder()
    sb.append("Input Image Size: $imageWidthSize x $imageHeightSize\n")
    sb.append("Number of threads: $numberThreads\n")
    sb.append("Full execution time: $fullTimeExecutionTime ms\n")
    return sb.toString()
  }

  fun close() {
    interpreter.close()
  }

  fun execute(data: Bitmap): ModelExecutionResult
  {
    try
    {
      fullTimeExecutionTime = SystemClock.uptimeMillis()

      val scaledBitmap = ImageHelper.scaleBitmapAndKeepRatio(data, imageHeightSize, imageWidthSize)
      val inputArray = ImageHelper.bitmapToArray(scaledBitmap)

      inputData.rewind()
      inputData.put(inputArray)

      interpreter.run(inputData, outputData)

      val (maskImageApplied, maskOnly, itemsFound) = convertBytebufferMaskToBitmap(outputData, imageWidthSize, imageHeightSize, scaledBitmap, segmentColors)
      fullTimeExecutionTime = SystemClock.uptimeMillis() - fullTimeExecutionTime

      return ModelExecutionResult(maskImageApplied, scaledBitmap, formatExecutionLog())
    }
    catch (e: Exception)
    {
      val exceptionLog = "something went wrong: ${e.message}"
      Log.d(TAG, exceptionLog)

      val emptyBitmap = ImageHelper.createEmptyBitmap(imageWidthSize, imageHeightSize)
      return ModelExecutionResult(emptyBitmap, emptyBitmap, exceptionLog)
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

  private fun convertBytebufferMaskToBitmap(inputBuffer: FloatBuffer, imageWidth: Int, imageHeight: Int, backgroundImage: Bitmap, colors: IntArray):
          Triple<Bitmap, Bitmap, Map<String, Int>>
  {
    val conf = Bitmap.Config.ARGB_8888
    val maskBitmap = Bitmap.createBitmap(imageWidth, imageHeight, conf)
    val resultBitmap = Bitmap.createBitmap(imageWidth, imageHeight, conf)
    val scaledBackgroundImage = ImageHelper.scaleBitmapAndKeepRatio(backgroundImage, imageHeight, imageWidth)
    val mSegmentBits = Array(imageWidth) { IntArray(imageHeight) }
    val itemsFound = HashMap<String, Int>()
    inputBuffer.rewind()

    for (y in 0 until imageHeight)
    {
      for (x in 0 until imageWidth)
      {
        var maxVal = 0f
        mSegmentBits[x][y] = 0

        for (c in 0 until NUM_CLASSES)
        {
          val value = inputBuffer.get((y * imageWidth * NUM_CLASSES + x * NUM_CLASSES + c))
          if (c == 0 || value > maxVal)
          {
            maxVal = value
            mSegmentBits[x][y] = c
          }
        }
        val label = labelsArrays[mSegmentBits[x][y]]
        val color = colors[mSegmentBits[x][y]]
        itemsFound.put(label, color)
        val newPixelColor = ColorUtils.compositeColors(colors[mSegmentBits[x][y]], scaledBackgroundImage.getPixel(x, y))
        resultBitmap.setPixel(x, y, newPixelColor)
        maskBitmap.setPixel(x, y, colors[mSegmentBits[x][y]])
      }
    }

    return Triple(resultBitmap, maskBitmap, itemsFound)
  }
}
