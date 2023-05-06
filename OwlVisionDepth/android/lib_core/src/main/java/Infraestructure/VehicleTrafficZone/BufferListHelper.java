package Infraestructure.VehicleTrafficZone;

import android.graphics.PointF;

import org.ejml.simple.SimpleMatrix;

import java.util.ArrayList;
import java.util.List;

public class BufferListHelper {

    public List<PointF> getBufferedPoints(List<PointF> points) {
        List<PointF> bufferedPoints = new ArrayList<>();
        int bufferSize = 50;
        int startIndex = Math.max(0, points.size() - bufferSize); // Início do buffer

        for (int i = startIndex; i < points.size(); i++) {
            int endIndex = Math.min(i + bufferSize, points.size()); // Fim do buffer
            int numPoints = endIndex - i; // Número de pontos no buffer

            float sumX = 0;
            float sumY = 0;

            for (int j = i; j < endIndex; j++) {
                PointF point = points.get(j);
                sumX += point.x;
                sumY += point.y;
            }

            float averageX = sumX / numPoints;
            float averageY = sumY / numPoints;

            bufferedPoints.add(new PointF(averageX, averageY));
        }

        return bufferedPoints;
    }
}
