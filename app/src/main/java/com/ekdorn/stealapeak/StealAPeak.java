package com.ekdorn.stealapeak;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

import java.util.Map;

public class StealAPeak extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == loginActivity) {
            if (resultCode == RESULT_CANCELED) {
                this.finish();
            } else {
                postCreate();
            }
        }
    }

    private void postCreate() {
        setContentView(R.layout.activity_steal_a_peak);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        View hView =  navigationView.getHeaderView(0);
        TextView nav_name = (TextView)hView.findViewById(R.id.nameView);
        nav_name.setText(FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
        TextView nav_phone = (TextView)hView.findViewById(R.id.phoneView);
        nav_phone.setText(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber());

        for (final Map.Entry<String, User> entry: PrefManager.get(this).getAllUsers().entrySet()) {
            MenuItem item = navigationView.getMenu().add(R.id.main_group, Menu.NONE, Menu.NONE, entry.getValue().getName());
            item.setTitleCondensed(entry.getKey());
            //item
        }
        navigationView.setNavigationItemSelectedListener(this);

        Console.reloadToken(null, this);
        Console.refreshAllContacts(this);
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

        //noinspection SimplifiableIfStatement
        switch (id) {
            case R.id.action_settings:
                return true;
            case R.id.action_logout:
                PrefManager.get(this).logOut();
                FirebaseAuth.getInstance().signOut();

                closeOptionsMenu();
                StealAPeak.this.recreate();
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Toast.makeText(this, item.getTitleCondensed(), Toast.LENGTH_SHORT).show();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
