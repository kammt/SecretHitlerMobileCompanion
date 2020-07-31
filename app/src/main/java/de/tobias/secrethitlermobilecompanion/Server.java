package de.tobias.secrethitlermobilecompanion;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Map;

import de.tobias.secrethitlermobilecompanion.SHClasses.GameLog;
import de.tobias.secrethitlermobilecompanion.SHClasses.PlayerList;
import fi.iki.elonen.NanoHTTPD;

import static android.content.Context.WIFI_SERVICE;

public class Server extends NanoHTTPD {
    private Context c;
    private int port;

    public Server(int port, Context c) {
        super(port);
        this.c = c;
        this.port = port;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public Response serve(IHTTPSession session) {
        Log.v(" Server request URI", session.getUri());
        Log.v("Server request method", session.getMethod().toString());

        StringBuilder sb = new StringBuilder();
        for(Map.Entry<String, String> k : session.getHeaders().entrySet()) {
            sb.append("  ").append(k.getKey()).append(": ").append(k.getValue()).append("\n");
        }

        Log.v("Server request header", "\n" + sb.toString());

        String uri = session.getUri();

        switch (uri) {
            case "/index.html":
            case "/":
                return newFixedLengthResponse(Response.Status.ACCEPTED, "text/html", getFile("index.html"));
            case "/index.js":
                return newFixedLengthResponse(Response.Status.ACCEPTED, "text/javascript", getFile("index.js"));
            case "/images.js":
                return newFixedLengthResponse(Response.Status.ACCEPTED, "text/javascript", getFile("images.js"));
            case "/PlayerPane.js":
                return newFixedLengthResponse(Response.Status.ACCEPTED, "text/javascript", getFile("PlayerPane.js"));
            case "/bootstrap.min.js":
                return newFixedLengthResponse(Response.Status.ACCEPTED, "text/javascript", getFile("bootstrap.min.js"));
            case "/jquery-3.5.1.slim.min.js":
                return newFixedLengthResponse(Response.Status.ACCEPTED, "text/javascript", getFile("jquery-3.5.1.slim.min.js"));
            case "/popper.min.js":
                return newFixedLengthResponse(Response.Status.ACCEPTED, "text/javascript", getFile("popper.min.js"));
            case "/css/style.css":
                return newFixedLengthResponse(Response.Status.ACCEPTED, "text/css", getFile("style.css"));
            case "/googlefonts.css":
                return newFixedLengthResponse(Response.Status.ACCEPTED, "text/css", getFile("googlefonts.css"));
            case "/fontawesome.css":
                return newFixedLengthResponse(Response.Status.ACCEPTED, "text/css", getFile("fontawesome.css"));
            case "/bootstrap.min.css":
                return newFixedLengthResponse(Response.Status.ACCEPTED, "text/css", getFile("bootstrap.min.css"));
            case "/getGameJSON":
                if (GameLog.isInitialised()) {
                    JSONObject obj = new JSONObject();
                    JSONObject game = new JSONObject();

                    try {
                        game.put("players", PlayerList.getPlayerListJSON());
                        game.put("plays", GameLog.getEventsJSON());
                        obj.put("game", game);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "application/json", "");
                    }
                    Log.v("/getGameJSON: JSON: ", obj.toString());
                    return newFixedLengthResponse(Response.Status.OK, "application/json", obj.toString());
                }
                return newFixedLengthResponse(Response.Status.SERVICE_UNAVAILABLE, "application/json", "");
            default:
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
                fileData.append(mLine).append("\n");
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
        WifiManager wifiManager = (WifiManager) c.getApplicationContext().getSystemService(WIFI_SERVICE);

        if(isUsingHotspot(wifiManager)) return "http://" + getHotspotIPAddress() + ":" + port;

        int ipAddress = wifiManager.getConnectionInfo().getIpAddress();
        final String formatedIpAddress = String.format("%d.%d.%d.%d", (ipAddress & 0xff), (ipAddress >> 8 & 0xff),
                (ipAddress >> 16 & 0xff), (ipAddress >> 24 & 0xff));


        return "http://" + formatedIpAddress + ":" + port;
    }


    public boolean isUsingHotspot(WifiManager wifiManager) {
        int actualState = 0;
        try {
            java.lang.reflect.Method method = wifiManager.getClass().getDeclaredMethod("getWifiApState");
            method.setAccessible(true);
            actualState = (Integer) method.invoke(wifiManager, (Object[]) null);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return actualState == 13; //public static int AP_STATE_ENABLED = 13;
    }

    private String getHotspotIPAddress() {
        String ip = "";
        try {
            Enumeration<NetworkInterface> enumNetworkInterfaces = NetworkInterface
                    .getNetworkInterfaces();
            while (enumNetworkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = enumNetworkInterfaces
                        .nextElement();
                Enumeration<InetAddress> enumInetAddress = networkInterface
                        .getInetAddresses();
                while (enumInetAddress.hasMoreElements()) {
                    InetAddress inetAddress = enumInetAddress.nextElement();

                    if (inetAddress.isSiteLocalAddress()) {
                        ip += inetAddress.getHostAddress();
                    }
                }
            }

        } catch (SocketException e) {
            e.printStackTrace();
        }
        return ip;
    }

}
