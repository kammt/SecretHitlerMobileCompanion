package de.tobias.secrethitlermobilecompanion.SHClasses;

import androidx.cardview.widget.CardView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public abstract class GameEvent {
    /*
        The GameEvent is a class which is inherited by all other Events that can happen in this game (ExecutionEvent, LegislativeSession etc.)
        This simplifies the handling of different event types by the GameLog class.
     */

    public boolean isSetup = false;
    public abstract void setupSetupCard(CardView cardView);

    public boolean permitEditing = true;
    public boolean isEditing = false;
    public abstract void setupEditCard(CardView cardView); //This function will be called when the user long presses on a card to edit it. It is called in after setupSetupCard() and will fill in the fields with the current values that the card has


    public abstract void setupCard(CardView cardView); //Called by the RecyclerViewAdapter. The classes have to set up the card e.g. insert the President and Chancellor names
    public abstract boolean allInvolvedPlayersAreUnselected(ArrayList<String> unselectedPlayers); //Should return true if all involved Players are unselected. Necessary for event blurring, see GameLog.java for usage

    public abstract JSONObject getJSON() throws JSONException;
}
