package com.calm_health.sports;


import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;

import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
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

import com.calm_health.sports.share.AppSharedPreferences;
import com.github.gcacace.signaturepad.views.SignaturePad;
import com.hero.ecgchart.ECGChart;

import com.tool.sports.com.analysis.CalmAnalysisListener;
import com.tool.sports.com.analysis.ProcessAnalysis;

import java.util.List;
import java.util.UUID;

import at.grabner.circleprogress.CircleProgressView;


public class ActivityMonitor extends AppCompatActivity implements BluetoothAdapter.LeScanCallback, CalmAnalysisListener, NavigationView.OnNavigationItemSelectedListener {
    private final static String TAG = "monitoractivitylog";
    private static final int REQUEST_ENABLE_BT = 1;
    private final static int REQUEST_PERMISSION_REQ_CODE = 34; // any 8-bit number
    private final static long SCAN_DURATION = 5000;
    private final Handler mHandler = new Handler();
    private boolean mIsScanning = false;
    private BluetoothAdapter mBluetoothAdapter = null;
    private BluetoothGatt mBluetoothGatt;
    private BluetoothGattCharacteristic mNotifyCharacteristic;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitor);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.setScrimColor(Color.TRANSPARENT);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        initGui();
        getDevice();
        init_calmnessModule();
        _checkPermission();
    }

    private void getDevice() {
        String strMac = AppSharedPreferences.getMac(this);
        if (strMac != null) {
            Log.d(TAG, "device: " + strMac);
            initBLE();
            startScanBLE();
        } else {
            Toast.makeText(this, "Device is not registered. ", Toast.LENGTH_SHORT).show();
        }
    }


    public void disconnect() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
    }

    @Override
    protected void onDestroy() {
        Log.i(TAG, "destroy");
        disconnect();
        super.onDestroy();

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
        } else
            super.onBackPressed();
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
                } else {
                    img_record.setBackgroundResource(R.drawable.status_record);
                    tx_record.setText("Record");
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mCircleCalm.setBarColor(getColor(R.color.primary), Color.RED);
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mHrtChart.update();
            }
        }, 1000);
    }

    private void initBLE() {
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
        }

        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        if (mBluetoothAdapter == null) {
            finish();
            return;
        }

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }

    private void startScanBLE() {

        if (mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.startLeScan(this);
        } else {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }

    private void stopScanBLE() {
        if (mBluetoothAdapter != null) {
            mBluetoothAdapter.stopLeScan(this);
            mBluetoothAdapter.cancelDiscovery();
        }
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode, final @NonNull String[] permissions, final @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION_REQ_CODE: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // We have been granted the Manifest.permission.ACCESS_COARSE_LOCATION permission. Now we may proceed with scanning.
                    startScanBLE();
                } else {
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
                }
                break;
            }
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Checks the orientation of the screen for landscape and portrait and set portrait mode always
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.i(TAG, "connected");
                mECGSweepChart.setConnection(true);
                mBluetoothGatt.discoverServices();

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.i(TAG, "disconnected");
                mECGSweepChart.setConnection(false);
                startScanBLE(); //retry connect
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.i(TAG, "discorverd");
                List<BluetoothGattService> services = gatt.getServices();
                setCharacteristic(services, HR_SERVICE_UUID);
            }
        }

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
    long hrReceive = -1;
    int hrNumber = 0;

    public void receiveData(BluetoothGattCharacteristic characteristic) {
        if (hrReceive == -1) hrReceive = System.currentTimeMillis();
        long ellipse = System.currentTimeMillis() - hrReceive;
        if (ellipse > 1000) {
            hrReceive = System.currentTimeMillis();
            Log.d("hrRec", "" + hrNumber);
            hrNumber = 0;
        }
        int ecgVal;
        boolean isSensorDetected;
        isSensorDetected = isSensorDetected(characteristic.getValue()[0]);
        setSensorDetectOnView(isSensorDetected);
        int hrsCount = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 1);
        if (hrsCount == 0) return;
//        Log.d(TAG, "" + hrsCount);
        int sum = 0;
        for (int i = 0; i < hrsCount; i++) {
            ecgVal = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 2 + i * 2);

            if (ecgVal >= Math.pow(2, 15))
                ecgVal = 1250;

            mECGSweepChart.addEcgData(ecgVal);    //mInputBuf.addLast(ecgVal);
            mCalmnessAnalysis.addEcgDataOne((double) ((ecgVal - 1200) / 800f));

            hrNumber++;
        }
        int batteryAmount = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 2 + hrsCount * 2);
        int accX = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 2 + hrsCount * 2 + 2);
        int accY = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 2 + hrsCount * 2 + 4);
        int accZ = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 2 + hrsCount * 2 + 6);
    }

    private boolean isSensorDetected(final byte value) {
        return ((value & 0x01) != 0);
    }

    private void setSensorDetectOnView(final boolean _isSensorDetected) {
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
//        Log.d(TAG, "" + device.getName());
        if (device != null && device.getName() != null) {
            if (device.getName().toLowerCase().contains("calm")) {
                connectBle(device);
            }
        }
    }

    public void connectBle(BluetoothDevice device) {

        if (device != null) {
            mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
        }

        stopScanBLE();

    }

    @Override
    public void on_calm_result(double v) {
        Log.d(TAG, "clam: " + v);
        mCircleCalm.setValue((float) v);
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
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        return false;
    }
}
