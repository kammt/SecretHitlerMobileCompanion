package de.tobiundmario.secrethitlermobilecompanion;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.google.android.material.snackbar.Snackbar;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import de.tobiundmario.secrethitlermobilecompanion.SHClasses.Claim;
import de.tobiundmario.secrethitlermobilecompanion.SHEvents.LegislativeSession;

public final class ExceptionHandler {

    private static Context context;
    private static String version = "(Error)";
    private static List<EditingLogEntry> editingLog = new ArrayList<>();

    private ExceptionHandler() {}

    public static void initialise(Context context) {
        ExceptionHandler.context = context;

        version = "(Error)";

        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            version = pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e1) {
            showErrorSnackbar(e1, "ExceptionHandler.initialise()");
        }
    }

    public static void destroy() {
        context = null;
    }

    public static void showErrorSnackbar(final Exception e, final String function) {
        Snackbar.make( ((ViewGroup) ((MainActivity) context).findViewById(android.R.id.content)).getChildAt(0), context.getString(R.string.snackbar_message_error), Snackbar.LENGTH_LONG)
                .setAction(context.getString(R.string.btn_report_error), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        reportOnGitHub(e, function);
                    }
                })
                .show();
    }

    private static String getBasicGitHubURL(String title, String labels) {
        return "https://github.com/TobeSoftwareGmbH/SecretHitlerMobileCompanion/issues/new?labels="+labels
                + "&title=" + title
                + "&body=";
    }

    public static void reportOnGitHub(Exception e, String function) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        String sStackTrace = sw.toString();

        String url = getBasicGitHubURL(e.getClass().getCanonicalName() + "+in+Version+" + version + " (API" + Build.VERSION.SDK_INT + ")", "bug")
                + sStackTrace
                + "%0A%0A"
                + "The error occurred in the function "+function
                + "%0AThe information above is automatically generated, please do not change it) %0A%0A"
                + "Steps to reproduce: %0A%0A"
                + "Other comments:";

        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        context.startActivity(browserIntent);
    }

    public static void reportFascistTrackError() {
        String url = getBasicGitHubURL("FascistTrack+issue+in+Version+" + version + " (API" + Build.VERSION.SDK_INT + ")", "bug,FascistTrack")
                + "&body="
                + getEditingLog()
                + "%0A(The information above is automatically generated, please do not change it) %0A%0A"
                + "Steps to reproduce: %0A%0A"
                + "Other comments:";

        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        context.startActivity(browserIntent);
    }

    public static String getEditingLog() {
        StringBuilder result = new StringBuilder();
        for (EditingLogEntry entry : editingLog) {
            result.append(entry.toString() + "%0A");
        }
        return result.toString();
    }

    public static void logLegislativeSessionUpdate(EditingLogEntry editingLogEntry) {
        editingLog.add(editingLogEntry);
    }

    public static void clearEditingLog() {
        editingLog.clear();
    }

    public static class EditingLogEntry {
        //values before
        private int electionTracker_before, libPolicies_before, fasPolicies_before;
        boolean rejectedBefore, vetoed_before;
        int playedPolicy_before;

        //values after edit
        private int electionTracker_after, libPolicies_after, fasPolicies_after;
        int playedPolicy_after;
        boolean vetoed_after, rejectedAfter;

        public EditingLogEntry() {
        }

        public void setElectionTracker_after(int electionTracker_after) {
            this.electionTracker_after = electionTracker_after;
        }

        public void setElectionTracker_before(int electionTracker_before) {
            this.electionTracker_before = electionTracker_before;
        }

        public void setFasPolicies_after(int fasPolicies_after) {
            this.fasPolicies_after = fasPolicies_after;
        }

        public void setFasPolicies_before(int fasPolicies_before) {
            this.fasPolicies_before = fasPolicies_before;
        }

        public void setLibPolicies_after(int libPolicies_after) {
            this.libPolicies_after = libPolicies_after;
        }

        public void setLibPolicies_before(int libPolicies_before) {
            this.libPolicies_before = libPolicies_before;
        }

        public void setLegislativeSessions(LegislativeSession before, LegislativeSession after) {
            rejectedBefore = before.getVoteEvent().isRejected();
            vetoed_before = !rejectedBefore && before.getClaimEvent().isVetoed();
            playedPolicy_before = rejectedBefore ? -1 : before.getClaimEvent().getPlayedPolicy();

            if(after == null) return;
            rejectedAfter = after.getVoteEvent().isRejected();
            vetoed_after = !rejectedAfter && after.getClaimEvent().isVetoed();
            playedPolicy_after = rejectedAfter ? -1 : after.getClaimEvent().getPlayedPolicy();
        }

        private String getLegSessionChanges() {
            if(rejectedAfter != rejectedBefore) {
                return "Rejected: " + rejectedBefore + " => " + rejectedAfter;
            } else if(!rejectedBefore) {
                return "PlayedPolicy: " + Claim.getClaimStringForJSON(context, playedPolicy_before) + " => " + Claim.getClaimStringForJSON(context, playedPolicy_after) + ", Vetoed: " + vetoed_before + "=>" + vetoed_after;
            }
            return "No Changes";
        }

        @NonNull
        @Override
        public String toString() {
            return "[LegislativeSession update (" + getLegSessionChanges() + ") led to the following changes: %0A"
            + "Election Tracker: "+electionTracker_before+" => "+electionTracker_after+", %0A"
            + "Liberal Policies: "+libPolicies_before+" => "+libPolicies_after+", %0A"
            + "Fascist Policies: "+fasPolicies_before+" => "+fasPolicies_after+"]";
        }
    }
}
