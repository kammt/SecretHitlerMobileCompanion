package de.tobias.secrethitlermobilecompanion.SHClasses;

import android.content.Context;
import android.text.Spannable;

import androidx.cardview.widget.CardView;

import org.json.JSONObject;

public abstract class GameEvent {
    public abstract void setupCard(CardView cardView);
}
