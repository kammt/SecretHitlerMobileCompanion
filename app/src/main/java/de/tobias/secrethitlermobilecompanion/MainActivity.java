package de.tobias.secrethitlermobilecompanion;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

import de.tobias.secrethitlermobilecompanion.SHClasses.ClaimEvent;
import de.tobias.secrethitlermobilecompanion.SHClasses.GameLog;
import de.tobias.secrethitlermobilecompanion.SHClasses.PlayerList;
import de.tobias.secrethitlermobilecompanion.SHClasses.VoteEvent;

public class MainActivity extends AppCompatActivity {

    LinearLayout logListtView;

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

        //logListtView = (LinearLayout) findViewById(R.id.GameLog);



    }

    @Override
    protected void onResume() {
        super.onResume();

        //final GameLog gameLog = new GameLog(logListtView, MainActivity.this);

        //gameLog.addEvent(new VoteEvent("Rüdiger", "Hildegunde", VoteEvent.VOTE_PASSED, this));
        //gameLog.addEvent(new ClaimEvent("Rüdiger", "Hildegunde", ClaimEvent.RRR, ClaimEvent.RR, ClaimEvent.FASCIST, this));

        //gameLog.addEvent(new VoteEvent("Hildegunde", "Ferdinand", VoteEvent.VOTE_PASSED, MainActivity.this));
       // gameLog.addEvent(new ClaimEvent("Hildegunde", "Ferdinand", ClaimEvent.BRR, ClaimEvent.BR, ClaimEvent.LIBERAL, MainActivity.this));


    }
}