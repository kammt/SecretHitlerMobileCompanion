package de.tobias.secrethitlermobilecompanion;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.IBinder;
import android.renderscript.ScriptGroup;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.Buffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Pattern;

import fi.iki.elonen.NanoHTTPD;

import static android.content.Context.WIFI_SERVICE;
import static java.net.HttpURLConnection.HTTP_OK;

public class Server extends NanoHTTPD {
    private Context c;

    public Server(int port, Context c) {
        super(port);
        this.c = c;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public Response serve(IHTTPSession session) {
        Log.v(" Server request URI", session.getUri());
        Log.v("Server request method", session.getMethod().toString());

        StringBuilder sb = new StringBuilder();
        for(Map.Entry<String, String> k : session.getHeaders().entrySet()) {
            sb.append("  " + k.getKey() + ": " +  k.getValue() + "\n");
        }

        Log.v("Server request header", "\n" + sb.toString());

        String uri = session.getUri();

        if(uri.equals("/index.html") || uri.equals("/")) {
            return newFixedLengthResponse(Response.Status.ACCEPTED, "text/html", getFile("index.html"));
        } else if(uri.equals("/index.js")) {
            return newFixedLengthResponse(Response.Status.ACCEPTED, "text/javascript",getFile("index.js"));
        } else if(uri.equals("/images.js")) {
            return newFixedLengthResponse(Response.Status.ACCEPTED, "text/javascript",getFile("images.js"));
        } else if(uri.equals("/css/style.css")) {
            return newFixedLengthResponse(Response.Status.ACCEPTED, "text/css",getFile("style.css"));
        } else {
            return newFixedLengthResponse(Response.Status.NOT_FOUND, "text/html", getFile("404.html"));
        }
    }

    public String getFile(String fileName) {
        StringBuilder fileData = new StringBuilder();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(
                    new InputStreamReader(c.getAssets().open(fileName)));
            String mLine;
            while ((mLine = reader.readLine()) != null) {
                fileData.append(mLine + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return fileData.toString();
    }


    public void startServer() {
        try {
            start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getURL() {
        WifiManager wifiManager = (WifiManager) c.getSystemService(WIFI_SERVICE);

        int ipAddress = wifiManager.getConnectionInfo().getIpAddress();
        final String formatedIpAddress = String.format("%d.%d.%d.%d", (ipAddress & 0xff), (ipAddress >> 8 & 0xff),
                (ipAddress >> 16 & 0xff), (ipAddress >> 24 & 0xff));

        return "http://" + formatedIpAddress + ":" + getListeningPort();
    }

}
