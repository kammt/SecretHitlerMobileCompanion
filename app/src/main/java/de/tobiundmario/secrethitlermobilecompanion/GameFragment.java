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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import org.json.JSONException;

import java.io.IOException;
import java.util.Arrays;

import de.tobiundmario.secrethitlermobilecompanion.SHCards.CardDialog;
import de.tobiundmario.secrethitlermobilecompanion.SHCards.GameEndCard;
import de.tobiundmario.secrethitlermobilecompanion.SHClasses.FascistTrackSelectionManager;
import de.tobiundmario.secrethitlermobilecompanion.SHClasses.GameManager.BackupManager;
import de.tobiundmario.secrethitlermobilecompanion.SHClasses.GameManager.GameEventsManager;
import de.tobiundmario.secrethitlermobilecompanion.SHClasses.GameManager.GameManager;
import de.tobiundmario.secrethitlermobilecompanion.SHClasses.GameManager.JSONManager;
import de.tobiundmario.secrethitlermobilecompanion.SHClasses.GameManager.PlayerListManager;
import de.tobiundmario.secrethitlermobilecompanion.SHClasses.GameManager.RecyclerViewManager;
import de.tobiundmario.secrethitlermobilecompanion.SHClasses.GameManager.ServerPaneManager;
import de.tobiundmario.secrethitlermobilecompanion.SHClasses.SharedPreferencesManager;
import de.tobiundmario.secrethitlermobilecompanion.SHEvents.DeckShuffledEvent;
import de.tobiundmario.secrethitlermobilecompanion.SHEvents.ExecutionEvent;
import de.tobiundmario.secrethitlermobilecompanion.SHEvents.LegislativeSession;
import de.tobiundmario.secrethitlermobilecompanion.SHEvents.LoyaltyInvestigationEvent;
import de.tobiundmario.secrethitlermobilecompanion.SHEvents.PolicyPeekEvent;
import de.tobiundmario.secrethitlermobilecompanion.SHEvents.SpecialElectionEvent;
import de.tobiundmario.secrethitlermobilecompanion.SHEvents.TopPolicyPlayedEvent;
import de.tobiundmario.secrethitlermobilecompanion.Server.ServerSercive;

public class GameFragment extends Fragment {

    private Context context;

    RecyclerView cardList, playerCardList;

    private BottomNavigationView bottomNavigationMenu_game;
    private BottomNavigationView.OnNavigationItemSelectedListener onNavigationItemSelectedListener;

    private BottomSheetBehavior bottomSheetBehaviorAdd;
    private BottomSheetBehavior bottomSheetBehaviorServer;

    private BroadcastReceiver serverPageUpdateReceiver;
    private IntentFilter serverUpdateFilter;

    private boolean started = false;

    private ServerPaneManager serverPaneManager;
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

        View view = getView();
        setupRecyclerViews(view);
        setupBottomMenu(view);

        if(GameEventsManager.server) startAndBindServerService();

        try {
            SharedPreferencesManager.writeCurrentPlayerListIfNew(context);
            BackupManager.backupToCache();
        } catch (JSONException | IOException e) {
            Log.e("Error", Arrays.toString(e.getStackTrace()));
        }

        bottomNavigationMenu_game.setVisibility(View.VISIBLE);
        bottomNavigationMenu_game.startAnimation(AnimationUtils.loadAnimation(context, R.anim.slide_in_bottom));

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

        GameEventsManager.addEvent(new GameEndCard(context));

        //Make the Menu non-functioning
        bottomNavigationMenu_game.getMenu().getItem(2).setCheckable(false);
        bottomNavigationMenu_game.getMenu().getItem(1).setCheckable(false);
        bottomNavigationMenu_game.setOnNavigationItemSelectedListener(null);
        deselectAllMenuItems();
        //Hide the BottomSheets
        bottomSheetBehaviorServer.setState(BottomSheetBehavior.STATE_HIDDEN);
        bottomSheetBehaviorAdd.setState(BottomSheetBehavior.STATE_HIDDEN);
        RecyclerViewManager.swipeEnabled = false;
        GameEventsManager.editingEnabled = false;
    }

    public void undoEndGameOptions() {
        bottomNavigationMenu_game.getMenu().getItem(2).setCheckable(true);
        bottomNavigationMenu_game.getMenu().getItem(1).setCheckable(true);
        bottomNavigationMenu_game.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener);
        RecyclerViewManager.swipeEnabled = true;
        GameEventsManager.editingEnabled = true;
    }

    public void endGame() {
        GameManager.setGameStarted(false);
        stopAndUnbindServerService();

        BackupManager.deleteBackup();

        Animation swipeOutBottom = new TranslateAnimation(0, 0, 0, 200);
        swipeOutBottom.setDuration(500);
        bottomNavigationMenu_game.startAnimation(swipeOutBottom);

        ((MainActivity) context).replaceFragment(MainActivity.page_main, true);
    }




    /*
    These functions below are necessary for layout initialisation
     */

    public void deselectAllMenuItems() {
        Menu menu = bottomNavigationMenu_game.getMenu();
        menu.getItem(0).setVisible(false);
        menu.getItem(0).setChecked(true);
    }

    public void setupBottomMenu(View fragmentLayout) {
        //Setting up the Bottom Menu
        bottomNavigationMenu_game = fragmentLayout.findViewById(R.id.bottomNavigationView_game);
        Menu menu = bottomNavigationMenu_game.getMenu();
        menu.getItem(0).setVisible(false);
        menu.getItem(0).setChecked(true); //As you cannot have no items selected, I created a third item, select that one and set it as hidden #Lifehack

        //When the game ends, the Menu items are disabled. Hence, we enable them again just in case
        bottomNavigationMenu_game.getMenu().getItem(2).setCheckable(true);
        bottomNavigationMenu_game.getMenu().getItem(1).setCheckable(true);

        //initialising the "Add Event" bottom Sheet
        ConstraintLayout bottomSheetAdd = fragmentLayout.findViewById(R.id.bottom_sheet_add_event);
        bottomSheetBehaviorAdd = BottomSheetBehavior.from(bottomSheetAdd);
        bottomSheetBehaviorAdd.setState(BottomSheetBehavior.STATE_HIDDEN);

        /*
        Setting up the OnClickListeners. For this, we get each ConstraintLayout by using fragmentLayout.findViewById
        However, we have to differentiate here, as we only want Legislative Session and Deck shuffled to be visible when manual mode is enabled
         */
        bottomSheetAdd.findViewById(R.id.legislative_session).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetBehaviorAdd.setState(BottomSheetBehavior.STATE_HIDDEN);
                GameEventsManager.addEvent(new LegislativeSession(context));
            }
        });

        bottomSheetAdd.findViewById(R.id.deck_shuffled).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetBehaviorAdd.setState(BottomSheetBehavior.STATE_HIDDEN);
                GameEventsManager.addEvent(new DeckShuffledEvent(context));
            }
        });

        View entry_loyaltyInvestigation = bottomSheetAdd.findViewById(R.id.loyalty_investigation);
        View entry_execution = bottomSheetAdd.findViewById(R.id.execution);
        View entry_policy_peek = bottomSheetAdd.findViewById(R.id.policy_peek);
        View entry_special_election = bottomSheetAdd.findViewById(R.id.special_election);
        View entry_top_policy = bottomSheetAdd.findViewById(R.id.topPolicy);

        if(!GameManager.gameTrack.isManualMode()) {
            entry_loyaltyInvestigation.setVisibility(View.GONE);
            entry_execution.setVisibility(View.GONE);
            entry_policy_peek.setVisibility(View.GONE);
            entry_special_election.setVisibility(View.GONE);
            entry_top_policy.setVisibility(View.GONE);
        } else {
            entry_loyaltyInvestigation.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    bottomSheetBehaviorAdd.setState(BottomSheetBehavior.STATE_HIDDEN);
                    GameEventsManager.addEvent(new LoyaltyInvestigationEvent(null, context));
                }
            });

            entry_execution.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    bottomSheetBehaviorAdd.setState(BottomSheetBehavior.STATE_HIDDEN);
                    GameEventsManager.addEvent(new ExecutionEvent(null, context));
                }
            });

            entry_policy_peek.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    bottomSheetBehaviorAdd.setState(BottomSheetBehavior.STATE_HIDDEN);
                    GameEventsManager.addEvent(new PolicyPeekEvent(null, context));
                }
            });

            entry_special_election.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    bottomSheetBehaviorAdd.setState(BottomSheetBehavior.STATE_HIDDEN);
                    GameEventsManager.addEvent(new SpecialElectionEvent(null, context));
                }
            });

            entry_top_policy.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    bottomSheetBehaviorAdd.setState(BottomSheetBehavior.STATE_HIDDEN);
                    GameEventsManager.addEvent(new TopPolicyPlayedEvent(context));
                }
            });
        }


        //Setting Up the Server Status Page
        ConstraintLayout bottomSheetServer = fragmentLayout.findViewById(R.id.bottom_sheet_server_status);
        bottomSheetBehaviorServer = BottomSheetBehavior.from(bottomSheetServer);
        bottomSheetBehaviorServer.setState(BottomSheetBehavior.STATE_HIDDEN);
        serverPaneManager.setupServerLayout(fragmentLayout);

        //Setting up the BottomSheetCallback
        BottomSheetBehavior.BottomSheetCallback callbackAdd = new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if(newState == BottomSheetBehavior.STATE_HIDDEN && bottomSheetBehaviorServer.getState() == BottomSheetBehavior.STATE_HIDDEN) { //If the state changed to hidden (i.e. the user closed the menu), the item should now be unselected
                    deselectAllMenuItems();
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) { }
        };

        BottomSheetBehavior.BottomSheetCallback callbackServer = new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if(newState == BottomSheetBehavior.STATE_HIDDEN && bottomSheetBehaviorAdd.getState() == BottomSheetBehavior.STATE_HIDDEN) { //If the state changed to hidden (i.e. the user closed the menu), the item should now be unselected
                    deselectAllMenuItems();
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) { }
        };
        bottomSheetBehaviorAdd.addBottomSheetCallback(callbackAdd);
        bottomSheetBehaviorServer.addBottomSheetCallback(callbackServer);

        //Adding the Listener to the BottomNavigationMenu
        onNavigationItemSelectedListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch(item.getItemId()) {
                    case R.id.navigation_add_event:
                        if(bottomSheetBehaviorAdd.getState() == BottomSheetBehavior.STATE_HIDDEN) {
                            bottomSheetBehaviorAdd.setState(BottomSheetBehavior.STATE_EXPANDED);

                            //Check if Server page is open. If so, we close it
                            if(bottomSheetBehaviorServer.getState() != BottomSheetBehavior.STATE_HIDDEN) bottomSheetBehaviorServer.setState(BottomSheetBehavior.STATE_HIDDEN);

                        } else bottomSheetBehaviorAdd.setState(BottomSheetBehavior.STATE_HIDDEN); //If it is already open and the user clicks it again, it should hide
                        break;
                    case R.id.navigation_server_status:
                        if(bottomSheetBehaviorServer.getState() == BottomSheetBehavior.STATE_HIDDEN) {
                            serverPaneManager.setServerStatus();
                            bottomSheetBehaviorServer.setState(BottomSheetBehavior.STATE_EXPANDED);

                            //Check if Add page is open. If so, we close it
                            if(bottomSheetBehaviorAdd.getState() != BottomSheetBehavior.STATE_HIDDEN) bottomSheetBehaviorAdd.setState(BottomSheetBehavior.STATE_HIDDEN);

                        } else bottomSheetBehaviorServer.setState(BottomSheetBehavior.STATE_HIDDEN); //If it is already open and the user clicks it again, it should hide
                        break;
                }
                return true;
            }
        };
        bottomNavigationMenu_game.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener);
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