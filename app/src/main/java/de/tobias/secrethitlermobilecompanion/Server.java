package de.tobias.secrethitlermobilecompanion;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.io.IOException;

import fi.iki.elonen.NanoHTTPD;

import static android.content.Context.WIFI_SERVICE;
import static java.net.HttpURLConnection.HTTP_OK;

public class Server extends NanoHTTPD {
    public Server(int port) {
        super(port);
    }

    @Override
    public Response serve(IHTTPSession session) {
        return newFixedLengthResponse("<html><head></head><body><h1>Juhu es funktioniert bää</h1><br><h2>Schervusch</h2></html>");
    }

    public void startServer() {
        try {
            start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getURL(Context c) {
        WifiManager wifiManager = (WifiManager) c.getSystemService(WIFI_SERVICE);

        int ipAddress = wifiManager.getConnectionInfo().getIpAddress();
        final String formatedIpAddress = String.format("%d.%d.%d.%d", (ipAddress & 0xff), (ipAddress >> 8 & 0xff),
                (ipAddress >> 16 & 0xff), (ipAddress >> 24 & 0xff));

        String result = "http://" + formatedIpAddress + ":" + getListeningPort();

        return result;
    }


}
