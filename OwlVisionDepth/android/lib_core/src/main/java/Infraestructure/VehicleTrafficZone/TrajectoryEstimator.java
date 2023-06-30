package Infraestructure.VehicleTrafficZone;

import android.graphics.Bitmap;
import android.graphics.PointF;
import android.util.Pair;

import java.util.ArrayList;
import java.util.List;

import Models.Node;
import Models.Point2D;
import Models.Point3D;
import Utils.ImageGenerator;
import Utils.Point3DHelper;
import Utils.RTTHelper;
import Utils.SegmentColors;

public class TrajectoryEstimator extends SegmentColors {
    protected Bitmap semanticMap;
    protected Bitmap depthMap;
    private double stepSize = 1.0;
    private double goalThreshold = 5.0;
    private double vehicleWidth = 15F;
    private double vehicleHeight = 18F;

    double maxSteeringAngle = Math.toRadians(30);
    double maxSpeed = 10.0;

    private DistanceEstimator distanceEstimator = new DistanceEstimator();
    private ImageGenerator imageGenerator = new ImageGenerator();
    private Point3DHelper pointHelper = new Point3DHelper();
    private RTTHelper rrtHelper = new RTTHelper();
    private RRT rrt = new RRT(stepSize, goalThreshold, vehicleWidth,  vehicleHeight);

    private int roadColor = getColorForLabel("Road");
    private boolean isTraversable = true;

    public Pair<Bitmap, List<PointF>> getTraversableZone(Bitmap semanticMap, Bitmap depthMap ) {
        this.semanticMap = semanticMap;
        this.depthMap = depthMap;

        Bitmap resizedSemantic = imageGenerator.resizeBitmap(semanticMap, 192, 640);
        Bitmap resizedDepth = imageGenerator.resizeBitmap(depthMap, 192, 640);

         int width = resizedSemantic.getWidth();
        int height = resizedSemantic.getHeight();

        Pair<Boolean, Point3D> pairCoordinates;
        List<Pair<Boolean, Point3D>> coordinateDictionary = new ArrayList<>();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {

                Point2D pointPixel = new Point2D(x, y);
                int semanticPixel = resizedSemantic.getPixel(x, y);

                int depthPixel = resizedDepth.getPixel(x, y);
                Point3D realCoordinates = distanceEstimator.
                        getRealPositions(pointPixel, depthPixel, width, height);

                if (semanticPixel == roadColor) {
                    pairCoordinates = new Pair<>(isTraversable, realCoordinates);
                }else {
                    pairCoordinates = new Pair(!isTraversable, realCoordinates);
                }

                coordinateDictionary.add(pairCoordinates);
            }
        }

        Bitmap newImage = imageGenerator.createBitmapImageXZ(coordinateDictionary, width, 200);

        return new Pair<>(newImage, generateListTrajectory(newImage, coordinateDictionary));
    }

    List<PointF> generateListTrajectory(Bitmap image, List<Pair<Boolean, Point3D>> coordinateDictionary){
        try {
            Node root = rrtHelper.getRoot(image);
            Node goal = rrtHelper.getGoal(image, vehicleWidth, vehicleHeight);

            List<Node> nodeList = rrt.findPath(image,root, goal, 100);

            return rrtHelper.convertNodesToPositions(nodeList, coordinateDictionary);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }
}