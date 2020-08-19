package de.tobiundmario.secrethitlermobilecompanion.SHClasses;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;

import de.tobiundmario.secrethitlermobilecompanion.R;

public class FascistTrackSelectionManager {

    public static final int TRACK_TYPE_5_TO_6 = 5;
    public static final int TRACK_TYPE_7_TO_8 = 6;
    public static final int TRACK_TYPE_9_TO_10 = 7;

    public static int selectedTrackIndex = -1;
    public static int recommendedTrackIndex = -1;

    public static ArrayList<CardView> trackCards = new ArrayList<>();
    public static ArrayList<FascistTrack> fasTracks = new ArrayList<>();
    public static CardView recommendedCard;

    public static void setupOfficialCard(CardView cardView, int trackType, final Context context) {
        TextView title_players = cardView.findViewById(R.id.tv_officialTrack_players);
        ImageView iv_trackImage = cardView.findViewById(R.id.iv_trackDrawable);
        Button btn_use = cardView.findViewById(R.id.btn_use);
        final int index;

        switch (trackType) {
            case TRACK_TYPE_5_TO_6:
                title_players.setText(context.getString(R.string.track_56));
                iv_trackImage.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.fascisttrack56));
                index = 0;
                break;
            case TRACK_TYPE_7_TO_8:
                title_players.setText(context.getString(R.string.track_78));
                iv_trackImage.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.fascisttrack78));
                index = 1;
                break;
            case TRACK_TYPE_9_TO_10:
                title_players.setText(context.getString(R.string.track_910));
                iv_trackImage.setImageDrawable(ContextCompat.getDrawable(context,  R.drawable.fascisttrack910));
                index = 2;
                break;
            default:
                index = -1;
        }

        btn_use.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                processSelection(index, context);
            }
        });
    }

    public static void changeRecommendedCard(int newRecommendationIndex, CardView cardView, Context context) {
        if(newRecommendationIndex == selectedTrackIndex) {
            updateTrackCard(cardView, true, context);
        }
        recommendedTrackIndex = newRecommendationIndex;
        recommendedCard = cardView;

        Log.v("Recommended Track", "Track is #" + recommendedTrackIndex + ", current selection is " + selectedTrackIndex);

        if(selectedTrackIndex == -1) processSelection(recommendedTrackIndex, context); //If there is no selection yet, automatically select the recommended one
    }

    public static void processSelection(int newSelection, Context context) {
        if(newSelection != -1) {
            //Unselecting the old card
            if (selectedTrackIndex != -1) {
                updateTrackCard(trackCards.get(selectedTrackIndex), false, context);
                if (selectedTrackIndex == recommendedTrackIndex) {
                    updateTrackCard(recommendedCard, false, context);
                }
            }

            //Select the new card
            if (newSelection == recommendedTrackIndex) {
                updateTrackCard(recommendedCard, true, context);
            }
            updateTrackCard(trackCards.get(newSelection), true, context);

            //Override the selection value
            selectedTrackIndex = newSelection;

            //Update the FascistTrack in GameLog
            GameLog.gameTrack = fasTracks.get(newSelection);
        } else if(selectedTrackIndex != -1){
            //When newSelection is -1, all items should be unselected
            updateTrackCard(trackCards.get(selectedTrackIndex), false, context);
            GameLog.gameTrack = null;

            selectedTrackIndex = -1;
            recommendedTrackIndex = -1;
        }
    }

    public static void updateTrackCard(CardView cardView, boolean selected, Context context) {
        Button button = cardView.findViewById(R.id.btn_use);

        if(selected) {
            button.setText(context.getString(R.string.in_use));
            button.setEnabled(false);

            button.setBackgroundColor(ContextCompat.getColor(context, R.color.fab_disabled));
        } else {
            button.setText(context.getString(R.string.use));
            button.setEnabled(true);

            button.setBackgroundColor(ContextCompat.getColor(context, R.color.start_server));
        }
    }
}
