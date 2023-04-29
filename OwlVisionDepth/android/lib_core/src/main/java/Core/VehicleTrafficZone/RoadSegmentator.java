package Core.VehicleTrafficZone;

import android.graphics.Bitmap;
import android.graphics.Color;

public class RoadSegmentator {

    private Bitmap semanticMap;
    private Bitmap depthMap;
    private int roadLabel;
    private float minDepth;
    private float maxDepth;

    public RoadSegmentator(Bitmap semanticMap, Bitmap depthMap, int roadLabel, float minDepth, float maxDepth) {
        this.semanticMap = semanticMap;
        this.depthMap = depthMap;
        this.roadLabel = roadLabel;
        this.minDepth = minDepth;
        this.maxDepth = maxDepth;
    }

    public Bitmap getTraversableZone(float vehicleWidth, float vehicleLength) {
        int width = semanticMap.getWidth();
        int height = semanticMap.getHeight();
        Bitmap traversableZone = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int semanticColor = semanticMap.getPixel(x, y);
                int semanticLabel = getSemanticLabel(semanticColor);

                if (semanticLabel == roadLabel) {
                    int depthColor = depthMap.getPixel(x, y);
                    float depth = getDepth(depthColor);

                    if (depth >= minDepth && depth <= maxDepth) {
                        boolean isTraversable = true;
                        for(int i = -(int)vehicleWidth/2; i < (int)vehicleWidth/2; i++) {
                            for(int j = -(int)vehicleLength/2; j < (int)vehicleLength/2; j++) {
                                int x_ = x+i;
                                int y_ = y+j;
                                if (x_ >= 0 && x_ < width && y_ >= 0 && y_ < height) {
                                    int semanticColor_ = semanticMap.getPixel(x_, y_);
                                    int semanticLabel_ = getSemanticLabel(semanticColor_);
                                    if (semanticLabel_ != roadLabel) {
                                        isTraversable = false;
                                    }
                                }
                            }
                        }
                        if(isTraversable)
                            traversableZone.setPixel(x, y, Color.WHITE);
                        else
                            traversableZone.setPixel(x, y, Color.BLACK);
                    } else {
                        traversableZone.setPixel(x, y, Color.BLACK);
                    }
                } else {
                    traversableZone.setPixel(x, y, Color.BLACK);
                }
            }
        }
        return traversableZone;
    }

    private int getSemanticLabel(int color)
    {
        return 0;
        // Implemente aqui a lógica para obter a classificação do pixel de acordo com a codificação escolhida.
    }

    private float getDepth(int color)
    {
        return 0;
        // Implemente aqui a lógica para obter o valor de profundidade de acordo com a codificação escolhida.
    }
}
