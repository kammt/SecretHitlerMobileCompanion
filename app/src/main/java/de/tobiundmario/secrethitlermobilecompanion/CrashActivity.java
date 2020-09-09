package de.tobiundmario.secrethitlermobilecompanion;

import android.app.ActivityManager;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class CrashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crash);
        final Throwable e = (Throwable) getIntent().getExtras().get("e");

        final String sStackTrace = Log.getStackTraceString(e);

        ((TextView) findViewById(R.id.tv_stacktrace)).setText(sStackTrace);

        ((Button) findViewById(R.id.btn_report)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String version = "(Error)";

                try {
                    PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
                    version = pInfo.versionName;
                } catch (PackageManager.NameNotFoundException e1) {
                    e1.printStackTrace();
                }

                String url = "https://github.com/TobeSoftwareGmbH/SecretHitlerMobileCompanion/issues/new?labels=bug"
                        + "&title=Uncaught+" + e.getClass().getCanonicalName() + "+in+Version+" + version + " (API" + Build.VERSION.SDK_INT + ")"
                        + "&body="
                        + "> " + sStackTrace
                        + "%0A%0A"
                        + "Note: The error was uncaught."
                        + "%0AThe information above is automatically generated, please do not change it) %0A%0A"
                        + "Steps to reproduce: %0A%0A"
                        + "Other comments:";

                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(browserIntent);
            }
        });

        ((Button) findViewById(R.id.btn_clear_data)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((ActivityManager) getSystemService(ACTIVITY_SERVICE))
                        .clearApplicationUserData();
                finishAffinity();
            }
        });

        ((Button) findViewById(R.id.btn_exit)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finishAffinity();
            }
        });

        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(@NonNull Thread thread, @NonNull Throwable throwable) {
                throwable.printStackTrace();
                finishAffinity();
            }
        });
    }
}