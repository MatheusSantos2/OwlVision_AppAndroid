package Utils;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.util.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import Models.Node;
import Models.Point3D;

public class RTTHelper {

    private int nRow = 0;

    public Node getRoot(Bitmap image) throws Exception {
        int imageWidth = image.getWidth();
        int imageHeight = image.getHeight();

        int midpointX = -1;
        int midpointY = -1;
        nRow++;

        if (nRow < imageHeight)
        {
            int y = imageHeight - nRow;

            for (int x = 0; x < imageWidth; x++) {
                int pixel = image.getPixel(x, y);
                if (isWhitePixel(pixel)) {
                    midpointX = x;
                    midpointY = y;
                    break;
                }
            }

            if (midpointX != -1 && midpointY != -1) {
                nRow = 0;
                Node firstNode = new Node(midpointX, midpointY);
                return firstNode;
            }
            else{
                return getRoot(image);
            }
        }else{
            nRow = 0;
            throw new Exception("Sem Linhas Brancas na Imagem");
        }
    }

    private boolean isWhitePixel(int pixel) {
        int red = (pixel >> 16) & 0xFF;
        int green = (pixel >> 8) & 0xFF;
        int blue = pixel & 0xFF;
        return red == 255 && green == 255 && blue == 255;
    }


    public Node getGoal(Bitmap image, double vehicleWidth, double vehicleHeight) {
        int numRows = image.getHeight();
        int numColumns = image.getWidth();

        for (int row = numRows - 1; row >= 0; row--) {
            for (int col = 0; col < numColumns; col++) {
                int pixel = image.getPixel(col, row);
                boolean isWhitePixel = isWhitePixel(pixel);

                if (isWhitePixel) {
                    double x = (double) col;
                    double y = (double) row;

                    x -= vehicleWidth / 2.0;
                    y -= vehicleHeight / 2.0;

                    return new Node(x, y);
                }
            }
        }

        return null;
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
}
