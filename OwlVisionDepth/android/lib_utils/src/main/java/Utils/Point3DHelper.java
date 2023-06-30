package Utils;

import android.util.Pair;

import java.util.List;

import Models.Point3D;

public class Point3DHelper {
    public static Point3D findMaxPoint(List<Point3D> points) {
        if (points.isEmpty()) {
            return null;
        }

        float maxXPoint = points.get(0).getX();
        float maxYPoint = points.get(0).getY();
        float maxZPoint = points.get(0).getY();

        for (Point3D point : points) {
            if (point.getX() > maxXPoint) {
                maxXPoint = point.getX();
            }
            if (point.getY() > maxYPoint) {
                maxYPoint = point.getY();
            }
            if(point.getZ() > maxZPoint){
                maxZPoint = point.getZ();
            }
        }

        return new Point3D(maxXPoint, maxYPoint, maxZPoint);
    }

    public static Point3D findMinPoint(List<Point3D> points) {
        if (points.isEmpty()) {
            return null;
        }

        float minXPoint = points.get(0).getX();
        float minYPoint = points.get(0).getY();
        float minZPoint = points.get(0).getY();

        for (Point3D point : points) {
            if (point.getX() < minXPoint) {
                minXPoint = point.getX();
            }
            if (point.getY() < minYPoint) {
                minYPoint = point.getY();
            }
            if (point.getZ() < minZPoint) {
                minZPoint = point.getZ();
            }
        }

        return new Point3D(minXPoint, minYPoint, minZPoint);
    }

    public Pair<Integer, Integer> generateDimensions(List<Point3D> points){
        Point3D maxPoint = findMaxPoint(points);
        Point3D minPoint = findMinPoint(points);

        double minX = minPoint.getX();
        double maxX = maxPoint.getX();
        double minY = minPoint.getY();
        double maxY = maxPoint.getY();
        double minZ = minPoint.getZ();
        double maxZ = maxPoint.getZ();

        double centerX = (maxX + minX) / 2;

        int imageWidth = (int) Math.ceil(Math.max(Math.abs(maxX - centerX), Math.abs(minX - centerX))) * 2;
        int imageLenght = (int) Math.ceil(maxY - minY);
        int imageHeight = (int) Math.ceil(maxZ - minZ);

        return new Pair<>(imageWidth,imageHeight);
    }
}
