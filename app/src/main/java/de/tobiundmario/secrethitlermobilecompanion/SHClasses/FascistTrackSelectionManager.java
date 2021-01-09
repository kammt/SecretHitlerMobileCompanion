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
import java.util.List;

import de.tobiundmario.secrethitlermobilecompanion.R;
import de.tobiundmario.secrethitlermobilecompanion.SHClasses.GameManager.GameEventsManager;
import de.tobiundmario.secrethitlermobilecompanion.SHClasses.GameManager.GameManager;
import de.tobiundmario.secrethitlermobilecompanion.SHClasses.GameManager.PlayerListManager;

public final class FascistTrackSelectionManager {

    public static final int TRACK_TYPE_5_TO_6 = 5;
    public static final int TRACK_TYPE_7_TO_8 = 6;
    public static final int TRACK_TYPE_9_TO_10 = 7;

    public static int selectedTrackIndex = -1;
    public static int recommendedTrackIndex = -1;

    public static List<CardView> trackCards = new ArrayList<>();
    public static List<FascistTrack> fasTracks = new ArrayList<>();
    public static CardView recommendedCard;
    public static CardView previousSelection;

    private FascistTrackSelectionManager() {}

    /*
    This class is responsible for handling clicks on the "Use"-button in the Fascist Track Selection Screen of the Setup phase.

    # Variable explanation:
    - selectedTrackIndex: The current index of the selected card. Everything that is or is greater than 3 is a custom track (since there are three official tracks). If there are no tracks selected, the value is -1
    - recommendedTrackIndex: The recommended track. Can have a value between 0 and 2. If there is no recommendation, the value is -1
    - trackCards and fasTracks are ArrayLists that hold the CardViews and FascistTrack classes of the official tracks. This system is not used for custom tracks as they can be deleted during the setup, which would involve clearing both ArrayLists / inserting new objects
    - recommendedCard holds the CardView of the recommended Track. This is necessary as we need to change the button when it is selected/unselected
    - previousSelection holds the CardView of the previous selection. This is needed as the cardViews of custom tracks are not to be found in the trackCards ArrayList
     */

    public static void destroy() {
        trackCards = null;
        fasTracks = null;
        recommendedCard = null;
        previousSelection = null;
    }

    public static void initialise() {
        trackCards = new ArrayList<>();
        fasTracks = new ArrayList<>();
    }

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
            updateTrackCard(cardView, true, context, -2);
        }
        recommendedTrackIndex = newRecommendationIndex;
        recommendedCard = cardView;

        Log.v("Recommended Track", "Track is #" + recommendedTrackIndex + ", current selection is " + selectedTrackIndex);

        if(selectedTrackIndex == -1) processSelection(recommendedTrackIndex, trackCards.get(recommendedTrackIndex), fasTracks.get(recommendedTrackIndex), context); //If there is no selection yet, automatically select the recommended one
    }

    public static void processSelection(int newSelection, CardView cardView, FascistTrack fascistTrack, Context context) {
        if(newSelection != -1) {
            //Unselecting the old card
            if (selectedTrackIndex != -1) updateTrackCard(previousSelection, false, context, selectedTrackIndex);

            //Select the new card
            updateTrackCard(cardView, true, context, newSelection); //If it is an official card, we just need to get the cardView from the ArrayList

            //Override the selection value
            selectedTrackIndex = newSelection;

            //Update the FascistTrack in GameLog
            GameManager.gameTrack = fascistTrack;
            previousSelection = cardView;
        } else if(selectedTrackIndex != -1){
            //When newSelection is -1, all items should be unselected
            updateTrackCard(trackCards.get(selectedTrackIndex), false, context, selectedTrackIndex);
            GameManager.gameTrack = null;

            recommendedTrackIndex = -1;
        }
    }

    public static void updateTrackCard(CardView cardView, boolean selected, Context context, int index) {
        if(index == recommendedTrackIndex) updateTrackCard(recommendedCard, selected, context, -2);
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
        if(ll_official_tracks.getChildCount() > 1 && fasTracks.size() > 0) return;
        else if(ll_official_tracks.getChildCount() > 1 && fasTracks.size() == 0) { //In this case, the arrays were cleared to prevent memory leaks. However, the CardViews are still there. Hence, we remove them first
            while(ll_official_tracks.getChildCount() > 1) ll_official_tracks.removeViewAt(ll_official_tracks.getChildCount() - 1);
        }

        //Adding the official tracks to the LinearLayout
        addOfficialTracksToLayout(LayoutInflater.from(c), ll_official_tracks, c);
    }

    private static void addOfficialTracksToLayout(LayoutInflater inflater, LinearLayout ll_official_tracks, Context c) {
        //For 5-6 players
        CardView cv_56 = (CardView) inflater.inflate(R.layout.card_official_track, ll_official_tracks, false);
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
        FascistTrackSelectionManager.fasTracks.add(1, ft_78);

        //For 9-10 players
        CardView cv_910 = (CardView) inflater.inflate(R.layout.card_official_track, ll_official_tracks, false);
        FascistTrackSelectionManager.setupOfficialCard(cv_910, FascistTrackSelectionManager.TRACK_TYPE_9_TO_10, c);
        ll_official_tracks.addView(cv_910);
        FascistTrackSelectionManager.trackCards.add(2, cv_910);

        FascistTrack ft_910 = new FascistTrack();
        ft_910.setActions(new int[] {FascistTrack.INVESTIGATION, FascistTrack.INVESTIGATION, FascistTrack.SPECIAL_ELECTION, FascistTrack.EXECUTION, FascistTrack.EXECUTION});
        ft_910.setElectionTrackerLength(3);
        FascistTrackSelectionManager.fasTracks.add(2, ft_910);
    }

    public static int getRecommendedTrack() {
        int playerCount = PlayerListManager.getPlayerList().size();
        if(playerCount == 5 || playerCount == 6) {
            return 0;
        } else if(playerCount == 7 || playerCount == 8) {
            return 1;
        } else if(playerCount == 9 || playerCount ==10) {
            return 2;
        }

        else return -1;
    }

    public static void setupRecommendedCard(LayoutInflater inflater, LinearLayout container_recommended_track) {
        int recommendation = getRecommendedTrack();
        Context context = GameEventsManager.getContext();

        //Only change the layout if the recommendation changed
        if(recommendation != FascistTrackSelectionManager.recommendedTrackIndex) {
            CardView cv_recommended_track = (CardView) inflater.inflate(R.layout.card_official_track, container_recommended_track, false);

            if (recommendation == 0) {
                FascistTrackSelectionManager.setupOfficialCard(cv_recommended_track, FascistTrackSelectionManager.TRACK_TYPE_5_TO_6, context);
            } else if (recommendation == 1) {
                FascistTrackSelectionManager.setupOfficialCard(cv_recommended_track, FascistTrackSelectionManager.TRACK_TYPE_7_TO_8, context);
            } else if (recommendation == 2) {
                FascistTrackSelectionManager.setupOfficialCard(cv_recommended_track, FascistTrackSelectionManager.TRACK_TYPE_9_TO_10, context);
            }

            container_recommended_track.removeAllViews();
            container_recommended_track.addView(cv_recommended_track);
            FascistTrackSelectionManager.changeRecommendedCard(recommendation, cv_recommended_track, context);
        }
    }
}
