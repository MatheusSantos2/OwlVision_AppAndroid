package Interpreter.OpenCV

import android.graphics.PointF

class TrajectoryGenerator {
    fun generateTrajectory(points: List<PointF>): List<PointF> {
        val sortedPoints = points.sortedBy { it.x }

        val trajectory = mutableListOf<PointF>()

        for (i in 0 until sortedPoints.size - 1) {
            val currentPoint = sortedPoints[i]
            val nextPoint = sortedPoints[i + 1]

            val midPointX = (currentPoint.x + nextPoint.x) / 2
            val midPointZ = (currentPoint.y + nextPoint.y) / 2

            val midPoint = PointF(midPointX, midPointZ)
            trajectory.add(midPoint)
        }

        return trajectory
    }
}