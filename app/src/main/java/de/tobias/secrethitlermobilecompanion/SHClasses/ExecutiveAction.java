package de.tobias.secrethitlermobilecompanion.SHClasses;

import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.Html;
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            tvInfo.setText(Html.fromHtml(getInfoText(),  Html.FROM_HTML_MODE_LEGACY), TextView.BufferType.SPANNABLE);
        } else {
            tvInfo.setText(Html.fromHtml(getInfoText()), TextView.BufferType.SPANNABLE);
        }

        ImageView ivIcon = cardLayout.findViewById(R.id.img_action);
        ivIcon.setImageDrawable(getDrawable());
    }
}
