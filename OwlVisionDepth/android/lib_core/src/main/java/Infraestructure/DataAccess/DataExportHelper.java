package Infraestructure.DataAccess;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

public class DataExportHelper {

    public static void exportDatabase(Context context) {
        try {
            File dbFile = context.getDatabasePath("Monitoring.db");
            File exportDir = new File(context.getExternalFilesDir(null), "database_exports");
            exportDir.mkdirs();
            File exportFile = new File(exportDir, "Monitoring.db");

            copyFile(dbFile, exportFile);

            Uri fileUri = FileProvider.getUriForFile(context, "com.example.myapp.fileprovider", exportFile);

            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("application/octet-stream");
            intent.putExtra(Intent.EXTRA_STREAM, fileUri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            context.startActivity(Intent.createChooser(intent, "Compartilhar banco de dados"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void copyFile(File sourceFile, File destFile) throws IOException {
        FileChannel source = null;
        FileChannel destination = null;
        try {
            source = new FileInputStream(sourceFile).getChannel();
            destination = new FileOutputStream(destFile).getChannel();
            destination.transferFrom(source, 0, source.size());
        } finally {
            if (source != null) {
                source.close();
            }
            if (destination != null) {
                destination.close();
            }
        }
    }
}
