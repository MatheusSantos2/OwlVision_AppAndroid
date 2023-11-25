package Infraestructure.DataAccess;

import android.content.Context;
import android.graphics.Bitmap;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ImageDriveHelper {

    private final File imagesDir;
    private Context context;

    public ImageDriveHelper(Context context, String folderPath) {
        this.context = context;
        imagesDir = new File(context.getExternalFilesDir(null), folderPath);
        imagesDir.mkdirs();
    }

    public void saveBitmap(Bitmap bitmap, int count) {

        File file = createImageFile(count);
        saveBitmapToFile(bitmap, file);
    }

    private File createImageFile(int count) {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss" + "_" + count, Locale.getDefault()).format(new Date());
        String imageFileName = "IMG_" + timeStamp + ".jpg";
        File imageFile = new File(imagesDir, imageFileName);
        return imageFile;
    }

    private void saveBitmapToFile(Bitmap bitmap, File file) {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
