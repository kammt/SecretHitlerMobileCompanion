package de.tobiundmario.secrethitlermobilecompanion.SHClasses.GameManager;

import android.content.Context;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONException;

import java.io.IOException;

import de.tobiundmario.secrethitlermobilecompanion.ExceptionHandler;
import de.tobiundmario.secrethitlermobilecompanion.MainActivity;
import de.tobiundmario.secrethitlermobilecompanion.R;
import de.tobiundmario.secrethitlermobilecompanion.RecyclerViewAdapters.CustomTracksRecyclerViewAdapter;
import de.tobiundmario.secrethitlermobilecompanion.RecyclerViewAdapters.EventCardRecyclerViewAdapter;
import de.tobiundmario.secrethitlermobilecompanion.RecyclerViewAdapters.ModifiedDefaultItemAnimator;
import de.tobiundmario.secrethitlermobilecompanion.RecyclerViewAdapters.OldPlayerListRecyclerViewAdapter;
import de.tobiundmario.secrethitlermobilecompanion.SHClasses.EventChange;
import de.tobiundmario.secrethitlermobilecompanion.SHEvents.DeckShuffledEvent;
import de.tobiundmario.secrethitlermobilecompanion.SHEvents.ExecutiveAction;
import de.tobiundmario.secrethitlermobilecompanion.SHEvents.GameEvent;
import de.tobiundmario.secrethitlermobilecompanion.SHEvents.LegislativeSession;
import de.tobiundmario.secrethitlermobilecompanion.SHEvents.TopPolicyPlayedEvent;

import static de.tobiundmario.secrethitlermobilecompanion.SHClasses.GameManager.BackupManager.backupToCache;
import static de.tobiundmario.secrethitlermobilecompanion.SHClasses.GameManager.GameEventsManager.eventList;
import static de.tobiundmario.secrethitlermobilecompanion.SHClasses.GameManager.GameEventsManager.remove;
import static de.tobiundmario.secrethitlermobilecompanion.SHClasses.GameManager.GameEventsManager.undoRemoval;
import static de.tobiundmario.secrethitlermobilecompanion.SHClasses.GameManager.LegislativeSessionManager.legSessionNo;

public final class RecyclerViewManager {
    private RecyclerViewManager() {}

    static private RecyclerView cardList;
    private static EventCardRecyclerViewAdapter cardListAdapter;
    public static boolean swipeEnabled = false;

    private static OldPlayerListRecyclerViewAdapter oldPlayerListRecyclerViewAdapter;
    private static CustomTracksRecyclerViewAdapter customTracksRecyclerViewAdapter;

    public static EventCardRecyclerViewAdapter getCardListAdapter() {
        return cardListAdapter;
    }
    public static RecyclerView getCardList() {
        return cardList;
    }

    public static void initialise(RecyclerView recyclerView) {
        cardList = recyclerView;
        cardListAdapter = new EventCardRecyclerViewAdapter(eventList, GameEventsManager.getContext());
        cardList.setAdapter(cardListAdapter);
        cardList.setItemAnimator(new ModifiedDefaultItemAnimator());
    }

    public static void destroy() {
        cardList = null;
        cardListAdapter = null;
        oldPlayerListRecyclerViewAdapter = null;
        customTracksRecyclerViewAdapter = null;
    }

    public static CustomTracksRecyclerViewAdapter getCustomTracksRecyclerViewAdapter() {
        return customTracksRecyclerViewAdapter;
    }

    public static OldPlayerListRecyclerViewAdapter getOldPlayerListRecyclerViewAdapter() {
        return oldPlayerListRecyclerViewAdapter;
    }

    public static void setupSwipeToDelete() {
        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                final int position = viewHolder.getAdapterPosition();
                final GameEvent event = eventList.get(position);

                /*
                Firstly, check if it is allowed to be removed
                It is allowed to be removed if
                - Manual mode is enabled
                - it is the last event
                - It is the last Legislative Session in the list with no DeckShuffledEvent in front of it
                 */
                if(GameManager.gameTrack.isManualMode() || position == eventList.size() - 1 || event instanceof LegislativeSession && ((LegislativeSession) event).getSessionNumber() == legSessionNo - 1 && !(eventList.get(eventList.size() - 1) instanceof DeckShuffledEvent)) {
                    remove(event);
                    Snackbar snackbar = Snackbar.make(cardList, GameEventsManager.getContext().getString(R.string.snackbar_GameEvent_removed_message), BaseTransientBottomBar.LENGTH_LONG);

                    snackbar.setAction(GameEventsManager.getContext().getString(R.string.undo), new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(event instanceof ExecutiveAction) {
                                LegislativeSession legislativeSession = ((ExecutiveAction) event).getLinkedLegislativeSession();
                                if(legislativeSession != null) {
                                    undoRemoval(legislativeSession, position - 1);
                                    return;
                                }
                            } else if(event instanceof TopPolicyPlayedEvent) {
                                LegislativeSession legislativeSession = ((TopPolicyPlayedEvent) event).getLinkedLegislativeSession();
                                if(legislativeSession != null) {
                                    undoRemoval(legislativeSession, position - 1);
                                    return;
                                }
                            }
                            undoRemoval(event, position);
                        }
                    }).show();

                    snackbar.addCallback(new Snackbar.Callback() {
                        @Override
                        public void onDismissed(Snackbar transientBottomBar, int e) {
                            if (e == 2) {
                                if(event instanceof ExecutiveAction) {
                                    LegislativeSession legislativeSession = ((ExecutiveAction) event).getLinkedLegislativeSession();
                                    if(legislativeSession != null) {
                                        JSONManager.addGameLogChange(new EventChange(legislativeSession, EventChange.EVENT_DELETE));
                                        JSONManager.addGameLogChange(new EventChange(event, EventChange.EVENT_DELETE));
                                    }
                                } else if(event instanceof TopPolicyPlayedEvent) {
                                    LegislativeSession legislativeSession = ((TopPolicyPlayedEvent) event).getLinkedLegislativeSession();
                                    if(legislativeSession != null) {
                                        JSONManager.addGameLogChange(new EventChange(legislativeSession, EventChange.EVENT_DELETE));
                                        JSONManager.addGameLogChange(new EventChange(event, EventChange.EVENT_DELETE));
                                    }
                                } else if (event instanceof LegislativeSession) {
                                    GameEvent presidentAction = ((LegislativeSession) event).getPresidentAction();
                                    if(presidentAction != null && !presidentAction.isSetup) {
                                        JSONManager.addGameLogChange(new EventChange(event, EventChange.EVENT_DELETE));
                                        JSONManager.addGameLogChange(new EventChange(presidentAction, EventChange.EVENT_DELETE));
                                    }

                                } else JSONManager.addGameLogChange(new EventChange(event, EventChange.EVENT_DELETE));

                                try {
                                    backupToCache();
                                } catch (IOException | JSONException ex) {
                                    ExceptionHandler.showErrorSnackbar(ex, "GameLog.onSwiped() (backupToCache)");
                                }
                            }

                            super.onDismissed(transientBottomBar, e);
                        }
                    });
                } else {
                    cardListAdapter.notifyItemChanged(position);
                    Toast.makeText(GameEventsManager.getContext(), GameEventsManager.getContext().getString(R.string.toast_message_deletion_disabled), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public boolean isItemViewSwipeEnabled() {
                return swipeEnabled;
            }

        };

        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(cardList);
    }

    public static void initialiseSetupRecyclerView(final RecyclerView recyclerView, final Context context, boolean isPlayerList) {
        try {
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            recyclerView.setAdapter(getRecyclerViewAdapter(isPlayerList, context));

            setupSwipeCallback(recyclerView, isPlayerList, context);

            if(isPlayerList) SharedPreferencesManager.setCorrectPlayerListExplanationText( ((MainActivity) context).fragment_setup.tv_choose_from_previous_games_players, context);
            else SharedPreferencesManager.setCustomTracksTitle( ((MainActivity) context).fragment_setup.tv_title_custom_tracks, context);
        } catch (JSONException e) {
            ExceptionHandler.showErrorSnackbar(e, "RecyclerViewManager.initialiseSetupRecyclerView()");
        }
    }

    private static RecyclerView.Adapter getRecyclerViewAdapter(boolean isPlayerList, Context context) throws JSONException {
        if(isPlayerList) {
            oldPlayerListRecyclerViewAdapter = new OldPlayerListRecyclerViewAdapter(SharedPreferencesManager.getPastPlayerLists(context), context);
            return oldPlayerListRecyclerViewAdapter;
        } else {
            customTracksRecyclerViewAdapter = new CustomTracksRecyclerViewAdapter(SharedPreferencesManager.getFascistTracks(context), context);
            return customTracksRecyclerViewAdapter;
        }
    }

    private static void setupSwipeCallback(final RecyclerView recyclerView,
                                           final boolean isPlayerList, final Context context) {
        final ItemTouchHelper.SimpleCallback itemTouchHelperCallback =
                new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                final int position = viewHolder.getAdapterPosition();
                SharedPreferencesManager.removeItemWithSnackbar(position, context, recyclerView, isPlayerList);
            }
        };
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recyclerView);
    }
}
