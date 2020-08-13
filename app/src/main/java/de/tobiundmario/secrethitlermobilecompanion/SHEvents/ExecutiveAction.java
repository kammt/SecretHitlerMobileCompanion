package de.tobiundmario.secrethitlermobilecompanion.SHEvents;

import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.Html;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;

import de.tobiundmario.secrethitlermobilecompanion.R;

public abstract class ExecutiveAction extends GameEvent {
    /*
    Every Executive action uses the same card layout in its normal state. Because of this, the setupCard function is implemented here. However, the image on the top right of the card and the info text is handled by the events themselves.
    This class retrieves them using getInfoText() and getDrawable()
     */
    public abstract String getInfoText();
    public abstract Drawable getDrawable();

    public void setupCard(CardView cardLayout) {
        TextView tvInfo = cardLayout.findViewById(R.id.infoText);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            tvInfo.setText(Html.fromHtml(getInfoText(),  Html.FROM_HTML_MODE_LEGACY), TextView.BufferType.SPANNABLE);
        } else {
            tvInfo.setText(Html.fromHtml(getInfoText()), TextView.BufferType.SPANNABLE);
        }

        ((ImageView) cardLayout.findViewById(R.id.img_action)).setImageDrawable(getDrawable());
    }
}
