package de.tobias.secrethitlermobilecompanion;

import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import de.tobias.secrethitlermobilecompanion.SHClasses.GameLog;
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
        //We have to differentiate between the first player and other players, as the first player need extra margin_left on his card. This has been solved using two layouts
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
                if(GameLog.isGameStarted()) {
                    CardView cv = (CardView) v;

                    if (cv.getAlpha() == 1.0) { //if it is unselected, select it
                        cv.setAlpha((float) 0.5);
                        hiddenPlayers.add(player); //add it to the list of hidden players
                    } else { //if it is selected (Alpha is smaller than 1), remove it from the list and reset alpha
                        cv.setAlpha(1);
                        hiddenPlayers.remove(player);
                    }
                    GameLog.blurEventsInvolvingHiddenPlayers(hiddenPlayers); //Tell GameLog to update the list of which cards to blur
                }
            }
        });

        cardView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if(!GameLog.isGameStarted()) {
                    //In the setup phase, we want to give the user the option to remove a user
                    new AlertDialog.Builder(context)
                            .setTitle(context.getString(R.string.are_you_sure))
                            .setPositiveButton(context.getString(R.string.yes), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    PlayerList.removePlayer(player);
                                }
                            })
                            .setNegativeButton(context.getString(R.string.no), null)
                            .setMessage(context.getString(R.string.delete_player_msg, player))
                            .show();
                }
                return false;
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
        if(PlayerList.isDead(position)) {
            PlayerList.setDeadSymbol(cv);
            return;
        }
        PlayerList.setClaimImage(cv, claim);
    }
}

