package de.tobias.secrethitlermobilecompanion;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import de.tobias.secrethitlermobilecompanion.SHClasses.DeckShuffledEvent;
import de.tobias.secrethitlermobilecompanion.SHClasses.GameEvent;
import de.tobias.secrethitlermobilecompanion.SHClasses.LegislativeSession;

public class CardRecyclerViewAdapter extends RecyclerView.Adapter<CardRecyclerViewAdapter.CardViewHolder> {

    List<GameEvent> events;
    private static final int EXECUTIVE_ACTION = 1;
    private static final int LEGISLATIVE_SESSION = 0;
    private static final int DECK_SHUFFLED = 2;

    public CardRecyclerViewAdapter(List<GameEvent> events){
        this.events = events;
    }

    public static class CardViewHolder extends RecyclerView.ViewHolder {
        CardView cv;

        CardViewHolder(View itemView) {
            super(itemView);
            cv = (CardView)itemView.findViewById(R.id.cardView);
        }
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    @Override
    public CardViewHolder onCreateViewHolder(ViewGroup viewGroup, int type) {
        //The card can use two layouts - Legislative session or Executive Action. Thus we check to which class the Event belongs
        View v;
        if(type == LEGISLATIVE_SESSION) {
            //It is a legislative session - then we inflate the correct layout
            v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_legislative_session, viewGroup, false);
        } else if (type == EXECUTIVE_ACTION) {
            v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_executive_action, viewGroup, false);
        } else {
            v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_deck_shuffled, viewGroup, false);
        }
        CardViewHolder cardViewHolder = new CardViewHolder(v);
        return cardViewHolder;
    }

    @Override
    public void onBindViewHolder(CardViewHolder cardViewHolder, int i) {
        GameEvent event = events.get(i);
        event.setupCard(cardViewHolder.cv);
    }


    @Override
    public int getItemViewType(int position) {
        GameEvent event = events.get(position);
        //The card can use two layouts - Legislative session or Executive Action. Thus we check to which class the Event belongs
        View v;
        if(event.getClass().isAssignableFrom(LegislativeSession.class)) {
            //It is a legislative session - then we inflate the correct layout
            return LEGISLATIVE_SESSION;
        } else if(event.getClass() == DeckShuffledEvent.class) {
            return DECK_SHUFFLED;
        } else return EXECUTIVE_ACTION;
    }


}

