package Infraestructure.VehicleTrafficZone;

import android.graphics.Bitmap;
import android.graphics.Color;

import java.util.ArrayList;
import java.util.List;

import Infraestructure.Entities.Point2D;
import Infraestructure.Entities.Point3D;

public class TrajectoryGenerator {
    private final int resolution;
    private final int imageWidth; // largura da imagem
    private final int imageHeight; // altura da imagem

    public TrajectoryGenerator(int resolution, int imageWidth, int imageHeight) {
        this.resolution = resolution;
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
    }

    public List<Point3D> generateTrajectory(Bitmap image, List<Point3D> points3D) {
        boolean[][] traversable = getTraversableZone(image);
        List<Point2D> normalizedPoints = getNormalizedPoints(traversable);
        List<Point3D> normalizedPoints3D = convertPoints3D(normalizedPoints, points3D);
        return calculateCentralTrajectory(normalizedPoints3D);
    }

    private boolean[][] getTraversableZone(Bitmap image) {
        boolean[][] traversable = new boolean[imageWidth][imageHeight];
        for (int x = 0; x < imageWidth; x++) {
            for (int y = 0; y < imageHeight; y++) {
                traversable[x][y] = isTraversable(image.getPixel(x, y));
            }
        }
        return traversable;
    }

    private boolean isTraversable(int pixel) {
        return pixel == Color.BLACK;
    }

    private List<Point2D> getNormalizedPoints(boolean[][] traversable) {
        List<Point2D> points = new ArrayList<>();
        for (int x = 0; x < imageWidth; x += resolution) {
            for (int y = 0; y < imageHeight; y += resolution) {
                if (traversable[x][y]) {
                    points.add(new Point2D(x, y));
                }
            }
        }
        return points;
    }

    // converte a lista de pontos normalizados para a lista de pontos 3D
    private List<Point3D> convertPoints3D(List<Point2D> normalizedPoints, List<Point3D> points3D) {
        List<Point3D> normalizedPoints3D = new ArrayList<>();
        for (Point2D point : normalizedPoints) {
            Point3D point3D = getClosestPoint(point, points3D);
            if (point3D != null) {
                normalizedPoints3D.add(point3D);
            }
        }
        return normalizedPoints3D;
    }

    private Point3D getClosestPoint(Point2D point, List<Point3D> points) {
        Point3D closestPoint = null;
        double minDistance = Double.MAX_VALUE;
        for (Point3D p : points) {
            float distance = p.distanceTo(new Point3D(point.getX(), point.getY(), p.getZ()));
            if (distance < minDistance) {
                minDistance = distance;
                closestPoint = p;
            }
        }
        return closestPoint;
    }

    private List<Point3D> calculateCentralTrajectory(List<Point3D> normalizedPoints3D) {
        List<Point3D> centralTrajectory = new ArrayList<>();
        for (int i = 1; i < normalizedPoints3D.size(); i++) {
            Point3D p1 = normalizedPoints3D.get(i - 1);
            Point3D p2 = normalizedPoints3D.get(i);
            Point3D midPoint = new Point3D((float) ((p1.getX() + p2.getX()) / 2.0), (float) ((p1.getY() + p2.getY()) / 2.0), (float) ((p1.getZ() + p2.getZ()) / 2.0));
            centralTrajectory.add(p1);
            centralTrajectory.add(midPoint);
        }
        centralTrajectory.add(normalizedPoints3D.get(normalizedPoints3D.size() - 1));
        return centralTrajectory;
    }
}