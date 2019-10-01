package com.ekdorn.stealapeak.parts;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import android.view.MenuItem;
import android.widget.Toast;

import com.ekdorn.stealapeak.R;
import com.ekdorn.stealapeak.StealAPeak;
import com.ekdorn.stealapeak.managers.Console;
import com.ekdorn.stealapeak.services.MessagingService;
import com.google.firebase.auth.FirebaseAuth;

import java.io.InputStream;


public class SettingsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportFragmentManager().beginTransaction().replace(android.R.id.content, new GeneralPreferenceFragment()).commit();

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    public static class GeneralPreferenceFragment extends PreferenceFragmentCompat {
        Preference name, pic;

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.pref_general, rootKey);
            setHasOptionsMenu(true);

            String myPhone = FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber();

            name = (Preference) findPreference("name");
            name.setDefaultValue(FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl().getScheme());
            name.setSummary(FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl().getScheme());
            name.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    Console.reloadName(preference.getContext(), newValue.toString(), new Console.OnSuccess() {
                        @Override
                        public void successful() {
                            Toast.makeText(preference.getContext(), "Name changed!", Toast.LENGTH_SHORT).show();
                            Console.sendToAll(preference.getContext(), MessagingService.SERVICE_NAME_CH, MessagingService.TYPE_FIELD_SERVICE);
                        }
                    });
                    return true;
                }
            });

            pic = (Preference) findPreference("pic");
            pic.setIcon(R.drawable.common_google_signin_btn_icon_dark);
            pic.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setType("image/*");
                    startActivityForResult(intent, 1);
                    return true;
                }
            });
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
            super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
            switch(requestCode) {
                case 1:
                    if(resultCode == RESULT_OK){
                        try {
                            Uri selectedImage = imageReturnedIntent.getData();
                            InputStream inputStream = getActivity().getContentResolver().openInputStream(selectedImage);
                            Drawable pic = Drawable.createFromStream(inputStream, selectedImage.toString());
                            inputStream.close();
                            this.pic.setIcon(pic);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    break;
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            this.finish();
        }
        return true;
    }
}
