package de.tobiundmario.secrethitlermobilecompanion.SHClasses;

import android.content.Context;
import android.os.Build;

import androidx.annotation.RequiresApi;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import de.tobiundmario.secrethitlermobilecompanion.SHEvents.ClaimEvent;
import de.tobiundmario.secrethitlermobilecompanion.SHEvents.DeckShuffledEvent;
import de.tobiundmario.secrethitlermobilecompanion.SHEvents.ExecutionEvent;
import de.tobiundmario.secrethitlermobilecompanion.SHEvents.GameEvent;
import de.tobiundmario.secrethitlermobilecompanion.SHEvents.LegislativeSession;
import de.tobiundmario.secrethitlermobilecompanion.SHEvents.LoyaltyInvestigationEvent;
import de.tobiundmario.secrethitlermobilecompanion.SHEvents.PolicyPeekEvent;
import de.tobiundmario.secrethitlermobilecompanion.SHEvents.SpecialElectionEvent;
import de.tobiundmario.secrethitlermobilecompanion.SHEvents.VoteEvent;

@RequiresApi(api = Build.VERSION_CODES.N)
public class JSONManager {
    public static ConcurrentHashMap<GameLogChange, Boolean> gameLogChangesMap = new ConcurrentHashMap<GameLogChange, Boolean>();
    public static Set<GameLogChange> gameLogChanges = gameLogChangesMap.newKeySet();

    private static HashSet<String> clientIPs = new HashSet<String>();

    public static void setClientIPsSet(HashSet<String> clientIPsIn) {
        clientIPs = clientIPsIn;
    }

    public static String getCompleteGameJSON(String clientIP) {
        JSONObject obj = new JSONObject();
        JSONObject game = new JSONObject();

        if (clientIP != null) {
            for (GameLogChange change : gameLogChanges) {
                change.addClientServedTo(clientIP);
            }
        }

        try {
            game.put("players", PlayerList.getPlayerListJSON());
            game.put("plays", GameLog.getEventsJSON());
            obj.put("game", game);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return obj.toString();
    }

    public static String getCompleteGameJSON() {
        return getCompleteGameJSON(null);
    }

    public static String getGameChangesJSON(String clientIP)  {
        // Log.v("Size gameLogChanges<>", String.valueOf(gameLogChanges.size()));
        JSONArray changesJSON = new JSONArray();

        try {
            for (GameLogChange change : gameLogChanges) {
                if (!change.getServedTo().contains(clientIP)) {
                    // Log.v("Serving change", change.getEvent().getJSON().toString());
                    changesJSON.put(change.serve(clientIP));
                }

                if (change.getServedTo().equals(clientIPs)) {
                    gameLogChanges.remove(change);
                    // Log.v("Removed change", change.getServedTo().toString() + "; " + change.getEvent().getJSON().toString());
                }

                // Log.v("Change served to", change.getServedTo().toString() + "; " + change.getEvent().getJSON().toString());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return changesJSON.toString();
    }

    public static void addGameLogChange(GameLogChange change) {
        gameLogChanges.add(change);
    }

    public static GameEvent createGameEventFromJSON (JSONObject jsonObject, Context c) throws JSONException {
        switch ((String) jsonObject.get("type")) {
            case "legislative-session":
                return createLegislativeSessionFromJSON(jsonObject, c);
            case "shuffle":
                return createDeckShuffleFromJSON(jsonObject, c);
            case "executive-action":
                switch ((String) jsonObject.get("executive_action_type")) {
                    case "investigate_loyalty":
                        return createLoyaltyInvestigationfromJSON(jsonObject, c);
                    case "execution":
                        return  createExecutionFromJSON(jsonObject, c);
                    case "policy_peek":
                        return createPolicyPeekFromJSON(jsonObject, c);
                    case "special_election":
                        return createSpecialElectionFromJSON(jsonObject, c);
                }
                break;
            default:
                throw new IllegalStateException("JSONObject couldn't be classified as a specific event!");
        }
        return null;
    }

    private static LegislativeSession createLegislativeSessionFromJSON(JSONObject jsonObject, Context c) throws JSONException {
        int sessionNumber = jsonObject.getInt("num");

        String presidentName = (String) jsonObject.get("president");
        String chancellorName = (String) jsonObject.get("chancellor");
        boolean rejected = jsonObject.getBoolean("rejected");
        VoteEvent voteEvent = new VoteEvent(presidentName, chancellorName, (rejected) ? VoteEvent.VOTE_FAILED : VoteEvent.VOTE_PASSED);

        ClaimEvent claimEvent = null;
        if(!rejected) {
            int presClaim = Claim.getClaimInt((String) jsonObject.get("president_claim"));
            int chancClaim = Claim.getClaimInt((String) jsonObject.get("chancellor_claim"));

            boolean vetoed = jsonObject.getBoolean("veto");
            int policyPlayed = Claim.getClaimInt((String) jsonObject.get("policy_played"));
            claimEvent = new ClaimEvent(presClaim, chancClaim, policyPlayed, vetoed);
        }

        LegislativeSession session = new LegislativeSession(voteEvent, claimEvent, c, false);
        session.setSessionNumber(sessionNumber);
        return  session;
    }

    private static DeckShuffledEvent createDeckShuffleFromJSON(JSONObject jsonObject, Context c) throws JSONException {
        int liberalPolicies = jsonObject.getInt("liberal_policies");
        int fascistPolicies = jsonObject.getInt("fascist_policies");

        return new DeckShuffledEvent(liberalPolicies, fascistPolicies, c, false);
    }

    private static LoyaltyInvestigationEvent createLoyaltyInvestigationfromJSON(JSONObject jsonObject, Context c) throws JSONException {
        String presidentName = (String) jsonObject.get("president");
        String target = (String) jsonObject.get("target");

        int claim = Claim.getClaimInt((String) jsonObject.get("claim"));
        return new LoyaltyInvestigationEvent(presidentName, target, claim, c, false);
    }

    private static ExecutionEvent createExecutionFromJSON(JSONObject jsonObject, Context c) throws JSONException {
        String presidentName = (String) jsonObject.get("president");
        String target = (String) jsonObject.get("target");

        return new ExecutionEvent(presidentName, target, c, false);
    }

    private static PolicyPeekEvent createPolicyPeekFromJSON(JSONObject jsonObject, Context c) throws JSONException {
        String presidentName = (String) jsonObject.get("president");
        int claim = Claim.getClaimInt((String) jsonObject.get("claim"));

        return new PolicyPeekEvent(presidentName, claim, c, false);
    }

    private static SpecialElectionEvent createSpecialElectionFromJSON(JSONObject jsonObject, Context c) throws JSONException {
        String presidentName = (String) jsonObject.get("president");
        String target = (String) jsonObject.get("target");

        return new SpecialElectionEvent(presidentName, target, c, false);
    }

    public JSONObject writeFascistTrackToJSON(FascistTrack fascistTrack) throws JSONException {
        JSONObject object = new JSONObject();

        object.put("actions", fascistTrack.getActions());

        object.put("hz", fascistTrack.getHzStartingPolicy());
        object.put("veto", fascistTrack.getVetoStartingPolicy());

        object.put("min", fascistTrack.getMinPlayers());
        object.put("max", fascistTrack.getMaxPlayers());
        return object;
    }

    public static FascistTrack restoreFascistTrackFromJSON(JSONObject object) throws JSONException {
        FascistTrack track = new FascistTrack(object.getInt("min"), object.getInt("max"));

        track.setActions((int[]) object.get("actions"));

        track.setHZStartingPolicy(object.getInt("hz"));
        track.setVetoStartingPolicy(object.getInt("veto"));
        return track;
    }
}
