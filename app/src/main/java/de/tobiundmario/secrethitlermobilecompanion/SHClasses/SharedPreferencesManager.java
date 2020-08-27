package de.tobiundmario.secrethitlermobilecompanion.SHClasses;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import de.tobiundmario.secrethitlermobilecompanion.MainActivity;
import de.tobiundmario.secrethitlermobilecompanion.R;
import de.tobiundmario.secrethitlermobilecompanion.RecyclerViewAdapters.CustomTracksRecyclerViewAdapter;
import de.tobiundmario.secrethitlermobilecompanion.RecyclerViewAdapters.OldPlayerListRecyclerViewAdapter;

public class SharedPreferencesManager {

    private static OldPlayerListRecyclerViewAdapter oldPlayerListRecyclerViewAdapter;
    private static CustomTracksRecyclerViewAdapter customTracksRecyclerViewAdapter;

    private static boolean playerListsTheSame(JSONObject one, JSONObject two) throws JSONException {
        int oneLength = (one.has("name")) ? one.length() - 1 : one.length();
        int twoLength = (two.has("name")) ? two.length() - 1 : two.length();

        for(int i = 0; i < oneLength; i++) {
            String name = (String) one.get("" + i);
            for (int j = 0; j < twoLength; j++) {
                if(two.get("" + j).equals(name)) break;

                if(j == twoLength - 1) return false; //We just checked the last value and the name is not there, they cannot be the same
            }
        }
        return true;
    }

    private static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences("past-values", Context.MODE_PRIVATE);
    }

    private static JSONArray addJSONObjectToArray(JSONObject jsonObject, JSONArray jsonArray, int position) throws JSONException {
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
    public static JSONArray getPastPlayerLists(Context context) throws JSONException {
        SharedPreferences preferences = getSharedPreferences(context);
        String pastPlayers = preferences.getString("old-players", null);

        if(pastPlayers == null) {
            return new JSONArray();
        }

        JSONObject object = new JSONObject(pastPlayers);

        return object.getJSONArray("players");
    }

    public static void writeCurrentPlayerListIfNew(Context context) throws JSONException {
        JSONObject playerListAsJSON = playerListtoJSON();

        if(playerListAlreadyPresent(playerListAsJSON, context)) return;

        JSONArray array = getPastPlayerLists(context);
        array.put(playerListAsJSON);
        writePastPlayerLists(array, context);
    }

    private static void writePastPlayerLists(JSONArray array, Context context) throws JSONException {
        SharedPreferences preferences = getSharedPreferences(context);

        JSONObject object = new JSONObject();
        object.put("players", array);

        preferences.edit().putString("old-players", object.toString()).apply();

        if(oldPlayerListRecyclerViewAdapter != null) {
            oldPlayerListRecyclerViewAdapter.oldPlayers = array;
            oldPlayerListRecyclerViewAdapter.notifyDataSetChanged();
        }
    }

    private static boolean playerListAlreadyPresent(JSONObject playerJSON, Context context) throws JSONException {
        JSONArray array = getPastPlayerLists(context);

        for(int i = 0; i < array.length(); i++) {
            JSONObject object = array.getJSONObject(i);

            int oneLength = (playerJSON.has("name")) ? playerJSON.length() - 1 : playerJSON.length();
            int twoLength = (object.has("name")) ? object.length() - 1 : object.length();
            if(oneLength != twoLength) continue; //They cannot be the same, skipping

            if(playerListsTheSame(playerJSON, object)) return true;
        }
        return false; //No objects match
    }

    private static JSONObject playerListtoJSON() throws JSONException {
        ArrayList<String> playerList = PlayerList.getPlayerList();

        JSONObject object = new JSONObject();
        for(int i = 0; i < playerList.size(); i++) {
            object.put("" + i, playerList.get(i));
        }
        return object;
    }

    public static void setPlayerListName(String name, int position, Context context) throws JSONException {
        JSONArray newArray = getPastPlayerLists(context);
        JSONObject object = newArray.getJSONObject(position);

        if(!name.matches("")) object.put("name", name);
        else if(object.has("name")) object.remove("name");
        writePastPlayerLists(newArray, context);
    }


    public static void setCorrectPlayerListExplanationText(TextView tv, Context context) throws JSONException {
        if(getPastPlayerLists(context).length() == 0) {
            tv.setText(context.getString(R.string.choose_from_previous_games_empty));
        } else {
            tv.setText(context.getString(R.string.choose_from_previous_games));
        }
    }

    public static JSONObject removePlayerList(int position, Context context) throws JSONException {
        JSONArray array = getPastPlayerLists(context);
        JSONObject removed = array.getJSONObject(position);
        array.remove(position);
        writePastPlayerLists(array, context);
        oldPlayerListRecyclerViewAdapter.notifyItemRemoved(position);

        setCorrectPlayerListExplanationText(((MainActivity) context).fragment_setup.tv_choose_from_previous_games_players, context);

        return removed;
    }

    public static void setupOldPlayerListRecyclerView(final RecyclerView recyclerView, final Context context) {
        try {
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            oldPlayerListRecyclerViewAdapter = new OldPlayerListRecyclerViewAdapter(getPastPlayerLists(context), context);
            recyclerView.setAdapter(oldPlayerListRecyclerViewAdapter);

            ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

                @Override
                public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                    return false;
                }

                @Override
                public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                    final int position = viewHolder.getAdapterPosition();
                    try {
                        final JSONObject removed = removePlayerList(position, context);

                        Snackbar snackbar = Snackbar.make(recyclerView, context.getString(R.string.snackbar_playerList_removed_message), BaseTransientBottomBar.LENGTH_LONG);

                        snackbar.setAction(context.getString(R.string.undo), new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                try {
                                    JSONArray playerListsArray = getPastPlayerLists(context);

                                    addJSONObjectToArray(removed, playerListsArray, position);

                                    writePastPlayerLists(playerListsArray, context);
                                    oldPlayerListRecyclerViewAdapter.notifyItemInserted(position);

                                    setCorrectPlayerListExplanationText(((MainActivity) context).fragment_setup.tv_choose_from_previous_games_players, context);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }).show();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


                }
            };
            new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recyclerView);

            setCorrectPlayerListExplanationText(((MainActivity) context).fragment_setup.tv_choose_from_previous_games_players, context);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    //FascistTrack related

    public static JSONArray getFascistTracks(Context context) throws JSONException {
        SharedPreferences preferences = getSharedPreferences(context);
        String tracks = preferences.getString("fas-tracks", null);

        if(tracks == null) {
            return new JSONArray();
        }

        JSONObject object = new JSONObject(tracks);

        return object.getJSONArray("tracks");
    }

    public static void writeFascistTrack(FascistTrack fascistTrack, Context context) throws JSONException {
        JSONObject trackAsJSON = JSONManager.writeFascistTrackToJSON(fascistTrack);

        JSONArray array = getFascistTracks(context);
        array.put(trackAsJSON);
        writeFascistTracks(array, context);
    }

    private static void writeFascistTracks(JSONArray array, Context context) throws JSONException {
        SharedPreferences preferences = getSharedPreferences(context);

        JSONObject object = new JSONObject();
        object.put("tracks", array);

        preferences.edit().putString("fas-tracks", object.toString()).apply();

        if(customTracksRecyclerViewAdapter != null) {
            customTracksRecyclerViewAdapter.tracks = array;
            customTracksRecyclerViewAdapter.notifyDataSetChanged();
            setCustomTracksTitle( ((MainActivity) context).fragment_setup.tv_title_custom_tracks, context);
        }
    }

    public static JSONObject removeFascistTrack(int position, Context context) throws JSONException {
        JSONArray array = getFascistTracks(context);
        JSONObject removed = array.getJSONObject(position);
        array.remove(position);
        writeFascistTracks(array, context);
        customTracksRecyclerViewAdapter.notifyItemRemoved(position);

        setCustomTracksTitle( ((MainActivity) context).fragment_setup.tv_title_custom_tracks, context);

        return removed;
    }

    public static void setupCustomTracksRecyclerView(final RecyclerView recyclerView, final Context context) {
        try {
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            customTracksRecyclerViewAdapter = new CustomTracksRecyclerViewAdapter(getFascistTracks(context), context);
            recyclerView.setAdapter(customTracksRecyclerViewAdapter);

            ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

                @Override
                public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                    return false;
                }

                @Override
                public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                    final int position = viewHolder.getAdapterPosition();
                    try {
                        final JSONObject removed = removeFascistTrack(position, context);

                        Snackbar snackbar = Snackbar.make(recyclerView, context.getString(R.string.snackbar_track_removed_message), BaseTransientBottomBar.LENGTH_LONG);

                        snackbar.setAction(context.getString(R.string.undo), new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                try {
                                    JSONArray tracksArray = getFascistTracks(context);

                                    addJSONObjectToArray(removed, tracksArray, position);

                                    writeFascistTracks(tracksArray, context);
                                    customTracksRecyclerViewAdapter.notifyItemInserted(position);

                                    setCustomTracksTitle( ((MainActivity) context).fragment_setup.tv_title_custom_tracks, context);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }).show();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


                }
            };
            new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recyclerView);

            setCustomTracksTitle( ((MainActivity) context).fragment_setup.tv_title_custom_tracks, context);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static void setCustomTracksTitle(TextView tv, Context context) throws JSONException {
        if(getFascistTracks(context).length() == 0) {
            tv.setText(context.getString(R.string.no_custom_tracks_title));
        } else {
            tv.setText(context.getString(R.string.custom_tracks_title));
        }
    }


}
