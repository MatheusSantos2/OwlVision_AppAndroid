package Infraestructure.VehicleTrafficZone;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.Rect;
import android.util.Pair;

import java.util.List;

import Utils.ImageGenerator;

public class TrajectoryValidator
{
    ImageGenerator imageGenerator = new ImageGenerator();
    public Pair<Bitmap, List<PointF>> processTraversablePixels(Bitmap originalImage, Bitmap segmentationImage, Bitmap depthImage)
    {
        Pair<Bitmap, Bitmap> images = divideImage(depthImage);
        int countLeftPoints = countBlockedPixels(images.first);
        int countRightPoints = countBlockedPixels(images.second);
        boolean isLeft = true;
        int region = 1;

        if (countRightPoints > countLeftPoints) {
            isLeft = false;
            region  = 2;
        }
        Bitmap imageSideSelected = imageGenerator.getPartialImage(segmentationImage, isLeft);

        TrajectoryEstimator traffic = new TrajectoryEstimator();

        Pair<Bitmap, List<PointF>> newResult = traffic.getTraversableZone(imageSideSelected, depthImage, region);
        Bitmap newImage = newResult.first;
        List<PointF> newPoints = newResult.second;
        return new Pair<>(newImage, newPoints);
    }

    public boolean isTraversableInCenter(Bitmap image) {
        int width = image.getWidth();
        int height = image.getHeight();

        int centerY = height / 2;
        int startY = centerY;
        int endY = height;

        int count = 0;
        int totalPixels = 0;

        for (int y = startY; y < endY; y++) {
            for (int x = 0; x < width; x++) {
                int pixelColor = image.getPixel(x, y);
                int color = getColor(pixelColor);

                if (color == Color.BLUE) {
                    count++;
                }

                totalPixels++;
            }
        }

        double blockedPercentage = (double) count / totalPixels;

        if (blockedPercentage > 0.4) {
            return false;
        }

        return true;
    }

    public int getColor(int pixelColor) {
        int red = Color.red(pixelColor);
        int green = Color.green(pixelColor);
        int blue = Color.blue(pixelColor);

        if (red >= green && red >= blue) {
            return red;
        } else if (green >= red && green >= blue) {
            return green;
        } else {
            return blue;
        }
    }

    private int countBlockedPixels(Bitmap image) {
        int width = image.getWidth();
        int height = image.getHeight();

        int blueIntensity = 0;
        int greenIntensity = 0;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixelColor = image.getPixel(x, y);

                int blue = Color.blue(pixelColor);
                int green = Color.green(pixelColor);

                blueIntensity += blue;
                greenIntensity += green;
            }
        }

        return blueIntensity + greenIntensity;
    }

    private Pair<Bitmap, Bitmap> divideImage(Bitmap image)
    {
        int width = image.getWidth();
        int height = image.getHeight();

        int halfWidth = width / 2;

        Bitmap leftImage = Bitmap.createBitmap(halfWidth, height, Bitmap.Config.ARGB_8888);
        Bitmap rightImage = Bitmap.createBitmap(halfWidth, height, Bitmap.Config.ARGB_8888);

        Canvas leftCanvas = new Canvas(leftImage);
        Canvas rightCanvas = new Canvas(rightImage);

        leftCanvas.drawBitmap(image, new Rect(0, 0, halfWidth, height), new Rect(0, 0, halfWidth, height), null);

        rightCanvas.drawBitmap(image, new Rect(halfWidth, 0, width, height), new Rect(0, 0, halfWidth, height), null);

        return new Pair<>(leftImage, rightImage);
    }
}
