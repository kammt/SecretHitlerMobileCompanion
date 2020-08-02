package de.tobias.secrethitlermobilecompanion;

import android.content.Context;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;

import java.util.ArrayList;

import de.tobias.secrethitlermobilecompanion.SHClasses.Claim;
import de.tobias.secrethitlermobilecompanion.SHClasses.PlayerList;

public class CardSetupHelper {
    public static ArrayAdapter<String> getPlayerNameAdapter(final Context context) {
        return new ArrayAdapter<String>(context,
                android.R.layout.simple_spinner_item, PlayerList.getAlivePlayerList()) {

            public View getView(int position, View convertView, ViewGroup parent) {
                View v = super.getView(position, convertView, parent);

                Typeface externalFont = ResourcesCompat.getFont(context, R.font.comfortaa_light);
                ((TextView) v).setTypeface(externalFont);

                return v;
            }


            public View getDropDownView(int position,  View convertView,  ViewGroup parent) {
                View v =super.getDropDownView(position, convertView, parent);

                Typeface externalFont = ResourcesCompat.getFont(context, R.font.comfortaa_light);
                ((TextView) v).setTypeface(externalFont);

                return v;
            }
        };
    }

    public static ArrayAdapter<String> getClaimAdapter(final Context context, ArrayList<String> data) {
        return new ArrayAdapter<String>(context,
                android.R.layout.simple_spinner_item, data) {

            public View getView(int position, View convertView, ViewGroup parent) {
                View v = super.getView(position, convertView, parent);

                Typeface externalFont = ResourcesCompat.getFont(context, R.font.comfortaa_light);
                TextView tv = (TextView) v;
                tv.setTypeface(externalFont);
                tv.setText(Claim.colorClaim(tv.getText().toString()), TextView.BufferType.SPANNABLE);

                return v;
            }


            public View getDropDownView(int position,  View convertView,  ViewGroup parent) {
                View v =super.getDropDownView(position, convertView, parent);

                Typeface externalFont = ResourcesCompat.getFont(context, R.font.comfortaa_light);
                TextView tv = (TextView) v;
                tv.setTypeface(externalFont);
                tv.setText(Claim.colorClaim(tv.getText().toString()), TextView.BufferType.SPANNABLE);

                return v;
            }
        };
    }

    public static ArrayAdapter<String> getPlayerNameAdapterWithDeadPlayer(final Context context, String deadPlayer) {
        ArrayList<String> playerList = PlayerList.getAlivePlayerList();
        playerList.add(deadPlayer);
        return new ArrayAdapter<String>(context,
                android.R.layout.simple_spinner_item, playerList) {

            public View getView(int position, View convertView, ViewGroup parent) {
                View v = super.getView(position, convertView, parent);

                Typeface externalFont = ResourcesCompat.getFont(context, R.font.comfortaa_light);
                ((TextView) v).setTypeface(externalFont);

                return v;
            }


            public View getDropDownView(int position,  View convertView,  ViewGroup parent) {
                View v =super.getDropDownView(position, convertView, parent);

                Typeface externalFont = ResourcesCompat.getFont(context, R.font.comfortaa_light);
                ((TextView) v).setTypeface(externalFont);

                return v;
            }
        };
    }
}
