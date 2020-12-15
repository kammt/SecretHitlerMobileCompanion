package de.tobiundmario.secrethitlermobilecompanion.SHClasses.GameManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import de.tobiundmario.secrethitlermobilecompanion.ExceptionHandler;
import de.tobiundmario.secrethitlermobilecompanion.SHClasses.Claim;
import de.tobiundmario.secrethitlermobilecompanion.SHEvents.ExecutiveAction;
import de.tobiundmario.secrethitlermobilecompanion.SHEvents.GameEvent;
import de.tobiundmario.secrethitlermobilecompanion.SHEvents.LegislativeSession;
import de.tobiundmario.secrethitlermobilecompanion.SHEvents.TopPolicyPlayedEvent;
import de.tobiundmario.secrethitlermobilecompanion.SHEvents.VoteEvent;

import static de.tobiundmario.secrethitlermobilecompanion.SHClasses.GameManager.GameEventsManager.restoredEventList;
import static de.tobiundmario.secrethitlermobilecompanion.SHClasses.GameManager.LegislativeSessionManager.addTrackAction;
import static de.tobiundmario.secrethitlermobilecompanion.SHClasses.GameManager.LegislativeSessionManager.processLegislativeSession;

public class BackupManager extends GameManager {

    private static String backupFileName = "backup.json";

    public static void backupToCache() throws IOException, JSONException {
        currentGameToFile(true, backupFileName, true);
    }

    public static boolean backupPresent() {
        return new File(GameEventsManager.getContext().getCacheDir(), backupFileName).exists();
    }

    public static void restoreBackup() {
        try {
            eventListFromFile(true, backupFileName);
            GameEventsManager.setGameStarted(true);
        } catch (JSONException e) {
            ExceptionHandler.showErrorSnackbar(e, "GameLog.restoreBackup()");
        }
    }

    public static void deleteBackup() {
        deleteFile(true, backupFileName);
    }

    /**
     * Backs up the entire game (events, players, etc.) into a JSON format and writes it into a file
     * @param cache if true, the file will be written to cache. If false, it wil be written to the app's data directory. This is to support permanent saving of games
     * @param fileName the supplied file name (with file extension)
     * @param settings if true, the used settings (sounds, server, fascistTrack) will be written as well
     * @throws IOException
     * @throws JSONException
     */
    public static void currentGameToFile(boolean cache, String fileName, boolean settings) throws IOException, JSONException {
        File file;
        if(cache) file = new File(GameEventsManager.getContext().getCacheDir(), fileName);
        else file = new File(GameEventsManager.getContext().getFilesDir(), fileName);

        String json = JSONManager.getCompleteGameJSON();

        FileWriter fw = new FileWriter(file.getAbsoluteFile());
        BufferedWriter bw = new BufferedWriter(fw);

        if(settings) {
            JSONObject object = new JSONObject(json);
            JSONObject settingsObject = new JSONObject();

            //We begin by adding the FascistTrack
            settingsObject.put("track", JSONManager.writeFascistTrackToJSON(GameManager.gameTrack));

            //Now all other settings
            settingsObject.put("sounds_execution", GameEventsManager.executionSounds);
            settingsObject.put("sounds_end", GameEventsManager.endSounds);
            settingsObject.put("sounds_policy", GameEventsManager.policySounds);

            settingsObject.put("server", GameEventsManager.server);

            object.put("settings", settingsObject);

            bw.write(object.toString());
        } else {
            bw.write(json);
        }
        bw.close();
        fw.close();
    }

    /**
     * Deletes a specified file
     * @param cache if true, the file will be written to cache. If false, it wil be written to the app's data directory. This is to support permanent saving of games
     * @param fileName the supplied file name (with file extension)
     */
    public static void deleteFile(boolean cache, String fileName) {
        File file;
        if(cache) file = new File(GameEventsManager.getContext().getCacheDir(), fileName);
        else file = new File(GameEventsManager.getContext().getFilesDir(), fileName);

        if(file.exists()) file.delete();
    }

    /**
     * Reads the contents from the specified file and passes it to the restoreGameFromJSON function
     * @param cache if true, the file will be written to cache. If false, it wil be written to the app's data directory. This is to support permanent saving of games
     * @param fileName the supplied file name (with file extension)
     * @throws JSONException
     */
    public static void eventListFromFile(boolean cache, String fileName) throws JSONException {
        File file;
        if(cache) file = new File(GameEventsManager.getContext().getCacheDir(), fileName);
        else file = new File(GameEventsManager.getContext().getFilesDir(), fileName);

        StringBuilder stringBuilder = new StringBuilder();
        try {
            FileInputStream fis = new FileInputStream(file);
            InputStreamReader inputStreamReader =
                    new InputStreamReader(fis, StandardCharsets.UTF_8);

            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                stringBuilder.append(line).append('\n');
                line = reader.readLine();
            }
            fis.close();
            inputStreamReader.close();
            reader.close();
        } catch (IOException e) {
            ExceptionHandler.showErrorSnackbar(e, "GameLog.eventListFromFile()");
        } finally {
            String contents = stringBuilder.toString();
            JSONObject object = new JSONObject(contents);
            restoreGameFromJSON(object);
        }
    }

    /**
     * Restores the entire game from a JSONObject
     * @param object the Object that was once written into a backup file
     * @throws JSONException
     */
    private static void restoreGameFromJSON(JSONObject object) throws JSONException {
        restoredEventList = new ArrayList<>();

        GameEventsManager.setGameStarted(false);
        GameEventsManager.liberalPolicies = 0;
        GameEventsManager.fascistPolicies = 0;
        GameEventsManager.electionTracker = 0;

        boolean executionSounds = false, endSounds = false, policySounds = false; //The sound settings will first be written into local variables. This is so that no sounds will be played during the event restoration as they have already been played before

        if(object.has("settings")) { //The file also included settings, they will be restored as well
            JSONObject settingsObject = object.getJSONObject("settings");

            GameManager.gameTrack = JSONManager.restoreFascistTrackFromJSON(settingsObject.getJSONObject("track"));

            GameEventsManager.server = settingsObject.getBoolean("server");

            executionSounds = settingsObject.getBoolean("sounds_execution");
            endSounds = settingsObject.getBoolean("sounds_end");
            policySounds = settingsObject.getBoolean("sounds_policy");
        }

        JSONObject game = object.getJSONObject("game");
        JSONArray players = game.getJSONArray("players");
        JSONArray plays = game.getJSONArray("plays");
        GameEventsManager.jsonData = plays;

        //Restore players
        for(int j = 0; j < players.length(); j++) {
            PlayerListManager.addPlayer(players.getString(j));
        }

        //Restore plays
        List<GameEvent> restoredEventList = new ArrayList<>();
        for(int i = 0; i < plays.length(); i++) {
            GameEvent event = JSONManager.createGameEventFromJSON((JSONObject) plays.get(i), GameEventsManager.getContext());
            restoredEventList.add(event);
            if(event instanceof LegislativeSession) {
                processLegislativeSession((LegislativeSession) event, false);
            }

            //We have to link Legislative Session and Executive Action back manually, as this is not saved in JSON
            //To do this, we check if the event before the Executive Action / TopPolicyPlayedEvent is a Legislative Session. If so, they are linked together
            if(!GameManager.gameTrack.isManualMode() && i > 0) {
                GameEvent priorEvent = restoredEventList.get(restoredEventList.size() - 2);
                if(priorEvent instanceof LegislativeSession) {
                    LegislativeSession legislativeSession = (LegislativeSession) priorEvent;

                    if (event instanceof ExecutiveAction) {
                        ((ExecutiveAction) event).setLinkedLegislativeSession(legislativeSession);
                        legislativeSession.setPresidentAction(event);
                    }

                    if (event instanceof TopPolicyPlayedEvent) {
                        ((TopPolicyPlayedEvent) event).setLinkedLegislativeSession(legislativeSession);
                        legislativeSession.setPresidentAction(event);
                    }
                }
            }
        }
        //When the auto-created executive action was not submitted (=> setup phase left) before the app closed, it will not be included in the backup. To mitigate this, we check if the last event is a LegislativeSession and if so add the track action again
        if(restoredEventList.size() > 0) {
            GameEvent lastEvent = restoredEventList.get(restoredEventList.size() - 1);
            if (lastEvent instanceof LegislativeSession && !GameManager.gameTrack.isManualMode()) {
                LegislativeSession legislativeSession = ((LegislativeSession) lastEvent);
                //However, we need to check if this LegislativeSession actually passed a fascist policy
                if(legislativeSession.getVoteEvent().getVotingResult() == VoteEvent.VOTE_PASSED && legislativeSession.getClaimEvent().getPlayedPolicy() == Claim.FASCIST && !legislativeSession.getClaimEvent().isVetoed())  addTrackAction(legislativeSession, true);
            }
        }

        //Finally, apply the settings
        GameEventsManager.endSounds = endSounds;
        GameEventsManager.executionSounds = executionSounds;
        GameEventsManager.policySounds = policySounds;
    }
}
