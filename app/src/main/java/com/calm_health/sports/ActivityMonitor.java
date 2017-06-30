package com.calm_health.sports;


import android.Manifest;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;

import android.support.annotation.RequiresApi;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.calm_health.profile.BleManager;
import com.calm_health.profile.BleProfileActivity;
import com.calm_health.sports.share.AppSharedPreferences;
import com.github.gcacace.signaturepad.views.SignaturePad;
import com.hero.ecgchart.ECGChart;

import com.hero.ecgchart.PointData;
import com.tool.sports.com.analysis.CalmAnalysisListener;
import com.tool.sports.com.analysis.ProcessAnalysis;

<<<<<<< HEAD
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
=======
>>>>>>> 13823b6122f8f9af4e494b8df8acbd4d5870f043
import java.util.UUID;

import at.grabner.circleprogress.CircleProgressView;


public class ActivityMonitor extends BleProfileActivity implements CalmAnalysisListener, ECGManagerCallbacks,
        NavigationView.OnNavigationItemSelectedListener {
    private final static String TAG = "monitoractivitylog";
    private static final int REQUEST_ENABLE_BT = 1;
    private final static int REQUEST_PERMISSION_REQ_CODE = 34; // any 8-bit number

    private ImageView mImgConnect;
    private final static String HR_SERVICE_UUID = "00002a37-0000-1000-8000-00805f9b34fb";
    private final static UUID CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    private ECGChart mECGSweepChart;
    private SignaturePad mHrtChart;
    private CircleProgressView mCircleCalm, mCircleMotion;

    private ProcessAnalysis mCalmnessAnalysis;

    LinearLayout lyt_status, lyt_record, lyt_zoom;
    LinearLayout lyt_info, lyt_graph;
    FrameLayout lyt_frame;
    ImageView img_record;
    TextView tx_record;

    boolean isZoomed = false;
    boolean isRecord = false;
    boolean isUserDisconnect = false;

<<<<<<< HEAD
    int isECG = -1;
    float accX = 0;
    float accY = 0;
    float accZ = 0;
    int batteryAmount = 0;
    int nPercent = -1;


=======
    @RequiresApi(api = Build.VERSION_CODES.M)
>>>>>>> 13823b6122f8f9af4e494b8df8acbd4d5870f043
    @Override
    protected void onCreateView(Bundle savedInstanceState) {
        setContentView(R.layout.activity_monitor);

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
        navigationView.getMenu().getItem(0).setChecked(true);

        initGui();

        init_calmnessModule();
        _checkPermission();

    }

    private String mStrDeviceMacAddress;

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void getDeviceFromPreference() {
        mStrDeviceMacAddress = AppSharedPreferences.getDeviceMacAddress(this);
        Toast.makeText(this, mStrDeviceMacAddress, Toast.LENGTH_SHORT).show();
        if (mStrDeviceMacAddress != "null") {
            Log.d(TAG, "getDeviceFromPreference: " + mStrDeviceMacAddress);
            super.startScan(mStrDeviceMacAddress);
        } else {
            Toast.makeText(this, "Device is not registered.", Toast.LENGTH_SHORT).show();
        }
    }


<<<<<<< HEAD
    public void disconnect() {
        isUserDisconnet = true;
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.disconnect();
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

=======
>>>>>>> 13823b6122f8f9af4e494b8df8acbd4d5870f043
    private static int mBatteryLevel = 0;

    @Override
    public void onPause() {
        Log.d(TAG, "onPause");
        AppSharedPreferences.setBatteryLevel(this, mBatteryLevel);
        super.onPause();
<<<<<<< HEAD
        AppSharedPreferences.setBatteryLevel(this, mBatteryLevel);
=======
>>>>>>> 13823b6122f8f9af4e494b8df8acbd4d5870f043
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy");
        AppSharedPreferences.setBatteryLevel(this, mBatteryLevel);
        super.onDestroy();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
<<<<<<< HEAD
    protected void onDestroy() {
        Log.i(TAG, "destroy");
//        disconnect();
        super.onDestroy();
=======
    protected void onResume() {
        Log.i(TAG, "onResume");
        isUserDisconnect = false;
        getDeviceFromPreference();
        super.onResume();
>>>>>>> 13823b6122f8f9af4e494b8df8acbd4d5870f043
    }


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

    private void init_calmnessModule() {
        mCalmnessAnalysis = new ProcessAnalysis(); // create instance
        mCalmnessAnalysis.setOnCalmnessCallback(this);
        mCalmnessAnalysis.startCalm(); // start Analysis Algorithm
    }

    boolean doubleBackToExitPressedOnce = false;

    @Override
    public void onBackPressed() {
        if (isZoomed) {
            isZoomed = false;
            getSupportActionBar().show();
            lyt_frame.setPadding(0, (int) ActivityMonitor.this.getResources().getDimension(R.dimen.maring_dp), 0, 0);
            lyt_status.setVisibility(View.VISIBLE);
            lyt_info.setVisibility(View.VISIBLE);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 0, 4f);
            params.setMargins(10, 0, 10, 0);
            lyt_graph.setLayoutParams(params);
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else {
            if (doubleBackToExitPressedOnce) {
<<<<<<< HEAD
                stopScanBLE();
//                disconnect();
=======

>>>>>>> 13823b6122f8f9af4e494b8df8acbd4d5870f043
                super.onBackPressed();
                return;
            }
            this.doubleBackToExitPressedOnce = true;
            Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    doubleBackToExitPressedOnce = false;
                }
            }, 2000);
        }
    }


    @Override
    protected BleManager<ECGManagerCallbacks> initializeManager() {
        final ECGManager manager = ECGManager.getInstance(getApplicationContext());
        manager.setGattCallbacks(this);
        return manager;
    }

    @Override
    protected void setDefaultUI() {

    }

    @Override
    protected int getDefaultDeviceName() {
        return 0;
    }

    @Override
    protected int getAboutTextId() {
        return 0;
    }

    @Override
    protected UUID getFilterUUID() {
        return null;
    }

    private void initGui() {
        lyt_status = (LinearLayout) findViewById(R.id.lyt_status);
        lyt_graph = (LinearLayout) findViewById(R.id.lyt_graph);
        lyt_info = (LinearLayout) findViewById(R.id.lyt_info);
        lyt_record = (LinearLayout) findViewById(R.id.lyt_record);
        lyt_zoom = (LinearLayout) findViewById(R.id.lyt_zoom);
        img_record = (ImageView) findViewById(R.id.img_record);
        tx_record = (TextView) findViewById(R.id.tx_record);
        lyt_frame = (FrameLayout) findViewById(R.id.lyt_frame);

        lyt_zoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                isZoomed = true;
                lyt_status.setVisibility(View.GONE);
                lyt_info.setVisibility(View.GONE);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, 0, 11f);
                params.setMargins(0, 0, 0, 0);
                lyt_graph.setLayoutParams(params);
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                getSupportActionBar().hide();
                lyt_frame.setPadding(0, 0, 0, 0);
            }
        });

        lyt_record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isRecord = !isRecord;

                if (isRecord) {
                    img_record.setBackgroundResource(R.drawable.status_recording);
                    tx_record.setText("Recording...");
<<<<<<< HEAD

=======
                    mCalmnessAnalysis.startCSVExport(mStrDeviceMacAddress.replace(":", "_"));
>>>>>>> 13823b6122f8f9af4e494b8df8acbd4d5870f043
                } else {
                    img_record.setBackgroundResource(R.drawable.status_record);
                    tx_record.setText("Record");
                    mCalmnessAnalysis.stopCSVExport();
                }
            }
        });

        mImgConnect = (ImageView) findViewById(R.id.img_connect);
        this.mECGSweepChart = (ECGChart) findViewById(R.id.ecg_sweep_chart);
        mHrtChart = (SignaturePad) findViewById(R.id.hrt_chart);


        int MAX_LENGTH = 40;
        float[] arr = new float[MAX_LENGTH];
        for (int i = 0; i < MAX_LENGTH; i++)
            arr[i] = 100;
        mHrtChart.setGraphType(SignaturePad.PLOT_TYPE_HEART_RATE);
        mHrtChart.setPts(arr);

        mCircleMotion = (CircleProgressView) findViewById(R.id.circle_motin);
        mCircleCalm = (CircleProgressView) findViewById(R.id.circle_calm);

        mCircleMotion.setBarColor(Color.rgb(0xf9, 0xff, 0x00), Color.rgb(0x98, 0x0e, 0x00));
        mCircleCalm.setBarColor(Color.rgb(0xf9, 0xff, 0x00), Color.rgb(0x98, 0x0e, 0x00));
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mHrtChart.update();
            }
        }, 1000);

        TimerTask drawEmitter = new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mECGSweepChart.setConnection(true);
                        if (isECG == 0) {
                            mECGSweepChart.setConnection(false);
                            mImgConnect.setImageResource(R.drawable.status_disconnected);
                        } else if (isECG == -1) {
                            mECGSweepChart.setConnection(false);
                            mImgConnect.setImageResource(R.drawable.status_disconnected);
                        } else {
                            mECGSweepChart.setConnection(true);
                            mImgConnect.setImageResource(R.drawable.status_connected);

                        }
                    }
                });
            }
        };
        Timer timer = new Timer();
        timer.schedule(drawEmitter, 0, 100);
    }

    @Override
<<<<<<< HEAD
=======
    public void onRequestPermissionsResult(final int requestCode, final @NonNull String[] permissions, final @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION_REQ_CODE: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // We have been granted the Manifest.permission.ACCESS_COARSE_LOCATION permission. Now we may proceed with scanning.
                } else {
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
                }
                break;
            }
        }
    }

    @Override
>>>>>>> 13823b6122f8f9af4e494b8df8acbd4d5870f043
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Checks the orientation of the screen for landscape and portrait and set portrait mode always
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

<<<<<<< HEAD
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.i("connect", "connected");
                mBluetoothGatt.discoverServices();
                updateView(1, 0, 0, 0, 0);
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.i("connect", "disconnected");
                updateView(-1, 0, 0, 0, 0);
            }
        }
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.i("connect", "discorverd");
                List<BluetoothGattService> services = gatt.getServices();
                setCharacteristic(services, HR_SERVICE_UUID);
            }
        }
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.i("connect", "Read");
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            Log.i("connect", "changed");
            receiveData(characteristic);
        }
    };

    public void setCharacteristic(List<BluetoothGattService> gattServices, String uuid) {
        if (gattServices == null) {
            return;
        }
        for (BluetoothGattService gattService : gattServices) {
            List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
            for (final BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                if (gattCharacteristic.getUuid().toString().equals(uuid)) {
                    // System.out.println("liufafa uuid-->"+uuid.toString());
                    final int charaProp = gattCharacteristic.getProperties();
                    if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
                        // If there is an active notification save_on a
                        // characteristic, clear
                        // it first so it doesn't update the data field save_on the
                        // user interface.
                        if (mNotifyCharacteristic != null) {
                            // setCharacteristicNotification(mNotifyCharacteristic,
                            // false);
                            mNotifyCharacteristic = null;
                        }
                        readCharacteristic(gattCharacteristic);
                    }
                    if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                        mNotifyCharacteristic = gattCharacteristic;
                        setCharacteristicNotification(gattCharacteristic, true);
                    }

                }
            }
        }
    }

    public void receiveData(BluetoothGattCharacteristic characteristic) {
        int ecgVal;
        boolean isSensorDetected;
        isSensorDetected = isSensorDetected(characteristic.getValue()[0]);

        int hrsCount = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 1);
        if (hrsCount == 0) return;
        Log.d("ecg", "" + hrsCount);
        for (int i = 0; i < hrsCount; i++) {
            ecgVal = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 2 + i * 2);
            if (ecgVal >= 2500)
                ecgVal = 1250;
            mCalmnessAnalysis.addEcgDataOne(ecgVal);
            mECGSweepChart.addEcgData(ecgVal);    //mInputBuf.addLast(ecgVal);
        }
        int batteryAmount = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 2 + hrsCount * 2);
        int accX = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 2 + hrsCount * 2 + 2);
        int accY = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 2 + hrsCount * 2 + 4);
        int accZ = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 2 + hrsCount * 2 + 6);
        updateView(1, batteryAmount, accX, accY, accZ);
    }

    public void updateView(int isECG, int batteryAmount, int accX, int accY, int accZ) {
        this.isECG = isECG;
        this.batteryAmount = batteryAmount;
        this.accX = accX;
        this.accY = accY;
        this.accZ = accZ;
    }
=======
    int nPercent = -1;
>>>>>>> 13823b6122f8f9af4e494b8df8acbd4d5870f043

    public int calcBattery(int batteryAmount) {
        double fvolt = (double) batteryAmount / 4095 * 0.6 * 114 / 14;
        Log.i("battery1", "" + fvolt);
        int fpercent = (int) ((fvolt - 3.6) / (0.6) * 100);
        if (fpercent <= 100 && fpercent >= 0) {
            if (nPercent != -1)
                fpercent = (fpercent + nPercent) / 2;
            nPercent = fpercent;
        }
        return nPercent;
    }

    private void setDeviceConnectStateOnView(final boolean _isSensorDetected) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (_isSensorDetected)
                    mImgConnect.setImageResource(R.drawable.status_connected);
                else
                    mImgConnect.setImageResource(R.drawable.status_disconnected);
            }
        });
    }

<<<<<<< HEAD
    public boolean setCharacteristicNotification(BluetoothGattCharacteristic characteristic, boolean enable) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            return false;
        }
        boolean ok = false;
        if (mBluetoothGatt.setCharacteristicNotification(characteristic, enable)) {
            BluetoothGattDescriptor clientConfig = characteristic.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID);
            if (clientConfig != null) {
                if (enable) {
                    ok = clientConfig.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                } else {
                    ok = clientConfig.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
                }
                if (ok) {
                    ok = mBluetoothGatt.writeDescriptor(clientConfig);
                }
            }
        }
        return ok;
    }


    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.readCharacteristic(characteristic);
    }

    @Override
    public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
//        if (device == null) return;
        Log.d("BLEDEV", "" + device.getName());
        if (device != null && device.getName() != null) {
            if (device.getName().toLowerCase().contains("calm")) {
                if (device.getAddress().contentEquals(mStrDeviceMacAddress))
                    connectBle(device);
            }
        }
    }

    public void connectBle(BluetoothDevice bledev) {
        if (mBluetoothAdapter == null || bledev.getAddress() == null) {
        }

        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(bledev.getAddress());
        if (device == null) {
        }

        if (device != null) {
            mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
        }
        stopScanBLE();
    }
=======
>>>>>>> 13823b6122f8f9af4e494b8df8acbd4d5870f043

    @Override
    public void on_calm_result(double v) {
        Log.d(TAG, "clam: " + v);
//        mCircleCalm.setValue((float) v);
        mCircleCalm.setValueAnimated((float) v);
    }

    @Override
    public void on_heart_rate_result(final double v) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mHrtChart.addPoint((float) v);
                Log.d(TAG, "heartrate: " + v);
                mHrtChart.update();
            }
        });
    }

    @Override
    public void on_atrial_fibrillation_result(String strAF_result) {
        Log.d(TAG, "on_atrial_fibrillation_result: " + strAF_result);
    }

    NavigationView navigationView;

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Log.i("menu", "menu");
        int id = item.getItemId();
        if (id == R.id.nav_exercise) {
        } else if (id == R.id.nav_sleep) {
            Intent intent = new Intent(ActivityMonitor.this, ActivitySleep.class);
            startActivity(intent);
            this.finish();
        } else if (id == R.id.nav_data) {
            Intent intent = new Intent(ActivityMonitor.this, ActivityData.class);
            startActivity(intent);
            this.finish();
        } else if (id == R.id.nav_profile) {
            Intent intent = new Intent(ActivityMonitor.this, ActivityProfile.class);
            startActivity(intent);
            this.finish();

        } else if (id == R.id.nav_setting) {
            Intent intent = new Intent(ActivityMonitor.this, ActivitySettings.class);
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

    @Override
    public void onHRSensorPositionFound(BluetoothDevice device, String position) {

    }

    boolean isConnect = false;

    @Override
    public void onHRValueReceived(BluetoothDevice device, int ecgVal, boolean isSensorDetected) {
        isConnect = isSensorDetected;

        if (ecgVal >= Math.pow(2, 15))
            ecgVal = 0;
        int nAF_NORMAL_UNKNOWN = mCalmnessAnalysis.addEcgDataOne(ecgVal);
        if (nAF_NORMAL_UNKNOWN != ProcessAnalysis.IS_UNKNOWN) {
            Log.d(TAG, "nAF_NORMAL_UNKNOWN:      " + nAF_NORMAL_UNKNOWN);
        }
        mECGSweepChart.addEcgData(new PointData(ecgVal, nAF_NORMAL_UNKNOWN));
    }

    @Override
    public void onBatteryReceived(int batteryAmount) {
        double fvolt = (double) batteryAmount / 4095 * 0.6 * 114 / 14;
        Log.i("battery1", "" + fvolt);
        int fpercent = (int) ((fvolt - 3.6) / (0.6) * 100);
        if (fpercent <= 100 && fpercent >= 0) {
            if (nPercent != -1)
                fpercent = (fpercent + nPercent) / 2;
            nPercent = fpercent;
            mBatteryLevel = nPercent;
//            Log.d(TAG, "onBatteryReceived: calced   " + nPercent);
        }

    }

    @Override
    public void onAccDataReceived(AccData data, boolean isSensorDeteted) {

    }

    @Override
    public void onDeviceConnected(final BluetoothDevice device) {

        setDeviceConnectStateOnView(true);
        mECGSweepChart.setConnection(true);
        super.onDeviceConnected(device);
    }


    @Override
    public void onDeviceDisconnected(final BluetoothDevice device) {
        setDeviceConnectStateOnView(false);
        mECGSweepChart.setConnection(false);
        super.onDeviceDisconnected(device);
    }

    @Override
    public void onLinklossOccur(final BluetoothDevice device) {
        setDeviceConnectStateOnView(false);
        super.onLinklossOccur(device);
    }
}
