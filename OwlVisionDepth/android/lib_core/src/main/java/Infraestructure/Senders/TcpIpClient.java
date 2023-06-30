package Infraestructure.Senders;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;


public class TcpIpClient {

    private final Context context;
    private BufferedReader in;
    private boolean isConnected = false;
    private int timeout = 500000;
    private long retryDelayMillis = 2000L;
    private int port = 80;
    private int port2 = 90;
    private String ipAddress = "192.168.3.114";
    private Socket socket;
    private Socket socket2;
    private PrintWriter out;
    private PrintWriter out2;

    public TcpIpClient(Context context){
        this.context = context;
    }

    public void connect() throws InterruptedException
    {
        try {
            if (!isConnected)
            {
                socket = new Socket(ipAddress, port);
                socket.setSoTimeout(timeout);
                out = new PrintWriter(socket.getOutputStream(), true);

                socket2 = new Socket(ipAddress, port2);
                socket2.setSoTimeout(timeout);
                out2 = new PrintWriter(socket2.getOutputStream(), true);

                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                isConnected = true;

                showPopUpHandler("Conectado", "Conexao estabelecida com a cadeira de rodas");
            }
        } catch (IOException e)
        {
            isConnected = false;
            String TAG = "TCPClient";
            Log.e(TAG, "Fail to execute TCP Client Connect: ${e.message}");
            e.printStackTrace();
        }

        if (!isConnected)
        {
            Thread.sleep(retryDelayMillis);
        }
    }

    private void showPopUpHandler(String title, String message)
    {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                showPopup(title, message);
            }
        });
    }

    public void sendMessage(String label, String message)
    {
        String result = label.concat(";").concat(message).concat("\n");

        if (label.contains("Trajectory"))
        {
            if (out != null) {
                out.println(result);
            }
        }
        else if (label.contains("Position"))
        {
            if (out2 != null) {
                out2.println(result);
            }
        }
    }

    public String receiveMessage() {
        String message = "";
        try {
            if (in != null) {
                message = in.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return message != null ? message : "";
    }

    public void disconnect() {
        try {
            if (out != null) {
                out.close();
            }
            if (in != null) {
                in.close();
            }
            if (socket != null) {
                socket.close();
            }
            if (out2 != null) {
                out2.close();
            }
            if (socket2 != null) {
                socket2.close();
            }

            showPopUpHandler("Desconectado", "Conexao encerrada com a cadeira de rodas");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean hasConnected()
    {
        return isConnected;
    }

    private void showPopup(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .setCancelable(false)
                .show();
    }
}