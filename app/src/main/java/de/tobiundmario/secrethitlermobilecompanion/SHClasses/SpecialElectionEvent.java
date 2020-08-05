package de.tobiundmario.secrethitlermobilecompanion.SHClasses;

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

import de.tobiundmario.secrethitlermobilecompanion.CardSetupHelper;
import de.tobiundmario.secrethitlermobilecompanion.R;

public class SpecialElectionEvent extends ExecutiveAction {
    Context context;
    private String presidentName, electedPlayerName;

    public SpecialElectionEvent(String presidentName, String electedPlayerName, Context context, boolean setup) {
        this.context = context;
        this.presidentName = presidentName;
        this.electedPlayerName = electedPlayerName;
        isSetup = setup;
    }

    @Override
    public String getInfoText() {
        return context.getString(R.string.specialElection_string, PlayerList.boldPlayerName(presidentName), PlayerList.boldPlayerName(electedPlayerName));
    }

    @Override
    public Drawable getDrawable() {
        return context.getDrawable(R.drawable.special_election);
    }

    @Override
    public void setupSetupCard(CardView cardView) {
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

        final Spinner electedSpinner = cardView.findViewById(R.id.spinner_executed_player);
        electedSpinner.setAdapter(playerListadapter);
        electedSpinner.setSelection(1); //Setting a different item on the elected player spinner so they don't have the same name at the beginning

        final FloatingActionButton fab_create = cardView.findViewById(R.id.fab_create);
        fab_create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presidentName = (String) presSpinner.getSelectedItem();
                electedPlayerName = (String) electedSpinner.getSelectedItem();

                if(presidentName.equals(electedPlayerName)) {
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
        electedSpinner.setSelection(PlayerList.getPlayerPosition( electedPlayerName ));
    }

    @Override
    public boolean allInvolvedPlayersAreUnselected(ArrayList<String> unselectedPlayers) {
        return unselectedPlayers.contains(presidentName) && unselectedPlayers.contains(electedPlayerName);
    }

    @Override
    public JSONObject getJSON() throws JSONException {
        JSONObject obj = new JSONObject();

        obj.put("type", "executive-action");
        obj.put("executive_action_type", "special_election");
        obj.put("president", presidentName);
        obj.put("target", electedPlayerName);

        return obj;
    }
}
