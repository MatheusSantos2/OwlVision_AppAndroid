package Infraestructure.VehicleTrafficZone;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PointF;
import android.util.Pair;

import java.util.ArrayList;
import java.util.List;

import Utils.SegmentColors;

public class RoadSegmentator extends SegmentColors {
    protected Bitmap semanticMap;
    protected Bitmap depthMap;
    protected int roadLabel;
    protected float minDepth;
    protected float maxDepth;
    protected float focalLength = 0.0028F;
    protected float realObjectSize = 32.085F;
    protected float referenceObjectSizeInImage = 1080;
    protected float distanceToObject=50F;

    protected float minDepthLimit;
    protected float maxDepthLimit;

    public RoadSegmentator(Bitmap semanticMap, Bitmap depthMap, int roadLabel) {
        this.semanticMap = semanticMap;
        this.depthMap = depthMap;
        this.roadLabel = roadLabel;
        calculateDepthLimits();
    }

    public Pair<Bitmap, List<PointF>> fillTraversableZone(Bitmap originalImage, float vehicleWidth, float vehicleLength) {
        int width = semanticMap.getWidth();
        int height = semanticMap.getHeight();
        List<PointF> points = new ArrayList<>();

        Bitmap traversableImage = originalImage.copy(Bitmap.Config.ARGB_8888, true);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixelColor = semanticMap.getPixel(x, y);
                int label = getColorForLabel("Road");
                if (pixelColor == label)
                { // red pixel indicates road
                    int depthColor = depthMap.getPixel(x, y);
                    float depth = getDepth(depthColor);

                    if (depth >= minDepthLimit && depth < maxDepthLimit)
                    {
                        float scale = realObjectSize / (referenceObjectSizeInImage * focalLength);
                        float pointDepth = scale * depth;

                        float depthRatio = (pointDepth - minDepthLimit) / (maxDepthLimit - minDepthLimit);
                        int color = Color.BLACK;

                        boolean isTraversable = true;
                        for (int i = -(int) vehicleWidth / 2; i < (int) vehicleWidth / 2; i++) {
                            for (int j = -(int) vehicleLength / 2; j < (int) vehicleLength / 2; j++) {
                                int x_ = x + i;
                                int y_ = y + j;
                                if (x_ < 0 || x_ >= width || y_ < 0 || y_ >= height) {
                                    continue;
                                }
                                int pixelColor_ = semanticMap.getPixel(x_, y_);
                                int red_ = Color.red(pixelColor_);
                                if (red_ != 255) { // non-red pixel indicates non-road
                                    isTraversable = false;
                                    break;
                                }
                            }
                            if (!isTraversable) {
                                break;
                            }
                        }

                        if (isTraversable) {
                            traversableImage.setPixel(x, y, color);
                            PointF point = new PointF(x, y);
                            points.add(new PointF(point.x, point.y * depthRatio));
                        }
                    }
                }
            }
        }

        return new Pair<>(traversableImage, points);
    }

    protected float getDepth(int color) {
        return ((color & 0x000000ff)) / 255.0f * (maxDepth - minDepth) + minDepth;
    }

    protected void calculateDepthLimits() {
        minDepth = 2;
        maxDepth = distanceToObject;

        int width = semanticMap.getWidth();
        int height = semanticMap.getHeight();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int semanticColor = semanticMap.getPixel(x, y);
                int semanticLabel = getSemanticLabel(semanticColor).component2();

                if (semanticLabel == roadLabel) {
                    int depthColor = depthMap.getPixel(x, y);
                    float depth = getDepth(depthColor);

                    if (depth < minDepth) {
                        minDepth = depth;
                    }

                    if (depth > maxDepth) {
                        maxDepth = depth;
                    }
                }
            }
        }

        minDepthLimit = 2;
        maxDepthLimit = 80;
    }
}