package de.tobiundmario.secrethitlermobilecompanion.Server;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import de.tobiundmario.secrethitlermobilecompanion.R;

public class ServerSercive extends Service {

    private final IBinder mBinder = new LocalBinder();
    public Server server;
    private String notifChannelID = "StickyNotificationServer";

    private BroadcastReceiver killSignalReceiver;
    public final static String ACTION_KILL_SERVER = "KILLSERVER";
    public final static String SERVER_STATE_CHANGED = "CHANGED";

    public class LocalBinder extends Binder {
        public ServerSercive getService() {
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
        IntentFilter filter = new IntentFilter();
        filter.addAction(ServerSercive.ACTION_KILL_SERVER);
        killSignalReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent != null) {
                    //Server is intended to be killed
                    killSelf();
                }
            }
        };
        registerReceiver(killSignalReceiver, filter);

        server = new Server(8080, this);
        server.startServer();
        Log.v("Server", "URL is " + server.getURL());

        //Notify the MainActivity that the Server was started
        Intent stoppedIntent = new Intent();
        stoppedIntent.setAction(SERVER_STATE_CHANGED);
        PendingIntent stoppedPendingIntent =
                PendingIntent.getBroadcast(this, 0, stoppedIntent, 0);
        try {
            stoppedPendingIntent.send();
        } catch (PendingIntent.CanceledException e) {
            e.printStackTrace();
        }

        startForeground(server.hashCode(), getForegroundNotification());
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        while(server.isAlive()) server.stop();
        unregisterReceiver(killSignalReceiver);
    }

    public void killSelf() {
        while(server.isAlive()) server.stop();
        stopForeground(true);

        //Notify the MainActivity that the Server was killed
        Intent stoppedIntent = new Intent();
        stoppedIntent.setAction(SERVER_STATE_CHANGED);
        PendingIntent stoppedPendingIntent =
                PendingIntent.getBroadcast(this, 0, stoppedIntent, 0);
        try {
            stoppedPendingIntent.send();
        } catch (PendingIntent.CanceledException e) {
            e.printStackTrace();
        }

        //Finally, kill the entire Service
        stopSelf();
    }

    private Notification getForegroundNotification() {
        Intent stopIntent = new Intent();
        stopIntent.setAction(ACTION_KILL_SERVER);
        PendingIntent stopPendingIntent =
                PendingIntent.getBroadcast(this, 0, stopIntent, 0);


        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) createChannel(notificationManager);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), notifChannelID)
                .setSmallIcon(R.drawable.ic_stat_name)
                .setContentTitle(getString(R.string.title_server_notification))
                .setContentText(getString(R.string.desc_server_notification))
                .addAction(R.drawable.execution, getString(R.string.stop_server_notification),
                        stopPendingIntent);

        return builder.getNotification();
    }

    @TargetApi(26)
    private void createChannel(NotificationManager notificationManager) {
        String name = getString(R.string.channel_title_server_notification);
        String description = getString(R.string.channel_desc_server_notification);
        int importance = NotificationManager.IMPORTANCE_LOW;

        NotificationChannel mChannel = new NotificationChannel(notifChannelID, name, importance);
        mChannel.setDescription(description);
        mChannel.enableLights(false);
        notificationManager.createNotificationChannel(mChannel);
    }

}
