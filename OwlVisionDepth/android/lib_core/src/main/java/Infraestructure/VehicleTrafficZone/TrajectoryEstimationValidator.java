package Infraestructure.VehicleTrafficZone;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.Rect;
import android.util.Pair;

import java.util.ArrayList;
import java.util.List;

public class TrajectoryEstimationValidator
{
    public Pair<Bitmap, List<PointF>> processTraversablePixels(Bitmap originalImage, Bitmap segmentationImage, Bitmap depthImage, float vehicleWidth, float vehicleLength)
    {

        Pair<Bitmap, Bitmap> images = divideImage(originalImage);
        int countLeftBlackPoints = countBlackPixels(images.first);
        int countRightBlackPoints = countBlackPixels(images.second);
        boolean isLeft = true;

        if (countRightBlackPoints > countLeftBlackPoints) {
            isLeft = false;
        }
        Bitmap imageSideSelected = getPartialImage(segmentationImage, isLeft);

        TrafficableTrajectoryEstimator traffic = new TrafficableTrajectoryEstimator(imageSideSelected, depthImage, 7);

        Pair<Bitmap, List<PointF>> newResult = traffic.getTraversableZone(originalImage, vehicleWidth, vehicleLength);
        Bitmap newImage = newResult.first;
        List<PointF> newPoints = newResult.second;
        return new Pair<>(newImage, newPoints);
    }

    public boolean isTraversableInCenter(Bitmap image) {
        int width = image.getWidth();
        int height = image.getHeight();

        int centerX = width / 2;
        int centerY = height / 2;
        int frameWidth = (int) (width * 0.2f);
        int frameHeight = (int) (height * 0.2f);
        int startX = centerX - frameWidth / 2;
        int startY = centerY - frameHeight / 2;

        int endX = startX + frameWidth;
        int endY = startY + frameHeight;

        for (int y = startY; y < endY; y++) {
            for (int x = startX; x < endX; x++) {
                if (x >= 0 && x < width && y >= 0 && y < height) {
                    int pixelColor = image.getPixel(x, y);
                    if (pixelColor == Color.BLACK) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public int countBlackPixels(Bitmap image) {
        int count = 0;
        int width = image.getWidth();
        int height = image.getHeight();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixelColor = image.getPixel(x, y);
                if (pixelColor == Color.BLACK) {
                    count++;
                }
            }
        }
        return count;
    }

    public Pair<Bitmap, Bitmap> divideImage(Bitmap image)
    {
        int width = image.getWidth();
        int height = image.getHeight();

        int halfWidth = width / 2;

        Bitmap leftImage = Bitmap.createBitmap(halfWidth, height, Bitmap.Config.ARGB_8888);
        Bitmap rightImage = Bitmap.createBitmap(halfWidth, height, Bitmap.Config.ARGB_8888);

        Canvas leftCanvas = new Canvas(leftImage);
        Canvas rightCanvas = new Canvas(rightImage);

        // Desenha a metade esquerda da imagem original no leftImage
        leftCanvas.drawBitmap(image, new Rect(0, 0, halfWidth, height), new Rect(0, 0, halfWidth, height), null);

        // Desenha a metade direita da imagem original no rightImage
        rightCanvas.drawBitmap(image, new Rect(halfWidth, 0, width, height), new Rect(0, 0, halfWidth, height), null);

        return new Pair<>(leftImage, rightImage);
    }

    public Bitmap getPartialImage(Bitmap originalImage, boolean desiredSide) {
        int width = originalImage.getWidth();
        int height = originalImage.getHeight();
        int centerX = width / 2;

        Bitmap partialImage = originalImage.copy(Bitmap.Config.ARGB_8888, true);

        if (desiredSide) { // Lado esquerdo
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < centerX; x++) {
                    partialImage.setPixel(x, y, Color.BLUE);
                }
            }
        }
        else
        { // Lado direito
            for (int y = 0; y < height; y++) {
                for (int x = centerX; x < width; x++) {
                    partialImage.setPixel(x, y, Color.BLUE);
                }
            }
        }

        return partialImage;
    }
}
