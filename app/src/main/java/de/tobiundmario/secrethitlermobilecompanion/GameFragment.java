package de.tobiundmario.secrethitlermobilecompanion;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONException;

import java.io.IOException;

import de.tobiundmario.secrethitlermobilecompanion.SHCards.CardDialog;
import de.tobiundmario.secrethitlermobilecompanion.SHCards.GameEndCard;
import de.tobiundmario.secrethitlermobilecompanion.SHClasses.FascistTrackSelectionManager;
import de.tobiundmario.secrethitlermobilecompanion.SHClasses.GameManager.BackupManager;
import de.tobiundmario.secrethitlermobilecompanion.SHClasses.GameManager.BottomSheetMenuManager;
import de.tobiundmario.secrethitlermobilecompanion.SHClasses.GameManager.GameEventsManager;
import de.tobiundmario.secrethitlermobilecompanion.SHClasses.GameManager.GameManager;
import de.tobiundmario.secrethitlermobilecompanion.SHClasses.GameManager.JSONManager;
import de.tobiundmario.secrethitlermobilecompanion.SHClasses.GameManager.PlayerListManager;
import de.tobiundmario.secrethitlermobilecompanion.SHClasses.GameManager.RecyclerViewManager;
import de.tobiundmario.secrethitlermobilecompanion.SHClasses.GameManager.ServerPaneManager;
import de.tobiundmario.secrethitlermobilecompanion.SHClasses.SharedPreferencesManager;
import de.tobiundmario.secrethitlermobilecompanion.Server.ServerSercive;

public class GameFragment extends Fragment {

    private Context context;

    RecyclerView cardList, playerCardList;

    private BroadcastReceiver serverPageUpdateReceiver;
    private IntentFilter serverUpdateFilter;

    private boolean started = false;

    private ServerPaneManager serverPaneManager;
    private BottomSheetMenuManager bottomSheetMenuManager;

    boolean serverConnected = false;
    private ServerSercive boundServerService;
    private ServiceConnection serverServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            boundServerService = ((ServerSercive.LocalBinder)service).getService();
            serverConnected = true;
            serverPaneManager.setServerStatus();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) { //Reminder: This is only called when the service crashes, NOT when the user hits the stop button
            boundServerService = null;
            serverConnected = false;
        }
    };

    public ServerSercive getBoundServerService() {
        return boundServerService;
    }

    public ServerPaneManager getServerPaneManager() {
        return serverPaneManager;
    }

    public boolean isServerConnected() {
        return serverConnected;
    }

    public GameFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_game, container, false);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopAndUnbindServerService();
    }

    public void start() {
        //Destroying the Setup to prevent memory leaks
        FascistTrackSelectionManager.destroy();
        CardDialog.destroy();
        SharedPreferencesManager.destroy();

        JSONManager.initialise();
        serverPaneManager = new ServerPaneManager(GameFragment.this);
        bottomSheetMenuManager = new BottomSheetMenuManager(GameFragment.this);

        View view = getView();
        setupRecyclerViews(view);
        bottomSheetMenuManager.setupBottomMenu(view);

        if(GameEventsManager.server) startAndBindServerService();

        bottomSheetMenuManager.animateMenuBar(true);

        //Creating the BroadcastReceiver
        serverUpdateFilter = new IntentFilter();
        serverUpdateFilter.addAction(ServerSercive.SERVER_STATE_CHANGED); //This action is used by the server to send updates to the MainActivity: When it is started or when it is killed
        serverUpdateFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION); //This action is used when a network change has taken place (e.g. disconnected from WiFi) It however does not send updates when the user enables/disables mobile hotspot
        serverUpdateFilter.addAction("android.net.wifi.WIFI_AP_STATE_CHANGED"); //This action is used when the mobile hotspot state changes

        serverPageUpdateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent != null) {
                    serverPaneManager.setServerStatus();
                }
            }
        };
        context.registerReceiver(serverPageUpdateReceiver, serverUpdateFilter);

        started = true;
    }

    private void performGameBackup() {
        try {
            SharedPreferencesManager.writeCurrentPlayerListIfNew(context);
            BackupManager.backupToCache();
        } catch (JSONException | IOException e) {
            ExceptionHandler.showErrorSnackbar(e, "GameFragement.performGameBackup()");
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if(started) context.unregisterReceiver(serverPageUpdateReceiver);
    }

    @Override
    public void onResume() {
        super.onResume();
        if(started) context.registerReceiver(serverPageUpdateReceiver, serverUpdateFilter);
    }

    /*
    These functions below are necessary for controlling certain game aspects (e.g. stopping the game)
     */

    public void displayEndGameOptions() {
        //The game has no events, so there is no point in creating the "Game Ended" dialogue. The game will end immediately
        if(GameEventsManager.jsonData.length() == 0) {
            endGame();
            return;
        }

        bottomSheetMenuManager.disableMenuBar();
        GameEventsManager.addEvent(new GameEndCard(context));
        RecyclerViewManager.swipeEnabled = false;
        GameEventsManager.editingEnabled = false;
    }

    public void undoEndGameOptions() {
        bottomSheetMenuManager.enableMenuBar();
        RecyclerViewManager.swipeEnabled = true;
        GameEventsManager.editingEnabled = true;
    }

    public void endGame() {
        GameManager.setGameStarted(false);
        stopAndUnbindServerService();

        BackupManager.deleteBackup();

        bottomSheetMenuManager.animateMenuBar(false);

        ((MainActivity) context).replaceFragment(MainActivity.page_main, true);
    }

    public void setupRecyclerViews(View fragmentLayout) {
        cardList = fragmentLayout.findViewById(R.id.cardList);
        cardList.setLayoutManager(new LinearLayoutManager(context, RecyclerView.VERTICAL, false));
        GameEventsManager.initialise(cardList, context);
        GameManager.setGameStarted(true);

        playerCardList = fragmentLayout.findViewById(R.id.playerList);
        playerCardList.setLayoutManager(new LinearLayoutManager(context, RecyclerView.HORIZONTAL, false));
        PlayerListManager.changeRecyclerView(playerCardList);
    }

    public void startAndBindServerService() {
        if(boundServerService != null && serverConnected) { //Don't start another service if a server is already running
            if(!boundServerService.server.isAlive()) boundServerService.server.startServer(); //If it is stopped, then start it
            return;
        }

        //Starting with Android O, a ForeGroundService must be called using a different function
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(new Intent(context, ServerSercive.class));
        } else {
            context.startService(new Intent(context, ServerSercive.class));
        }

        context.bindService(new Intent(context, ServerSercive.class),
                serverServiceConnection,
                Context.BIND_AUTO_CREATE);
        serverConnected = true;
    }

    public void stopAndUnbindServerService() {
        if (serverConnected) {
            //Stop server
            boundServerService.killSelf();

            // Detach our existing connection.
            context.unbindService(serverServiceConnection);
            serverConnected = false;
            boundServerService = null;
        }
    }
}