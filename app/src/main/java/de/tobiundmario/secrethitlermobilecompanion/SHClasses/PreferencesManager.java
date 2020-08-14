package de.tobiundmario.secrethitlermobilecompanion.SHClasses;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class PreferencesManager {

    private static boolean JSONObjectsTheSame(JSONObject one, JSONObject two) throws JSONException {
        for(int i = 0; i < one.length(); i++) {
            if(!two.has(one.getString("" + i))) return false;
        }
        return true;
    }

    private static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences("past-values", Context.MODE_PRIVATE);
    }

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

        if(!playerListAlreadyPresent(playerListAsJSON, context)) return;

        JSONArray array = getPastPlayerLists(context);
        array.put(playerListAsJSON);
        writePastPlayerLists(array, context);
    }

    private static void writePastPlayerLists(JSONArray array, Context context) throws JSONException {
        SharedPreferences preferences = getSharedPreferences(context);

        JSONObject object = new JSONObject();
        object.put("players", array);

        preferences.edit().putString("old-players", object.toString()).apply();
    }

    private static boolean playerListAlreadyPresent(JSONObject playerJSON, Context context) throws JSONException {
        JSONArray array = getPastPlayerLists(context);
        for(int i = 0; i < array.length(); i++) {
            JSONObject object = array.getJSONObject(i);

            if(object.length() != playerJSON.length()) continue; //They cannot be the same, skipping
            if(object.equals(playerJSON)) return true; //They are exactly the same

            if(JSONObjectsTheSame(playerJSON, object)) return true;
        }
        return false; //No objects match
    }

    public static JSONObject playerListtoJSON() throws JSONException {
        ArrayList<String> playerList = PlayerList.getPlayerList();

        JSONObject object = new JSONObject();
        for(int i = 0; i < playerList.size(); i++) {
            object.put("" + i, playerList.get(i));
        }
        return object;
    }

}
