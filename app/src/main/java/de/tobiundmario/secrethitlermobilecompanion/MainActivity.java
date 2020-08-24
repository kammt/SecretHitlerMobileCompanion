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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Transformation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import net.glxn.qrgen.android.QRCode;

import org.json.JSONException;

import java.lang.reflect.InvocationTargetException;

import de.tobiundmario.secrethitlermobilecompanion.SHCards.CardDialog;
import de.tobiundmario.secrethitlermobilecompanion.SHCards.GameEndCard;
import de.tobiundmario.secrethitlermobilecompanion.SHClasses.FascistTrack;
import de.tobiundmario.secrethitlermobilecompanion.SHClasses.GameLog;
import de.tobiundmario.secrethitlermobilecompanion.SHClasses.PlayerList;
import de.tobiundmario.secrethitlermobilecompanion.SHClasses.PreferencesManager;
import de.tobiundmario.secrethitlermobilecompanion.SHEvents.DeckShuffledEvent;
import de.tobiundmario.secrethitlermobilecompanion.SHEvents.ExecutionEvent;
import de.tobiundmario.secrethitlermobilecompanion.SHEvents.LegislativeSession;
import de.tobiundmario.secrethitlermobilecompanion.SHEvents.LoyaltyInvestigationEvent;
import de.tobiundmario.secrethitlermobilecompanion.SHEvents.PolicyPeekEvent;
import de.tobiundmario.secrethitlermobilecompanion.SHEvents.SpecialElectionEvent;
import de.tobiundmario.secrethitlermobilecompanion.Server.ServerSercive;

public class MainActivity extends AppCompatActivity {

    RecyclerView cardList, playerCardList;

    private ServerSercive boundServerService;

    private BottomNavigationView bottomNavigationMenu_game;
    private ConstraintLayout bottomSheetAdd;
    private BottomSheetBehavior bottomSheetBehaviorAdd;

    private ConstraintLayout bottomSheetServer;
    private BottomSheetBehavior bottomSheetBehaviorServer;
    private String serverURL;
    private TextView tv_server_desc, tv_server_title;
    private FloatingActionButton fab_share, fab_copy, fab_toggle_server;
    private ImageView qrImage;
    private Bitmap qrBitmap;
    private Animation fab_close, fab_open;
    private boolean fabsVisible = true;

    boolean serverConnected = false;
    private BroadcastReceiver serverPageUpdateReceiver;

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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupRecyclerViews();
        setupBottomMenu();

        setGameMode(false);
        GameLog.initialise(cardList, this);
        PlayerList.initialise(playerCardList, this);

        checkForBackups();

        //TODO These methods are for testing purposes only and should be removed from the onCreate function after testing
        //autoCreateGame();
        //displayEndGameOptions();
        FragmentTransaction F_T =getSupportFragmentManager().beginTransaction();
        F_T.replace(R.id.fragment_placeholder, new MainScreenFragment());
        F_T.commit();
    }

    public void replaceFragments(Class fragmentClass, boolean fade) {
        Fragment fragment = null;
        try {
            fragment = (Fragment) fragmentClass.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Insert the fragment by replacing any existing fragment
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction ft = fragmentManager.beginTransaction();

        ft.setCustomAnimations(android.R.anim.fade_in,
                android.R.anim.fade_out);
        ft.replace(R.id.fragment_placeholder, fragment)
                .commit();
    }

    @Override
    public void onBackPressed() {
        if(GameLog.isGameStarted()) { //Game is currently running, we ask the user if he wants to end the game
            if(!GameLog.swipeEnabled) return; //This means that the "Game Ended" Screen is currently showing, we do not want to show the dialog during this

            CardDialog.showMessageDialog(this, getString(R.string.title_end_game_policies), null, getString(R.string.yes), new Runnable() {
                @Override
                public void run() {
                    displayEndGameOptions();
                }
            }, getString(R.string.no), null);
        } else{ //User is in the empty screen, end the activity
            finish();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(serverPageUpdateReceiver);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    private void autoCreateGame() {
        PlayerList.addPlayer("Rüdiger");
        PlayerList.addPlayer("Markus");
        PlayerList.addPlayer("Friedrich");
        PlayerList.addPlayer("Leonard");
        PlayerList.addPlayer("Anke");
        PlayerList.addPlayer("Björn");
        PlayerList.addPlayer("Knut");
        PlayerList.addPlayer("Richard");

        startGame(true, true, true, true);

        FascistTrack ft_78 = new FascistTrack();
        ft_78.setActions(new int[] {FascistTrack.NO_POWER, FascistTrack.INVESTIGATION, FascistTrack.SPECIAL_ELECTION, FascistTrack.EXECUTION, FascistTrack.EXECUTION});
        GameLog.gameTrack = ft_78;
    }

    @Override
    protected void onResume() {
        super.onResume();

        IntentFilter filter = new IntentFilter();
        filter.addAction(ServerSercive.SERVER_STATE_CHANGED); //This action is used by the server to send updates to the MainActivity: When it is started or when it is killed
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION); //This action is used when a network change has taken place (e.g. disconnected from WiFi) It however does not send updates when the user enables/disables mobile hotspot
        filter.addAction("android.net.wifi.WIFI_AP_STATE_CHANGED"); //This action is used when the mobile hotspot state changes

        serverPageUpdateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent != null) {
                    setServerStatus();
                }
            }
        };
        registerReceiver(serverPageUpdateReceiver, filter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopAndUnbindServerService();

        //These functions set references to MainActivity to null, as not doing this would result in a memory leak
        GameLog.destroy();
        PlayerList.destroy();
    }

    public void checkForBackups() {
        if(GameLog.backupPresent()) {
            CardDialog.showMessageDialog(this, getString(R.string.dialog_restore_title), getString(R.string.dialog_restore_msg), getString(R.string.yes), new Runnable() {
                @Override
                public void run() {
                    setGameMode(true);
                    GameLog.restoreBackup();
                }
            }, getString(R.string.no), new Runnable() {
                @Override
                public void run() {
                    GameLog.deleteBackup();
                }
            });
        }
    }

    public void startGame(boolean executionSounds, boolean policySounds, boolean endSounds, boolean server) {
        setupBottomMenu();
        setGameMode(true);
        GameLog.setGameStarted(true);
        if(server) startAndBindServerService();
        GameLog.policySounds = policySounds;
        GameLog.executionSounds = executionSounds;
        GameLog.endSounds = endSounds;
    }

    public void displayEndGameOptions() {
        //Make the Menu infunctional
        bottomNavigationMenu_game.getMenu().getItem(2).setCheckable(false);
        bottomNavigationMenu_game.getMenu().getItem(1).setCheckable(false);
        bottomNavigationMenu_game.setOnNavigationItemSelectedListener(null);
        deselectAllMenuItems();
        //Hide the BottomSheets
        bottomSheetBehaviorServer.setState(BottomSheetBehavior.STATE_HIDDEN);
        bottomSheetBehaviorAdd.setState(BottomSheetBehavior.STATE_HIDDEN);

        GameLog.addEvent(new GameEndCard(this));
        GameLog.swipeEnabled = false;
    }

    public void endGame() {
        GameLog.setGameStarted(false);
        stopAndUnbindServerService();

        GameLog.deleteBackup();

        Animation swipeOutBottom = new TranslateAnimation(0, 0, 0, 200);
        swipeOutBottom.setDuration(500);
        swipeOutBottom.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }
            @Override
            public void onAnimationEnd(Animation animation) {
                //This is that the elements are not hidden until the animations are done
                setGameMode(false);
                GameLog.initialise(cardList, MainActivity.this);
                PlayerList.initialise(playerCardList, MainActivity.this);
                cardList.setVisibility(View.GONE);
                playerCardList.setVisibility(View.GONE);

                Animation fadeIn = new AlphaAnimation((float) 0, (float) 1);
                fadeIn.setDuration(500);
            }
            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        bottomNavigationMenu_game.startAnimation(swipeOutBottom);

        Animation fadeOut = new AlphaAnimation((float) 1, (float) 0);
        fadeOut.setDuration(500);
        cardList.startAnimation(fadeOut);
        playerCardList.startAnimation(fadeOut);
    }

    public void setGameMode(boolean gameMode) {
        if(!gameMode) {
            bottomNavigationMenu_game.setVisibility(View.GONE);
            GameLog.setGameStarted(false);
        } else {
            bottomNavigationMenu_game.setClickable(true);

            GameLog.initialise(cardList, this);
            GameLog.setGameStarted(true);
            try {
                PreferencesManager.writeCurrentPlayerListIfNew(this);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            PlayerList.getPlayerRecyclerViewAdapter().notifyDataSetChanged(); //When a game starts, the add Player button will not be added to the RecyclerView anymore. As this would result in a mismatch between the ItemCount and the amount of items that are actually there, a IndexOutOfBoundsException would be thrown. This refresh fixes this
            playerCardList.setVisibility(View.VISIBLE);

            bottomNavigationMenu_game.setVisibility(View.VISIBLE);
            bottomNavigationMenu_game.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_in_bottom));
        }
    }


    public void deselectAllMenuItems() {
        Menu menu = bottomNavigationMenu_game.getMenu();
        menu.getItem(0).setVisible(false);
        menu.getItem(0).setChecked(true);
    }

    public void setupBottomMenu() {
        //Setting up the Bottom Menu
        bottomNavigationMenu_game = findViewById(R.id.bottomNavigationView_game);
        Menu menu = bottomNavigationMenu_game.getMenu();
        menu.getItem(0).setVisible(false);
        menu.getItem(0).setChecked(true); //As you cannot have no items selected, I created a third item, select that one and set it as hidden #Lifehack

        //When the game ends, the Menu items are disabled. Hence, we enable them again just in case
        bottomNavigationMenu_game.getMenu().getItem(2).setCheckable(true);
        bottomNavigationMenu_game.getMenu().getItem(1).setCheckable(true);

        //initialising the "Add Event" bottom Sheet
        bottomSheetAdd = findViewById(R.id.bottom_sheet_add_event);
        bottomSheetBehaviorAdd = BottomSheetBehavior.from(bottomSheetAdd);
        bottomSheetBehaviorAdd.setState(BottomSheetBehavior.STATE_HIDDEN);

        //Setting up the OnClickListeners. For this, we get each ConstraintLayout by using findViewById
        bottomSheetAdd.findViewById(R.id.legislative_session).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetBehaviorAdd.setState(BottomSheetBehavior.STATE_HIDDEN);
                GameLog.addEvent(new LegislativeSession(MainActivity.this));
            }
        });

        bottomSheetAdd.findViewById(R.id.loyalty_investigation).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetBehaviorAdd.setState(BottomSheetBehavior.STATE_HIDDEN);
                GameLog.addEvent(new LoyaltyInvestigationEvent(null, MainActivity.this));
            }
        });

        bottomSheetAdd.findViewById(R.id.execution).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetBehaviorAdd.setState(BottomSheetBehavior.STATE_HIDDEN);
                GameLog.addEvent(new ExecutionEvent(null, MainActivity.this));
            }
        });

        bottomSheetAdd.findViewById(R.id.deck_shuffled).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetBehaviorAdd.setState(BottomSheetBehavior.STATE_HIDDEN);
                GameLog.addEvent(new DeckShuffledEvent(MainActivity.this));
            }
        });

        bottomSheetAdd.findViewById(R.id.policy_peek).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetBehaviorAdd.setState(BottomSheetBehavior.STATE_HIDDEN);
                GameLog.addEvent(new PolicyPeekEvent(null, MainActivity.this));
            }
        });

        bottomSheetAdd.findViewById(R.id.special_election).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetBehaviorAdd.setState(BottomSheetBehavior.STATE_HIDDEN);
                GameLog.addEvent(new SpecialElectionEvent(null, MainActivity.this));
            }
        });

        //Setting Up the Server Status Page
        bottomSheetServer = findViewById(R.id.bottom_sheet_server_status);
        bottomSheetBehaviorServer = BottomSheetBehavior.from(bottomSheetServer);
        bottomSheetBehaviorServer.setState(BottomSheetBehavior.STATE_HIDDEN);
        setupServerLayout();

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



    public void setupServerLayout() {
        tv_server_desc = findViewById(R.id.tv_server_url_desc);
        tv_server_title = findViewById(R.id.tv_title_server_status);

        qrImage = findViewById(R.id.img_qr);

        fab_share = findViewById(R.id.fab_share);
        fab_copy = findViewById(R.id.fab_copy_address);
        fab_toggle_server = findViewById(R.id.fab_toggle_server);

        fab_close = AnimationUtils.loadAnimation(this, R.anim.fab_close);
        fab_open = AnimationUtils.loadAnimation(this, R.anim.fab_open);

        fab_copy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Server URL", serverURL);
                clipboard.setPrimaryClip(clip);

                Toast.makeText(MainActivity.this, getString(R.string.url_copied_to_clipboard), Toast.LENGTH_SHORT).show();
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

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
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
        boolean usingHotspot = isUsingHotspot((WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE));

        if(boundServerService != null && boundServerService.server.isAlive()) { //ServerService is bound and Server is running

            if(!isMobile && !isWifi && !usingHotspot) {//Server is running but device is not connected to a network

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    tv_server_title.setText(Html.fromHtml(getString(R.string.title_server_status) + " <font color='#ff9900'>" + getString(R.string.server_status_not_connected) + "</font>",  Html.FROM_HTML_MODE_LEGACY), TextView.BufferType.SPANNABLE);
                } else {
                    tv_server_title.setText(Html.fromHtml(getString(R.string.title_server_status) + " <font color='#ff9900'>" + getString(R.string.server_status_not_connected) + "</font>"), TextView.BufferType.SPANNABLE);
                }

                qrImage.setImageDrawable(getDrawable(R.drawable.qr_placeholder));
                qrImage.setClickable(false);

                startFABAnimation(false);
                tv_server_desc.setText(getString(R.string.server_status_url_not_connected));

            } else if(isMobile && !usingHotspot && !isWifi) {//Only connected to mobile data

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    tv_server_title.setText(Html.fromHtml(getString(R.string.title_server_status) + " <font color='#ff9900'>" + getString(R.string.server_status_using_mobile_data) + "</font>",  Html.FROM_HTML_MODE_LEGACY), TextView.BufferType.SPANNABLE);
                } else {
                    tv_server_title.setText(Html.fromHtml(getString(R.string.title_server_status) + " <font color='#ff9900'>" + getString(R.string.server_status_using_mobile_data) + "</font>"), TextView.BufferType.SPANNABLE);
                }

                qrImage.setImageDrawable(getDrawable(R.drawable.qr_placeholder));
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

                tv_server_desc.setText(getString(R.string.server_status_url, serverURL));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    tv_server_title.setText(Html.fromHtml(getString(R.string.title_server_status) + " <font color='#009933'>" + getString(R.string.server_running) + "</font>",  Html.FROM_HTML_MODE_LEGACY), TextView.BufferType.SPANNABLE);
                } else {
                    tv_server_title.setText(Html.fromHtml(getString(R.string.title_server_status) + " <font color='#009933'>" + getString(R.string.server_running) + "</font>"), TextView.BufferType.SPANNABLE);
                }

                startFABAnimation(true);
            }

            ColorStateList colorStopServer = ColorStateList.valueOf(getColor(R.color.stop_server));
            fab_toggle_server.setImageDrawable(getDrawable(R.drawable.ic_baseline_stop_24));
            fab_toggle_server.setBackgroundTintList(colorStopServer);

        } else {
            tv_server_desc.setText(getString(R.string.server_status_url_disabled));

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                tv_server_title.setText(Html.fromHtml(getString(R.string.title_server_status) + " <font color='#cc0000'>" + getString(R.string.server_stopped) + "</font>",  Html.FROM_HTML_MODE_LEGACY), TextView.BufferType.SPANNABLE);
            } else {
                tv_server_title.setText(Html.fromHtml(getString(R.string.title_server_status) + " <font color='#cc0000'>" + getString(R.string.server_stopped) + "</font>"), TextView.BufferType.SPANNABLE);
            }

            qrImage.setImageDrawable(getDrawable(R.drawable.qr_placeholder));
            qrImage.setClickable(false);

            startFABAnimation(false);

            ColorStateList colorStartServer = ColorStateList.valueOf(getColor(R.color.start_server));
            fab_toggle_server.setImageDrawable(getDrawable(R.drawable.ic_baseline_play_arrow_24));
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


    public void setupRecyclerViews() {
        cardList = findViewById(R.id.cardList);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        cardList.setLayoutManager(layoutManager);

        playerCardList = findViewById(R.id.playerList);
        LinearLayoutManager layoutManager2 = new LinearLayoutManager(this);
        layoutManager2.setOrientation(RecyclerView.HORIZONTAL);
        playerCardList.setLayoutManager(layoutManager2);
        PlayerList.initialise(playerCardList, this);
    }

    private void startAndBindServerService() {
        if(boundServerService != null && serverConnected) { //Don't start another service if a server is already running
            if(!boundServerService.server.isAlive()) boundServerService.server.startServer(); //If it is stopped, then start it
            return;
        }

        //Starting with Android O, a ForeGroundService must be called using a different function
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(new Intent(this, ServerSercive.class));
        } else {
            startService(new Intent(this, ServerSercive.class));
        }

        bindService(new Intent(MainActivity.this, ServerSercive.class),
                serverServiceConnection,
                Context.BIND_AUTO_CREATE);
        serverConnected = true;
    }

    void stopAndUnbindServerService() {
        if (serverConnected) {
            //Stop server
            boundServerService.killSelf();

            // Detach our existing connection.
            unbindService(serverServiceConnection);
            serverConnected = false;
            boundServerService = null;
        }
    }

    public static class ProgressBarAnimation extends Animation{
        private ProgressBar progressBar;
        private float from;
        private float  to;

        public ProgressBarAnimation(ProgressBar progressBar, float from, float to) {
            super();
            this.progressBar = progressBar;
            this.from = from;
            this.to = to;
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            super.applyTransformation(interpolatedTime, t);
            float value = from + (to - from) * interpolatedTime;
            progressBar.setProgress((int) value);
        }

    }
}