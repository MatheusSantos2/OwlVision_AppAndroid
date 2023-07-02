package Infraestructure.VehicleTrafficZone;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import Models.Node;
import Utils.RTTHelper;

public class RRT {
    private Node root;
    private double stepSize;
    private double goalThreshold;
    private List<Node> tree;
    private double vehicleWidth;
    private double vehicleHeight;
    private int imageWidth;
    private int imageHeight;
    private int[][] imagePixels;
    private int vehicleColor = Color.BLUE;
    private int targetColor = Color.GREEN;

    private RTTHelper rrtHelper = new RTTHelper();
    private double proximityRadius = 10;

    public RRT(double stepSize, double goalThreshold, double vehicleWidth, double vehicleHeight)
    {
        this.stepSize = stepSize;
        this.goalThreshold = goalThreshold;
        this.vehicleWidth = vehicleWidth;
        this.vehicleHeight = vehicleHeight;
    }

    public Pair<List<Node>, Bitmap> findPath(Bitmap image, Node root, Node target, int maxIterations) {
        this.root = root;
        this.tree = new ArrayList<>();
        this.tree.add(root);

        Bitmap copiedImage = Bitmap.createBitmap(image);
        int rootX = (int) this.root.getX();
        int rootY = (int) this.root.getY();
        rrtHelper.paintRectangle(copiedImage, rootX, rootY, (int) this.vehicleWidth, (int) this.vehicleHeight, vehicleColor);
        rrtHelper.paintRectangle(copiedImage, (int) target.getX(), (int) target.getY(), (int) this.vehicleWidth, (int) this.vehicleHeight, targetColor);

        Node nearestNode;
        Node newNode;
        Node goal = null;

        initializeImage(image);

        for (int i = 0; i < maxIterations; i++) {
            Node randomNode = getRandomNode();
            nearestNode = findNearestNode(randomNode);
            newNode = steer(nearestNode, randomNode);

            if (isValidNode(newNode)) {
                newNode.setParent(nearestNode);
                tree.add(newNode);

                copiedImage.setPixel((int) newNode.getX(), (int) newNode.getY(), Color.BLACK);

                double distanceToTarget = calculateDistance(newNode, target);
                if (distanceToTarget <= proximityRadius) {
                    goal = target;

                    break;
                }
            }
        }

        if (goal != null) {
            return new Pair<>(getPathFromRoot(goal), copiedImage) ;
        }

        return null;
    }

    private void initializeImage(Bitmap image)
    {
        this.imageWidth = image.getWidth();
        this.imageHeight = image.getHeight();
        this.imagePixels = new int[imageWidth][imageHeight];

        for (int x = 0; x < imageWidth; x++) {
            for (int y = 0; y < imageHeight; y++) {
                imagePixels[x][y] = image.getPixel(x, y);
            }
        }
    }

    private Node getRandomNode() {
        int x = randomInt(0, imageWidth - 1);
        int y = randomInt(0, imageHeight - 1);
        return new Node(x, y);
    }

    private Node findNearestNode(Node targetNode) {
        Node nearestNode = new Node(0,0);
        double minDistance = Double.MAX_VALUE;

        for (Node node : tree) {
            double distance = calculateDistance(node, targetNode);
            if (distance < minDistance) {
                nearestNode = node;
                minDistance = distance;
            }
        }

        return nearestNode;
    }

    private Node steer(Node fromNode, Node toNode) {
        double distance = calculateDistance(fromNode, toNode);
        if (distance <= stepSize) {
            return toNode;
        }

        double theta = Math.atan2(toNode.getY() - fromNode.getY(), toNode.getX() - fromNode.getX());
        int newX = (int) Math.round(fromNode.getX() + stepSize * Math.cos(theta));
        int newY = (int) Math.round(fromNode.getY() + stepSize * Math.sin(theta));

        return new Node(newX, newY);
    }

    private boolean isValidNode(Node node) {
        if (!isWithinBounds((int)node.getX(), (int)node.getY())) {
            return false;
        }

        double halfWidth = vehicleWidth / 4.0;
        double halfHeight = vehicleHeight / 4.0;

        if (!isWhitePixel((int) node.getX(), (int) node.getY(), (int)halfWidth, (int)halfHeight)) {
            return false;
        }

        return true;
    }


    private boolean isWithinBounds(int x, int y) {
        return x >= 0 && x < imageWidth && y >= 0 && y < imageHeight;
    }

    private boolean isWhitePixel(int x, int y, double halfWidth, double halfHeight) {
        int startX = (int) (x - halfWidth);
        int endX = (int) (x + halfWidth);
        int startY = (int) (y - halfHeight);
        int endY = (int) (y + halfHeight);

        for (int i = startX; i <= endX; i++) {
            for (int j = startY; j <= endY; j++) {
                if (i >= 0 && i < imageWidth && j >= 0 && j < imageHeight) {
                    int pixel = imagePixels[i][j];
                    if (!isWhite(pixel)) {
                        return false;
                    }
                } else {
                    return false;
                }
            }
        }

        return true;
    }

    private boolean isWhite(int pixel) {
        int red = Color.red(pixel);
        int green = Color.green(pixel);
        int blue = Color.blue(pixel);
        return red == 255 && green == 255 && blue == 255;
    }

    private List<Node> getPathFromRoot(Node node) {
        List<Node> path = new ArrayList<>();

        while (node != null) {
            path.add(node);
            node = node.getParent();
        }

        return path;
    }

    private boolean isReachedGoal(Node node, Node target) {
        double distance = calculateDistance(node, target);
        return distance <= goalThreshold;
    }

    private int randomInt(int min, int max) {
        Random random = new Random();
        return random.nextInt(max - min + 1) + min;
    }

    public List<Node> equidistantPath(List<Node> nodes, int numPoints) {
        List<Node> equidistantNodes = new ArrayList<>();

        if (nodes.size() < 2) {
            return equidistantNodes;
        }

        double totalDistance = 0.0;
        for (int i = 0; i < nodes.size() - 1; i++) {
            totalDistance += calculateDistance(nodes.get(i), nodes.get(i + 1));
        }

        double targetSpacing = totalDistance / (numPoints - 1);

        equidistantNodes.add(nodes.get(0));

        int currentNodeIndex = 0;
        double currentDistance = 0.0;
        for (int i = 1; i < numPoints - 1; i++) {
            double targetDistance = i * targetSpacing;

            while (currentDistance < targetDistance) {
                Node currentNode = nodes.get(currentNodeIndex);
                Node nextNode = nodes.get(currentNodeIndex + 1);
                double segmentDistance = calculateDistance(currentNode, nextNode);

                if (currentDistance + segmentDistance <= targetDistance) {
                    currentDistance += segmentDistance;
                    currentNodeIndex++;
                } else {
                    double remainingDistance = targetDistance - currentDistance;
                    double ratio = remainingDistance / segmentDistance;
                    double newX = currentNode.getX() + ratio * (nextNode.getX() - currentNode.getX());
                    double newY = currentNode.getY() + ratio * (nextNode.getY() - currentNode.getY());
                    Node equidistantNode = new Node(newX, newY);
                    equidistantNodes.add(equidistantNode);
                    currentDistance = targetDistance;
                }
            }
        }

        equidistantNodes.add(nodes.get(nodes.size() - 1));

        return equidistantNodes;
    }

    private double calculateDistance(Node node1, Node node2) {
        double dx = node2.getX() - node1.getX();
        double dy = node2.getY() - node1.getY();
        return Math.sqrt(dx * dx + dy * dy);
    }
}