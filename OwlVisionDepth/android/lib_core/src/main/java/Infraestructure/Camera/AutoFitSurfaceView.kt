package Infraestructure.Camera

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.SurfaceView
import kotlin.math.roundToInt

class AutoFitSurfaceView(
  context: Context,
  attrs: AttributeSet? = null,
  defStyle: Int = 0
) : SurfaceView(context, attrs, defStyle)
{

  private var aspectRatio = 0f
  private var widthDiff = 0
  private var heightDiff = 0
  private var requestLayout = false

  fun setAspectRatio(width: Int, height: Int) {
    require(width > 0 && height > 0) { "Size cannot be negative" }
    aspectRatio = width.toFloat() / height.toFloat()
    requestLayout()
  }

  override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
    val width = MeasureSpec.getSize(widthMeasureSpec)
    val height = MeasureSpec.getSize(heightMeasureSpec)
    if (aspectRatio == 0f)
    {
      setMeasuredDimension(width, height)
    }
    else
    {
      // Performs center-crop transformation of the camera frames
      val newWidth: Int
      val newHeight: Int
      if (width < height * aspectRatio)
      {
        newHeight = height
        newWidth = (height / aspectRatio).roundToInt()
      }
      else
      {
        newWidth = width
        newHeight = (width / aspectRatio).roundToInt()
      }

      Log.d(TAG, "Measured dimensions set: $newWidth x $newHeight")
      widthDiff = width - newWidth
      heightDiff = height - newHeight
      requestLayout = true
      setMeasuredDimension(newWidth, newHeight)
    }
  }

  override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
    if (requestLayout) {
      requestLayout = false
      layout(
        widthDiff / 2,
        heightDiff / 2,
        right + (widthDiff / 2),
        bottom + (heightDiff / 2)
      )
    }
    super.onLayout(changed, left, top, right, bottom)
  }

  companion object {
    private val TAG = AutoFitSurfaceView::class.java.simpleName
  }
}