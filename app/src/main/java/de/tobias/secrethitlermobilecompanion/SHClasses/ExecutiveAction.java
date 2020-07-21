package de.tobias.secrethitlermobilecompanion.SHClasses;

import android.graphics.drawable.Drawable;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;

import de.tobias.secrethitlermobilecompanion.R;

public abstract class ExecutiveAction extends GameEvent {
    public abstract String getInfoText();
    public abstract Drawable getDrawable();

    public void setupCard(CardView cardLayout) {
        TextView tvInfo = cardLayout.findViewById(R.id.infoText);
        tvInfo.setText(getInfoText());

        ImageView ivIcon = cardLayout.findViewById(R.id.img_action);
        ivIcon.setImageDrawable(getDrawable());
    }
}
