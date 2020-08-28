package de.tobiundmario.secrethitlermobilecompanion.SHClasses;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
    public static CardView previousSelection;

    /*
    This class is responsible for handling clicks on the "Use"-button in the Fascist Track Selection Screen of the Setup phase.

    # Variable explanation:
    - selectedTrackIndex: The current index of the selected card. Everything that is or is greater than 3 is a custom track (since there are three official tracks). If there are no tracks selected, the value is -1
    - recommendedTrackIndex: The recommended track. Can have a value between 0 and 2. If there is no recommendation, the value is -1
    - trackCards and fasTracks are ArrayLists that hold the CardViews and FascistTrack classes of the official tracks. This system is not used for custom tracks as they can be deleted during the setup, which would involve clearing both ArrayLists / inserting new objects
    - recommendedCard holds the CardView of the recommended Track. This is necessary as we need to change the button when it is selected/unselected
    - previousSelection holds the CardView of the previous selection. This is needed as the cardViews of custom tracks are not to be found in the trackCards ArrayList
     */

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
                processSelection(index, trackCards.get(index), fasTracks.get(index), context);
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

        if(selectedTrackIndex == -1) processSelection(recommendedTrackIndex, trackCards.get(recommendedTrackIndex), fasTracks.get(recommendedTrackIndex), context); //If there is no selection yet, automatically select the recommended one
    }

    public static void processSelection(int newSelection, CardView cardView, FascistTrack fascistTrack, Context context) {
        if(newSelection != -1) {
            //Unselecting the old card
            if (selectedTrackIndex != -1) {

                updateTrackCard(previousSelection, false, context);
                if (selectedTrackIndex == recommendedTrackIndex) {
                    updateTrackCard(recommendedCard, false, context);
                }

            }

            //Select the new card
            if (newSelection == recommendedTrackIndex) {
                updateTrackCard(recommendedCard, true, context);
            }

            updateTrackCard(cardView, true, context); //If it is an official card, we just need to get the cardView from the ArrayList

            //Override the selection value
            selectedTrackIndex = newSelection;

            //Update the FascistTrack in GameLog
            GameLog.gameTrack = fascistTrack;
            previousSelection = cardView;
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

    public static void setupOfficialTrackList(LinearLayout ll_official_tracks, Context c) {
        if(ll_official_tracks.getChildCount() > 1) return;

        //Adding the official tracks to the LinearLayout
        LayoutInflater inflater = LayoutInflater.from(c);

        //For 5-6 players
        final CardView cv_56 = (CardView) inflater.inflate(R.layout.card_official_track, ll_official_tracks, false);
        FascistTrackSelectionManager.setupOfficialCard(cv_56, FascistTrackSelectionManager.TRACK_TYPE_5_TO_6, c);
        ll_official_tracks.addView(cv_56);
        FascistTrackSelectionManager.trackCards.add(0, cv_56);

        FascistTrack ft_56 = new FascistTrack();
        ft_56.setActions(new int[] {FascistTrack.NO_POWER, FascistTrack.NO_POWER, FascistTrack.DECK_PEEK, FascistTrack.EXECUTION, FascistTrack.EXECUTION});
        ft_56.setElectionTrackerLength(3);
        FascistTrackSelectionManager.fasTracks.add(0, ft_56);

        //For 7-8 players
        CardView cv_78 = (CardView) inflater.inflate(R.layout.card_official_track, ll_official_tracks, false);
        FascistTrackSelectionManager.setupOfficialCard(cv_78, FascistTrackSelectionManager.TRACK_TYPE_7_TO_8, c);
        ll_official_tracks.addView(cv_78);
        FascistTrackSelectionManager.trackCards.add(1, cv_78);

        FascistTrack ft_78 = new FascistTrack();
        ft_78.setActions(new int[] {FascistTrack.NO_POWER, FascistTrack.INVESTIGATION, FascistTrack.SPECIAL_ELECTION, FascistTrack.EXECUTION, FascistTrack.EXECUTION});
        ft_78.setElectionTrackerLength(3);
        FascistTrackSelectionManager.fasTracks.add(0, ft_78);

        //For 9-10 players
        CardView cv_910 = (CardView) inflater.inflate(R.layout.card_official_track, ll_official_tracks, false);
        FascistTrackSelectionManager.setupOfficialCard(cv_910, FascistTrackSelectionManager.TRACK_TYPE_9_TO_10, c);
        ll_official_tracks.addView(cv_910);
        FascistTrackSelectionManager.trackCards.add(2, cv_910);

        FascistTrack ft_910 = new FascistTrack();
        ft_910.setActions(new int[] {FascistTrack.INVESTIGATION, FascistTrack.INVESTIGATION, FascistTrack.SPECIAL_ELECTION, FascistTrack.EXECUTION, FascistTrack.EXECUTION});
        ft_910.setElectionTrackerLength(3);
        FascistTrackSelectionManager.fasTracks.add(0, ft_910);
    }
}
