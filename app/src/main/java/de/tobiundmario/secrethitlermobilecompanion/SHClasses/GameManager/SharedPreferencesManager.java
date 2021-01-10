package de.tobiundmario.secrethitlermobilecompanion.SHClasses.GameManager;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import de.tobiundmario.secrethitlermobilecompanion.ExceptionHandler;
import de.tobiundmario.secrethitlermobilecompanion.MainActivity;
import de.tobiundmario.secrethitlermobilecompanion.R;
import de.tobiundmario.secrethitlermobilecompanion.RecyclerViewAdapters.CustomTracksRecyclerViewAdapter;
import de.tobiundmario.secrethitlermobilecompanion.RecyclerViewAdapters.OldPlayerListRecyclerViewAdapter;
import de.tobiundmario.secrethitlermobilecompanion.SHClasses.FascistTrack;

public final class SharedPreferencesManager {

    private SharedPreferencesManager() {}

    private static boolean playerListsTheSame(JSONObject one, JSONObject two) throws JSONException {
        int[] lengths = new int[2];
        lengths[0] = getPlayerListLength(one);
        lengths[1] = getPlayerListLength(two);
        if(lengths[0] != lengths[1]) return false;

        for(int i = 0; i < lengths[0]; i++) {
            String name = (String) one.get(Integer.toString(i));

            boolean isPresent = contains(two, name);
            if(!isPresent) return false;
        }
        return true;
    }

    private static boolean contains(JSONObject jsonObject, String name) throws JSONException {
        int length = getPlayerListLength(jsonObject);
        for (int j = 0; j < length; j++) {
            String secondName = (String) jsonObject.get(Integer.toString(j));
            if(secondName.equals(name)) return true;
        }
        return false; //We just checked the last value and the name is not there, they cannot be the same
    }

    private static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences("past-values", Context.MODE_PRIVATE);
    }

    public static JSONArray addJSONObjectToArray(JSONObject jsonObject, JSONArray jsonArray, int position) throws JSONException {
        JSONObject objectToInsert = null;
        JSONObject objectAtPos;
        int originalLength = jsonArray.length();

        for(int i = position; i < originalLength + 1; i++) {//We start at the insertion point, we don't need to do anything before that point
            if(objectToInsert == null) objectToInsert = jsonObject; //We are at the insertion point, so we want to insert the list here

            if(i == originalLength) { //When it is in the last list item, we just want to insert the item from before and end the loop
                jsonArray.put(i, objectToInsert);
                break;
            }

            objectAtPos = jsonArray.getJSONObject(i); //Before inserting, we get the current object to move it to the next position later on
            jsonArray.put(i, objectToInsert);

            objectToInsert = objectAtPos;
        }

        return jsonArray;
    }


    //Old Player Lists related
    public static void writeCurrentPlayerListIfNew(Context context) throws JSONException {
        JSONObject playerListAsJSON = playerListtoJSON();

        if(playerListAlreadyPresent(playerListAsJSON, context)) return;

        JSONArray array = getJsonArray(context, true);
        array.put(playerListAsJSON);
        writePastPlayerLists(array, context);
    }

    private static void writePastPlayerLists(JSONArray array, Context context) throws JSONException {
        SharedPreferences preferences = getSharedPreferences(context);

        JSONObject object = new JSONObject();
        object.put("players", array);

        preferences.edit().putString("old-players", object.toString()).apply();

        OldPlayerListRecyclerViewAdapter oldPlayerListRecyclerViewAdapter = RecyclerViewManager.getOldPlayerListRecyclerViewAdapter();
        if(oldPlayerListRecyclerViewAdapter != null) {
            oldPlayerListRecyclerViewAdapter.oldPlayers = array;
            oldPlayerListRecyclerViewAdapter.notifyDataSetChanged();
        }
    }

    private static boolean playerListAlreadyPresent(JSONObject playerJSON, Context context) throws JSONException {
        JSONArray array = getJsonArray(context, true);

        for(int i = 0; i < array.length(); i++) {
            JSONObject object = array.getJSONObject(i);
            if(playerListsTheSame(playerJSON, object)) return true;
        }
        return false; //No objects match
    }

    private static int getPlayerListLength(JSONObject object) {
        return (object.has("name")) ? object.length() - 1 : object.length();
    }

    private static JSONObject playerListtoJSON() throws JSONException {
        ArrayList<String> playerList = PlayerListManager.getPlayerList();

        JSONObject object = new JSONObject();
        for(int i = 0; i < playerList.size(); i++) {
            object.put(Integer.toString(i), playerList.get(i));
        }
        return object;
    }

    public static void setPlayerListName(String name, int position, Context context) throws JSONException {
        JSONArray newArray = getJsonArray(context, true);
        JSONObject object = newArray.getJSONObject(position);

        if(!name.matches("")) object.put("name", name);
        else if(object.has("name")) object.remove("name");
        writePastPlayerLists(newArray, context);
    }


    public static JSONObject removePlayerList(int position, Context context) throws JSONException {
        JSONArray array = getJsonArray(context, true);
        JSONObject removed = array.getJSONObject(position);
        array.remove(position);
        writePastPlayerLists(array, context);
        RecyclerViewManager.getOldPlayerListRecyclerViewAdapter().notifyItemRemoved(position);

        setCustomTitle(context, true);

        return removed;
    }

    //FascistTrack related
    public static void writeFascistTrack(FascistTrack fascistTrack, Context context) throws JSONException {
        JSONObject trackAsJSON = JSONManager.writeFascistTrackToJSON(fascistTrack);

        JSONArray array = getJsonArray(context, false);
        array.put(trackAsJSON);
        writeFascistTracks(array, context);
    }

    private static void writeFascistTracks(JSONArray array, Context context) throws JSONException {
        SharedPreferences preferences = getSharedPreferences(context);

        JSONObject object = new JSONObject();
        object.put("tracks", array);

        preferences.edit().putString("fas-tracks", object.toString()).apply();

        CustomTracksRecyclerViewAdapter customTracksRecyclerViewAdapter = RecyclerViewManager.getCustomTracksRecyclerViewAdapter();
        if(customTracksRecyclerViewAdapter != null) {
            customTracksRecyclerViewAdapter.tracks = array;
            customTracksRecyclerViewAdapter.notifyDataSetChanged();
            setCustomTitle(context, false);
        }
    }

    public static JSONObject removeFascistTrack(int position, Context context) throws JSONException {
        JSONArray array = getJsonArray(context, false);
        JSONObject removed = array.getJSONObject(position);
        array.remove(position);
        writeFascistTracks(array, context);
        RecyclerViewManager.getCustomTracksRecyclerViewAdapter().notifyItemRemoved(position);

        setCustomTitle(context, false);

        return removed;
    }

    public static JSONArray getJsonArray(Context context, boolean isPlayerList) throws JSONException {
        SharedPreferences preferences = getSharedPreferences(context);
        String tracks = preferences.getString(isPlayerList ? "old-players" : "fas-tracks", null);

        if(tracks == null) {
            return new JSONArray();
        }

        JSONObject object = new JSONObject(tracks);

        return object.getJSONArray(isPlayerList ? "players" : "tracks");
    }

    public static void setCustomTitle(Context context, boolean isPlayerList) throws JSONException {
        TextView tv;
        String message;
        boolean empty = getJsonArray(context, isPlayerList).length() == 0;
        if(isPlayerList) {
            tv = ((MainActivity) context).fragment_setup.tv_choose_from_previous_games_players;
            message = empty ?  context.getString(R.string.choose_from_previous_games_empty) : context.getString(R.string.choose_from_previous_games);
        } else {
            tv = ((MainActivity) context).fragment_setup.tv_title_custom_tracks;
            message = empty ? context.getString(R.string.no_custom_tracks_title) : context.getString(R.string.custom_tracks_title);
        }
        tv.setText(message);
    }

    public static void removeItemWithSnackbar(final int position, final Context context, RecyclerView recyclerView, final boolean isPlayerList) {
        try {
            final JSONObject removed = isPlayerList ? removePlayerList(position, context) : removeFascistTrack(position, context);

            Snackbar snackbar = Snackbar.make(recyclerView, context.getString(R.string.snackbar_track_removed_message), BaseTransientBottomBar.LENGTH_LONG);

            snackbar.setAction(context.getString(R.string.undo), new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        RecyclerView.Adapter recyclerViewAdapter = isPlayerList ? RecyclerViewManager.getOldPlayerListRecyclerViewAdapter() : RecyclerViewManager.getCustomTracksRecyclerViewAdapter();
                        JSONArray jsonArray = getJsonArray(context, isPlayerList);

                        addJSONObjectToArray(removed, jsonArray, position);

                        if(isPlayerList) {
                            writePastPlayerLists(jsonArray, context);
                        } else {
                            writeFascistTracks(jsonArray, context);
                        }
                        setCustomTitle(context, isPlayerList);

                        recyclerViewAdapter.notifyItemInserted(position);

                    } catch (JSONException e) {
                        ExceptionHandler.showErrorSnackbar(e, "SharedPreferencesManager.removeItemWithSnackbar() (Snackbar action)");
                    }
                }
            }).show();
        } catch (JSONException e) {
            ExceptionHandler.showErrorSnackbar(e, "SharedPreferencesManager.removeItemWithSnackbar()");
        }
    }


}
