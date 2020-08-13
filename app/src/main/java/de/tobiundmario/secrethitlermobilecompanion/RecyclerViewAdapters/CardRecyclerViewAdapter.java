package de.tobiundmario.secrethitlermobilecompanion.RecyclerViewAdapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import de.tobiundmario.secrethitlermobilecompanion.R;
import de.tobiundmario.secrethitlermobilecompanion.SHCards.GameEndCard;
import de.tobiundmario.secrethitlermobilecompanion.SHCards.GameSetupCard;
import de.tobiundmario.secrethitlermobilecompanion.SHClasses.GameLog;
import de.tobiundmario.secrethitlermobilecompanion.SHEvents.DeckShuffledEvent;
import de.tobiundmario.secrethitlermobilecompanion.SHEvents.GameEvent;
import de.tobiundmario.secrethitlermobilecompanion.SHEvents.LegislativeSession;
import de.tobiundmario.secrethitlermobilecompanion.SHEvents.LoyaltyInvestigationEvent;
import de.tobiundmario.secrethitlermobilecompanion.SHEvents.PolicyPeekEvent;

public class CardRecyclerViewAdapter extends RecyclerView.Adapter<CardRecyclerViewAdapter.CardViewHolder> {

    List<GameEvent> events;
    private static final int EXECUTIVE_ACTION = 1;
    private static final int LEGISLATIVE_SESSION = 0;
    private static final int DECK_SHUFFLED = 2;

    private static final int LEGISLATIVE_SESSION_SETUP = 3;
    private static final int LOYALTY_INVESTIGATION_SETUP = 4;
    private static final int EXECUTION_SETUP = 5;
    private static final int DECK_SHUFFLED_SETUP = 6;
    private static final int POLICY_PEEK_SETUP = 7;

    private static final int GAME_SETUP = 8;
    private static final int GAME_END = 9;


    public CardRecyclerViewAdapter(List<GameEvent> events){
        this.events = events;
    }

    public static class CardViewHolder extends RecyclerView.ViewHolder {
        CardView cv;

        CardViewHolder(View itemView) {
            super(itemView);
            cv = itemView.findViewById(R.id.cardView);
        }
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    @NonNull
    @Override
    public CardViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int type) {
        //The card can use two layouts - Legislative session or Executive Action. Thus we check to which class the Event belongs
        View v = null;
        if(type == LEGISLATIVE_SESSION) {
            v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_legislative_session, viewGroup, false);
        } else if (type == LEGISLATIVE_SESSION_SETUP) {
            v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.setup_card_legislative_session, viewGroup, false);
        } else if (type == EXECUTIVE_ACTION) {
            v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_executive_action, viewGroup, false);
        } else if (type == DECK_SHUFFLED){
            v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_deck_shuffled, viewGroup, false);
        } else if (type == LOYALTY_INVESTIGATION_SETUP) {
            v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.setup_card_loyalty_investigation, viewGroup, false);
        } else if (type == EXECUTION_SETUP) {
            v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.setup_card_execution, viewGroup, false);
        } else if (type == DECK_SHUFFLED_SETUP) {
            v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.setup_card_deck_shuffled, viewGroup, false);
        } else if (type == POLICY_PEEK_SETUP) {
            v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.setup_card_policy_peek, viewGroup, false);
        } else if (type == GAME_SETUP) {
            v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.setup_card_game_settings, viewGroup, false);
        } else if (type == GAME_END) {
            v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.setup_card_game_end, viewGroup, false);
        }
        return new CardViewHolder(v);
    }

    @Override
    public void onBindViewHolder(CardViewHolder cardViewHolder, final int i) {
        final CardView cv = cardViewHolder.cv;
        final GameEvent event = events.get(i);
        if(event.isSetup) {
            event.setupSetupCard(cv);
            if(event.isEditing) event.setCurrentValues(cv);

            if(event.getClass() != GameEndCard.class && event.getClass() != GameSetupCard.class) { //Checking, since both those cards are marked as setup but do not have cancel buttons
                //The Cancel button is visible on every card, hence we initialise it here to save code
                ImageView iv_cancel = cv.findViewById(R.id.img_cancel);
                iv_cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(!event.isEditing) GameLog.remove(event);
                        else {
                            event.isEditing = false;
                            event.isSetup = false;
                            GameLog.getCardListAdapter().notifyItemChanged(i);
                        }
                    }
                });
            }

        } else {
            event.setupCard(cv);
            if(event.permitEditing) {
                cv.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        event.isEditing = true;
                        event.isSetup = true;
                        notifyItemChanged(events.indexOf(event));
                        return false;
                    }
                });
            }
        }
    }


    @Override
    public int getItemViewType(int position) {
        GameEvent event = events.get(position);
        //The card can use three layouts - Legislative session, Deck shuffled or Executive Action. Thus we check to which class the Event belongs
        //Additionally, there is the possibility that the card is a setup card - requiring a different layout
        View v;
        if(event.getClass().isAssignableFrom(LegislativeSession.class)) {

            //It is a legislative session - then we inflate the correct layout
            if(event.isSetup) return LEGISLATIVE_SESSION_SETUP;
            else return LEGISLATIVE_SESSION;

        } else if(event.getClass() == DeckShuffledEvent.class) {

            if(event.isSetup) return DECK_SHUFFLED_SETUP;
            else return DECK_SHUFFLED;

        } else if(event.isSetup) {

            if(event.getClass() == LoyaltyInvestigationEvent.class) return LOYALTY_INVESTIGATION_SETUP;
            else if(event.getClass() == PolicyPeekEvent.class) return POLICY_PEEK_SETUP;
            else if (event.getClass() == GameSetupCard.class) return GAME_SETUP;
            else if (event.getClass() == GameEndCard.class) return GAME_END;
            else return EXECUTION_SETUP;

        } else return EXECUTIVE_ACTION;
    }

    @Override
    public void onViewAttachedToWindow(@NonNull CardViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        //This function is called when a prior removed View is re-added by Android. Now, we check if it has to be blurred, beginning by getting its position
        int position = holder.getLayoutPosition();

        CardView cv = holder.cv;
        boolean toBeBlurred = GameLog.hiddenEventIndexes.contains(position);

        if(toBeBlurred) cv.setAlpha((float) 0.5);
        else if(cv.getAlpha() < 1) cv.setAlpha(1); //Views that are re-added have the same opacity as before. Because of that, we also check if it has to be un-blurred
    }
}

