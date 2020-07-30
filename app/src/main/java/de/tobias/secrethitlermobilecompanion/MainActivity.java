package de.tobias.secrethitlermobilecompanion;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
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

    private FloatingActionButton fab_main, fab_legislative, fab_execution, fab_policypeek, fab_specialelection, fab_investigation, fab_deckshuffled;
    private TextView tv_legislative, tv_execution, tv_policypeek, tv_specialelection, tv_investigation, tv_deckshuffled;
    private Animation fab_open, fab_close, fab_clock, fab_anticlock;

    private LinearLayout setupLayout;
    private BottomNavigationView bottomNavigationMenu;
    private ConstraintLayout bottomSheetAdd;
    private BottomSheetBehavior bottomSheetBehaviorAdd;

    boolean isOpen = false;
    boolean serverConnected = false;

    private ServiceConnection serverServiceConnection = new ServiceConnection() { //TODO unused, remove?

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            boundServerService = ((ServerSercive.LocalBinder)service).getService();
            Toast.makeText(MainActivity.this, boundServerService.server.getURL(), Toast.LENGTH_LONG).show();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            boundServerService = null;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
        bottomNavigationMenu = findViewById(R.id.bottomNavigationView);
        Menu menu = bottomNavigationMenu.getMenu();
        menu.getItem(0).setVisible(false);
        menu.getItem(0).setChecked(true); //As you cannot have no items selected, I created a third item, select that one and set it as hidden #Lifehack

        //initialising the bottom Sheet
        bottomSheetAdd = findViewById(R.id.bottom_sheet);
        bottomSheetBehaviorAdd = BottomSheetBehavior.from(bottomSheetAdd);
        bottomSheetBehaviorAdd.setState(BottomSheetBehavior.STATE_HIDDEN);

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

        BottomSheetBehavior.BottomSheetCallback callback = new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if(newState == BottomSheetBehavior.STATE_HIDDEN) { //If the state changed to hidden (i.e. the user closed the menu), the item should now be unselected
                    deselectAllMenuItems();
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        };
        bottomSheetBehaviorAdd.addBottomSheetCallback(callback);

        bottomNavigationMenu.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch(item.getItemId()) {
                    case R.id.navigation_add_event:
                        bottomSheetBehaviorAdd.setState(BottomSheetBehavior.STATE_EXPANDED);
                        //TODO
                        break;
                    case R.id.navigation_server_status:
                        //TODO
                        break;
                }
                return true;
            }
        });
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

    void startAndBindServerService() {
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
            // Detach our existing connection.
            unbindService(serverServiceConnection);
            serverConnected = false;

            Intent stopIntent = new Intent(MainActivity.this, ServerSercive.class);
            stopIntent.setAction(ServerSercive.ACTION_KILL_SERVER);
            startService(stopIntent);
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
        stopAndUnbindServerService();
    }

}