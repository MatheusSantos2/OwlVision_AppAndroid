package Utils

import android.graphics.PointF

class StringHelper
{
    fun convertListInString(points:List<PointF> ) : Array<String>
    {
        val pointsAsString = points.joinToString(",") { "${it.x},${it.y}" }

        return pointsAsString.split(",").toTypedArray()
    }

    fun convertPointsToString(points: List<PointF>): String {
        val stringBuilder = StringBuilder()

        for (point in points)
        {
            stringBuilder.append(point.x)
                    .append(",")
                    .append(point.y)
                    .append(";")
        }

        if (stringBuilder.isNotEmpty()) {
            stringBuilder.deleteCharAt(stringBuilder.length - 1)
        }
        return stringBuilder.toString()
    }
}