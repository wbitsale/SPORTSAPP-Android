package com.calm_health.sports;

import android.app.Activity;
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
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.w3c.dom.Text;

public class ActivitySettings extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    RelativeLayout lyt_chn_pwd, lyt_close, lyt_pair, lyt_update, lyt_privacy, lyt_about, lyt_out;
    LinearLayout lyt_battery;
    TextView tv_dev_name, tv_dev_mac, tv_connect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

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
        navigationView.getMenu().getItem(4).setChecked(true);
        initGUI();
    }

    public void initGUI() {
        lyt_chn_pwd = (RelativeLayout) findViewById(R.id.lyt_cng_pwd);
        lyt_close = (RelativeLayout) findViewById(R.id.lyt_close);
        lyt_pair = (RelativeLayout) findViewById(R.id.lyt_pair);
        lyt_update = (RelativeLayout) findViewById(R.id.lyt_update);
        lyt_privacy = (RelativeLayout) findViewById(R.id.lyt_privacy);
        lyt_about = (RelativeLayout) findViewById(R.id.lyt_about);
        lyt_out = (RelativeLayout) findViewById(R.id.lyt_out);
        lyt_battery = (LinearLayout) findViewById(R.id.lyt_battery_setting);
        tv_dev_mac = (TextView) findViewById(R.id.device_mac_setting);
        tv_dev_name = (TextView) findViewById(R.id.device_name_setting);
        tv_connect = (TextView) findViewById(R.id.tx_connect_setting);

        lyt_battery.post(new Runnable() {
            @Override
            public void run() {
                int nWidth = lyt_battery.getWidth();
                int nHeight = lyt_battery.getHeight();
                int newWidth = nWidth * (100 - 20) / 100;
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        newWidth, nHeight);
                lyt_battery.setLayoutParams(params);
            }
        });


        lyt_chn_pwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        lyt_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        lyt_pair.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ActivitySettings.this, ActivityScan.class);
                startActivity(intent);
            }
        });

        lyt_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ActivitySettings.this, ActivityOtaDfu.class);
                startActivity(intent);
            }
        });

        lyt_privacy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        lyt_about.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        lyt_out.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ActivitySettings.this, ActivityLoginScreen.class);
                startActivity(intent);

            }
        });
    }

    NavigationView navigationView;

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Log.i("menu", "menu");
        int id = item.getItemId();
        if (id == R.id.nav_exercise) {
            Intent intent = new Intent(ActivitySettings.this, ActivityMonitor.class);
            startActivity(intent);
            this.finish();
        } else if (id == R.id.nav_sleep) {
            Intent intent = new Intent(ActivitySettings.this, ActivitySleep.class);
            startActivity(intent);
            this.finish();

        } else if (id == R.id.nav_data) {
            Intent intent = new Intent(ActivitySettings.this, ActivityData.class);
            startActivity(intent);
            this.finish();
        } else if (id == R.id.nav_profile) {
            Intent intent = new Intent(ActivitySettings.this, ActivityProfile.class);
            startActivity(intent);
            this.finish();
        } else if (id == R.id.nav_setting) {
        } else if (id == R.id.nav_exit) {
            this.finish();
            System.exit(0);
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawers();
        return true;
    }
}
