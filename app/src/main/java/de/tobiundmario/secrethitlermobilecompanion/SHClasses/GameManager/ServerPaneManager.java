package de.tobiundmario.secrethitlermobilecompanion.SHClasses.GameManager;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import net.glxn.qrgen.android.QRCode;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

import de.tobiundmario.secrethitlermobilecompanion.GameFragment;
import de.tobiundmario.secrethitlermobilecompanion.R;
import de.tobiundmario.secrethitlermobilecompanion.Server.Server;
import de.tobiundmario.secrethitlermobilecompanion.Server.ServerSercive;

import static android.content.Context.WIFI_SERVICE;

public class ServerPaneManager {

    private GameFragment gameFragment;
    private final Context context;

    private boolean fabsVisible = true;

    private String serverURL;
    private Bitmap qrBitmap;

    private FloatingActionButton fab_share, fab_copy, fab_toggle_server;
    private TextView tv_server_desc, tv_server_title;
    private ImageView qrImage;
    private Animation fab_close, fab_open;

    public ServerPaneManager(GameFragment gameFragment) {
        this.gameFragment = gameFragment;
        context =  gameFragment.getContext();
    }

    public static void setSpannable(TextView textView, String spannableText) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            textView.setText(Html.fromHtml(spannableText,  Html.FROM_HTML_MODE_LEGACY), TextView.BufferType.SPANNABLE);
        } else {
            textView.setText(Html.fromHtml(spannableText), TextView.BufferType.SPANNABLE);
        }
    }

    public void setupServerLayout(View fragmentLayout) {
        ServerSercive serverSercive = gameFragment.getBoundServerService();
        final String serverURL = (serverSercive == null) ? null : serverSercive.server.getURL();

        tv_server_desc = fragmentLayout.findViewById(R.id.tv_server_url_desc);
        tv_server_title = fragmentLayout.findViewById(R.id.tv_title_server_status);

        qrImage = fragmentLayout.findViewById(R.id.img_qr);

        fab_share = fragmentLayout.findViewById(R.id.fab_share);
        fab_copy = fragmentLayout.findViewById(R.id.fab_copy_address);
        fab_toggle_server = fragmentLayout.findViewById(R.id.fab_toggle_server);

        fab_close = AnimationUtils.loadAnimation(context, R.anim.fab_close);
        fab_open = AnimationUtils.loadAnimation(context, R.anim.fab_open);

        fab_copy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Server URL", gameFragment.getBoundServerService().server.getURL());
                clipboard.setPrimaryClip(clip);

                Toast.makeText(context, context.getString(R.string.url_copied_to_clipboard), Toast.LENGTH_SHORT).show();
            }
        });

        fab_share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent share = new Intent(Intent.ACTION_SEND);
                share.setType("text/plain");
                share.putExtra(Intent.EXTRA_TEXT, context.getString(R.string.share_server_url_string, gameFragment.getBoundServerService().server.getURL()));
                context.startActivity(Intent.createChooser(share, context.getString(R.string.share_server_url_title)));
            }
        });

        qrImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(gameFragment.getBoundServerService().server.getURL()));
                context.startActivity(browserIntent);
            }
        });

        fab_toggle_server.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String statusString;
                if(gameFragment.isServerConnected()) {
                    gameFragment.stopAndUnbindServerService();
                    statusString = context.getString(R.string.server_stopping);
                } else {
                    gameFragment.startAndBindServerService();
                    statusString = context.getString(R.string.server_starting);
                }
                setSpannable(tv_server_title, context.getString(R.string.title_server_status) + " <font color='#ff9900'>" + statusString + "</font>");
            }
        });
    }



    public void setServerStatus() {
        int connectionType = Server.getConnectionType(context);

        boolean usingHotspot = isUsingHotspot((WifiManager) context.getApplicationContext().getSystemService(WIFI_SERVICE));

        ServerSercive boundServerService = gameFragment.getBoundServerService();

        String titleColor = "", titleServerStatus = "", serverDescText = "";
        ColorStateList fabColorScheme;
        Drawable fab_icon;
        if(boundServerService != null && boundServerService.server.isAlive()) { //ServerService is bound and Server is running

            if(connectionType == Server.NOT_CONNECTED && !usingHotspot) {//Server is running but device is not connected to a network

                titleColor = "#ff9900";
                titleServerStatus = context.getString(R.string.server_status_not_connected);
                serverDescText = context.getString(R.string.server_status_url_not_connected);

                qrImage.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.qr_placeholder));
                qrImage.setClickable(false);

                startFABAnimation(false);
            } else if(connectionType == Server.MOBILE_DATA && !usingHotspot) {//Only connected to mobile data

                titleColor = "#ff9900";
                titleServerStatus = context.getString(R.string.server_status_using_mobile_data);
                serverDescText = context.getString(R.string.server_status_url_mobile_data);

                qrImage.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.qr_placeholder));
                qrImage.setClickable(false);

                startFABAnimation(false);
            } else {//everything is fine
                if(serverURL == null || !serverURL.equals(boundServerService.server.getURL())) { //Only recreate the QR Code when the URL changed
                    serverURL = boundServerService.server.getURL();

                    qrBitmap = QRCode.from(serverURL).withSize(200,200).bitmap();
                }

                qrImage.setImageBitmap(qrBitmap);
                qrImage.setClickable(true);

                serverDescText = context.getString(R.string.server_status_url, serverURL);
                titleColor = "#009933";
                titleServerStatus = context.getString(R.string.server_running);

                startFABAnimation(true);
            }

            fabColorScheme = ColorStateList.valueOf(context.getColor(R.color.stop_server));
            fab_icon = ContextCompat.getDrawable(context, R.drawable.ic_stop);
        } else {
            serverDescText = context.getString(R.string.server_status_url_disabled);
            titleColor = "#cc0000";
            titleServerStatus = context.getString(R.string.server_stopped);

            qrImage.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.qr_placeholder));
            qrImage.setClickable(false);

            startFABAnimation(false);

            fabColorScheme = ColorStateList.valueOf(context.getColor(R.color.start_server));
            fab_icon = ContextCompat.getDrawable(context, R.drawable.ic_start);
        }

        setSpannable(tv_server_desc, serverDescText);
        setSpannable(tv_server_title, context.getString(R.string.title_server_status) + " <font color='" + titleColor + "'>" + titleServerStatus + "</font>");

        fab_toggle_server.setImageDrawable(fab_icon);
        fab_toggle_server.setBackgroundTintList(fabColorScheme);
    }

    private void startFABAnimation(boolean open) {
        if(!open && fabsVisible) {//Fabs are visible, hiding
            fab_share.startAnimation(fab_close);
            fab_copy.startAnimation(fab_close);
            fabsVisible = false;
        } else if(open && !fabsVisible){
            fab_share.startAnimation(fab_open);
            fab_copy.startAnimation(fab_open);
            fabsVisible= true;
        }
    }

    public boolean isUsingHotspot(WifiManager wifiManager) {
        int actualState = 0;
        try {
            java.lang.reflect.Method method = wifiManager.getClass().getDeclaredMethod("getWifiApState");
            method.setAccessible(true);
            actualState = (Integer) method.invoke(wifiManager, (Object[]) null);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            Log.e("Error", Arrays.toString(e.getStackTrace()));
        }
        return actualState == 13; //public static int AP_STATE_ENABLED = 13;
    }
}
