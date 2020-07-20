package de.tobias.secrethitlermobilecompanion;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;

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


    private FloatingActionButton fab_main, fab_legislative, fab_execution, fab_policypeek, fab_specialelection, fab_investigation;
    private TextView tv_legislative, tv_execution, tv_policypeek, tv_specialelection, tv_investigation;
    private Animation fab_open, fab_close, fab_clock, fab_anticlock;

    boolean isOpen = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Server server = new Server(8080, this);
        server.startServer();
        Log.v("Server", "URL is " + server.getURL());

        PlayerList.addPlayer("Rüdiger");
        PlayerList.addPlayer("Hildegunde");
        PlayerList.addPlayer("Ferdinand");

        cardList = (RecyclerView) findViewById(R.id.cardList);
        layoutManager = new LinearLayoutManager(this);
        cardList.setLayoutManager(layoutManager);

        final GameLog gameLog = new GameLog(cardList, MainActivity.this);

        setupFabMenu();

        nix(gameLog);
        gameLog.addEvent(new ExecutionEvent("Ferdinand", "Mario", this));
    }

    public void nix(GameLog gameLog) {
        VoteEvent ve1 = new VoteEvent("Rüdiger", "Hildegunde", VoteEvent.VOTE_PASSED, this);
        ClaimEvent ce1 = new ClaimEvent("Rüdiger", "Hildegunde", ClaimEvent.RRR, ClaimEvent.RR, ClaimEvent.FASCIST, false, this);

        gameLog.addEvent(new LegislativeSession(ve1, ce1, this));

        VoteEvent ve2 =new VoteEvent("Hildegunde", "Ferdinand", VoteEvent.VOTE_PASSED, MainActivity.this);
        ClaimEvent ce2 = new ClaimEvent("Hildegunde", "Ferdinand", ClaimEvent.BRR, ClaimEvent.BR, ClaimEvent.LIBERAL, true, MainActivity.this);

        gameLog.addEvent(new LegislativeSession(ve2, ce2, this));


        VoteEvent ve3 =new VoteEvent("Ferdinand", "Mario", VoteEvent.VOTE_FAILED, MainActivity.this);
        gameLog.addEvent(new LegislativeSession(ve3, null, this));
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

}