package de.tobiundmario.secrethitlermobilecompanion.SHEvents;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import de.tobiundmario.secrethitlermobilecompanion.R;
import de.tobiundmario.secrethitlermobilecompanion.SHCards.CardSetupHelper;
import de.tobiundmario.secrethitlermobilecompanion.SHClasses.GameManager.GameEventsManager;
import de.tobiundmario.secrethitlermobilecompanion.SHClasses.GameManager.PlayerListManager;

public class ExecutionEvent extends ExecutiveAction {
    Context context;

    public ExecutionEvent(String presidentName, String executedPlayerName, Context context) {
        this.context = context;
        this.presidentName = presidentName;
        this.targetName = executedPlayerName;

        apply();
    }

    public ExecutionEvent(String presidentName, Context context) {
        this.context = context;
        isSetup = true;
        this.presidentName = presidentName;
    }

    private void apply() {
        PlayerListManager.setAsDead(targetName, true);
        if(GameEventsManager.executionSounds) MediaPlayer.create(context, R.raw.playershot).start();
    }


    public void resetOnRemoval() {
        PlayerListManager.setAsDead(targetName, false);
    }

    public void undoRemoval() {
        PlayerListManager.setAsDead(targetName, true);
    }

    @Override
    public String getInfoText() {
        return context.getString(R.string.executed_string, PlayerListManager.boldPlayerName(presidentName), PlayerListManager.boldPlayerName(targetName));
    }

    @Override
    public Drawable getDrawable() {
        return ContextCompat.getDrawable(context, R.drawable.execution);
    }

    @Override
    public void initialiseSetupCard(CardView cardView) {
        TextView title = cardView.findViewById(R.id.title);
        title.setText(context.getString(R.string.new_execution));

        TextView tv_executed = cardView.findViewById(R.id.txt_executed_player);
        tv_executed.setText(context.getString(R.string.executed));

        //Setting up Spinners
        final Spinner presSpinner = cardView.findViewById(R.id.spinner_president);
        ArrayAdapter<String> playerListadapter = CardSetupHelper.getArrayAdapter(context, PlayerListManager.getAlivePlayerList(), false);
        playerListadapter.setDropDownViewResource(android.R.layout
                .simple_spinner_dropdown_item);
        presSpinner.setAdapter(playerListadapter);

        if(presidentName != null) CardSetupHelper.lockPresidentSpinner(presidentName, presSpinner);

        final Spinner executedSpinner = cardView.findViewById(R.id.spinner_executed_player);
        executedSpinner.setAdapter(playerListadapter);
        executedSpinner.setSelection(1); //Setting a different item on the executed player spinner so they don't have the same name at the beginning

        //Initialising all other important aspects
        final Button btn_create = cardView.findViewById(R.id.btn_setup_forward);

        btn_create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isEditing) resetOnRemoval(); //Undo actions made by the old content (i.e. removing the dead-image set before)
                presidentName = (String) presSpinner.getSelectedItem();
                targetName = (String) executedSpinner.getSelectedItem();

                if(presidentName.equals(targetName)) {
                    Toast.makeText(context, context.getString(R.string.err_names_cannot_be_the_same), Toast.LENGTH_LONG).show();
                } else {
                    apply();
                    isSetup = false;
                    GameEventsManager.notifySetupPhaseLeft(ExecutionEvent.this);
                }
            }
        });
    }

    @Override
    public void setCurrentValues(CardView cardView) {
        Spinner presSpinner = cardView.findViewById(R.id.spinner_president);
        Spinner executedSpinner = cardView.findViewById(R.id.spinner_executed_player);

        presSpinner.setSelection(PlayerListManager.getPlayerPosition( presidentName ));

        //Here we face a problem. We cannot just set the selection before, as the player was marked as dead, thus not being in the selection anymore. To mitigate this, we add the player name temporarily to a new adapter and set it. See the function in CardSetupHelper for details
        ArrayList<String> playerListWithDeadPlayer = PlayerListManager.getAlivePlayerList();
        playerListWithDeadPlayer.add(targetName);

        ArrayAdapter<String> playerListadapterWithDeadPlayer = CardSetupHelper.getArrayAdapter(context, playerListWithDeadPlayer,false);
        playerListadapterWithDeadPlayer.setDropDownViewResource(android.R.layout
                .simple_spinner_dropdown_item);
        executedSpinner.setAdapter(playerListadapterWithDeadPlayer);
        executedSpinner.setSelection(PlayerListManager.getAlivePlayerCount() );
    }

    @Override
    public boolean allInvolvedPlayersAreUnselected(List<String> unselectedPlayers) {
        return unselectedPlayers.contains(presidentName) && unselectedPlayers.contains(targetName);
    }

    @Override
    public JSONObject getJSON() throws JSONException {
        JSONObject obj = new JSONObject();

        obj.put("id", id);
        obj.put("type", "executive-action");
        obj.put("executive_action_type", "execution");
        obj.put("president", presidentName);
        obj.put("target", targetName);

        return obj;
    }
}
