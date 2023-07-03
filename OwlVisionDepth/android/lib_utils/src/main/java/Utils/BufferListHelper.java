package Utils;

import android.graphics.PointF;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public class BufferListHelper {

    DecimalFormat decimalFormat = new DecimalFormat("#.##");

    public List<PointF> getBufferedPoint(List<PointF> points)
    {
        try{
            return getBufferedPoints(points);
        }catch (ParseException e) {
            e.printStackTrace();
        }
        return points;
    }

    private List<PointF> getBufferedPoints(List<PointF> points) throws ParseException {
        List<PointF> bufferedPoints = new ArrayList<>();
        int bufferSize = 5;
        int startIndex = Math.max(0, points.size() - bufferSize);

        for (int i = startIndex; i < points.size(); i++) {
            int endIndex = Math.min(i + bufferSize, points.size());
            int numPoints = endIndex - i;

            float sumX = 0;
            float sumY = 0;

            for (int j = i; j < endIndex; j++) {
                PointF point = points.get(j);
                sumX += point.x;
                sumY += point.y;
            }

            float averageX = 0;
            float averageY = 0;

            if (numPoints > 0) {
                averageX = sumX / numPoints;
                averageY = sumY / numPoints;
            }

            averageX =  decimalFormat.parse(decimalFormat.format(averageX)).floatValue();
            averageY =  decimalFormat.parse(decimalFormat.format(averageY)).floatValue();

            bufferedPoints.add(new PointF(averageX, averageY));
        }

        return bufferedPoints;
    }
}
