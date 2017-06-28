package com.calm_health.sports;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.calm_health.sports.share.AppSharedPreferences;


import java.util.ArrayList;
import java.util.Arrays;

public class ActivityScan extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener, BluetoothAdapter.LeScanCallback {
    Button btn_pair, btn_cancel;
    ImageView img_calm, img_rotate;
    private ListView deviceList;
    TextView tx_status;

    private LeDeviceListAdapter mLeDeviceListAdapter;
    private BluetoothAdapter mBluetoothAdapter = null;
    private static final int REQUEST_ENABLE_BT = 1;
    private final static int REQUEST_PERMISSION_REQ_CODE = 34; // any 8-bit number
    private final static int SCAN_DURATION = 5000;
    private final static String STR_FILTER = "CALM";
    int currentPos = -1;
    Bledevices currentBle;
    ObjectAnimator rotateAnimator;


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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

//        Intent intent = new Intent(ActivityScan.this, ActivityOtaDfu.class);
//
//        startActivity(intent);
//        finish();
        
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(true);
        }

        initView();
        _checkPermission();
    }

    public void initView() {
        deviceList = (ListView) findViewById(R.id.device_list);
        mLeDeviceListAdapter = new LeDeviceListAdapter();
        deviceList.setAdapter(mLeDeviceListAdapter);
        deviceList.setOnItemClickListener(this);
        deviceList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        btn_pair = (Button) findViewById(R.id.btn_pair);
        btn_cancel = (Button) findViewById(R.id.btn_cancel);
        tx_status = (TextView) findViewById(R.id.tx_status);
        img_calm = (ImageView) findViewById(R.id.img_calm);
        img_rotate = (ImageView) findViewById(R.id.img_rotate);


        btn_pair.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentBle != null) {
                    AppSharedPreferences.setDeviceMacAddress(ActivityScan.this, currentBle.device.getAddress());
                    AppSharedPreferences.setDeviceName(ActivityScan.this, currentBle.device.getName());

                    Intent intent = new Intent(ActivityScan.this, ActivityMonitor.class);

                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(ActivityScan.this, "Please select device.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Intent intent = new Intent(ActivityScan.this, ActivityMonitor.class);
                Intent intent = new Intent(ActivityScan.this, ActivityOtaDfu.class);

                startActivity(intent);
                finish();
            }
        });

        img_calm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startScanBLE();
                rotateAnimator = ObjectAnimator.ofFloat(img_rotate, View.ROTATION, new float[]{0.0F, 360.0F}).setDuration(500L);
                rotateAnimator.setInterpolator(new LinearInterpolator());
                rotateAnimator.setRepeatCount(-1);
                if (rotateAnimator.isStarted())
                    rotateAnimator.cancel();
                rotateAnimator.start();

                tx_status.setText("Scanning nearby for devices...");
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        stopScanBLE();
                    }
                }, SCAN_DURATION);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        this.finish();
        return true;
    }

    private void startScanBLE() {
        if (mLeDeviceListAdapter.mLeDevices.size() > 0) {
            currentPos = -1;
            mLeDeviceListAdapter.mLeDevices.clear();
            unCheckall();
        }
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
        rotateAnimator.cancel();
//        if(mBluetoothAdapter() > 0)
//            tx_status.setText("Please scan new devices.");
//        else
        tx_status.setText("No device found. Try again.");
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // User chose not to enable Bluetooth.
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            finish();
            return;
        } else if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_OK) {
            // initBLE();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onResume() {
        super.onResume();
        initBLE();
    }

    public void unCheckall() {
        LeDeviceListAdapter adapter = ((LeDeviceListAdapter) deviceList.getAdapter());
        for (int i = 0; i < deviceList.getChildCount(); i++) {
            RelativeLayout itemLayout = (RelativeLayout) deviceList.getChildAt(i);
            CheckBox cb = (CheckBox) itemLayout.findViewById(R.id.rd_select);
            cb.setChecked(false);
            adapter.mCheckStates[i] = false;
        }
    }

    @Override
    public void onLeScan(final BluetoothDevice device, final int rssi, byte[] scanRecord) {
        // TODO Auto-generated method stub
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                {
                    Bledevices leDevice = new Bledevices();
                    leDevice.device = device;
                    leDevice.isChecked = false;
                    leDevice.signal = rssi;
                    mLeDeviceListAdapter.addDevice(leDevice);
                    mLeDeviceListAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    @Override
    public void onClick(View view) {

    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        unCheckall();
        currentBle = mLeDeviceListAdapter.getDevice(position);
        if (currentBle == null)
            return;
        currentPos = position;
        CheckBox chb = (CheckBox) view.findViewById(R.id.rd_select);
        chb.setChecked(true);
    }

    private class LeDeviceListAdapter extends BaseAdapter {
        private ArrayList<Bledevices> mLeDevices;
        private LayoutInflater mInflator;
        boolean mCheckStates[];

        LeDeviceListAdapter() {
            super();
            mLeDevices = new ArrayList<>();
            mInflator = ActivityScan.this.getLayoutInflater();
            mCheckStates = new boolean[0];
        }

        void addDevice(Bledevices dev) {
            if ((dev.device.getName() != null) && (dev.device.getName().toUpperCase().contains(STR_FILTER))) {
                int i;
                int listSize = mLeDevices.size();
                for (i = 0; i < listSize; i++) {
                    if (mLeDevices.get(i).device.equals(dev.device)) {
                        break;
                    }
                }
                if (i >= listSize) {
                    mLeDevices.add(dev);
                    mCheckStates = Arrays.copyOf(mCheckStates, mCheckStates.length + 1);
                }
            }
        }

        Bledevices getDevice(int position) {
            return mLeDevices.get(position);
        }

        @Override
        public int getCount() {
            return mLeDevices.size();
        }

        @Override
        public Object getItem(int i) {
            return mLeDevices.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(final int i, View view, ViewGroup viewGroup) {
            final ViewHolder viewHolder;
            // General ListView optimization code.
            if (view == null) {
                view = mInflator.inflate(R.layout.item_device, null);
                viewHolder = new ViewHolder();
                viewHolder.deviceName = (TextView) view.findViewById(R.id.tx_name);
                viewHolder.deviceMac = (TextView) view.findViewById(R.id.tx_mac);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }
            Bledevices bleDevice = mLeDevices.get(i);
            final String deviceName = bleDevice.device.getName();
            final String deviceMac = bleDevice.device.getAddress();
            if (deviceName != null && deviceName.length() > 0) {
                viewHolder.deviceName.setText(deviceName);
                viewHolder.deviceMac.setText(deviceMac);

            } else {
                viewHolder.deviceName.setText("Unknow device");
                viewHolder.deviceName.setText("Unknow Mac");
            }
            return view;
        }
    }

    private static class ViewHolder {
        TextView deviceName;
        TextView deviceMac;
    }

    private class Bledevices {
        BluetoothDevice device;
        int signal = 0;
        boolean isChecked = false;
    }
}
