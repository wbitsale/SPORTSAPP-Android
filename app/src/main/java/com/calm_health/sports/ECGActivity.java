package com.calm_health.sports;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.hero.ecgchart.ECGChart;

import java.util.UUID;


import com.calm_health.profile.BleManager;
import com.calm_health.profile.BleProfileActivity;
public class ECGActivity extends BleProfileActivity implements ECGManagerCallbacks {

    private final static String GRAPH_STATUS = "graph_status";
    private final static String IS_CONNECT = "hr_value";

    private boolean isGraphInProgress = false;
    private TextView mHRSValue, mHRSPosition, mTV_battery_level;
    private TextView mTV_accX, mTV_accY, mTV_accZ, mTV_debug;
    private ImageView mImgHrsConnect; // hand connect state

    private ECGChart mChart;

    private boolean isConnect = false;

    @Override
    protected void onCreateView(final Bundle savedInstanceState) {
        setContentView(R.layout.activity_feature_ecg);
        setGUI();
        _checkPermission();
    }

    private final static int REQUEST_PERMISSION_REQ_CODE = 34; // any 8-bit number

    private boolean _checkPermission() {
        int nLog = ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.BLUETOOTH);
        Log.d("permisiontest", "nLog: " + nLog);
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
            // When user pressed Deny and still wants to use this functionality, show the rationale
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.BLUETOOTH)) {
                Log.d("permisiontest", "1 BLUETOOTH if");
            } else {
                Log.d("permisiontest", "1 BLUETOOTH else");
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.BLUETOOTH}, REQUEST_PERMISSION_REQ_CODE);
            }
        }
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
            // When user pressed Deny and still wants to use this functionality, show the rationale
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.BLUETOOTH_ADMIN)) {
                Log.d("permisiontest", "2 BLUETOOTH_ADMIN if");
            } else {
                Log.d("permisiontest", "2 BLUETOOTH_ADMIN else");
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.BLUETOOTH_ADMIN}, REQUEST_PERMISSION_REQ_CODE);
            }
        }
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // When user pressed Deny and still wants to use this functionality, show the rationale
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSION_REQ_CODE);
            }

        }
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // When user pressed Deny and still wants to use this functionality, show the rationale
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_PERMISSION_REQ_CODE);
            }
        }
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
            // When user pressed Deny and still wants to use this functionality, show the rationale
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.BLUETOOTH)) {
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.BLUETOOTH}, REQUEST_PERMISSION_REQ_CODE);
            }
        }
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // When user pressed Deny and still wants to use this functionality, show the rationale
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                Log.d("permisiontest", "3 ACCESS_COARSE_LOCATION if");
            } else {
                Log.d("permisiontest", "3 ACCESS_COARSE_LOCATION else");
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_PERMISSION_REQ_CODE);
            }
        }
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.NFC) != PackageManager.PERMISSION_GRANTED) {
            // When user pressed Deny and still wants to use this functionality, show the rationale
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.NFC)) {
                Log.d("permisiontest", "4 NFC if");
            } else {
                Log.d("permisiontest", "4 NFC else");
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.NFC}, REQUEST_PERMISSION_REQ_CODE);
            }
        }

        return false;
    }


    private void setBatteryLevelOnView(final int value) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mTV_battery_level.setText(("Battery: " + value + "mV"));
            }
        });
    }

    @Override
    public void onBatteryReceived(int value) {
        setBatteryLevelOnView(value);
    }

    private void setGUI() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mTV_accX = (TextView) findViewById(R.id.textView_accx);
        mTV_accY = (TextView) findViewById(R.id.textView_accY);
        mTV_accZ = (TextView) findViewById(R.id.textView_accZ);
        mTV_battery_level = (TextView) findViewById(R.id.text_battery_level);
        mHRSValue = (TextView) findViewById(R.id.text_hrs_value);
        mHRSPosition = (TextView) findViewById(R.id.text_hrs_position);
        mTV_debug = (TextView) findViewById(R.id.tv_debug);
        mImgHrsConnect = (ImageView) findViewById(R.id.image_hrs_position);
        mChart = (ECGChart) findViewById(R.id.ecg_sweep_chart);

    }


    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStart() {
        super.onStart();
        final Intent intent = getIntent();

        //getSupportActionBar().setDisplayHomeAsUpEnabled(false);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull final Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        isGraphInProgress = savedInstanceState.getBoolean(GRAPH_STATUS);
        isConnect = savedInstanceState.getBoolean(IS_CONNECT);
        if (isGraphInProgress) {
            startShowGraph();
            setDeviceConnectStateOnView();
        }
    }

    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean(GRAPH_STATUS, isGraphInProgress);
        outState.putBoolean(IS_CONNECT, isConnect);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopShowGraph();
    }

    @Override
    protected int getLoggerProfileTitle() {
        return R.string.hrs_feature_title;
    }

    @Override
    protected int getAboutTextId() {
        return R.string.hrs_about_text;
    }

    @Override
    protected int getDefaultDeviceName() {
        return R.string.hrs_default_name;
    }

    @Override
    protected UUID getFilterUUID() {
        return ECGManager.HR_SERVICE_UUID;
    }

    void startShowGraph() {
        isGraphInProgress = true;

    }

    void stopShowGraph() {
        isGraphInProgress = false;
        setDefaultUI();

    }

    @Override
    protected BleManager<ECGManagerCallbacks> initializeManager() {
        final ECGManager manager = ECGManager.getInstance(getApplicationContext());
        manager.setGattCallbacks(this);
        return manager;
    }


    @Override
    public void onServicesDiscovered(final BluetoothDevice device, final boolean optionalServicesFound) {
        // this may notify user or show some views
    }

    @Override
    public void onDeviceReady(final BluetoothDevice device) {
        startShowGraph();
    }

    @Override
    public void onHRSensorPositionFound(final BluetoothDevice device, final String position) {
        setHRSPositionOnView(position);
    }

    @Override
    public void onHRValueReceived(final BluetoothDevice device, int ecgVal, boolean isSensorDetected) {
        isConnect = isSensorDetected;
        setDeviceConnectStateOnView();
        if (ecgVal >= Math.pow(2, 15)) {

            mChart.addEcgData(0);
            return;
        }
        mChart.addEcgData(ecgVal);

    }


    @Override
    public void onAccDataReceived(AccData data, boolean isSensorDeteted) {
        setAccDataOnView(data);
    }

    private void setAccDataOnView(final AccData data) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String xSignal = "";
                String ySignal = "";
                String zSignal = "";
//                if ((data.x & 0x8000) == 0x8000) {
//                    xSignal = "-";
//                    data.x = data.x & 0x7fff;
//                }
//                if ((data.y & 0x8000) == 0x8000) {
//                    ySignal = "-";
//                    data.y = data.y & 0x7fff;
//                }
//                if ((data.z & 0x8000) == 0x8000) {
//                    zSignal = "-";
//                    data.z = data.z & 0x7fff;
//                }

                mTV_accX.setText(("x: " + xSignal + data.x));
                mTV_accY.setText(("y: " + ySignal + data.y));
                mTV_accZ.setText(("z: " + zSignal + data.z));

            }
        });
    }

    @Override
    public void onDeviceDisconnected(final BluetoothDevice device) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mHRSValue.setText(R.string.not_available_value);
                mHRSPosition.setText(R.string.not_available);
                stopShowGraph();
                mChart.setConnection(false);
            }
        });


        super.onDeviceDisconnected(device);
    }

    public void onDeviceConnected(final BluetoothDevice device) {
        super.onDeviceConnected(device);
        mChart.setConnection(true);
    }


    @Override
    protected void setDefaultUI() {
        mHRSValue.setText(R.string.not_available_value);
        mHRSPosition.setText(R.string.not_available);
        mImgHrsConnect.setImageResource(R.drawable.disconnection);
    }


    public void onDebugStop(View v) {
    }

    // set ui value
    private void setDebugStringOnView(final String str) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mTV_debug.setText(str);
            }
        });
    }

    private void setDeviceConnectStateOnView() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isConnect) mImgHrsConnect.setImageResource(R.drawable.connection);
                else mImgHrsConnect.setImageResource(R.drawable.disconnection);
            }
        });
    }

    private void setHRSPositionOnView(final String position) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (position != null) {
                    mHRSPosition.setText(position);
                }
            }
        });
    }
}
