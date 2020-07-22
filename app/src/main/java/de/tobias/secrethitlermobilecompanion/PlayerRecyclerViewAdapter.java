package de.tobias.secrethitlermobilecompanion;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import de.tobias.secrethitlermobilecompanion.SHClasses.Claim;
import de.tobias.secrethitlermobilecompanion.SHClasses.DeckShuffledEvent;
import de.tobias.secrethitlermobilecompanion.SHClasses.GameEvent;
import de.tobias.secrethitlermobilecompanion.SHClasses.GameLog;
import de.tobias.secrethitlermobilecompanion.SHClasses.LegislativeSession;
import de.tobias.secrethitlermobilecompanion.SHClasses.PlayerList;

public class PlayerRecyclerViewAdapter extends RecyclerView.Adapter<PlayerRecyclerViewAdapter.PlayerCardViewHolder> {

    List<String> players;
    Context context;
    private ArrayList<String> hiddenPlayers = new ArrayList<>();

    public PlayerRecyclerViewAdapter(List<String> players, Context context){
        this.players = players;
        this.context = context;
    }

    public static class PlayerCardViewHolder extends RecyclerView.ViewHolder {
        CardView cv;

        PlayerCardViewHolder(View itemView) {
            super(itemView);
            cv = (CardView)itemView.findViewById(R.id.cardView);
        }
    }

    @Override
    public int getItemCount() {
        return players.size();
    }

    @Override
    public PlayerCardViewHolder onCreateViewHolder(ViewGroup viewGroup, int type) {
        View v;
        if(type == 0) v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_player_list_single_player_first_entry, viewGroup, false);
        else v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_player_list_single_player, viewGroup, false);

        PlayerCardViewHolder cardViewHolder = new PlayerCardViewHolder(v);
        return cardViewHolder;
    }

    @Override
    public void onBindViewHolder(PlayerCardViewHolder cardViewHolder, int i) {
        CardView cardView = cardViewHolder.cv;

        final String player = players.get(i);
        TextView tvPlayerName = cardView.findViewById(R.id.tv_playerName);
        tvPlayerName.setText(player);

        cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CardView cv = (CardView) v;

                if(cv.getAlpha() == 1.0) {
                    cv.setAlpha( (float) 0.5);
                    hiddenPlayers.add(player);
                } else {
                    cv.setAlpha(1);
                    hiddenPlayers.remove(player);
                }
                GameLog.blurEventsInvolvingHiddenPlayers(hiddenPlayers);
            }
        });
    }


    @Override
    public int getItemViewType(int position) {
        return (position == 0) ? 0 : 1;
    }

    @Override
    public void onViewAttachedToWindow(@NonNull PlayerCardViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        int position = holder.getLayoutPosition();
        int claim = PlayerList.getMembershipClaims().get(position);

        CardView cv = holder.cv;
        PlayerList.setClaimImage(cv, claim);
    }
}

