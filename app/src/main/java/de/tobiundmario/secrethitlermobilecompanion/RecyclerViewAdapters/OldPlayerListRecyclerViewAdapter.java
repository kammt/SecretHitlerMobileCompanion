package de.tobiundmario.secrethitlermobilecompanion.RecyclerViewAdapters;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import de.tobiundmario.secrethitlermobilecompanion.R;
import de.tobiundmario.secrethitlermobilecompanion.SHCards.CardDialog;
import de.tobiundmario.secrethitlermobilecompanion.SHClasses.PlayerList;
import de.tobiundmario.secrethitlermobilecompanion.SHClasses.SharedPreferencesManager;

public class OldPlayerListRecyclerViewAdapter extends RecyclerView.Adapter<OldPlayerListRecyclerViewAdapter.OldPlayerListViewHolder> {

    public JSONArray oldPlayers;
    Context context;

    public OldPlayerListRecyclerViewAdapter(JSONArray oldPlayers, Context context){
        this.oldPlayers = oldPlayers;
        this.context = context;
    }

    public static class OldPlayerListViewHolder extends RecyclerView.ViewHolder {
        CardView cv;
        ArrayList<String> players;

        OldPlayerListViewHolder(View itemView) {
            super(itemView);
            cv = (CardView) itemView;
        }
    }

    @Override
    public int getItemCount() {
        return oldPlayers.length();
    }

    @Override
    public OldPlayerListViewHolder onCreateViewHolder(ViewGroup viewGroup, int type) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_old_player_list, viewGroup, false);

        OldPlayerListViewHolder cardViewHolder = new OldPlayerListViewHolder(v);
        return cardViewHolder;
    }

    @Override
    public void onBindViewHolder(OldPlayerListViewHolder oldPlayerListViewHolder, final int pos) {
        try {
            JSONObject object = oldPlayers.getJSONObject(pos);
            final ArrayList<String> players = new ArrayList<>();
            StringBuilder stringBuilder = new StringBuilder();

            int listLength;
            String groupName;
            if(object.has("name")) {
                groupName = object.getString("name");
                listLength = object.length() - 1;
            } else {
                groupName = null;
                listLength = object.length();
            }

            for (int j = 0; j < listLength; j++) {
                String playerName = object.getString("" + j);
                players.add(playerName);

                stringBuilder.append(playerName);
                if(j != listLength - 1) stringBuilder.append(", ");
            }
            oldPlayerListViewHolder.players = players;
            String playerListAsString = stringBuilder.toString();

            CardView cardView = oldPlayerListViewHolder.cv;
            TextView tv_rename = cardView.findViewById(R.id.tv_rename_player_list);
            TextView tv_playerNames = cardView.findViewById(R.id.tv_playerNames);
            Button btn_use = cardView.findViewById(R.id.btn_use);

            btn_use.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PlayerList.setPlayerList(players);
                }
            });

            if(groupName != null)tv_rename.setText(groupName);
            else tv_rename.setText(context.getString(R.string.unnamed_player_list));

            tv_rename.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CardDialog.showInputDialog(context, context.getString(R.string.input_group_name), context.getString(R.string.hint_player_List_name), context.getString(R.string.btn_ok), new CardDialog.InputDialogSubmittedListener() {
                        @Override
                        public void onInputDialogSubmitted(EditText inputField, Dialog rootDialog) {
                            String groupName = inputField.getText().toString();

                            try {
                                SharedPreferencesManager.setPlayerListName(groupName, pos, context);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            rootDialog.dismiss();
                        }
                    }, context.getString(R.string.dialog_mismatching_claims_btn_cancel), null);
                }
            });

            tv_playerNames.setText(playerListAsString);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}

