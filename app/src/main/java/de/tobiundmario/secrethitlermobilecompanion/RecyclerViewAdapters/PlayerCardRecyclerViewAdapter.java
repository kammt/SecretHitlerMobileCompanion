package de.tobiundmario.secrethitlermobilecompanion.RecyclerViewAdapters;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import de.tobiundmario.secrethitlermobilecompanion.R;
import de.tobiundmario.secrethitlermobilecompanion.SHCards.CardDialog;
import de.tobiundmario.secrethitlermobilecompanion.SHClasses.GameManager.GameManager;
import de.tobiundmario.secrethitlermobilecompanion.SHClasses.GameManager.PlayerListManager;

public class PlayerCardRecyclerViewAdapter extends RecyclerView.Adapter<DimmableViewHolder> {

    ArrayList<String> players;
    Context context;
    private ArrayList<String> hiddenPlayers = new ArrayList<>();

    private static int ADD_BUTTON = 2;
    private static int ADD_BUTTON_POSITION_ONE = 3;
    private static int NORMAL = 1;
    private static int FIRST_CARD = 0;

    public PlayerCardRecyclerViewAdapter(ArrayList<String> players, Context context) {
        this.players = players;
        this.context = context;
    }

    public ArrayList<String> getHiddenPlayers() {
        return hiddenPlayers;
    }

    @Override
    public int getItemCount() {
        if(GameManager.isGameStarted()) return players.size();
        else return players.size() + 1;
    }

    @Override
    public DimmableViewHolder onCreateViewHolder(ViewGroup viewGroup, int type) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_player_list_single_player, viewGroup, false);

        if(type == ADD_BUTTON || type == ADD_BUTTON_POSITION_ONE) {
            v.setClickable(true);
            v.setFocusable(true);
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CardDialog.showInputDialog(context, context.getString(R.string.title_input_player_name), context.getString(R.string.hint_player_name), context.getString(R.string.btn_ok), new CardDialog.InputDialogSubmittedListener() {
                        @Override
                        public void onInputDialogSubmitted(EditText inputField, Dialog rootDialog) {
                            String playerName = inputField.getText().toString();
                            if(PlayerListManager.playerAlreadyExists(playerName)) {
                                inputField.setError(context.getString(R.string.error_player_already_exists));
                                return;
                            }
                            if(playerName.matches("")) {
                                rootDialog.dismiss();
                                return;
                            }
                            PlayerListManager.addPlayer(playerName);
                            rootDialog.dismiss();
                        }
                    }, context.getString(R.string.dialog_mismatching_claims_btn_cancel), null);
                }
            });

            TextView tv_plus = v.findViewById(R.id.tv_addplayer);
            tv_plus.setVisibility(View.VISIBLE);

            ImageView iv_symbol = v.findViewById(R.id.img_secretRole);
            iv_symbol.setAlpha((float) 0.5);

            TextView tvPlayerName = v.findViewById(R.id.tv_playerName);
            tvPlayerName.setText(context.getString(R.string.new_player));
        }

        if(type == FIRST_CARD || type == ADD_BUTTON_POSITION_ONE) {
            Resources r = context.getResources();
            int px = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    16,
                    r.getDisplayMetrics()
            );

            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) v.getLayoutParams();
            params.leftMargin = px;
            v.setLayoutParams(params);
        }

        return new DimmableViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final DimmableViewHolder cardViewHolder, int i) {
        if(cardViewHolder.getItemViewType() == ADD_BUTTON || cardViewHolder.getItemViewType() == ADD_BUTTON_POSITION_ONE) return;

        CardView cardView = cardViewHolder.cv;

        final String player = players.get(i);
        TextView tvPlayerName = cardView.findViewById(R.id.tv_playerName);
        tvPlayerName.setText(player);

        if(hiddenPlayers.contains(player)) {
            cardViewHolder.alpha = 0.5f;
            cardView.setAlpha(0.5f);
        }

        cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(GameManager.isGameStarted()) {
                    CardView cv = (CardView) v;

                    if (cv.getAlpha() == 1.0) { //if it is unselected, select it
                        cv.setAlpha((float) 0.5);
                        cardViewHolder.alpha = 0.5f;
                        hiddenPlayers.add(player); //add it to the list of hidden players
                    } else { //if it is selected (Alpha is smaller than 1), remove it from the list and reset alpha
                        cv.setAlpha(1);
                        cardViewHolder.alpha = 1f;
                        hiddenPlayers.remove(player);
                    }
                    GameManager.blurEventsInvolvingHiddenPlayers(hiddenPlayers); //Tell GameLog to update the list of which cards to blur
                }
            }
        });
    }


    @Override
    public int getItemViewType(int position) {
        if(position >= players.size()) return (position == 0) ? ADD_BUTTON_POSITION_ONE : ADD_BUTTON;
        else return (position == 0) ? FIRST_CARD : NORMAL;
    }

    @Override
    public void onViewAttachedToWindow(@NonNull DimmableViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        if(holder.getItemViewType() == ADD_BUTTON || !GameManager.isGameStarted()) return;

        int position = holder.getLayoutPosition();
        int claim = PlayerListManager.getMembershipClaims().get(position);

        CardView cv = holder.cv;
        if(PlayerListManager.isDead(position)) {
            PlayerListManager.setDeadSymbol(cv);
            return;
        }
        PlayerListManager.setClaimImage(cv, claim);
    }
}

