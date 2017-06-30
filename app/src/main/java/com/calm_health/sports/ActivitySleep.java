package com.calm_health.sports;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.icu.util.Calendar;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.TimePicker;

import com.kevalpatel.ringtonepicker.RingtonePickerDialog;
import com.kevalpatel.ringtonepicker.RingtonePickerListener;
import com.tool.sports.com.analysis.CalmAnalysisListener;

import org.w3c.dom.Text;

import java.io.IOException;
import java.sql.Time;

public class ActivitySleep extends AppCompatActivity implements  NavigationView.OnNavigationItemSelectedListener {
    //FirstView
    ImageView img_connect;
    LinearLayout lyt_rington;
    TextView tv_rington;
    ImageButton ibtn_sleep, ibtn_cancel, ibtn_wake;

    TextView tv_sleep_clock;
    int nTime = 900;

    //Second View
    LinearLayout lyt_status_sleep;
    TextView tv_charge_sleep;
    TextView tv_content_sleep;
    TextView tv_cancel_sleep;
    TextView tv_wake;
    LinearLayout lyt_sleep;
    LinearLayout lyt_sleepstart;
    LinearLayout lyt_sleepafter;

    private Uri mCurrentSelectedUri;
    private MediaPlayer mMediaPlayer;
    private boolean isRinging = false;

    Handler handler;
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            int currentHour = new Time(System.currentTimeMillis()).getHours();
            int minute = new Time(System.currentTimeMillis()).getMinutes();

            Log.i("Time",""+(currentHour * 100 + minute)+":"+nTime);

            if(nTime == (currentHour * 100 + minute)){
                if(!isRinging) {
                    isRinging = true;
                    mMediaPlayer = new MediaPlayer();
                    try {
                        mMediaPlayer.setDataSource(ActivitySleep.this, mCurrentSelectedUri);
                        mMediaPlayer.prepare();
                        mMediaPlayer.setLooping(true);
                        mMediaPlayer.start();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    lyt_sleep.setBackgroundColor(Color.WHITE);
                    lyt_sleepstart.setVisibility(View.INVISIBLE);
                    lyt_sleepafter.setVisibility(View.VISIBLE);
                    lyt_rington.setVisibility(View.INVISIBLE);
                    lyt_status_sleep.setVisibility(View.INVISIBLE);
                    tv_charge_sleep.setVisibility(View.INVISIBLE);
                    tv_content_sleep.setText("Wake Up Time");
                    tv_wake.setText("Wake Up");
                    tv_cancel_sleep.setText("Snooze");

                    tv_content_sleep.setTextColor(Color.BLACK);
                    tv_sleep_clock.setTextColor(Color.BLACK);
                    tv_charge_sleep.setTextColor(Color.BLACK);
                    tv_cancel_sleep.setTextColor(Color.BLACK);
                    tv_wake.setTextColor(Color.BLACK);
                }
            }
            else
                handler.postDelayed(this,1000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sleep);
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
        navigationView.getMenu().getItem(1).setChecked(true);
        initUI();
    }
    public void initUI(){
        handler = new Handler();

        img_connect = (ImageView) findViewById(R.id.img_connect_sleep);
        lyt_rington = (LinearLayout) findViewById(R.id.lyt_rington);
        tv_rington = (TextView) findViewById(R.id.tx_rington);
        ibtn_sleep = (ImageButton) findViewById(R.id.btn_sleep);
        ibtn_wake = (ImageButton) findViewById(R.id.ibtn_wake);
        ibtn_cancel = (ImageButton) findViewById(R.id.btn_cancel_snooze);
        tv_sleep_clock = (TextView) findViewById(R.id.textClock);

        lyt_sleep = (LinearLayout) findViewById(R.id.lyt_sleep);
        lyt_status_sleep = (LinearLayout) findViewById(R.id.lyt_status_sleep);
        tv_charge_sleep = (TextView) findViewById(R.id.tx_charge_sleep);
        tv_content_sleep = (TextView) findViewById(R.id.tx_content_sleep);
        tv_cancel_sleep = (TextView) findViewById(R.id.tx_cancel_sleep);
        tv_wake = (TextView) findViewById(R.id.tx_wake_sleep);

        lyt_sleepafter = (LinearLayout) findViewById(R.id.lyt_sleepafter);
        lyt_sleepstart = (LinearLayout) findViewById(R.id.lyt_sleepstart);



        lyt_rington.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {

            }
        });
        if(GlobalVar.nSleepMode == 0){
            lyt_sleep.setBackgroundColor(Color.WHITE);
            lyt_sleepstart.setVisibility(View.VISIBLE);
            lyt_sleepafter.setVisibility(View.INVISIBLE);
            lyt_rington.setVisibility(View.VISIBLE);
            lyt_status_sleep.setVisibility(View.VISIBLE);
            tv_charge_sleep.setVisibility(View.INVISIBLE);
            tv_content_sleep.setText("Set Wake Up Time");

            tv_content_sleep.setTextColor(Color.BLACK);
            tv_sleep_clock.setTextColor(Color.BLACK);
            tv_charge_sleep.setTextColor(Color.BLACK);
            tv_cancel_sleep.setTextColor(Color.BLACK);
            tv_wake.setTextColor(Color.BLACK);
        }
        else if(GlobalVar.nSleepMode == 1)
        {
            lyt_sleep.setBackgroundResource(R.drawable.gradbkgblack);
            lyt_sleepstart.setVisibility(View.INVISIBLE);
            lyt_sleepafter.setVisibility(View.VISIBLE);
            lyt_rington.setVisibility(View.INVISIBLE);
            lyt_status_sleep.setVisibility(View.INVISIBLE);
            tv_charge_sleep.setVisibility(View.VISIBLE);
            tv_content_sleep.setText("Wake Up Time");
            tv_wake.setText("Wake Now");
            tv_cancel_sleep.setText("Cancel");

            tv_content_sleep.setTextColor(Color.WHITE);
            tv_sleep_clock.setTextColor(Color.WHITE);
            tv_charge_sleep.setTextColor(Color.WHITE);
            tv_cancel_sleep.setTextColor(Color.WHITE);
            tv_wake.setTextColor(Color.WHITE);
        }
        else if(GlobalVar.nSleepMode == 2){
            lyt_sleep.setBackgroundColor(Color.WHITE);
            lyt_sleepstart.setVisibility(View.INVISIBLE);
            lyt_sleepafter.setVisibility(View.VISIBLE);
            lyt_rington.setVisibility(View.INVISIBLE);
            lyt_status_sleep.setVisibility(View.INVISIBLE);
            tv_charge_sleep.setVisibility(View.INVISIBLE);
            tv_content_sleep.setText("Wake Up Time");
            tv_wake.setText("Wake Up");
            tv_cancel_sleep.setText("Snooze");

            tv_content_sleep.setTextColor(Color.BLACK);
            tv_sleep_clock.setTextColor(Color.BLACK);
            tv_charge_sleep.setTextColor(Color.BLACK);
            tv_cancel_sleep.setTextColor(Color.BLACK);
            tv_wake.setTextColor(Color.BLACK);
        }
        ibtn_sleep.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                GlobalVar.nSleepMode = 1;
                lyt_sleep.setBackgroundResource(R.drawable.gradbkgblack);
                lyt_sleepstart.setVisibility(View.INVISIBLE);
                lyt_sleepafter.setVisibility(View.VISIBLE);
                lyt_rington.setVisibility(View.INVISIBLE);
                lyt_status_sleep.setVisibility(View.INVISIBLE);
                tv_charge_sleep.setVisibility(View.VISIBLE);
                tv_content_sleep.setText("Wake Up Time");
                tv_wake.setText("Wake Now");
                tv_cancel_sleep.setText("Cancel");

                tv_content_sleep.setTextColor(Color.WHITE);
                tv_sleep_clock.setTextColor(Color.WHITE);
                tv_charge_sleep.setTextColor(Color.WHITE);
                tv_cancel_sleep.setTextColor(Color.WHITE);
                tv_wake.setTextColor(Color.WHITE);

                handler.postDelayed(runnable,500);
            }
        });
        ibtn_wake.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                GlobalVar.nSleepMode = 0;
                lyt_sleep.setBackgroundColor(Color.WHITE);
                lyt_sleepstart.setVisibility(View.VISIBLE);
                lyt_sleepafter.setVisibility(View.INVISIBLE);
                lyt_rington.setVisibility(View.VISIBLE);
                lyt_status_sleep.setVisibility(View.VISIBLE);
                tv_charge_sleep.setVisibility(View.INVISIBLE);
                tv_content_sleep.setText("Set Wake Up Time");

                tv_content_sleep.setTextColor(Color.BLACK);
                tv_sleep_clock.setTextColor(Color.BLACK);
                tv_charge_sleep.setTextColor(Color.BLACK);
                tv_cancel_sleep.setTextColor(Color.BLACK);
                tv_wake.setTextColor(Color.BLACK);
            }
        });
        ibtn_cancel.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if(isRinging) {
                    mMediaPlayer.stop();
                    isRinging = false;
                }
                GlobalVar.nSleepMode = 0;
                lyt_sleep.setBackgroundColor(Color.WHITE);
                lyt_sleepstart.setVisibility(View.VISIBLE);
                lyt_sleepafter.setVisibility(View.INVISIBLE);
                lyt_rington.setVisibility(View.VISIBLE);
                lyt_status_sleep.setVisibility(View.VISIBLE);
                tv_charge_sleep.setVisibility(View.INVISIBLE);
                tv_content_sleep.setText("Set Wake Up Time");

                tv_content_sleep.setTextColor(Color.BLACK);
                tv_sleep_clock.setTextColor(Color.BLACK);
                tv_charge_sleep.setTextColor(Color.BLACK);
                tv_cancel_sleep.setTextColor(Color.BLACK);
                tv_wake.setTextColor(Color.BLACK);
            }
        });
        tv_sleep_clock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogFragment newFragment = new TimePickerFragment();
                newFragment.show(getFragmentManager(),"TimePicker");
            }
        });
        lyt_rington.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                RingtonePickerDialog.Builder ringtonePickerBuilder = new RingtonePickerDialog
                        .Builder(ActivitySleep.this, getSupportFragmentManager());

                //Set title of the dialog.
                //If set null, no title will be displayed.
                ringtonePickerBuilder.setTitle("Select ringtone");

                //Add the desirable ringtone types.

                ringtonePickerBuilder.addRingtoneType(RingtonePickerDialog.Builder.TYPE_MUSIC);
                ringtonePickerBuilder.addRingtoneType(RingtonePickerDialog.Builder.TYPE_NOTIFICATION);
                ringtonePickerBuilder.addRingtoneType(RingtonePickerDialog.Builder.TYPE_RINGTONE);
                ringtonePickerBuilder.addRingtoneType(RingtonePickerDialog.Builder.TYPE_ALARM);

                //set the text to display of the positive (ok) button.
                //If not set OK will be the default text.
                ringtonePickerBuilder.setPositiveButtonText("SET RINGTONE");

                //set text to display as negative button.
                //If set null, negative button will not be displayed.
                ringtonePickerBuilder.setCancelButtonText("CANCEL");

                //Set flag true if you want to play the com.ringtonepicker.sample of the clicked tone.
                ringtonePickerBuilder.setPlaySampleWhileSelection(true);

                //Set the callback listener.
                ringtonePickerBuilder.setListener(new RingtonePickerListener() {
                    @Override
                    public void OnRingtoneSelected(String ringtoneName, Uri ringtoneUri) {
                        mCurrentSelectedUri = ringtoneUri;
                        tv_rington.setText(ringtoneName);
//                        ringtoneTv.setText("Name : " + ringtoneName + "\nUri : " + ringtoneUri);
                    }
                });

                //set the currently selected uri, to mark that ringtone as checked by default.
                //If no ringtone is currently selected, pass null.
                ringtonePickerBuilder.setCurrentRingtoneUri(mCurrentSelectedUri);

                //Display the dialog.
                ringtonePickerBuilder.show();
            }
        });
    }

    public class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener{

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState){
            //Use the current time as the default values for the time picker
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);

            //Create and return a new instance of TimePickerDialog
            return new TimePickerDialog(getActivity(),this, hour, minute,
                    DateFormat.is24HourFormat(getActivity()));
        }

        //onTimeSet() callback method
        public void onTimeSet(TimePicker view, int hourOfDay, int minute){
            //Do something with the user chosen time
            //Get reference of host activity (XML Layout File) TextView widget
            String strTime = "";//tv_sleep_clock.setText();
            nTime = hourOfDay * 100 + minute;
            if(hourOfDay < 10) {
                strTime += "0" + (hourOfDay % 12) + ":";
            }
            else
                strTime += (hourOfDay % 12) + ":";
            if(minute < 10)
                strTime += "0" + (minute);
            else
                strTime += "" + minute;
            if(hourOfDay >= 12)
                strTime += "PM";
            else
                strTime += "AM";
            tv_sleep_clock.setText(strTime);
        }
    }

    NavigationView navigationView;
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Log.i("menu", "menu");
        int id = item.getItemId();
        if (id == R.id.nav_exercise) {
            Intent intent = new Intent(ActivitySleep.this, ActivityMonitor.class);
            startActivity(intent);
            this.finish();
        } else if (id == R.id.nav_sleep) {

        } else if (id == R.id.nav_data) {
            Intent intent = new Intent(ActivitySleep.this, ActivityData.class);
            startActivity(intent);
            this.finish();
        } else if (id == R.id.nav_profile) {
            Intent intent = new Intent(ActivitySleep.this, ActivityProfile.class);
            startActivity(intent);
            this.finish();

        } else if (id == R.id.nav_setting) {
            Intent intent = new Intent(ActivitySleep.this, ActivitySettings.class);
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
