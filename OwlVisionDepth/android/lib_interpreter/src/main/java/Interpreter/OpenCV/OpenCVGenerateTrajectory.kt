package Interpreter.OpenCV

import android.graphics.PointF
import org.opencv.core.MatOfPoint2f
import org.opencv.core.Point
import org.opencv.imgproc.Imgproc

class OpenCVGenerateTrajectory {
    fun generateTrajectory(points: List<PointF>): List<PointF> {
        val trajectory = mutableListOf<PointF>()

        val contours = points.map { MatOfPoint2f(Point(it.x.toDouble(), it.y.toDouble())) }

        for (contour in contours) {
            val centroid = calculateCentroid(contour)
            trajectory.add(centroid)
        }

        return trajectory
    }

    private fun calculateCentroid(contour: MatOfPoint2f): PointF {
        // Calcular o momento do contorno
        val moments = Imgproc.moments(contour)
        val cX = moments.m10 / moments.m00
        val cY = moments.m01 / moments.m00

        return PointF(cX.toFloat(), cY.toFloat())
    }
}