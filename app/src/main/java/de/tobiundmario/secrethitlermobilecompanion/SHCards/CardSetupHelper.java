package de.tobiundmario.secrethitlermobilecompanion.SHCards;

import android.content.Context;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;

import java.util.ArrayList;
import java.util.List;

import de.tobiundmario.secrethitlermobilecompanion.R;
import de.tobiundmario.secrethitlermobilecompanion.SHClasses.Claim;
import de.tobiundmario.secrethitlermobilecompanion.SHClasses.GameManager.GameManager;
import de.tobiundmario.secrethitlermobilecompanion.SHClasses.GameManager.PlayerListManager;

public class CardSetupHelper {
    public static void lockPresidentSpinner(String presidentName, Spinner spinner) {
        int position = PlayerListManager.getPlayerPosition(presidentName);
        spinner.setSelection(position);
        if(!GameManager.gameTrack.isManualMode()) spinner.setEnabled(false);
    }

    public static ArrayAdapter<String> getPlayerNameAdapter(final Context context) {
        return new ArrayAdapter<String>(context,
                android.R.layout.simple_spinner_item, PlayerListManager.getAlivePlayerList()) {

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

    public static ArrayAdapter<String> getClaimAdapter(final Context context, List<String> data) {
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
        ArrayList<String> playerList = PlayerListManager.getAlivePlayerList();
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
