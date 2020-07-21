package de.tobias.secrethitlermobilecompanion.SHClasses;

import android.content.Context;
import android.graphics.drawable.Drawable;

import java.util.ArrayList;

import de.tobias.secrethitlermobilecompanion.R;

public class ExecutionEvent extends ExecutiveAction {
    Context context;
    private String presidentName, executedPlayerName;

    public ExecutionEvent(String presidentName, String executedPlayerName, Context context) {
        this.context = context;
        this.presidentName = presidentName;
        this.executedPlayerName = executedPlayerName;
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
    public boolean allInvolvedPlayersAreUnselected(ArrayList<String> unselectedPlayers) {
        return unselectedPlayers.contains(presidentName) && unselectedPlayers.contains(executedPlayerName);
    }
}
