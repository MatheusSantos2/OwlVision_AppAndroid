package Infraestructure.DataAccess;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MonitoringSqlLiteHelper extends SQLiteOpenHelper
{
    private static final String DATABASE_NAME = "Monitoring.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_NAME = "Monitoring";

    public MonitoringSqlLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTableQuery = "CREATE TABLE IF NOT EXISTS Monitoring (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "data TEXT," +
                "label TEXT," +
                "velocidadeAtual REAL," +
                "velocidadeAlmejada REAL," +
                "posicaoXAlmejada REAL," +
                "posicaoYAlmejada REAL," +
                "posicaoXAtual REAL," +
                "posicaoYAtual REAL)";

        db.execSQL(createTableQuery);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            onCreate(db);
        }
    }

    public void insert(String message) {

        String[] messageArray = stringToArray(message);

        if (messageArray.length == 6)
        {
            SQLiteDatabase db = getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put("data", getDateTime());
            values.put("label", "Trajetory");
            values.put("velocidadeAtual", messageArray[0]);
            values.put("velocidadeAlmejada", messageArray[1]);
            values.put("posicaoXAlmejada", messageArray[2]);
            values.put("posicaoYAlmejada", messageArray[3]);
            values.put("posicaoXAtual", messageArray[4]);
            values.put("posicaoYAtual", messageArray[5]);

            long newRowId = db.insert("Monitoring", null, values);

            db.close();
        }
    }

    public String[] stringToArray(String input) {
        String[] array = input.split(",");
        for (int i = 0; i < array.length; i++) {
            array[i] = array[i].trim();
        }
        return array;
    }

    public String getDateTime() {
        Date currentDate = new Date();
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        return format.format(currentDate);
    }
}