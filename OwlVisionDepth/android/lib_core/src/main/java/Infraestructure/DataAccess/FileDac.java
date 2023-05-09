package Infraestructure.DataAccess;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;


public class FileDac
{
    public static void saveMessageToFile(Context context, String message, String fileName) {
        try {
            File documentsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
            File file = new File(documentsDir, fileName);

            FileOutputStream fos = new FileOutputStream(file);
            OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
            BufferedWriter bw = new BufferedWriter(osw);
            bw.write(message);
            bw.newLine();
            bw.close();

            String filePath = file.getAbsolutePath();
            Toast.makeText(context, "Mensagem salva no arquivo: " + filePath, Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}