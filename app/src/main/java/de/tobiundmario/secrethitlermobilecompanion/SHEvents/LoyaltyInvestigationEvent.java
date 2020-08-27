package de.tobiundmario.secrethitlermobilecompanion.SHEvents;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
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

import de.tobiundmario.secrethitlermobilecompanion.R;
import de.tobiundmario.secrethitlermobilecompanion.SHClasses.Claim;
import de.tobiundmario.secrethitlermobilecompanion.SHClasses.GameLog;
import de.tobiundmario.secrethitlermobilecompanion.SHClasses.PlayerList;

import static de.tobiundmario.secrethitlermobilecompanion.SHCards.CardSetupHelper.getPlayerNameAdapter;

public class LoyaltyInvestigationEvent extends ExecutiveAction {

    private Context c;
    private String presidentName, playerName;
    private int claim;

    //Defining the OnClickListeners outside of the function to call them separately in the setupEditCard() function
    private View.OnClickListener iv_fascistListener, iv_liberalListener;

    public LoyaltyInvestigationEvent(String presidentName, String playerName, int claim, Context context) {
        this.presidentName = presidentName;
        this.playerName = playerName;
        this.claim = claim;
        this.c = context;
        PlayerList.setClaim(playerName, claim);
    }

    public LoyaltyInvestigationEvent(String presidentName, Context context) {
        this.presidentName = presidentName;
        this.c = context;
        isSetup = true;
    }

    public void resetOnRemoval() {
        PlayerList.setClaim(playerName, Claim.NO_CLAIM);
    }

    public void undoRemoval() {
        PlayerList.setClaim(playerName, claim);
    }

    @Override
    public String getInfoText() {
        return c.getString(R.string.investigation_string, PlayerList.boldPlayerName(presidentName), PlayerList.boldPlayerName(playerName), Claim.getClaimString(c, claim));
    }

    @Override
    public Drawable getDrawable() {
        return c.getDrawable(R.drawable.investigate_loyalty);
    }

    @Override
    public void initialiseSetupCard(CardView cardView) {
        //Setting up Spinners
        final Spinner presSpinner = cardView.findViewById(R.id.spinner_president);
        ArrayAdapter<String> playerListadapter = getPlayerNameAdapter(c);
        playerListadapter.setDropDownViewResource(android.R.layout
                .simple_spinner_dropdown_item);
        presSpinner.setAdapter(playerListadapter);

        final Spinner investigatedSpinner = cardView.findViewById(R.id.spinner_investigated_player);
        investigatedSpinner.setAdapter(playerListadapter);
        investigatedSpinner.setSelection(1); //Setting a different item on the investigated player spinner so they don't have the same name at the beginning

        //Initialising all other important aspects
        final ImageView iv_fascist = cardView.findViewById(R.id.img_policy_fascist);
        final ImageView iv_liberal = cardView.findViewById(R.id.img_policy_liberal);
        final FloatingActionButton fab_create = cardView.findViewById(R.id.fab_create);


        iv_liberalListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                iv_liberal.setAlpha((float) 1);
                iv_fascist.setAlpha((float) 0.2);

                ColorStateList csl = ColorStateList.valueOf(c.getColor(R.color.colorLiberal));
                fab_create.setBackgroundTintList(csl);
            }
        };
        iv_liberal.setOnClickListener(iv_liberalListener);

        iv_fascistListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                iv_fascist.setAlpha((float) 1);
                iv_liberal.setAlpha((float) 0.2);

                ColorStateList csl = ColorStateList.valueOf(c.getColor(R.color.colorFascist));
                fab_create.setBackgroundTintList(csl);
            }
        };
        iv_fascist.setOnClickListener(iv_fascistListener);

        fab_create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isEditing) {
                    resetOnRemoval(); //Undo the old values (i.e. un-setting the claim image)
                }

                presidentName = (String) presSpinner.getSelectedItem();
                playerName = (String) investigatedSpinner.getSelectedItem();
                claim = (iv_fascist.getAlpha() == (float) 1) ? Claim.FASCIST : Claim.LIBERAL;

                if(presidentName.equals(playerName)) {
                    Toast.makeText(c, c.getString(R.string.err_names_cannot_be_the_same), Toast.LENGTH_LONG).show();
                } else {
                    isSetup = false;
                    PlayerList.setClaim(playerName, claim);
                    GameLog.notifySetupPhaseLeft(LoyaltyInvestigationEvent.this);
                }
            }
        });
    }

    @Override
    public void setCurrentValues(CardView cardView) {
        Spinner presSpinner = cardView.findViewById(R.id.spinner_president);
        Spinner investigatedSpinner = cardView.findViewById(R.id.spinner_investigated_player);

        if(claim == Claim.LIBERAL) iv_liberalListener.onClick(null);
        else iv_fascistListener.onClick(null);

        presSpinner.setSelection(PlayerList.getPlayerPosition( presidentName ));
        investigatedSpinner.setSelection(PlayerList.getPlayerPosition( playerName ));
    }

    @Override
    public boolean allInvolvedPlayersAreUnselected(ArrayList<String> unselectedPlayers) {
        return unselectedPlayers.contains(presidentName) && unselectedPlayers.contains(playerName);
    }

    @Override
    public JSONObject getJSON() throws JSONException {
        JSONObject obj = new JSONObject();

        obj.put("id", id);
        obj.put("type", "executive-action");
        obj.put("executive_action_type", "investigate_loyalty");
        obj.put("president", presidentName);
        obj.put("target", playerName);
        obj.put("claim", Claim.getClaimStringForJSON(c, claim));

        return obj;
    }
}
