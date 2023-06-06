package Infraestructure.Senders;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class TCPClient {
    private static TCPClient instance;

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private boolean isConnected = false;
    private int timeout = 50000;
    private int maxRetries = 3;
    private long retryDelayMillis = 2000L;
    private int retryCount = 0;

    public static synchronized TCPClient getInstance() {
        if (instance == null) {
            instance = new TCPClient();
        }
        return instance;
    }

    public void connect() throws InterruptedException
    {
        while (retryCount < maxRetries && !isConnected)
        {
            try {
                if (!isConnected)
                {
                    String ipAddress = "192.168.3.114";
                    int port = 80;
                    socket = new Socket(ipAddress, port);
                    socket.setSoTimeout(timeout);
                    out = new PrintWriter(socket.getOutputStream(), true);
                    in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    isConnected = true;
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
                retryCount++;
                Thread.sleep(retryDelayMillis);
            }
        }

    }

    public void sendMessage(String message) {
        if (out != null) {
            out.println(message);
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean hasConnected()
    {
        return isConnected;
    }
}