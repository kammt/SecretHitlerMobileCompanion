package de.tobias.secrethitlermobilecompanion.SHClasses;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;

import java.util.ArrayList;

import de.tobias.secrethitlermobilecompanion.MainActivity;
import de.tobias.secrethitlermobilecompanion.PlayerRecyclerViewAdapter;
import de.tobias.secrethitlermobilecompanion.R;

public class PlayerList {
    private static ArrayList<String> playerList = new ArrayList<>();
    private static ArrayList<Integer> claimList = new ArrayList<>();
    private static ArrayList<Boolean> isDead = new ArrayList<>();

    private static Context c;
    private static PlayerRecyclerViewAdapter playerRecyclerViewAdapter;
    private static RecyclerView playerListRecyclerView;

    public static void addPlayer(String name) {
        playerList.add(name);
        claimList.add(Claim.NO_CLAIM);
        isDead.add(false);
        playerRecyclerViewAdapter.notifyItemInserted(playerList.size() - 1);
    }

    public static void removePlayer(String player) {
        if(!GameLog.isGameStarted()) {
            int index = playerList.indexOf(player);

            playerList.remove(player);
            claimList.remove(index);
            isDead.remove(index);

            playerRecyclerViewAdapter.notifyItemRemoved(index);
        }
    }

    public static void setClaim(String player, int playerPartyMemberShip) {
        int position = playerList.indexOf(player);
        claimList.set(position, playerPartyMemberShip);
        playerRecyclerViewAdapter.notifyItemChanged(position);
    }

    public static boolean playerAlreadyExists(String name) {
        return playerList.contains(name);
    }

    public static void setClaimImage(CardView cardView, int playerPartyMemberShip) {
        /*
        Receives the Player-card as an input and then adds the claim-symbol to it. Is called by the RecyclerViewAdapter
         */

        Drawable membershipDrawable;
        if(playerPartyMemberShip == Claim.LIBERAL) membershipDrawable = c.getDrawable(R.drawable.membership_liberal);
        else if (playerPartyMemberShip == Claim.FASCIST) membershipDrawable = c.getDrawable(R.drawable.membership_fascist);
        else return; //This function will always be called when a view is re-added. This means that when there is no claim of a player, we abort the function.

        ImageView ivmembership = cardView.findViewById(R.id.img_secretRole);
        ivmembership.setImageDrawable(membershipDrawable);
        ivmembership.setAlpha((float) 0.4);

        TextView tvquestionMark = cardView.findViewById(R.id.tv_questionmark);
        tvquestionMark.setText("?");
        tvquestionMark.setVisibility(View.VISIBLE);
    }

    public static void setDeadSymbol(CardView cardView) {
        /*
        Receives the Player-card as an input and then adds the dead-symbol to it. Is called by the RecyclerViewAdapter
         */
        ImageView ivmembership = cardView.findViewById(R.id.img_secretRole);
        ivmembership.setImageDrawable(c.getDrawable(R.drawable.dead));
        ivmembership.setAlpha((float) 1);

        TextView tvquestionMark = cardView.findViewById(R.id.tv_questionmark);
        tvquestionMark.setVisibility(View.INVISIBLE);
    }

    public static ArrayList<String> getPlayerList() {
        return playerList;
    }

    public static ArrayList<String> getAlivePlayerList() {
        /*
        Returns a list of all alive players. Is used by the Spinners in the setup cards, as no dead players should be selectable.
         */
        ArrayList<String> result = new ArrayList<>();
        for(int i = 0; i < playerList.size(); i++) {
            if(!isDead(i)) result.add(playerList.get(i));
        }
        return result;
    }

    public static ArrayList<Integer> getMembershipClaims() {
        return claimList;
    }

    public static void initialise(RecyclerView playerCardList, Context context) {
        playerList = new ArrayList<>();
        claimList = new ArrayList<>();
        isDead = new ArrayList<>();

        playerRecyclerViewAdapter = new PlayerRecyclerViewAdapter(playerList, context);
        playerCardList.setAdapter(playerRecyclerViewAdapter);

        playerListRecyclerView = playerCardList;
        c = context;
    }

    public static void setAsDead(String playerName, boolean dead) {
        int position = playerList.indexOf(playerName);
        isDead.set(position, dead);
        playerRecyclerViewAdapter.notifyItemChanged(position);

        if(getAlivePlayerCount() == 2) {
            ((MainActivity) c).displayEndGameOptions();
        }
    }

    public static int getAlivePlayerCount() {
        return getAlivePlayerList().size();
    }

    public static boolean isDead(int playerPosition) {
        return isDead.get(playerPosition);
    }

    public static void reset() {
        playerList = new ArrayList<>();
        claimList = new ArrayList<>();
        isDead = new ArrayList<>();
        playerRecyclerViewAdapter.notifyDataSetChanged();
    }

    public static JSONArray getPlayerListJSON() {
        JSONArray arr = new JSONArray();

        for(String player : playerList) {
            arr.put(player);
        }

        return arr;
    }
}
