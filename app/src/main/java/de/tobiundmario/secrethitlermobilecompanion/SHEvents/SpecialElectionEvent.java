package de.tobiundmario.secrethitlermobilecompanion.SHEvents;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import de.tobiundmario.secrethitlermobilecompanion.R;
import de.tobiundmario.secrethitlermobilecompanion.SHCards.CardSetupHelper;
import de.tobiundmario.secrethitlermobilecompanion.SHClasses.GameLog;
import de.tobiundmario.secrethitlermobilecompanion.SHClasses.PlayerList;

public class SpecialElectionEvent extends ExecutiveAction {
    Context context;

    public SpecialElectionEvent(String presidentName, String electedPlayerName, Context context) {
        this.context = context;
        this.presidentName = presidentName;
        this.targetName = electedPlayerName;
    }

    public SpecialElectionEvent(String presidentName, Context context) {
        this.presidentName = presidentName;
        this.context = context;
        isSetup = true;
    }

    @Override
    public String getInfoText() {
        return context.getString(R.string.specialElection_string, PlayerList.boldPlayerName(presidentName), PlayerList.boldPlayerName(targetName));
    }

    @Override
    public Drawable getDrawable() {
        return context.getDrawable(R.drawable.special_election);
    }

    @Override
    public void initialiseSetupCard(CardView cardView) {
        //Changing the text
        TextView title = cardView.findViewById(R.id.title);
        title.setText(context.getString(R.string.new_special_election));

        TextView tvspecialelected = cardView.findViewById(R.id.txt_executed_player);
        tvspecialelected.setText(context.getString(R.string.elected));

        //Setting up Spinners
        final Spinner presSpinner = cardView.findViewById(R.id.spinner_president);
        ArrayAdapter<String> playerListadapter = CardSetupHelper.getPlayerNameAdapter(context);
        playerListadapter.setDropDownViewResource(android.R.layout
                .simple_spinner_dropdown_item);
        presSpinner.setAdapter(playerListadapter);

        if(presidentName != null) CardSetupHelper.lockPresidentSpinner(presidentName, presSpinner);

        final Spinner electedSpinner = cardView.findViewById(R.id.spinner_executed_player);
        electedSpinner.setAdapter(playerListadapter);
        electedSpinner.setSelection(1); //Setting a different item on the elected player spinner so they don't have the same name at the beginning

        final FloatingActionButton fab_create = cardView.findViewById(R.id.fab_create);
        fab_create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presidentName = (String) presSpinner.getSelectedItem();
                targetName = (String) electedSpinner.getSelectedItem();

                if(presidentName.equals(targetName)) {
                    Toast.makeText(context, context.getString(R.string.err_names_cannot_be_the_same), Toast.LENGTH_LONG).show();
                } else {
                    isSetup = false;
                    GameLog.notifySetupPhaseLeft(SpecialElectionEvent.this);
                }
            }
        });
    }

    @Override
    public void setCurrentValues(CardView cardView) {
        Spinner electedSpinner = cardView.findViewById(R.id.spinner_executed_player);
        Spinner presSpinner = cardView.findViewById(R.id.spinner_president);

        presSpinner.setSelection(PlayerList.getPlayerPosition( presidentName ));
        electedSpinner.setSelection(PlayerList.getPlayerPosition( targetName ));
    }

    @Override
    public boolean allInvolvedPlayersAreUnselected(ArrayList<String> unselectedPlayers) {
        return unselectedPlayers.contains(presidentName) && unselectedPlayers.contains(targetName);
    }

    @Override
    public JSONObject getJSON() throws JSONException {
        JSONObject obj = new JSONObject();

        obj.put("id", id);
        obj.put("type", "executive-action");
        obj.put("executive_action_type", "special_election");
        obj.put("president", presidentName);
        obj.put("target", targetName);

        return obj;
    }
}
