package Interpreter.MLSemanticSegmentation

import Interpreter.Models.ModelExecutionResult
import Utils.ImageUtils
import android.content.Context
import android.graphics.Bitmap
import android.os.SystemClock
import android.util.Log
import java.io.FileInputStream
import java.io.IOException
import java.nio.channels.FileChannel
import org.tensorflow.lite.Interpreter
import android.graphics.Color
import androidx.core.graphics.ColorUtils
import org.tensorflow.lite.gpu.GpuDelegate
import java.nio.*
import java.util.*
import kotlin.collections.HashMap


class SemanticSegmentationModelExecutor(context: Context, private var useGPU: Boolean = false)
{
  private var gpuDelegate: GpuDelegate? = null

  private val segmentationMasks: FloatBuffer
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

    val segmentColors = IntArray(NUM_CLASSES)
    val labelsArrays = arrayOf("Unlabeled", "Building", "Fence", "Other", "Pedestrian", "Pole", "RoadLine", "Road", "SideWalk", "Vegetation",
            "Vehicles", "Wall", "TrafficSign", "Sky", "Ground", "Bridge", "RailTrack", "GuardRail", "TrafficLight", "Static", "Dynamic",
            "Water", "Terrain")

    init
    {
      val random = Random(System.currentTimeMillis())
      segmentColors[0] = Color.TRANSPARENT
      for (i in 1 until NUM_CLASSES)
      {
        segmentColors[i] = Color.argb((128), getRandomRGBInt(random), getRandomRGBInt(random), getRandomRGBInt(random))
      }
    }

    private fun getRandomRGBInt(random: Random) = (255 * random.nextFloat()).toInt()
  }

  init
  {
    interpreter = getInterpreter(context, imageSegmentationModel, useGPU)
    segmentationMasks = FloatBuffer.allocate(1 * imageHeightSize * imageWidthSize * NUM_CLASSES)
    segmentationMasks.order()
  }

  @Throws(IOException::class)
  private fun getInterpreter(context: Context, modelName: String, useGpu: Boolean = false): Interpreter
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

  private fun formatExecutionLog(): String {
    val sb = StringBuilder()
    sb.append("Input Image Size: $imageWidthSize x $imageHeightSize\n")
    sb.append("GPU enabled: $useGPU\n")
    sb.append("Number of threads: $numberThreads\n")
    sb.append("Full execution time: $fullTimeExecutionTime ms\n")
    return sb.toString()
  }

  fun close() {
    interpreter.close()
    if (gpuDelegate != null) {
      gpuDelegate!!.close()
    }
  }

  fun execute(data: Bitmap): ModelExecutionResult
  {
    try
    {
      fullTimeExecutionTime = SystemClock.uptimeMillis()

      val scaledBitmap = ImageUtils.scaleBitmapAndKeepRatio(data, imageHeightSize, imageWidthSize)
      val contentArray = ImageUtils.convertBitmapToFloatBuffer(scaledBitmap, imageWidthSize, imageHeightSize, IMAGE_MEAN, IMAGE_STD)

      interpreter.run(contentArray, segmentationMasks)
      val (maskImageApplied, maskOnly, itemsFound) = convertBytebufferMaskToBitmap(segmentationMasks, imageWidthSize, imageHeightSize, scaledBitmap, segmentColors)
      fullTimeExecutionTime = SystemClock.uptimeMillis() - fullTimeExecutionTime

      return ModelExecutionResult(maskImageApplied, scaledBitmap, formatExecutionLog())
    }
    catch (e: Exception)
    {
      val exceptionLog = "something went wrong: ${e.message}"
      Log.d(TAG, exceptionLog)

      val emptyBitmap = ImageUtils.createEmptyBitmap(imageWidthSize, imageHeightSize)
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
    val scaledBackgroundImage = ImageUtils.scaleBitmapAndKeepRatio(backgroundImage, imageHeight, imageWidth)
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
