package de.tobias.secrethitlermobilecompanion;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

public class ServerSercive extends Service {

    private final IBinder mBinder = new LocalBinder();
    public Server server;
    private NotificationManager notificationManager;
    private NotificationCompat.Builder builder;
    private String notifChannelID = "StickyNotificationServer";

    public class LocalBinder extends Binder {
        ServerSercive getService() {
            return ServerSercive.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        server = new Server(8080, this);
        server.startServer();
        Log.v("Server", "URL is " + server.getURL());
        startForeground(server.hashCode(), getForegroundNotification());
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        server.stop();
    }

    private Notification getForegroundNotification() {
        notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) createChannel(notificationManager);
        builder = new NotificationCompat.Builder(getApplicationContext(), notifChannelID).setSmallIcon(R.drawable.fascist_logo).setContentTitle(getString(R.string.title_server_notification)).setContentText(getString(R.string.desc_server_notification));
        return builder.getNotification();
    }

    @TargetApi(26)
    private void createChannel(NotificationManager notificationManager) {
        String name = getString(R.string.channel_title_server_notification);
        String description = getString(R.string.channel_desc_server_notification);
        int importance = NotificationManager.IMPORTANCE_DEFAULT;

        NotificationChannel mChannel = new NotificationChannel(notifChannelID, name, importance);
        mChannel.setDescription(description);
        mChannel.enableLights(false);
        notificationManager.createNotificationChannel(mChannel);
    }

}
