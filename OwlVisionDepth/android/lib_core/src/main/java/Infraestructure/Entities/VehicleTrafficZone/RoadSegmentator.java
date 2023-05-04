package Infraestructure.Entities.VehicleTrafficZone;

import android.graphics.Bitmap;
import android.graphics.Color;

import Utils.SegmentColors;

public class RoadSegmentator extends SegmentColors {
    protected Bitmap semanticMap;
    protected Bitmap depthMap;
    protected int roadLabel;
    protected float minDepth;
    protected float maxDepth;
    protected float focalLength;
    protected float realObjectSize;
    protected float referenceObjectSizeInImage;
    protected float minDepthLimit;
    protected float maxDepthLimit;
    protected float distanceToObject;

    public RoadSegmentator(Bitmap semanticMap, Bitmap depthMap, int roadLabel) {
        this.semanticMap = semanticMap;
        this.depthMap = depthMap;
        this.roadLabel = roadLabel;
        this.focalLength = focalLength;
        this.realObjectSize = realObjectSize;
        this.referenceObjectSizeInImage = referenceObjectSizeInImage;
        this.distanceToObject = distanceToObject;

        calculateDepthLimits();
    }

    public Bitmap getTraversableZone(float vehicleWidth, float vehicleLength) {
        int width = semanticMap.getWidth();
        int height = semanticMap.getHeight();
        Bitmap traversableZone = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        int startColor = Color.BLUE;
        int endColor = Color.RED;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int semanticColor = semanticMap.getPixel(x, y);
                int semanticLabel = getSemanticLabel(semanticColor).component2();

                if (semanticLabel == roadLabel) {
                    int depthColor = depthMap.getPixel(x, y);
                    float depth = getDepth(depthColor);

                    if (depth >= minDepthLimit && depth <= maxDepthLimit) {
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
                                int semanticColor_ = semanticMap.getPixel(x_, y_);
                                int semanticLabel_ = getSemanticLabel(semanticColor_).component2();
                                if (semanticLabel_ != roadLabel) {
                                    isTraversable = false;
                                    break;
                                }
                            }
                            if (!isTraversable) {
                                break;
                            }
                        }

                        if (isTraversable) {
                            traversableZone.setPixel(x, y, color);
                        }
                    }
                }
            }
        }

        return traversableZone;
    }

    protected float getDepth(int color) {
        return ((color & 0x000000ff)) / 255.0f * (maxDepth - minDepth) + minDepth;
    }

    /*protected int interpolateColor(int startColor, int endColor, float ratio) {
        int startA = (startColor >> 24) & 0xff;
        int startR = (startColor >> 16) & 0xff;
        int startG = (startColor >> 8) & 0xff;
        int startB = startColor & 0xff;

        int endA = (endColor >> 24) & 0xff;
        int endR = (endColor >> 16) & 0xff;
        int endG = (endColor >> 8) & 0xff;
        int endB = endColor & 0xff;

        int interpolatedA = (int) (startA * (1 - ratio) + endA * ratio);
        int interpolatedR = (int) (startR * (1 - ratio) + endR * ratio);
        int interpolatedG = (int) (startG * (1 - ratio) + endG * ratio);
        int interpolatedB = (int) (startB * (1 - ratio) + endB * ratio);

        return (interpolatedA << 24) | (interpolatedR << 16) | (interpolatedG << 8) | interpolatedB;
    }*/

    protected void calculateDepthLimits() {
        minDepth = Float.MAX_VALUE;
        maxDepth = Float.MIN_VALUE;

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

        minDepthLimit = distanceToObject - realObjectSize / 2;
        maxDepthLimit = distanceToObject + realObjectSize / 2;
    }
}