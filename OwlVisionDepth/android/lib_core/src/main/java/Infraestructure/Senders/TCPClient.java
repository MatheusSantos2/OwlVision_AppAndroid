package Infraestructure.Senders;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;



public class TCPClient {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    public void connect() {
        try {
            String ipAddress = "192.168.3.114";
            int port = 80;
            socket = new Socket(ipAddress, port);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e)
        {
            String TAG = "TCPClient";
            Log.e(TAG, "Fail to execute ImageSegmentationModelExecutor: ${e.message}");
            e.printStackTrace();
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
        return message;
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

}