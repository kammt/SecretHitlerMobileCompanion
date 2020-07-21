package de.tobias.secrethitlermobilecompanion;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.renderscript.ScriptGroup;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import de.tobias.secrethitlermobilecompanion.SHClasses.ClaimEvent;
import de.tobias.secrethitlermobilecompanion.SHClasses.ExecutionEvent;
import de.tobias.secrethitlermobilecompanion.SHClasses.GameLog;
import de.tobias.secrethitlermobilecompanion.SHClasses.LegislativeSession;
import de.tobias.secrethitlermobilecompanion.SHClasses.PlayerList;
import de.tobias.secrethitlermobilecompanion.SHClasses.VoteEvent;

public class MainActivity extends AppCompatActivity {

    RecyclerView cardList;
    private RecyclerView.LayoutManager layoutManager;

    private ServerSercive boundServerService;

    private FloatingActionButton fab_main, fab_legislative, fab_execution, fab_policypeek, fab_specialelection, fab_investigation;
    private TextView tv_legislative, tv_execution, tv_policypeek, tv_specialelection, tv_investigation;
    private Animation fab_open, fab_close, fab_clock, fab_anticlock;

    boolean isOpen = false;
    boolean serverConnected = false;

    private ServiceConnection serverServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            boundServerService = ((ServerSercive.LocalBinder)service).getService();
            Toast.makeText(MainActivity.this, boundServerService.server.getURL(), Toast.LENGTH_LONG).show();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            boundServerService = null;
            Toast.makeText(MainActivity.this,
                    "Service disconnected",
                    Toast.LENGTH_SHORT).show();
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStart() {
        super.onStart();

        cardList = (RecyclerView) findViewById(R.id.cardList);
        layoutManager = new LinearLayoutManager(this);
        cardList.setLayoutManager(layoutManager);

        setupFabMenu();

        startAndBindService();

        PlayerList.addPlayer("Rüdiger");
        PlayerList.addPlayer("Hildegunde");
        PlayerList.addPlayer("Ferdinand");

        final GameLog gameLog = new GameLog(cardList, MainActivity.this);
        testGameLog(gameLog);
    }

    void startAndBindService() {

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

    void stopAndUnbindService() {
        if (serverConnected) {
            // Detach our existing connection.
            unbindService(serverServiceConnection);
            serverConnected = false;

            Intent stopIntent = new Intent(MainActivity.this, ServerSercive.class);
            stopIntent.setAction(ServerSercive.ACTION_KILL_SERVER);
            startService(stopIntent);
        }
    }

    public void testGameLog(GameLog gameLog) {
        VoteEvent ve1 = new VoteEvent("Rüdiger", "Hildegunde", VoteEvent.VOTE_PASSED, this);
        ClaimEvent ce1 = new ClaimEvent("Rüdiger", "Hildegunde", ClaimEvent.RRR, ClaimEvent.RR, ClaimEvent.FASCIST, false, this);

        gameLog.addEvent(new LegislativeSession(ve1, ce1, this));

        VoteEvent ve2 =new VoteEvent("Hildegunde", "Ferdinand", VoteEvent.VOTE_PASSED, MainActivity.this);
        ClaimEvent ce2 = new ClaimEvent("Hildegunde", "Ferdinand", ClaimEvent.BRR, ClaimEvent.BR, ClaimEvent.LIBERAL, true, MainActivity.this);

        gameLog.addEvent(new LegislativeSession(ve2, ce2, this));


        VoteEvent ve3 =new VoteEvent("Ferdinand", "Mario", VoteEvent.VOTE_FAILED, MainActivity.this);
        gameLog.addEvent(new LegislativeSession(ve3, null, this));

        gameLog.addEvent(new ExecutionEvent("Ferdinand", "Mario", this));
    }

    public void setupFabMenu() {
        //Loading Animations
        fab_close = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_close);
        fab_open = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_open);
        fab_clock = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_rotate_clock);
        fab_anticlock = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_rotate_anticlock);

        //Initialising FABs and TextViews
        tv_legislative = (TextView) findViewById(R.id.tv_legislative_session);
        tv_execution = (TextView) findViewById(R.id.tv_execution);
        tv_specialelection = (TextView) findViewById(R.id.tv_special_election);
        tv_investigation = (TextView) findViewById(R.id.tv_investigation);
        tv_policypeek = (TextView) findViewById(R.id.tv_policy_peek);

        fab_legislative = (FloatingActionButton) findViewById(R.id.fab_legislative_session);
        fab_execution = (FloatingActionButton) findViewById(R.id.fab_execution);
        fab_specialelection = (FloatingActionButton) findViewById(R.id.fab_special_election);
        fab_investigation = (FloatingActionButton) findViewById(R.id.fab_investigation);
        fab_policypeek = (FloatingActionButton) findViewById(R.id.fab_policy_peek);

        fab_main = (FloatingActionButton) findViewById(R.id.fab_main_add);
        fab_main.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isOpen) closeFabMenu();
                else openFabMenu();
            }
        });
    }

    public void closeFabMenu() {
        isOpen = false;
        fab_main.startAnimation(fab_anticlock);

        tv_legislative.setVisibility(View.INVISIBLE);
        fab_legislative.startAnimation(fab_close);
        fab_legislative.setClickable(false);

        tv_execution.setVisibility(View.INVISIBLE);
        fab_execution.startAnimation(fab_close);
        fab_execution.setClickable(false);

        tv_specialelection.setVisibility(View.INVISIBLE);
        fab_specialelection.startAnimation(fab_close);
        fab_specialelection.setClickable(false);

        tv_investigation.setVisibility(View.INVISIBLE);
        fab_investigation.startAnimation(fab_close);
        fab_investigation.setClickable(false);

        tv_policypeek.setVisibility(View.INVISIBLE);
        fab_policypeek.startAnimation(fab_close);
        fab_policypeek.setClickable(false);
    }

    public void openFabMenu() {
        isOpen = true;
        fab_main.startAnimation(fab_clock);

        tv_legislative.setVisibility(View.VISIBLE);
        fab_legislative.startAnimation(fab_open);
        fab_legislative.setClickable(true);

        tv_execution.setVisibility(View.VISIBLE);
        fab_execution.startAnimation(fab_open);
        fab_execution.setClickable(true);

        tv_specialelection.setVisibility(View.VISIBLE);
        fab_specialelection.startAnimation(fab_open);
        fab_specialelection.setClickable(true);

        tv_investigation.setVisibility(View.VISIBLE);
        fab_investigation.startAnimation(fab_open);
        fab_investigation.setClickable(true);

        tv_policypeek.setVisibility(View.VISIBLE);
        fab_policypeek.startAnimation(fab_open);
        fab_policypeek.setClickable(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopAndUnbindService();
    }

}