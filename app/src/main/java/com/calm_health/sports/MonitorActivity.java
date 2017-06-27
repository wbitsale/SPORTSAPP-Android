package com.calm_health.sports;


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
import android.content.pm.PackageManager;
import android.os.Handler;
import android.support.annotation.NonNull;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.github.gcacace.signaturepad.views.SignaturePad;
import com.hero.ecgchart.ECGChart;


import java.util.List;
import java.util.UUID;


public class MonitorActivity extends AppCompatActivity implements BluetoothAdapter.LeScanCallback {
    private final static String TAG = "monitoractivitylog";
    private static final int REQUEST_ENABLE_BT = 1;
    private final static int REQUEST_PERMISSION_REQ_CODE = 34; // any 8-bit number
    private final static long SCAN_DURATION = 5000;
    private final Handler mHandler = new Handler();
    private boolean mIsScanning = false;
    private BluetoothAdapter mBluetoothAdapter = null;
    private BluetoothGatt mBluetoothGatt;
    private BluetoothGattCharacteristic mNotifyCharacteristic;

    private final static String HR_SERVICE_UUID = "00002a37-0000-1000-8000-00805f9b34fb";
    private final static UUID CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    private ECGChart mECGSweepChart;
    private SignaturePad mHrtChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitor);
        initGui();
        initBLE();
        startScanBLE();
    }

    private void initGui() {
        this.mECGSweepChart = (ECGChart) findViewById(R.id.ecg_sweep_chart);
        mHrtChart = (SignaturePad) findViewById(R.id.hrt_chart);

        int MAX_LENGTH = 220;
        float[] arr = new float[MAX_LENGTH];
        for (int i = 0; i < MAX_LENGTH; i++)
            arr[i] = (float) Math.random() * 200;
        mHrtChart.setGraphType(SignaturePad.PLOT_TYPE_HEART_RATE);
        mHrtChart.setPts(arr);
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
        int hrsCount = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 1);
        if (hrsCount == 0) return;
        Log.d(TAG, "" + hrsCount);
        int sum = 0;
        for (int i = 0; i < hrsCount; i++) {
            ecgVal = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 2 + i * 2);

            if (ecgVal >= Math.pow(2, 15))
                ecgVal = 1250;

            mECGSweepChart.addEcgData(ecgVal);    //mInputBuf.addLast(ecgVal);
//            mCalmnessAnalysis.addEcgDataOne((double) ((ecgVal - 1200) / 800f));
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
}
