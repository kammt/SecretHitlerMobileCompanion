package de.tobias.secrethitlermobilecompanion.SHClasses;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;

import de.tobias.secrethitlermobilecompanion.CardRecyclerViewAdapter;
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
    }

    public static void setClaim(String player, int playerPartyMemberShip, Context context) {
        int position = playerList.indexOf(player);
        claimList.add(position, playerPartyMemberShip);

        CardView playerCard = (CardView) playerListRecyclerView.getLayoutManager().findViewByPosition(position);
        if(playerCard != null) {
            setClaimImage(playerCard, playerPartyMemberShip);
        }
    }

    public static void setClaimImage(CardView cardView, int playerPartyMemberShip) {
        Drawable membershipDrawable;
        if(playerPartyMemberShip == Claim.LIBERAL) membershipDrawable = c.getDrawable(R.drawable.membership_liberal);
        else if (playerPartyMemberShip == Claim.FASCIST) membershipDrawable = c.getDrawable(R.drawable.membership_fascist);
        else return;

        ImageView ivmembership = cardView.findViewById(R.id.img_secretRole);
        ivmembership.setImageDrawable(membershipDrawable);
        ivmembership.setAlpha((float) 0.4);

        TextView tvquestionMark = cardView.findViewById(R.id.tv_questionmark);
        tvquestionMark.setText("?");
        tvquestionMark.setVisibility(View.VISIBLE);
    }

    public static void setDeadSymbol(CardView cardView) {
        ImageView ivmembership = cardView.findViewById(R.id.img_secretRole);
        ivmembership.setImageDrawable(c.getDrawable(R.drawable.dead));
        ivmembership.setAlpha((float) 1);

        TextView tvquestionMark = cardView.findViewById(R.id.tv_questionmark);
        tvquestionMark.setVisibility(View.INVISIBLE);
    }

    public static ArrayList<String> getPlayerList() {
        return playerList;
    }
    public static ArrayList<Integer> getMembershipClaims() {
        return claimList;
    }

    public static void setupPlayerList(RecyclerView playerCardList, Context context) {
        playerRecyclerViewAdapter = new PlayerRecyclerViewAdapter(playerList, context);
        playerCardList.setAdapter(playerRecyclerViewAdapter);

        playerListRecyclerView = playerCardList;
        c = context;
    }

    public static void setAsDead(String playerName) {
        int position = playerList.indexOf(playerName);
        isDead.add(position, true);

        CardView playerCard = (CardView) playerListRecyclerView.getLayoutManager().findViewByPosition(position);
        if(playerCard != null) {
            setDeadSymbol(playerCard);
        }
    }

    public static boolean isDead(int playerPosition) {
        return isDead.get(playerPosition);
    }
}
