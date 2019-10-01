package com.ekdorn.stealapeak;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;

import android.util.Log;
import android.view.View;

import com.ekdorn.stealapeak.managers.CryptoManager;
import com.ekdorn.stealapeak.managers.PrefManager;
import com.google.android.material.navigation.NavigationView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Switch;
import android.widget.TextView;

import com.ekdorn.stealapeak.database.AppDatabase;
import com.ekdorn.stealapeak.managers.Console;
import com.ekdorn.stealapeak.managers.ContactsManager;
import com.ekdorn.stealapeak.parts.LoginActivity;
import com.ekdorn.stealapeak.parts.SettingsActivity;
import com.ekdorn.stealapeak.parts.UserSearchFragment;
import com.ekdorn.stealapeak.services.MessagingService;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.lang.ref.WeakReference;

public class StealAPeak extends AppCompatActivity {
    NavigationView navigationView;

    private static final int loginActivity = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivityForResult(intent, loginActivity);
        } else {
            postCreate();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        View hView =  navigationView.getHeaderView(0);
        TextView nav_name = (TextView)hView.findViewById(R.id.nameView);
        nav_name.setText(FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl().getScheme());
        TextView nav_phone = (TextView)hView.findViewById(R.id.phoneView);
        nav_phone.setText(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == loginActivity) {
            if (resultCode == RESULT_CANCELED) {
                this.finish();
            } else {
                postCreate();
            }
        }
    }

    private void postCreate() {
        setContentView(R.layout.outer_activity_stealapeak);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        final ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                ((Switch) findViewById(R.id.switcher)).setChecked(false);
            }
        };
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        Log.e("TAG", "postCreate: USER:" +
                "\nNAME: " + FirebaseAuth.getInstance().getCurrentUser().getDisplayName() +
                "\nEMAIL: " + FirebaseAuth.getInstance().getCurrentUser().getEmail() +
                "\nPHONE: " + FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber() +
                "\nPROVIDER: " + FirebaseAuth.getInstance().getCurrentUser().getProviderId() +
                "\nUID: " + FirebaseAuth.getInstance().getCurrentUser().getUid() +
                "\nPHOTO: " + FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl() +
                "\nMETA: " + FirebaseAuth.getInstance().getCurrentUser().getMetadata());
        Log.e("TAG", "postCreate: " + CryptoManager.getPublicKey(this));
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        /*View hView =  navigationView.getHeaderView(0);
        TextView nav_name = (TextView)hView.findViewById(R.id.nameView);
        nav_name.setText(FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl().getScheme());
        TextView nav_phone = (TextView)hView.findViewById(R.id.phoneView);
        nav_phone.setText(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber());*/

        ContactsManager.create(navigationView.getMenu(), new ContactsManager.OnSelected() {
            @Override
            public void selected() {
                DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                drawer.closeDrawer(GravityCompat.START);
            }
            @Override
            public void added() {
                DrawerLayout ndrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                ndrawer.openDrawer(GravityCompat.START);
            }
        }, new WeakReference<StealAPeak>(this));
        navigationView.setNavigationItemSelectedListener(ContactsManager.get());

        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (task.isSuccessful()) {
                            Console.reloadToken(task.getResult().getToken());
                        }
                    }
                });
        //if (PreferenceManager.getDefaultSharedPreferences(this).getString("sync", "-1").equals("-1")) Console.refreshAllContacts(this);

        FragmentManager manager = this.getSupportFragmentManager();
        manager.beginTransaction().add(R.id.user_search_frame, new UserSearchFragment()).commit();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.steal_apeak, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.action_settings:
                Intent intentDialog = new Intent(StealAPeak.this, SettingsActivity.class);
                intentDialog.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intentDialog);
                return true;
            case R.id.action_logout:
                Console.sendToAll(StealAPeak.this, MessagingService.SERVICE_RELOGIN, MessagingService.TYPE_FIELD_SERVICE);

                AppDatabase.getDatabase(this).clearDb();
                PreferenceManager.getDefaultSharedPreferences(this).edit().clear().apply();
                FirebaseAuth.getInstance().signOut();

                closeOptionsMenu();
                StealAPeak.this.recreate();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
