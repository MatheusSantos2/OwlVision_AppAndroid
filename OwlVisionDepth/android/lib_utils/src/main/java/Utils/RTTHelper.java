package Utils;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PointF;
import android.util.Pair;

import org.opencv.core.Point;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import Models.Node;
import Models.Point3D;

public class RTTHelper {

    private int nRow = 0;

    public Node getMidpointOfFirstWhiteLine(Bitmap image) throws Exception {
        int imageWidth = image.getWidth();
        int imageHeight = image.getHeight();

        int midpointX = -1;
        int midpointY = -1;
        boolean foundWhiteLine = false;

        for (int y = imageHeight - 1; y >= 0; y--) {
            for (int x = imageWidth/2; x < imageWidth; x++) {
                int pixel = image.getPixel(x, y);
                if (isWhitePixel(pixel)) {
                    double whitePercentageLeft = calculateWhitePercentage(image, x - 1, y);
                    double whitePercentageRight = calculateWhitePercentage(image, x + 1, y);

                    if (whitePercentageLeft >= 0.2 && whitePercentageRight >= 0.2) {
                        midpointX = x;
                        midpointY = y;
                        foundWhiteLine = true;
                        break;
                    }
                }
            }
            if (foundWhiteLine) {
                break;
            }
        }

        if (midpointX != -1 && midpointY != -1) {
            Node midpoint = new Node(midpointX, midpointY);
            return midpoint;
        } else {
            throw new Exception("Nenhuma linha branca encontrada na imagem com vizinhança adequada.");
        }
    }

    private boolean isWhitePixel(int pixel) {
        int red = Color.red(pixel);
        int green = Color.green(pixel);
        int blue = Color.blue(pixel);
        int whiteThreshold = 100;

        return (red >= whiteThreshold) && (green >= whiteThreshold) && (blue >= whiteThreshold);
    }
    public Node getMidpointOfFirstWhiteLineTop(Bitmap image) throws Exception {
        int imageWidth = image.getWidth();
        int imageHeight = image.getHeight();

        int midpointX = -1;
        int midpointY = -1;
        boolean foundWhiteLine = false;

        for (int y = 0; y < imageHeight/2; y++) {
            for (int x = imageWidth/2; x < imageWidth; x++) {
                int pixel = image.getPixel(x, y);
                if (isWhitePixel(pixel)) {
                    double whitePercentageLeft = calculateWhitePercentage(image, x - 1, y);
                    double whitePercentageRight = calculateWhitePercentage(image, x + 1, y);

                    if (whitePercentageLeft >= 0.2 && whitePercentageRight >= 0.2) {
                        boolean hasMagentaColor = hasMagentaColorInRadius(image, x, y, 10);
                        if (!hasMagentaColor) {
                            midpointX = x;
                            midpointY = y;
                            foundWhiteLine = true;
                            break;
                        }
                    }
                }
            }
            if (foundWhiteLine) {
                break;
            }
        }

        if (midpointX != -1 && midpointY != -1) {
            Node midpoint = new Node(midpointX, midpointY);
            return midpoint;
        } else {
            throw new Exception("Nenhuma linha branca encontrada no topo da imagem com vizinhança adequada.");
        }
    }

    private boolean hasMagentaColorInRadius(Bitmap image, int x, int y, int radius) {
        int imageWidth = image.getWidth();
        int imageHeight = image.getHeight();

        for (int i = -radius; i <= radius; i++) {
            for (int j = -radius; j <= radius; j++) {
                int nx = x + i;
                int ny = y + j;

                if (nx >= 0 && nx < imageWidth && ny >= 0 && ny < imageHeight) {
                    int pixel = image.getPixel(nx, ny);
                    if (pixel == Color.MAGENTA) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private double calculateWhitePercentage(Bitmap image, int x, int y) {
        int whiteCount = 0;
        int totalCount = 0;
        int radius = 1;

        int imageWidth = image.getWidth();
        int imageHeight = image.getHeight();

        for (int i = -radius; i <= radius; i++) {
            for (int j = -radius; j <= radius; j++) {
                int nx = x + i;
                int ny = y + j;

                if (nx >= 0 && nx < imageWidth && ny >= 0 && ny < imageHeight) {
                    int pixel = image.getPixel(nx, ny);
                    if (isWhitePixel(pixel)) {
                        whiteCount++;
                    }
                    totalCount++;
                }
            }
        }

        return (double) whiteCount / totalCount;
    }

    public List<PointF> convertNodesToPositions(List<Node> nodeList, List<Pair<Boolean, Point3D>> positions) {
        List<PointF> positionList = new ArrayList<>();

        float xMin = Float.MAX_VALUE;
        float xMax = Float.MIN_VALUE;
        float zMin = Float.MAX_VALUE;
        float zMax = Float.MIN_VALUE;

        for (Pair<Boolean, Point3D> entry : positions) {
            Point3D point = entry.second;
            float x = point.getX();
            float z = point.getZ();

            xMin = Math.min(xMin, x);
            xMax = Math.max(xMax, x);
            zMin = Math.min(zMin, z);
            zMax = Math.max(zMax, z);
        }

        for (Node node : nodeList) {
            float x = (float) node.getX();
            float z = (float) node.getY();

            float denormalizedX = x * (xMax - xMin) + xMin;
            float denormalizedZ = z * (zMax - zMin) + zMin;

            PointF position = new PointF(denormalizedX, denormalizedZ);
            positionList.add(position);
        }

        return positionList;
    }

    public Pair<Double, Double> getVehicleSize(double imageWidht, double imageHeight, double sceneWidth, double sceneHeight, double vehicleWidth, double vehicleHeight) {
        double scaleX = vehicleWidth / sceneWidth;
        double scaleY = vehicleHeight / sceneHeight;

         vehicleWidth = scaleX * imageWidht;
         vehicleHeight = scaleY * imageHeight;

        return new Pair<>(vehicleWidth, vehicleHeight);
    }

    public void paintRectangle(Bitmap image, int x, int y, int width, int height, int color) {
        for (int i = x; i < x + width; i++) {
            for (int j = y; j < y + height; j++) {
                if (i >= 0 && i < image.getWidth() && j >= 0 && j < image.getHeight()) {
                    image.setPixel(i, j, color);
                }
            }
        }
    }

    public List<Node> getEquidistantNodes(List<Node> nodes, int numPoints) {
        List<Node> equidistantNodes = new ArrayList<>();

        if (nodes.isEmpty()) {
            return equidistantNodes;
        }

        Node root = nodes.get(0);
        Node goal = nodes.get(nodes.size() - 1);
        double totalDistance = calculateDistance(root, goal);

        double spacing = totalDistance / (numPoints - 1);
        double currentDistance = 0.0;

        equidistantNodes.add(root);

        Node currentNode = goal;
        double distanceFromGoal = 0.0;

        while (currentNode.getParent() != null) {
            Node parent = currentNode.getParent();
            double segmentDistance = calculateDistance(currentNode, parent);

            if (currentDistance + segmentDistance >= spacing) {
                double remainingDistance = spacing - currentDistance;
                double ratio = remainingDistance / segmentDistance;
                double newX = parent.getX() + ratio * (currentNode.getX() - parent.getX());
                double newY = parent.getY() + ratio * (currentNode.getY() - parent.getY());
                Node equidistantNode = new Node(newX, newY);
                equidistantNodes.add(equidistantNode);
                currentDistance = spacing;
            }

            currentDistance += segmentDistance;
            distanceFromGoal += segmentDistance;

            if (distanceFromGoal >= spacing) {
                equidistantNodes.add(currentNode);
                distanceFromGoal = 0.0;
            }

            currentNode = parent;
        }

        equidistantNodes.add(goal);

        return equidistantNodes;
    }

    public double calculateDistance(Node node1, Node node2) {
        double dx = node2.getX() - node1.getX();
        double dy = node2.getY() - node1.getY();
        return Math.sqrt(dx * dx + dy * dy);
    }
}
