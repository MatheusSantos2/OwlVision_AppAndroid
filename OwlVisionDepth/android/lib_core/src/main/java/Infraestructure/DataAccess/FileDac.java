package Infraestructure.DataAccess;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;


public class FileDac
{
    public static void saveMessageToFile(Context context, String message, String fileName) {
        try {
            FileOutputStream outputStream = context.openFileOutput(fileName, Context.MODE_PRIVATE);

            outputStream.write(message.getBytes());
            outputStream.close();

            String filePath = new File(context.getFilesDir(), fileName).getAbsolutePath();
            Toast.makeText(context, "Mensagem salva no arquivo: " + filePath, Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}