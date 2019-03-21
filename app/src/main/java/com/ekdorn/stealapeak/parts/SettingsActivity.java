package com.ekdorn.stealapeak.parts;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.ekdorn.stealapeak.R;
import com.ekdorn.stealapeak.managers.Console;
import com.ekdorn.stealapeak.services.MessagingService;
import com.google.firebase.auth.FirebaseAuth;


public class SettingsActivity extends AppCompatActivity {

    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(final Preference preference, Object value) {
            String stringValue = value.toString();
            preference.setSummary(stringValue);

            if (preference instanceof ListPreference) {
                int i = ((ListPreference)preference).findIndexOfValue(stringValue);
                CharSequence[] entries = ((ListPreference)preference).getEntries();
                preference.setSummary(entries[i]);
            } else if (preference.getKey().equals("name")) {
                if (!stringValue.equals(PreferenceManager.getDefaultSharedPreferences(preference.getContext()).getString("name", null))) {
                    Console.reloadName(preference.getContext(), stringValue, new Console.OnSuccess() {
                        @Override
                        public void successful() {
                            Toast.makeText(preference.getContext(), "Name changed!", Toast.LENGTH_SHORT).show();
                            Console.sendToAll(preference.getContext(), MessagingService.SERVICE_NAME_CH, MessagingService.TYPE_FIELD_SERVICE);
                        }
                    });
                }
            }
            return true;
        }
    };

    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     *
     * @see #sBindPreferenceSummaryToValueListener
     */
    private static void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new GeneralPreferenceFragment()).commit();

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
    }



    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class GeneralPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_general);
            setHasOptionsMenu(true);

            String myPhone = FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber();
            Log.e("TAG", "onCreate: " + myPhone);
            EditTextPreference name = (EditTextPreference) findPreference("name");
            name.setDefaultValue(myPhone);

            bindPreferenceSummaryToValue(findPreference("name"));
            bindPreferenceSummaryToValue(findPreference("sync"));
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            if (item.getItemId() == android.R.id.home) {
                GeneralPreferenceFragment.this.getActivity().finish();
            }
            return true;
        }
    }
}
