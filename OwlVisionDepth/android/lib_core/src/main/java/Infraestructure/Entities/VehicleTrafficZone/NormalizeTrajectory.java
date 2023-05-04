package Infraestructure.Entities.VehicleTrafficZone;

import android.graphics.Bitmap;

import java.util.ArrayList;
import java.util.List;

import Infraestructure.Entities.Point2D;
import Infraestructure.Entities.Point3D;

public class NormalizeTrajectory {

    private float focalLength;
    private float realObjectSize;

    public NormalizeTrajectory(float focalLength, float realObjectSize) {
        this.focalLength = focalLength;
        this.realObjectSize = realObjectSize;
    }

    public List<Point3D> getNormalizedPoints(List<Point2D> points2D, Bitmap depthMap) {

        List<Point3D> normalizedPoints = new ArrayList<>();

        int width = depthMap.getWidth();
        int height = depthMap.getHeight();

        for (Point2D point2D : points2D) {
            int x = (int) point2D.getX();
            int y = (int) point2D.getY();

            if (x < 0 || x >= width || y < 0 || y >= height) {
                continue;
            }

            int depthColor = depthMap.getPixel(x, y);
            float depth = ((depthColor & 0x000000ff)) / 255.0f;
            float scale = realObjectSize / (focalLength * depth);
            float realX = (x - width / 2) * scale;
            float realY = (y - height / 2) * scale;
            float realZ = depth * scale;
            normalizedPoints.add(new Point3D(realX, realY, realZ));
        }

        return normalizedPoints;
    }
}