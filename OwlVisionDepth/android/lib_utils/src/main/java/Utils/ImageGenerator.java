package Utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.Pair;


import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import Models.Point3D;

public class ImageGenerator {
    public Bitmap createBitmapImageXZ(List<Pair<Boolean, Point3D>> positions, Integer imageWidth, Integer imageHeight) {
        Bitmap bitmap = Bitmap.createBitmap(imageWidth, imageHeight, Bitmap.Config.RGB_565);
        bitmap.eraseColor(Color.BLACK);

        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);

        float xMin = Float.MAX_VALUE;
        float xMax = Float.MIN_VALUE;
        float zMin = Float.MAX_VALUE;
        float zMax = Float.MIN_VALUE;

        for ( Pair<Boolean, Point3D> point : positions) {
            float x = point.second.getX();
            float z = point.second.getZ();

            xMin = Math.min(xMin, x);
            xMax = Math.max(xMax, x);
            zMin = Math.min(zMin, z);
            zMax = Math.max(zMax, z);
        }

        float xRange = Math.max(Math.abs(xMin), Math.abs(xMax));
        float zRange = zMax - zMin;

        for (Pair<Boolean, Point3D> point : positions) {
            if (point.first) {
                float x = point.second.getX();
                float z = point.second.getZ();

                float px = (x - xMin) / xRange * imageWidth;

                float py = (z - zMin) / zRange * imageHeight;
                paint.setColor(Color.WHITE);
                canvas.drawPoint(px, py, paint);
            }
            else{
                float x = point.second.getX();
                float z = point.second.getZ();

                float px = (x - xMin) / xRange * imageWidth;

                float py = (z - zMin) / zRange * imageHeight;
                paint.setColor(Color.MAGENTA);
                canvas.drawPoint(px, py, paint);
            }
        }

        return bitmapMirror(flipBitmapVertically(bitmap));
    }

    public Bitmap createBitmapImageXY(List<Pair<Boolean, Point3D>> positions, Integer imageWidth, Integer imageHeight) {
        Bitmap bitmap = Bitmap.createBitmap(imageWidth, imageHeight, Bitmap.Config.RGB_565);
        bitmap.eraseColor(Color.BLACK);

        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);

        float xMin = Float.MAX_VALUE;
        float xMax = Float.MIN_VALUE;
        float zMin = Float.MAX_VALUE;
        float zMax = Float.MIN_VALUE;

        for ( Pair<Boolean, Point3D> point : positions) {
            float x = point.second.getX();
            float z = point.second.getY();

            xMin = Math.min(xMin, x);
            xMax = Math.max(xMax, x);
            zMin = Math.min(zMin, z);
            zMax = Math.max(zMax, z);
        }

        float xRange = Math.max(Math.abs(xMin), Math.abs(xMax));
        float zRange = zMax - zMin;

        for (Pair<Boolean, Point3D> point : positions) {
            if (point.first) {
                float x = point.second.getX();
                float z = point.second.getY();

                float px = (x - xMin) /  xRange * imageWidth;
                float py = (z - zMin) / zRange * imageHeight;

                paint.setColor(Color.WHITE);
                canvas.drawPoint(px, py, paint);
            }
            else{
                float x = point.second.getX();
                float z = point.second.getZ();

                float px = (x - xMin) /  xRange * imageWidth;
                float py = (z - zMin) / zRange * imageHeight;

                paint.setColor(Color.MAGENTA);
                canvas.drawPoint(px, py, paint);
            }
        }

        return bitmap;
    }

    public Bitmap createBitmapImageYZ(List<Pair<Boolean, Point3D>> positions, Integer imageWidth, Integer imageHeight) {
        Bitmap bitmap = Bitmap.createBitmap(imageWidth, imageHeight, Bitmap.Config.RGB_565);
        bitmap.eraseColor(Color.BLACK);

        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);

        float xMin = Float.MAX_VALUE;
        float xMax = Float.MIN_VALUE;
        float zMin = Float.MAX_VALUE;
        float zMax = Float.MIN_VALUE;

        for ( Pair<Boolean, Point3D> point : positions) {
            float x = point.second.getY();
            float z = point.second.getZ();

            xMin = Math.min(xMin, x);
            xMax = Math.max(xMax, x);
            zMin = Math.min(zMin, z);
            zMax = Math.max(zMax, z);
        }

        float xRange = Math.max(Math.abs(xMin), Math.abs(xMax));
        float zRange = zMax - zMin;

        for (Pair<Boolean, Point3D> point : positions) {
            if (point.first) {
                float x = point.second.getY();
                float z = point.second.getZ();

                float px = (x - xMin) /  xRange * imageWidth;
                float py = (z - zMin) / zRange * imageHeight;

                paint.setColor(Color.WHITE);
                canvas.drawPoint(px, py, paint);
            }
            else{
                float x = point.second.getY();
                float z = point.second.getZ();

                float px = (x - xMin) /  xRange * imageWidth;
                float py = (z - zMin) / zRange * imageHeight;

                paint.setColor(Color.MAGENTA);
                canvas.drawPoint(px, py, paint);
            }
        }

        return bitmap;
    }

    public Bitmap createBitmapFromRecreatedImage(List<Point3D> coordinates) {

        coordinates.sort(Comparator.comparing(Point3D::getX).thenComparing(Point3D::getY));

        int minX = (int) coordinates.get(0).getX();
        int minY = (int) coordinates.get(0).getY();
        int maxX = (int) coordinates.get(coordinates.size() - 1).getX();
        int maxY = (int) coordinates.get(coordinates.size() - 1).getY();

        int width = maxX - minX + 1;
        int height = maxY - minY + 1;
        Mat image = new Mat(height, width, CvType.CV_8UC3, new Scalar(255, 255, 255));

        for (Point3D coordinate : coordinates) {
            int x = (int) coordinate.getX() - minX;
            int y = (int) coordinate.getY() - minY;
            Imgproc.circle(image, new Point(x, y), 1, new Scalar(0, 0, 0), -1);
        }

        Bitmap bitmap = Bitmap.createBitmap(image.cols(), image.rows(), Bitmap.Config.ARGB_8888);

        ByteBuffer buffer = ByteBuffer.allocate((int) (image.total() * image.channels()));
        image.get(0, 0, buffer.array());

        bitmap.copyPixelsFromBuffer(buffer);

        return bitmap;
    }

    public Bitmap convertPositionListToBitmapDepthScale(List<Point3D> positionList, int width, int height) {
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        int[] pixels = new int[width * height];

        for (int i = 0; i < width * height; i++) {
            int a = 0xFF;
            float value = positionList.get(i).getZ();
            int r = (int) (value * 255.0f);
            int g = (int) (value * 255.0f);
            int b = (int) (value * 255.0f);

            pixels[i] = (a << 24) | (r << 16) | (g << 8) | b;
        }

        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return flipBitmapVertically(bitmap);
    }

    public Bitmap bitmapMirror(Bitmap bitmap) {
        Matrix matrix = new Matrix();
        matrix.preScale(-1, 1);

        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, false);
    }
    
    public Bitmap createEmptyImage(int imageWidth, int imageHeight)
    {
        Bitmap bitmap = Bitmap.createBitmap(imageWidth, imageHeight, Bitmap.Config.RGB_565);
        bitmap.eraseColor(Color.BLACK);
        
        return bitmap;
    }

    public Bitmap flipBitmapVertically(Bitmap bitmap) {
        Matrix matrix = new Matrix();
        matrix.postRotate(180);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    public Bitmap rotateBitmapClockwise(Bitmap bitmap) {
        Matrix matrix = new Matrix();
        matrix.postRotate(90);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    public static Bitmap resizeBitmap(Bitmap inputBitmap, int newWidth, int newHeight) {
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(inputBitmap, newWidth, newHeight, false);
        return resizedBitmap;
    }
}
