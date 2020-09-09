package de.tobiundmario.secrethitlermobilecompanion;

import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import net.glxn.qrgen.android.QRCode;

import org.json.JSONException;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import de.tobiundmario.secrethitlermobilecompanion.SHCards.CardDialog;
import de.tobiundmario.secrethitlermobilecompanion.SHCards.GameEndCard;
import de.tobiundmario.secrethitlermobilecompanion.SHClasses.FascistTrackSelectionManager;
import de.tobiundmario.secrethitlermobilecompanion.SHClasses.GameLog;
import de.tobiundmario.secrethitlermobilecompanion.SHClasses.JSONManager;
import de.tobiundmario.secrethitlermobilecompanion.SHClasses.PlayerList;
import de.tobiundmario.secrethitlermobilecompanion.SHClasses.SharedPreferencesManager;
import de.tobiundmario.secrethitlermobilecompanion.SHEvents.DeckShuffledEvent;
import de.tobiundmario.secrethitlermobilecompanion.SHEvents.ExecutionEvent;
import de.tobiundmario.secrethitlermobilecompanion.SHEvents.LegislativeSession;
import de.tobiundmario.secrethitlermobilecompanion.SHEvents.LoyaltyInvestigationEvent;
import de.tobiundmario.secrethitlermobilecompanion.SHEvents.PolicyPeekEvent;
import de.tobiundmario.secrethitlermobilecompanion.SHEvents.SpecialElectionEvent;
import de.tobiundmario.secrethitlermobilecompanion.SHEvents.TopPolicyPlayedEvent;
import de.tobiundmario.secrethitlermobilecompanion.Server.ServerSercive;

import static android.content.Context.WIFI_SERVICE;

public class GameFragment extends Fragment {

    private Context context;

    RecyclerView cardList, playerCardList;

    private BottomNavigationView bottomNavigationMenu_game;

    private BottomSheetBehavior bottomSheetBehaviorAdd;
    private BottomSheetBehavior bottomSheetBehaviorServer;

    private String serverURL;
    private TextView tv_server_desc, tv_server_title;
    private FloatingActionButton fab_share, fab_copy, fab_toggle_server;
    private ImageView qrImage;
    private Bitmap qrBitmap;
    private Animation fab_close, fab_open;
    private boolean fabsVisible = true;

    private BroadcastReceiver serverPageUpdateReceiver;
    private IntentFilter serverUpdateFilter;

    private boolean started = false;

    boolean serverConnected = false;
    private ServerSercive boundServerService;
    private ServiceConnection serverServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            boundServerService = ((ServerSercive.LocalBinder)service).getService();
            serverConnected = true;
            setServerStatus();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) { //Reminder: This is only called when the service crashes, NOT when the user hits the stop button
            boundServerService = null;
            serverConnected = false;
        }
    };

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

        View view = getView();
        setupRecyclerViews(view);
        setupBottomMenu(view);

        if(GameLog.server) startAndBindServerService();

        try {
            SharedPreferencesManager.writeCurrentPlayerListIfNew(context);
            GameLog.backupToCache();
        } catch (JSONException | IOException e) {
            e.printStackTrace();
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
                    setServerStatus();
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
        if(GameLog.getEventsJSON().length() == 0) {
            endGame();
            return;
        }

        GameLog.addEvent(new GameEndCard(context));

        //Make the Menu non-functioning
        bottomNavigationMenu_game.getMenu().getItem(2).setCheckable(false);
        bottomNavigationMenu_game.getMenu().getItem(1).setCheckable(false);
        bottomNavigationMenu_game.setOnNavigationItemSelectedListener(null);
        deselectAllMenuItems();
        //Hide the BottomSheets
        bottomSheetBehaviorServer.setState(BottomSheetBehavior.STATE_HIDDEN);
        bottomSheetBehaviorAdd.setState(BottomSheetBehavior.STATE_HIDDEN);
        GameLog.swipeEnabled = false;
    }

    public void endGame() {
        GameLog.setGameStarted(false);
        stopAndUnbindServerService();

        GameLog.deleteBackup();

        Animation swipeOutBottom = new TranslateAnimation(0, 0, 0, 200);
        swipeOutBottom.setDuration(500);
        bottomNavigationMenu_game.startAnimation(swipeOutBottom);

        ((MainActivity) context).replaceFragment(MainActivity.main, true);
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
                GameLog.addEvent(new LegislativeSession(context));
            }
        });

        bottomSheetAdd.findViewById(R.id.deck_shuffled).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetBehaviorAdd.setState(BottomSheetBehavior.STATE_HIDDEN);
                GameLog.addEvent(new DeckShuffledEvent(context));
            }
        });

        View entry_loyaltyInvestigation = bottomSheetAdd.findViewById(R.id.loyalty_investigation);
        View entry_execution = bottomSheetAdd.findViewById(R.id.execution);
        View entry_policy_peek = bottomSheetAdd.findViewById(R.id.policy_peek);
        View entry_special_election = bottomSheetAdd.findViewById(R.id.special_election);
        View entry_top_policy = bottomSheetAdd.findViewById(R.id.topPolicy);

        if(!GameLog.gameTrack.isManualMode()) {
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
                    GameLog.addEvent(new LoyaltyInvestigationEvent(null, context));
                }
            });

            entry_execution.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    bottomSheetBehaviorAdd.setState(BottomSheetBehavior.STATE_HIDDEN);
                    GameLog.addEvent(new ExecutionEvent(null, context));
                }
            });

            entry_policy_peek.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    bottomSheetBehaviorAdd.setState(BottomSheetBehavior.STATE_HIDDEN);
                    GameLog.addEvent(new PolicyPeekEvent(null, context));
                }
            });

            entry_special_election.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    bottomSheetBehaviorAdd.setState(BottomSheetBehavior.STATE_HIDDEN);
                    GameLog.addEvent(new SpecialElectionEvent(null, context));
                }
            });

            entry_top_policy.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    bottomSheetBehaviorAdd.setState(BottomSheetBehavior.STATE_HIDDEN);
                    GameLog.addEvent(new TopPolicyPlayedEvent(context));
                }
            });
        }


        //Setting Up the Server Status Page
        ConstraintLayout bottomSheetServer = fragmentLayout.findViewById(R.id.bottom_sheet_server_status);
        bottomSheetBehaviorServer = BottomSheetBehavior.from(bottomSheetServer);
        bottomSheetBehaviorServer.setState(BottomSheetBehavior.STATE_HIDDEN);
        setupServerLayout(fragmentLayout);

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
        bottomNavigationMenu_game.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
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
                            setServerStatus();
                            bottomSheetBehaviorServer.setState(BottomSheetBehavior.STATE_EXPANDED);

                            //Check if Add page is open. If so, we close it
                            if(bottomSheetBehaviorAdd.getState() != BottomSheetBehavior.STATE_HIDDEN) bottomSheetBehaviorAdd.setState(BottomSheetBehavior.STATE_HIDDEN);

                        } else bottomSheetBehaviorServer.setState(BottomSheetBehavior.STATE_HIDDEN); //If it is already open and the user clicks it again, it should hide
                        break;
                }
                return true;
            }
        });
    }



    public void setupServerLayout(View fragmentLayout) {
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
                ClipData clip = ClipData.newPlainText("Server URL", serverURL);
                clipboard.setPrimaryClip(clip);

                Toast.makeText(context, getString(R.string.url_copied_to_clipboard), Toast.LENGTH_SHORT).show();
            }
        });

        fab_share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent share = new Intent(Intent.ACTION_SEND);
                share.setType("text/plain");
                share.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_server_url_string, serverURL));
                startActivity(Intent.createChooser(share, getString(R.string.share_server_url_title)));
            }
        });

        qrImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(serverURL));
                startActivity(browserIntent);
            }
        });

        fab_toggle_server.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(serverConnected) {
                    stopAndUnbindServerService();

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        tv_server_title.setText(Html.fromHtml(getString(R.string.title_server_status) + " <font color='#ff9900'>" + getString(R.string.server_stopping) + "</font>",  Html.FROM_HTML_MODE_LEGACY), TextView.BufferType.SPANNABLE);
                    } else {
                        tv_server_title.setText(Html.fromHtml(getString(R.string.title_server_status) + " <font color='#ff9900'>" + getString(R.string.server_stopping) + "</font>"), TextView.BufferType.SPANNABLE);
                    }
                } else {
                    startAndBindServerService();

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        tv_server_title.setText(Html.fromHtml(getString(R.string.title_server_status) + " <font color='#ff9900'>" + getString(R.string.server_starting) + "</font>",  Html.FROM_HTML_MODE_LEGACY), TextView.BufferType.SPANNABLE);
                    } else {
                        tv_server_title.setText(Html.fromHtml(getString(R.string.title_server_status) + " <font color='#ff9900'>" + getString(R.string.server_starting) + "</font>"), TextView.BufferType.SPANNABLE);
                    }
                }
            }
        });
    }

    public void setServerStatus() {
        boolean isMobile = false, isWifi = false;

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork != null) {
            // connected to the internet
            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                // connected to wifi
                isWifi = true;
            } else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                // connected to mobile data
                isMobile = true;
            }
        }
        boolean usingHotspot = isUsingHotspot((WifiManager) context.getApplicationContext().getSystemService(WIFI_SERVICE));

        if(boundServerService != null && boundServerService.server.isAlive()) { //ServerService is bound and Server is running

            if(!isMobile && !isWifi && !usingHotspot) {//Server is running but device is not connected to a network

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    tv_server_title.setText(Html.fromHtml(getString(R.string.title_server_status) + " <font color='#ff9900'>" + getString(R.string.server_status_not_connected) + "</font>",  Html.FROM_HTML_MODE_LEGACY), TextView.BufferType.SPANNABLE);
                } else {
                    tv_server_title.setText(Html.fromHtml(getString(R.string.title_server_status) + " <font color='#ff9900'>" + getString(R.string.server_status_not_connected) + "</font>"), TextView.BufferType.SPANNABLE);
                }

                qrImage.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.qr_placeholder));
                qrImage.setClickable(false);

                startFABAnimation(false);
                tv_server_desc.setText(getString(R.string.server_status_url_not_connected));

            } else if(isMobile && !usingHotspot && !isWifi) {//Only connected to mobile data

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    tv_server_title.setText(Html.fromHtml(getString(R.string.title_server_status) + " <font color='#ff9900'>" + getString(R.string.server_status_using_mobile_data) + "</font>",  Html.FROM_HTML_MODE_LEGACY), TextView.BufferType.SPANNABLE);
                } else {
                    tv_server_title.setText(Html.fromHtml(getString(R.string.title_server_status) + " <font color='#ff9900'>" + getString(R.string.server_status_using_mobile_data) + "</font>"), TextView.BufferType.SPANNABLE);
                }

                qrImage.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.qr_placeholder));
                qrImage.setClickable(false);

                startFABAnimation(false);
                tv_server_desc.setText(getString(R.string.server_status_url_mobile_data));

            } else {//everything is fine
                if(serverURL == null || !serverURL.equals(boundServerService.server.getURL())) { //Only recreate the QR Code when the URL changed
                    serverURL = boundServerService.server.getURL();

                    qrBitmap = QRCode.from(serverURL).withSize(200,200).bitmap();
                }

                qrImage.setImageBitmap(qrBitmap);
                qrImage.setClickable(true);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    tv_server_desc.setText(Html.fromHtml(getString(R.string.server_status_url, serverURL),  Html.FROM_HTML_MODE_LEGACY), TextView.BufferType.SPANNABLE);
                } else {
                    tv_server_desc.setText(Html.fromHtml(getString(R.string.server_status_url, serverURL)), TextView.BufferType.SPANNABLE);
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    tv_server_title.setText(Html.fromHtml(getString(R.string.title_server_status) + " <font color='#009933'>" + getString(R.string.server_running) + "</font>",  Html.FROM_HTML_MODE_LEGACY), TextView.BufferType.SPANNABLE);
                } else {
                    tv_server_title.setText(Html.fromHtml(getString(R.string.title_server_status) + " <font color='#009933'>" + getString(R.string.server_running) + "</font>"), TextView.BufferType.SPANNABLE);
                }

                startFABAnimation(true);
            }

            ColorStateList colorStopServer = ColorStateList.valueOf(context.getColor(R.color.stop_server));
            fab_toggle_server.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_stop));
            fab_toggle_server.setBackgroundTintList(colorStopServer);

        } else {
            tv_server_desc.setText(getString(R.string.server_status_url_disabled));

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                tv_server_title.setText(Html.fromHtml(getString(R.string.title_server_status) + " <font color='#cc0000'>" + getString(R.string.server_stopped) + "</font>",  Html.FROM_HTML_MODE_LEGACY), TextView.BufferType.SPANNABLE);
            } else {
                tv_server_title.setText(Html.fromHtml(getString(R.string.title_server_status) + " <font color='#cc0000'>" + getString(R.string.server_stopped) + "</font>"), TextView.BufferType.SPANNABLE);
            }

            qrImage.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.qr_placeholder));
            qrImage.setClickable(false);

            startFABAnimation(false);

            ColorStateList colorStartServer = ColorStateList.valueOf(context.getColor(R.color.start_server));
            fab_toggle_server.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_start));
            fab_toggle_server.setBackgroundTintList(colorStartServer);
        }
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
            e.printStackTrace();
        }
        return actualState == 13; //public static int AP_STATE_ENABLED = 13;
    }


    public void setupRecyclerViews(View fragmentLayout) {
        cardList = fragmentLayout.findViewById(R.id.cardList);
        cardList.setLayoutManager(new LinearLayoutManager(context, RecyclerView.VERTICAL, false));
        GameLog.initialise(cardList, context);
        GameLog.setGameStarted(true);

        playerCardList = fragmentLayout.findViewById(R.id.playerList);
        playerCardList.setLayoutManager(new LinearLayoutManager(context, RecyclerView.HORIZONTAL, false));
        PlayerList.changeRecyclerView(playerCardList);
    }

    private void startAndBindServerService() {
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

    void stopAndUnbindServerService() {
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