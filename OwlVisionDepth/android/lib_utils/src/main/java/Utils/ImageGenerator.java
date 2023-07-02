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

    public Bitmap mapColors(Bitmap bitmap, int color) {
        Bitmap resultBitmap = bitmap.copy(bitmap.getConfig(), true);

        int width = resultBitmap.getWidth();
        int height = resultBitmap.getHeight();

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int pixel = resultBitmap.getPixel(x, y);

                // Verificar se o pixel é preto
                if (pixel == Color.BLACK) {
                    resultBitmap.setPixel(x, y, color);
                }
            }
        }

        return resultBitmap;
    }

    public Bitmap fillWhiteWithMagenta(Bitmap bitmap, int threshold) {
        Bitmap resultBitmap = bitmap.copy(bitmap.getConfig(), true);

        int width = resultBitmap.getWidth();
        int height = resultBitmap.getHeight();

        int magentaColor = Color.parseColor("#FF00FF"); // Cor magenta (hexadecimal)
        int whiteColor = Color.WHITE;

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int pixel = resultBitmap.getPixel(x, y);

                if (pixel == whiteColor) {
                    boolean hasMagentaNeighbor = false;

                    for (int offsetX = -1; offsetX <= 1; offsetX++) {
                        for (int offsetY = -1; offsetY <= 1; offsetY++) {
                            int neighborX = x + offsetX;
                            int neighborY = y + offsetY;

                            if (neighborX >= 0 && neighborX < width && neighborY >= 0 && neighborY < height) {
                                int neighborPixel = resultBitmap.getPixel(neighborX, neighborY);

                                if (calculateColorDistance(neighborPixel, magentaColor) <= threshold) {
                                    hasMagentaNeighbor = true;
                                    break;
                                }
                            }
                        }
                        if (hasMagentaNeighbor) {
                            break;
                        }
                    }

                    if (hasMagentaNeighbor) {
                        resultBitmap.setPixel(x, y, magentaColor);
                    }
                }
            }
        }

        return resultBitmap;
    }

    private int calculateColorDistance(int color1, int color2) {
        int r1 = Color.red(color1);
        int g1 = Color.green(color1);
        int b1 = Color.blue(color1);

        int r2 = Color.red(color2);
        int g2 = Color.green(color2);
        int b2 = Color.blue(color2);

        return Math.abs(r1 - r2) + Math.abs(g1 - g2) + Math.abs(b1 - b2);
    }

    public void createMagentaStain(Bitmap image, int threshold, int stainColor, int stainRadius) {
        int width = image.getWidth();
        int height = image.getHeight();
        boolean[][] visited = new boolean[width][height];

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (!visited[x][y] && isMagentaPixel(image.getPixel(x, y), threshold)) {
                    List<Point> region = new ArrayList<>();
                    floodFill(image, visited, x, y, threshold, region);

                    applyStainToRegion(image, region, stainColor, stainRadius);
                }
            }
        }
    }

    private void applyStainToRegion(Bitmap image, List<Point> region, int stainColor, int stainRadius) {
        for (Point point : region) {
            int centerX = (int)point.x;
            int centerY = (int)point.y;

            for (int dx = -stainRadius; dx <= stainRadius; dx++) {
                for (int dy = -stainRadius; dy <= stainRadius; dy++) {
                    int nx = centerX + dx;
                    int ny = centerY + dy;

                    if (nx >= 0 && nx < image.getWidth() && ny >= 0 && ny < image.getHeight()) {
                        double distance = Math.sqrt(dx * dx + dy * dy);
                        double intensity = 1.0 - (distance / stainRadius);

                        int pixel = image.getPixel(nx, ny);
                        int newColor = blendColors(pixel, stainColor, intensity);
                        image.setPixel(nx, ny, newColor);
                    }
                }
            }
        }
    }

    private int blendColors(int color1, int color2, double ratio) {
        int alpha = (int) (Color.alpha(color1) * (1.0 - ratio) + Color.alpha(color2) * ratio);
        int red = (int) (Color.red(color1) * (1.0 - ratio) + Color.red(color2) * ratio);
        int green = (int) (Color.green(color1) * (1.0 - ratio) + Color.green(color2) * ratio);
        int blue = (int) (Color.blue(color1) * (1.0 - ratio) + Color.blue(color2) * ratio);
        return Color.argb(alpha, red, green, blue);
    }

    private void floodFill(Bitmap image, boolean[][] visited, int x, int y, int threshold, List<Point> region) {
        int width = image.getWidth();
        int height = image.getHeight();
        int targetColor = image.getPixel(x, y);

        if (visited[x][y] || !isMagentaPixel(targetColor, threshold)) {
            return;
        }

        visited[x][y] = true;
        region.add(new Point(x, y));

        int[] dx = {0, 0, -1, 1};
        int[] dy = {-1, 1, 0, 0};

        for (int i = 0; i < 4; i++) {
            int nx = x + dx[i];
            int ny = y + dy[i];

            if (nx >= 0 && nx < width && ny >= 0 && ny < height) {
                floodFill(image, visited, nx, ny, threshold, region);
            }
        }
    }

    private boolean isMagentaPixel(int pixel, int threshold) {
        int red = Color.red(pixel);
        int green = Color.green(pixel);
        int blue = Color.blue(pixel);
        int magentaThreshold = threshold;

        return (red >= 255 - magentaThreshold) && (green <= magentaThreshold) && (blue >= 255 - magentaThreshold);
    }
}
