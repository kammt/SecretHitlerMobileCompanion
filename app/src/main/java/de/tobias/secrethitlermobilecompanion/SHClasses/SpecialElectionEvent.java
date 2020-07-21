package de.tobias.secrethitlermobilecompanion.SHClasses;

import android.content.Context;
import android.graphics.drawable.Drawable;

import java.util.ArrayList;

import de.tobias.secrethitlermobilecompanion.R;

public class SpecialElectionEvent extends ExecutiveAction {
    Context context;
    private String presidentName, electedPlayerName;

    public SpecialElectionEvent(String presidentName, String electedPlayerName, Context context) {
        this.context = context;
        this.presidentName = presidentName;
        this.electedPlayerName = electedPlayerName;
    }

    @Override
    public String getInfoText() {
        return context.getString(R.string.specialElection_string, presidentName, electedPlayerName);
    }

    @Override
    public Drawable getDrawable() {
        return context.getDrawable(R.drawable.special_election);
    }

    @Override
    public boolean allInvolvedPlayersAreUnselected(ArrayList<String> unselectedPlayers) {
        return unselectedPlayers.contains(presidentName) && unselectedPlayers.contains(electedPlayerName);
    }
}
