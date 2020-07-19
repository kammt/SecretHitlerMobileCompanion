package de.tobias.secrethitlermobilecompanion;

import androidx.appcompat.app.AppCompatActivity;

import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;

import java.io.IOException;

import de.tobias.secrethitlermobilecompanion.SHClasses.ClaimEvent;
import de.tobias.secrethitlermobilecompanion.SHClasses.GameLog;
import de.tobias.secrethitlermobilecompanion.SHClasses.PlayerList;
import de.tobias.secrethitlermobilecompanion.SHClasses.VoteEvent;
import fi.iki.elonen.NanoHTTPD;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Server server = new Server(8080, this);
        //server.startServer();
        //Log.v("Server", "URL is " + server.getURL());

        PlayerList playerList = new PlayerList();
        playerList.addPlayer("Mario");
        playerList.addPlayer("Tobias");
        playerList.addPlayer("Leander");

        GameLog gameLog = new GameLog();

        gameLog.addEvent(new VoteEvent(playerList.getID("Mario"), playerList.getID("Tobias"), VoteEvent.VOTE_PASSED));
        gameLog.addEvent(new ClaimEvent(playerList.getID("Mario"), playerList.getID("Tobias"), ClaimEvent.RRR, ClaimEvent.RR, ClaimEvent.FASCIST));

        gameLog.addEvent(new VoteEvent(playerList.getID("Tobias"), playerList.getID("Leander"), VoteEvent.VOTE_PASSED));
        gameLog.addEvent(new ClaimEvent(playerList.getID("Tobias"), playerList.getID("Leander"), ClaimEvent.BRR, ClaimEvent.BR, ClaimEvent.LIBERAL));
    }
}