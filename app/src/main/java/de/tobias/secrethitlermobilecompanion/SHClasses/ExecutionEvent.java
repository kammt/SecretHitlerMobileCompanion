package de.tobias.secrethitlermobilecompanion.SHClasses;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.cardview.widget.CardView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import de.tobias.secrethitlermobilecompanion.CardSetupHelper;
import de.tobias.secrethitlermobilecompanion.R;

public class ExecutionEvent extends ExecutiveAction {
    Context context;
    private String presidentName, executedPlayerName;

    public ExecutionEvent(String presidentName, String executedPlayerName, Context context, boolean setup) {
        this.context = context;
        this.presidentName = presidentName;
        this.executedPlayerName = executedPlayerName;
        isSetup = setup;

        if(!setup) {
            PlayerList.setAsDead(executedPlayerName);
            if(GameLog.executionSounds) MediaPlayer.create(context, R.raw.playershot).start();
        }
    }

    @Override
    public String getInfoText() {
        return context.getString(R.string.executed_string, presidentName, executedPlayerName);
    }

    @Override
    public Drawable getDrawable() {
        return context.getDrawable(R.drawable.execution);
    }

    @Override
    public void setupSetupCard(CardView cardView) {
        //Setting up Spinners
        final Spinner presSpinner = cardView.findViewById(R.id.spinner_president);
        ArrayAdapter<String> playerListadapter = CardSetupHelper.getPlayerNameAdapter(context);
        playerListadapter.setDropDownViewResource(android.R.layout
                .simple_spinner_dropdown_item);
        presSpinner.setAdapter(playerListadapter);

        final Spinner executedSpinner = cardView.findViewById(R.id.spinner_executed_player);
        executedSpinner.setAdapter(playerListadapter);
        executedSpinner.setSelection(1); //Setting a different item on the executed player spinner so they don't have the same name at the beginning

        //Initialising all other important aspects
        final FloatingActionButton fab_create = cardView.findViewById(R.id.fab_create);
        ImageView iv_cancel = cardView.findViewById(R.id.img_cancel);

        iv_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GameLog.remove(GameLog.eventList.get(GameLog.eventList.size() - 1));
            }
        });

        fab_create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presidentName = (String) presSpinner.getSelectedItem();
                executedPlayerName = (String) executedSpinner.getSelectedItem();

                if(presidentName.equals(executedPlayerName)) {
                    Toast.makeText(context, context.getString(R.string.err_names_cannot_be_the_same), Toast.LENGTH_LONG).show();
                } else {
                    PlayerList.setAsDead(executedPlayerName);
                    if(GameLog.executionSounds) MediaPlayer.create(context, R.raw.playershot).start();
                    isSetup = false;
                    GameLog.notifySetupPhaseLeft();
                }
            }
        });
    }

    @Override
    public boolean allInvolvedPlayersAreUnselected(ArrayList<String> unselectedPlayers) {
        return unselectedPlayers.contains(presidentName) && unselectedPlayers.contains(executedPlayerName);
    }

    @Override
    public JSONObject getJSON() throws JSONException {
        JSONObject obj = new JSONObject();

        obj.put("type", "executive-action");
        obj.put("executive_action_type", "execution");
        obj.put("president", presidentName);
        obj.put("target", executedPlayerName);

        return obj;
    }
}
