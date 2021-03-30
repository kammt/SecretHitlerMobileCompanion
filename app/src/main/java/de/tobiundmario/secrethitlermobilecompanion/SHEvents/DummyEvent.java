package de.tobiundmario.secrethitlermobilecompanion.SHEvents;

import android.view.View;
import android.widget.TextView;

import androidx.cardview.widget.CardView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import de.tobiundmario.secrethitlermobilecompanion.R;

/**
 * This event does nothing. It is used for the main screen, as inserting a Dummy Event inserts a "demo CardView" into the RecyclerView
 */
public class DummyEvent extends GameEvent {
    @Override
    public void initialiseSetupCard(CardView cardView) {

    }

    @Override
    public void setCurrentValues(CardView cardView) {

    }

    @Override
    public void initialiseCard(CardView cardView) {
        cardView.findViewById(R.id.warning_mismatching_claims).setVisibility(View.GONE);
        ((TextView) cardView.findViewById(R.id.pres_name)).setText("████████");
        ((TextView) cardView.findViewById(R.id.chanc_name)).setText("████████");

        ((TextView) cardView.findViewById(R.id.pres_claim)).setText("██");
        ((TextView) cardView.findViewById(R.id.chanc_claim)).setText("██");
    }

    @Override
    public boolean allInvolvedPlayersAreUnselected(List<String> unselectedPlayers) {
        return false;
    }

    @Override
    public JSONObject getJSON() throws JSONException {
        return null;
    }
}
