package de.tobiundmario.secrethitlermobilecompanion.RecyclerViewAdapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import de.tobiundmario.secrethitlermobilecompanion.R;
import de.tobiundmario.secrethitlermobilecompanion.SHCards.GameEndCard;
import de.tobiundmario.secrethitlermobilecompanion.SHClasses.GameLog;
import de.tobiundmario.secrethitlermobilecompanion.SHEvents.DeckShuffledEvent;
import de.tobiundmario.secrethitlermobilecompanion.SHEvents.GameEvent;
import de.tobiundmario.secrethitlermobilecompanion.SHEvents.LegislativeSession;
import de.tobiundmario.secrethitlermobilecompanion.SHEvents.LoyaltyInvestigationEvent;
import de.tobiundmario.secrethitlermobilecompanion.SHEvents.PolicyPeekEvent;
import de.tobiundmario.secrethitlermobilecompanion.SHEvents.TopPolicyPlayedEvent;

public class EventCardRecyclerViewAdapter extends RecyclerView.Adapter<DimmableViewHolder> {

    List<GameEvent> events;
    Context c;
    private static final int EXECUTIVE_ACTION = 1;
    private static final int LEGISLATIVE_SESSION = 0;
    private static final int DECK_SHUFFLED = 2;
    private static final int TOP_POLICY = 8;

    private static final int LEGISLATIVE_SESSION_SETUP = 3;
    private static final int LOYALTY_INVESTIGATION_SETUP = 4;
    private static final int EXECUTION_SETUP = 5;
    private static final int DECK_SHUFFLED_SETUP = 6;
    private static final int POLICY_PEEK_SETUP = 7;
    private static final int TOP_POLICY_SETUP = 10;

    private static final int GAME_END = 9;


    public EventCardRecyclerViewAdapter(List<GameEvent> events, Context c){
        this.events = events;
        this.c = c;
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    public void blurCardIfNeeded(DimmableViewHolder cardViewHolder, int position) {
        boolean toBeBlurred = GameLog.hiddenEventIndexes.contains(position);
        CardView cv = cardViewHolder.cv;

        if(toBeBlurred) {
            cv.setAlpha(0.5f);
            cardViewHolder.alpha = 0.5f;
        }
        else if(cv.getAlpha() < 1) {
            cv.setAlpha(1f); //Views that are re-added have the same opacity as before. Because of that, we also check if it has to be un-blurred
            cardViewHolder.alpha = 1f;
        }
    }

    @NonNull
    @Override
    public DimmableViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int type) {
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
        } else if(type == TOP_POLICY) {
            v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_top_policy, viewGroup, false);
        } else if(type == TOP_POLICY_SETUP) {
            v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.setup_card_top_policy, viewGroup, false);
        } else if (type == GAME_END) {
            v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.setup_card_game_end, viewGroup, false);
        }
        return new DimmableViewHolder(v);
    }

    @Override
    public void onBindViewHolder(DimmableViewHolder cardViewHolder, final int position) {
        final CardView cv = cardViewHolder.cv;
        final GameEvent event = events.get(position);
        final Button btn_cancel = cv.findViewById(R.id.btn_setup_back);

        if(event.isSetup) {
             //The Cancel button is visible on every card, hence we initialise it here to save code
            btn_cancel.setText(c.getString(R.string.dialog_mismatching_claims_btn_cancel));
            btn_cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!event.isEditing) GameLog.remove(event);
                    else {
                        event.isEditing = false;
                        event.isSetup = false;
                        GameLog.getCardListAdapter().notifyItemChanged(position);
                    }
                }
            });

            if(!GameLog.gameTrack.isManualMode() && !(event instanceof LegislativeSession) && !(event instanceof DeckShuffledEvent) && !(event instanceof GameEndCard) && !event.isEditing) { //If manual mode is disabled, then we don't want to have cancel buttons on automatically generated actions
                btn_cancel.setVisibility(View.GONE);
            } else {
                btn_cancel.setVisibility(View.VISIBLE);
            }

            event.initialiseSetupCard(cv);
            if(event.isEditing) event.setCurrentValues(cv);
        } else {
            event.initialiseCard(cv);
            if(event.permitEditing) {
                cv.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        if(GameLog.editingEnabled) {
                            if (event instanceof LegislativeSession && ((LegislativeSession) event).getSessionNumber() != GameLog.legSessionNo - 1) {
                                Toast.makeText(c, c.getString(R.string.toast_message_edit_blocked), Toast.LENGTH_LONG).show();
                            } else {
                                event.isEditing = true;
                                event.isSetup = true;
                                notifyItemChanged(position);
                            }
                        }
                        return false;
                    }
                });
            }

            blurCardIfNeeded(cardViewHolder, position);
        }
    }


    @Override
    public int getItemViewType(int position) {
        GameEvent event = events.get(position);
        //The card can use three layouts - Legislative session, Deck shuffled or Executive Action. Thus we check to which class the Event belongs
        //Additionally, there is the possibility that the card is a setup card - requiring a different layout
        if(event instanceof LegislativeSession) {

            //It is a legislative session - then we inflate the correct layout
            if(event.isSetup) return LEGISLATIVE_SESSION_SETUP;
            else return LEGISLATIVE_SESSION;

        } else if(event instanceof DeckShuffledEvent) {

            if(event.isSetup) return DECK_SHUFFLED_SETUP;
            else return DECK_SHUFFLED;

        } else if(event.isSetup) {

            if(event instanceof LoyaltyInvestigationEvent) return LOYALTY_INVESTIGATION_SETUP;
            else if(event instanceof PolicyPeekEvent) return POLICY_PEEK_SETUP;
            else if (event instanceof TopPolicyPlayedEvent) return TOP_POLICY_SETUP;
            else if (event instanceof GameEndCard) return GAME_END;
            else return EXECUTION_SETUP;

        } else if (event instanceof TopPolicyPlayedEvent) {
            return TOP_POLICY;
        } else return EXECUTIVE_ACTION;
    }

    @Override
    public void onViewAttachedToWindow(@NonNull DimmableViewHolder holder) {
        //This function is called when a prior removed View is re-added by Android. Now, we check if it has to be blurred, beginning by getting its position
        int position = holder.getLayoutPosition();

        blurCardIfNeeded(holder, position);
    }
}

