package de.tobiundmario.secrethitlermobilecompanion;

import android.os.Bundle;
import android.text.InputType;
import android.view.MenuItem;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.EditTextPreference;
import androidx.preference.PreferenceFragmentCompat;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new SettingsFragment())
                    .commit();
        }
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            this.finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);

            EditTextPreference.OnBindEditTextListener listener = new EditTextPreference.OnBindEditTextListener() {
                @Override
                public void onBindEditText(@NonNull EditText editText) {
                    editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                }
            };

            EditTextPreference serverPortPref = (EditTextPreference) findPreference("serverPort");
            if (serverPortPref != null) {
                serverPortPref.setOnBindEditTextListener(listener);
            }

            EditTextPreference fPoliciesPref = (EditTextPreference) findPreference("fPolicies");
            if (fPoliciesPref != null) {
                fPoliciesPref.setOnBindEditTextListener(listener);
            }

            EditTextPreference lPoliciesPref = (EditTextPreference) findPreference("lPolicies");
            if (lPoliciesPref != null) {
                lPoliciesPref.setOnBindEditTextListener(listener);
            }
        }
    }
}