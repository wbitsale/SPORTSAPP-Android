package com.calm_health.sports;

import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

public class ActivityData extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.setScrimColor(Color.TRANSPARENT);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.getMenu().getItem(2).setChecked(true);
    }

    NavigationView navigationView;
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Log.i("menu", "menu");
        int id = item.getItemId();
        if (id == R.id.nav_exercise) {
            Intent intent = new Intent(ActivityData.this, ActivityMonitor.class);
            startActivity(intent);
            this.finish();
        } else if (id == R.id.nav_sleep) {
            Intent intent = new Intent(ActivityData.this, ActivitySleep.class);
            startActivity(intent);
            this.finish();
        } else if (id == R.id.nav_data) {

        } else if (id == R.id.nav_profile) {
            Intent intent = new Intent(ActivityData.this, ActivityProfile.class);
            startActivity(intent);
            this.finish();
        } else if (id == R.id.nav_setting) {
            Intent intent = new Intent(ActivityData.this, ActivitySettings.class);
            startActivity(intent);
            this.finish();
        } else if (id == R.id.nav_exit) {
            this.finish();
            System.exit(0);
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawers();
        return true;
    }
}
