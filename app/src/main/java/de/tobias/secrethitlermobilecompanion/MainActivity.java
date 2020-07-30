package de.tobias.secrethitlermobilecompanion;

import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import net.glxn.qrgen.android.QRCode;

import de.tobias.secrethitlermobilecompanion.SHClasses.Claim;
import de.tobias.secrethitlermobilecompanion.SHClasses.ClaimEvent;
import de.tobias.secrethitlermobilecompanion.SHClasses.DeckShuffledEvent;
import de.tobias.secrethitlermobilecompanion.SHClasses.ExecutionEvent;
import de.tobias.secrethitlermobilecompanion.SHClasses.GameLog;
import de.tobias.secrethitlermobilecompanion.SHClasses.LegislativeSession;
import de.tobias.secrethitlermobilecompanion.SHClasses.LoyaltyInvestigationEvent;
import de.tobias.secrethitlermobilecompanion.SHClasses.PlayerList;
import de.tobias.secrethitlermobilecompanion.SHClasses.PolicyPeekEvent;
import de.tobias.secrethitlermobilecompanion.SHClasses.SpecialElectionEvent;
import de.tobias.secrethitlermobilecompanion.SHClasses.VoteEvent;

public class MainActivity extends AppCompatActivity {

    RecyclerView cardList, playerCardList;

    private ServerSercive boundServerService;

    private LinearLayout setupLayout;
    private BottomNavigationView bottomNavigationMenu;
    private ConstraintLayout bottomSheetAdd;
    private BottomSheetBehavior bottomSheetBehaviorAdd;

    private ConstraintLayout bottomSheetServer;
    private BottomSheetBehavior bottomSheetBehaviorServer;
    private String serverURL;
    private TextView tv_server_desc, tv_server_title;
    private FloatingActionButton fab_share, fab_copy, fab_toggle_server;
    private ImageView qrImage;
    private Bitmap qrBitmap;

    boolean serverConnected = false;
    private BroadcastReceiver serviceUpdateReceiver;

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

        IntentFilter filter = new IntentFilter();
        filter.addAction(ServerSercive.ACTION_SERVER_STOPPED);
        filter.addAction(ServerSercive.ACTION_SERVER_STARTED);

        serviceUpdateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent != null) {
                    setServerStatus();
                }
            }
        };
        registerReceiver(serviceUpdateReceiver, filter);

        setupRecyclerViews();
        startAndBindServerService();

        GameLog.initialise(cardList);
        setupBottomMenu();
        testGameLog();
    }

    public void deselectAllMenuItems() {
        Menu menu = bottomNavigationMenu.getMenu();
        menu.getItem(0).setVisible(false);
        menu.getItem(0).setChecked(true);
    }

    public void setupBottomMenu() {
        //Setting up the Bottom Menu
        bottomNavigationMenu = findViewById(R.id.bottomNavigationView);
        Menu menu = bottomNavigationMenu.getMenu();
        menu.getItem(0).setVisible(false);
        menu.getItem(0).setChecked(true); //As you cannot have no items selected, I created a third item, select that one and set it as hidden #Lifehack

        //initialising the "Add Event" bottom Sheet
        bottomSheetAdd = findViewById(R.id.bottom_sheet_add_event);
        bottomSheetBehaviorAdd = BottomSheetBehavior.from(bottomSheetAdd);
        bottomSheetBehaviorAdd.setState(BottomSheetBehavior.STATE_HIDDEN);

        //Setting up the OnClickListeners
        setupLayout = findViewById(R.id.cardSetup);
        bottomSheetAdd.findViewById(R.id.legislative_session).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetBehaviorAdd.setState(BottomSheetBehavior.STATE_HIDDEN);

                //As running both functions at the same time (Hence to animations) would lead to lag, the function to create and display the card is executed after the sheet is gone
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        CardSetupHelper.setupCard(setupLayout, CardSetupHelper.LEGISLATIVE_SESSION, MainActivity.this);
                    }
                }, 180);
            }
        });

        bottomSheetAdd.findViewById(R.id.loyalty_investigation).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetBehaviorAdd.setState(BottomSheetBehavior.STATE_HIDDEN);

                //As running both functions at the same time (Hence to animations) would lead to lag, the function to create and display the card is executed after the sheet is gone
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        CardSetupHelper.setupCard(setupLayout, CardSetupHelper.LOYALTY_INVESTIGATION, MainActivity.this);
                    }
                }, 180);
            }
        });

        bottomSheetAdd.findViewById(R.id.execution).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetBehaviorAdd.setState(BottomSheetBehavior.STATE_HIDDEN);

                //As running both functions at the same time (Hence to animations) would lead to lag, the function to create and display the card is executed after the sheet is gone
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        CardSetupHelper.setupCard(setupLayout, CardSetupHelper.EXECUTION, MainActivity.this);
                    }
                }, 180);
            }
        });

        bottomSheetAdd.findViewById(R.id.deck_shuffled).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetBehaviorAdd.setState(BottomSheetBehavior.STATE_HIDDEN);

                //As running both functions at the same time (Hence to animations) would lead to lag, the function to create and display the card is executed after the sheet is gone
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        CardSetupHelper.setupCard(setupLayout, CardSetupHelper.DECK_SHUFFLED, MainActivity.this);
                    }
                }, 180);
            }
        });

        bottomSheetAdd.findViewById(R.id.policy_peek).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetBehaviorAdd.setState(BottomSheetBehavior.STATE_HIDDEN);

                //As running both functions at the same time (Hence to animations) would lead to lag, the function to create and display the card is executed after the sheet is gone
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        CardSetupHelper.setupCard(setupLayout, CardSetupHelper.POLICY_PEEK, MainActivity.this);
                    }
                }, 180);
            }
        });

        bottomSheetAdd.findViewById(R.id.special_election).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetBehaviorAdd.setState(BottomSheetBehavior.STATE_HIDDEN);

                //As running both functions at the same time (Hence to animations) would lead to lag, the function to create and display the card is executed after the sheet is gone
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        CardSetupHelper.setupCard(setupLayout, CardSetupHelper.SPECIAL_ELECTION, MainActivity.this);
                    }
                }, 180);
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
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        };
        BottomSheetBehavior.BottomSheetCallback callbackServer = new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if(newState == BottomSheetBehavior.STATE_HIDDEN && bottomSheetBehaviorAdd.getState() == BottomSheetBehavior.STATE_HIDDEN) { //If the state changed to hidden (i.e. the user closed the menu), the item should now be unselected
                    deselectAllMenuItems();
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        };
        bottomSheetBehaviorAdd.addBottomSheetCallback(callbackAdd);
        bottomSheetBehaviorServer.addBottomSheetCallback(callbackServer);

        //Adding the Listener to the BottomNavigationMenu
        bottomNavigationMenu.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
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
        if(boundServerService != null && boundServerService.server.isAlive()) { //ServerService is bound and Server is running

            if(serverURL == null || !serverURL.equals(boundServerService.server.getURL())) {
                serverURL = boundServerService.server.getURL();

                qrBitmap = QRCode.from(serverURL).withSize(200,200).bitmap();
            }

            qrImage.setImageBitmap(qrBitmap);

            tv_server_desc.setText(getString(R.string.server_status_url, serverURL));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                tv_server_title.setText(Html.fromHtml(getString(R.string.title_server_status) + " <font color='#009933'>" + getString(R.string.server_running) + "</font>",  Html.FROM_HTML_MODE_LEGACY), TextView.BufferType.SPANNABLE);
            } else {
                tv_server_title.setText(Html.fromHtml(getString(R.string.title_server_status) + " <font color='#009933'>" + getString(R.string.server_running) + "</font>"), TextView.BufferType.SPANNABLE);
            }

            fab_copy.setClickable(true);
            fab_share.setClickable(true);
            fab_toggle_server.setImageDrawable(getDrawable(R.drawable.ic_baseline_stop_24));

        } else {
            tv_server_desc.setText(getString(R.string.server_status_url_disabled));

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                tv_server_title.setText(Html.fromHtml(getString(R.string.title_server_status) + " <font color='#cc0000'>" + getString(R.string.server_stopped) + "</font>",  Html.FROM_HTML_MODE_LEGACY), TextView.BufferType.SPANNABLE);
            } else {
                tv_server_title.setText(Html.fromHtml(getString(R.string.title_server_status) + " <font color='#cc0000'>" + getString(R.string.server_stopped) + "</font>"), TextView.BufferType.SPANNABLE);
            }

            qrImage.setImageDrawable(getDrawable(R.drawable.qr_placeholder));

            fab_copy.setClickable(false);
            fab_share.setClickable(false);

            fab_toggle_server.setImageDrawable(getDrawable(R.drawable.ic_baseline_play_arrow_24));
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }



    public void setupRecyclerViews() {
        cardList = findViewById(R.id.cardList);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        cardList.setLayoutManager(layoutManager);

        playerCardList = findViewById(R.id.playerList);
        LinearLayoutManager layoutManager2 = new LinearLayoutManager(this);
        layoutManager2.setOrientation(RecyclerView.HORIZONTAL);
        playerCardList.setLayoutManager(layoutManager2);
        PlayerList.setupPlayerList(playerCardList, this);
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

    public void testGameLog() {
        PlayerList.addPlayer("Rüdiger");
        PlayerList.addPlayer("Hildegunde");
        PlayerList.addPlayer("Ferdinand");
        PlayerList.addPlayer("Mario");


        VoteEvent ve1 = new VoteEvent("Rüdiger", "Hildegunde", VoteEvent.VOTE_PASSED, this);
        ClaimEvent ce1 = new ClaimEvent("Rüdiger", "Hildegunde", Claim.RRR, Claim.RR, Claim.FASCIST, false, this);

        GameLog.addEvent(new LegislativeSession(ve1, ce1, this));

        VoteEvent ve2 =new VoteEvent("Hildegunde", "Ferdinand", VoteEvent.VOTE_PASSED, MainActivity.this);
        ClaimEvent ce2 = new ClaimEvent("Hildegunde", "Ferdinand", Claim.RRB, Claim.RB, Claim.LIBERAL, true, MainActivity.this);

        GameLog.addEvent(new LegislativeSession(ve2, ce2, this));


        VoteEvent ve3 =new VoteEvent("Ferdinand", "Mario", VoteEvent.VOTE_FAILED, MainActivity.this);
        GameLog.addEvent(new LegislativeSession(ve3, null, this));

        GameLog.addEvent(new ExecutionEvent("Ferdinand", "Mario", this));
        GameLog.addEvent(new PolicyPeekEvent("Ferdinand", Claim.RBB, this));
        GameLog.addEvent(new LoyaltyInvestigationEvent("Ferdinand", "Mario", Claim.LIBERAL, this));
        GameLog.addEvent(new SpecialElectionEvent("Ferdinand", "Mario", this));

        GameLog.addEvent(new DeckShuffledEvent(6, 11));
    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(serviceUpdateReceiver);
        stopAndUnbindServerService();
    }

}