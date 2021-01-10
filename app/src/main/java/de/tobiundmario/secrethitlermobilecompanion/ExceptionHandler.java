package de.tobiundmario.secrethitlermobilecompanion;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.snackbar.Snackbar;

import java.io.PrintWriter;
import java.io.StringWriter;

public final class ExceptionHandler {

    private static Context context;
    private static String version = "(Error)";

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

    public static void reportOnGitHub(Exception e, String function) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        String sStackTrace = sw.toString();

        String url = "https://github.com/TobeSoftwareGmbH/SecretHitlerMobileCompanion/issues/new?labels=bug"
                + "&title=" + e.getClass().getCanonicalName() + "+in+Version+" + version + " (API" + Build.VERSION.SDK_INT + ")"
                + "&body="
                + "> " + sStackTrace
                + "%0A%0A"
                + "The error occurred in the function "+function
                + "%0AThe information above is automatically generated, please do not change it) %0A%0A"
                + "Steps to reproduce: %0A%0A"
                + "Other comments:";

        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        context.startActivity(browserIntent);
    }
}
