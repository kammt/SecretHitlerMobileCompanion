package de.tobias.secrethitlermobilecompanion.SHClasses;

import android.content.Context;
import android.text.Spannable;

import androidx.cardview.widget.CardView;

import org.json.JSONObject;

import java.util.ArrayList;

public abstract class GameEvent {
    public abstract void setupCard(CardView cardView); //Called by the RecyclerViewAdapter. The classes have to set up the card e.g. insert the President and Chancellor names
    public abstract boolean allInvolvedPlayersAreUnselected(ArrayList<String> unselectedPlayers); //Should return true if all involved Players are unselected. Necessary for event blurring, see GameLog.java for usage
}
