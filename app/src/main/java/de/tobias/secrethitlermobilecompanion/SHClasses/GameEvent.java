package de.tobias.secrethitlermobilecompanion.SHClasses;

import androidx.cardview.widget.CardView;

import java.util.ArrayList;

public abstract class GameEvent {
    /*
        The GameEvent is a class which is inherited by all other Events that can happen in this game (ExecutionEvent, LegislativeSession etc.)
        This simplifies the handling of different event types by the GameLog class.
     */

    public abstract void setupCard(CardView cardView); //Called by the RecyclerViewAdapter. The classes have to set up the card e.g. insert the President and Chancellor names
    public abstract boolean allInvolvedPlayersAreUnselected(ArrayList<String> unselectedPlayers); //Should return true if all involved Players are unselected. Necessary for event blurring, see GameLog.java for usage
}
