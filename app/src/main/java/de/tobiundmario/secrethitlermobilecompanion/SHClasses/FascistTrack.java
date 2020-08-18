package de.tobiundmario.secrethitlermobilecompanion.SHClasses;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import org.json.JSONException;
import org.json.JSONObject;

import de.tobiundmario.secrethitlermobilecompanion.R;

public class FascistTrack {

    public static final int NO_POWER = 0;
    public static final int EXECUTION = 1;
    public static final int INVESTIGATION = 2;
    public static final int DECK_PEEK = 3;
    public static final int SPECIAL_ELECTION = 4;

    public static final int TRACK_TYPE_5_TO_6 = 5;
    public static final int TRACK_TYPE_7_TO_8 = 6;
    public static final int TRACK_TYPE_9_TO_10 = 7;

    private int[] actions = new int[6];
    private int fasPolicies = 6;
    private int libPolicies = 5;

    private boolean isOfficial = false;
    private Drawable officialTrackImage = null;

    private int minPlayers;
    private int maxPlayers;

    private int hzStartingPolicy = 3;
    private int vetoStartingPolicy = 5;

    public FascistTrack(int minPlayers, int maxPlayers) {
        this.minPlayers = minPlayers;
        this.maxPlayers = maxPlayers;
    }

    public void setActions(int[] actions) {
        this.actions = actions;
    }

    public int[] getActions() {
        return actions;
    }

    public void setAction(int position, int action) {
        actions[position] = action;
    }

    public int getAction(int position) {
        return actions[position];
    }

    public void setHZStartingPolicy(int HZStartingPolicy) {
        this.hzStartingPolicy = HZStartingPolicy;
    }

    public int getHzStartingPolicy() {
        return hzStartingPolicy;
    }

    public void setVetoStartingPolicy(int vetoStartingPolicy) {
        this.vetoStartingPolicy = vetoStartingPolicy;
    }

    public int getVetoStartingPolicy() {
        return vetoStartingPolicy;
    }

    public int getMinPlayers() {
        return minPlayers;
    }

    public void setMinPlayers(int minPlayers) {
        this.minPlayers = minPlayers;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public void setMaxPlayers(int maxPlayers) {
        this.maxPlayers = maxPlayers;
    }

    public boolean isOfficial() {
        return isOfficial;
    }

    public void setOfficial(boolean official) {
        isOfficial = official;
    }

    public void setOfficialTrackImage(Drawable officialTrackImage) {
        this.officialTrackImage = officialTrackImage;
    }

    public Drawable getOfficialTrackImage() {
        return officialTrackImage;
    }

    public void setLibPolicies(int libPolicies) {
        this.libPolicies = libPolicies;
    }

    public int getLibPolicies() {
        return libPolicies;
    }

    public void setFasPolicies(int fasPolicies) {
        this.fasPolicies = fasPolicies;

        //Add another entry to the array or remove one
        int[] oldArray = actions;
        actions = new int[fasPolicies];

        for (int i = 0; i < actions.length; i++) {
            try {
                actions[i] = oldArray[i];
            }catch (ArrayIndexOutOfBoundsException e) {
                break;
            }
        }
    }

    public int getFasPolicies() {
        return fasPolicies;
    }

    public JSONObject writeToJSON() throws JSONException {
        JSONObject object = new JSONObject();

        object.put("actions", actions);

        object.put("hz", hzStartingPolicy);
        object.put("veto", vetoStartingPolicy);

        object.put("min", minPlayers);
        object.put("max", maxPlayers);

        object.put("official", isOfficial);
        if(isOfficial) object.put("trackImage", officialTrackImage);

        return object;
    }

    public static FascistTrack restoreFromJSON(JSONObject object) throws JSONException {
        FascistTrack track = new FascistTrack(object.getInt("min"), object.getInt("max"));

        track.setActions((int[]) object.get("actions"));

        track.setHZStartingPolicy(object.getInt("hz"));
        track.setVetoStartingPolicy(object.getInt("veto"));

        track.setOfficial(object.getBoolean("official"));
        if(track.isOfficial()) track.setOfficialTrackImage((Drawable) object.get("trackImage"));

        return track;
    }

    public static void setupOfficialCard(CardView cardView, int trackType, Context context) {
        TextView title_players = cardView.findViewById(R.id.tv_officialTrack_players);
        ImageView iv_trackImage = cardView.findViewById(R.id.iv_trackDrawable);

        switch (trackType) {
            case TRACK_TYPE_5_TO_6:
                title_players.setText(context.getString(R.string.track_56));
                iv_trackImage.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.fascisttrack56));
                break;
            case TRACK_TYPE_7_TO_8:
                title_players.setText(context.getString(R.string.track_78));
                iv_trackImage.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.fascisttrack78));
                break;
            case TRACK_TYPE_9_TO_10:
                title_players.setText(context.getString(R.string.track_910));
                iv_trackImage.setImageDrawable(ContextCompat.getDrawable(context,  R.drawable.fascisttrack910));
                break;
        }
    }

}
