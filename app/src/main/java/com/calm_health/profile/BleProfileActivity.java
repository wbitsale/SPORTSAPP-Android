
package com.calm_health.profile;

import android.Manifest;
import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelUuid;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import no.nordicsemi.android.log.ILogSession;
import no.nordicsemi.android.log.LocalLogSession;
import no.nordicsemi.android.log.Logger;


import com.calm_health.scanner.DeviceListAdapter;
import com.calm_health.scanner.ScannerFragment;
import com.calm_health.sports.R;

import no.nordicsemi.android.nrftoolbox.utility.DebugLogger;
import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat;
import no.nordicsemi.android.support.v18.scanner.ScanCallback;
import no.nordicsemi.android.support.v18.scanner.ScanFilter;
import no.nordicsemi.android.support.v18.scanner.ScanResult;
import no.nordicsemi.android.support.v18.scanner.ScanSettings;

public abstract class BleProfileActivity extends AppCompatActivity implements BleManagerCallbacks {
    private static final String TAG = "BaseProfileActivity";
    private final static int REQUEST_PERMISSION_REQ_CODE = 34; // any 8-bit number
    private static final String SIS_CONNECTION_STATUS = "connection_status";
    private static final String SIS_DEVICE_NAME = "device_name";
    private static final String BATTERY = "battery";
    public int battery_level = 0;

    protected static final int REQUEST_ENABLE_BT = 2;

    private BleManager<? extends BleManagerCallbacks> mBleManager;


    private Button mConnectButton;
    private ILogSession mLogSession;

    private boolean mDeviceConnected = false;
    private String mDeviceName;

    @Override
    protected final void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter = new DeviceListAdapter(this);
        ensureBLESupported();
        if (!isBLEEnabled()) {
            showBLEDialog();
        }

		/*
         * We use the managers using a singleton pattern. It's not recommended for the Android, because the singleton instance remains after Activity has been
		 * destroyed but it's simple and is used only for this demo purpose. In final application Managers should be created as a non-static objects in
		 * Services. The Service should implement ManagerCallbacks interface. The application Activity may communicate with such Service using binding,
		 * broadcast listeners, local broadcast listeners (see support.v4 library), or messages. See the Proximity profile for Service approach.
		 */
        mBleManager = initializeManager();

        // In onInitialize method a final class may register local broadcast receivers that will listen for events from the service
        onInitialize(savedInstanceState);
        // The onCreateView class should... create the view
        onCreateView(savedInstanceState);


        // Common nRF Toolbox view references are obtained here
        setUpView();
        // View is ready to be used
        onViewCreated(savedInstanceState);
    }

    @Override
    public void onPause() {
        disconnect();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        disconnect();
        super.onDestroy();
    }

    /**
     * You may do some initialization here. This method is called from {@link #onCreate(Bundle)} before the view was created.
     */
    protected void onInitialize(final Bundle savedInstanceState) {
        // empty default implementation
    }

    /**
     * Called from {@link #onCreate(Bundle)}. This method should build the activity UI, f.e. using {@link #setContentView(int)}. Use to obtain references to
     * views. Connect/Disconnect button, the device name view and battery level view are manager automatically.
     *
     * @param savedInstanceState contains the data it most recently supplied in {@link #onSaveInstanceState(Bundle)}. Note: <b>Otherwise it is null</b>.
     */
    protected abstract void onCreateView(final Bundle savedInstanceState);

    /**
     * Called after the view has been created.
     *
     * @param savedInstanceState contains the data it most recently supplied in {@link #onSaveInstanceState(Bundle)}. Note: <b>Otherwise it is null</b>.
     */
    protected void onViewCreated(final Bundle savedInstanceState) {
        // empty default implementation
    }

    /**
     * Called after the view and the toolbar has been created.
     */
    protected final void setUpView() {
        // set GUI

        mConnectButton = (Button) findViewById(R.id.action_connect);
    }

    @Override
    public void onBackPressed() {
        mBleManager.disconnect();
        super.onBackPressed();
    }

    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(SIS_CONNECTION_STATUS, mDeviceConnected);
        outState.putString(SIS_DEVICE_NAME, mDeviceName);
        outState.putInt(BATTERY, battery_level);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull final Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mDeviceConnected = savedInstanceState.getBoolean(SIS_CONNECTION_STATUS);
        mDeviceName = savedInstanceState.getString(SIS_DEVICE_NAME);
        battery_level = savedInstanceState.getInt(BATTERY);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {


            }
        });

        if (mDeviceConnected) {
            if (mConnectButton != null)
                mConnectButton.setText(R.string.action_disconnect);
        } else {
            if (mConnectButton != null)
                mConnectButton.setText(R.string.action_connect);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.help, menu);
        return true;
    }

    /**
     * Use this method to handle menu actions other than home and about.
     *
     * @param itemId the menu item id
     * @return <code>true</code> if action has been handled
     */
    protected boolean onOptionsItemSelected(final int itemId) {
        // Overwrite when using menu other than R.menu.help
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        final int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                onBackPressed();
                break;
            default:
                return onOptionsItemSelected(id);
        }
        return true;
    }

    /**
     * Called when user press CONNECT or DISCONNECT button. See layout files -> onClick attribute.
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void onConnectClicked(final View view) {
        if (isBLEEnabled()) {
            if (!mDeviceConnected) {
                setDefaultUI();
//                showDeviceScanningDialog(getFilterUUID());
                startScan();
            } else {
                mBleManager.disconnect();
            }
        } else {
            showBLEDialog();
        }
    }

    protected void disconnect() {
        Log.d(TAG, "disconnect:  mDeviceConnected = " + mDeviceConnected);
        if (mDeviceConnected) {
            mBleManager.disconnect();
        } else {

        }
    }

    /**
     * Returns the title resource id that will be used to create logger session. If 0 is returned (default) logger will not be used.
     *
     * @return the title resource id
     */
    protected int getLoggerProfileTitle() {
        return 0;
    }

    /**
     * This method may return the local log content provider authority if local log sessions are supported.
     *
     * @return local log session content provider URI
     */
    //    @Override
    public void onDeviceSelected(final BluetoothDevice device, final String name) {
        Log.d(TAG, "onDeviceSelected: " + device.getAddress() + ", " + device.getName());

        mDeviceName = name;
        if (mConnectButton != null)
            mConnectButton.setText(R.string.action_connecting);
        mBleManager.connect(device);
    }

    @Override
    public void onDeviceConnecting(final BluetoothDevice device) {
        // do nothing
    }

    @Override
    public void onDeviceConnected(final BluetoothDevice device) {

        mDeviceConnected = true;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mConnectButton != null)
                    if (mConnectButton != null)
                        mConnectButton.setText(R.string.action_disconnect);
            }
        });
    }

    @Override
    public void onDeviceDisconnecting(final BluetoothDevice device) {
        // do nothing
    }

    @Override
    public void onDeviceDisconnected(final BluetoothDevice device) {
        mDeviceConnected = false;
        mBleManager.close();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mConnectButton != null)
                    mConnectButton.setText(R.string.action_connect);

            }
        });
    }

    @Override
    public void onLinklossOccur(final BluetoothDevice device) {
        mDeviceConnected = false;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
            }
        });
    }

    @Override
    public void onServicesDiscovered(final BluetoothDevice device, boolean optionalServicesFound) {
        // this may notify user or show some views
    }

    @Override
    public void onDeviceReady(final BluetoothDevice device) {
        // empty default implementation
    }

    @Override
    public void onBondingRequired(final BluetoothDevice device) {
        showToast(R.string.bonding);
    }

    @Override
    public void onBonded(final BluetoothDevice device) {
        showToast(R.string.bonded);
    }

    @Override
    public boolean shouldEnableBatteryLevelNotifications(final BluetoothDevice device) {
        // Yes, we want battery level updates
        return true;
    }

    @Override
    public void onBatteryValueReceived(final BluetoothDevice device, final int value) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
            }
        });
    }

    @Override
    public void onError(final BluetoothDevice device, final String message, final int errorCode) {
        DebugLogger.e(TAG, "Error occurred: " + message + ",  error code: " + errorCode);
        showToast(message + " (" + errorCode + ")");
    }

    @Override
    public void onDeviceNotSupported(final BluetoothDevice device) {
        showToast(R.string.not_supported);
    }

    /**
     * Shows a message as a Toast notification. This method is thread safe, you can call it from any thread
     *
     * @param message a message to be shown
     */
    protected void showToast(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(BleProfileActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Shows a message as a Toast notification. This method is thread safe, you can call it from any thread
     *
     * @param messageResId an resource id of the message to be shown
     */
    protected void showToast(final int messageResId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(BleProfileActivity.this, messageResId, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Returns <code>true</code> if the device is connected. Services may not have been discovered yet.
     */
    protected boolean isDeviceConnected() {
        return mDeviceConnected;
    }

    /**
     * Returns the name of the device that the phone is currently connected to or was connected last time
     */
    protected String getDeviceName() {
        return mDeviceName;
    }

    /**
     * Initializes the Bluetooth Low Energy manager. A manager is used to communicate with profile's services.
     *
     * @return the manager that was created
     */
    protected abstract BleManager<? extends BleManagerCallbacks> initializeManager();

    /**
     * Restores the default UI before reconnecting
     */
    protected abstract void setDefaultUI();

    /**
     * Returns the default device name resource id. The real device name is obtained when connecting to the device. This one is used when device has
     * disconnected.
     *
     * @return the default device name resource id
     */
    protected abstract int getDefaultDeviceName();

    /**
     * Returns the string resource id that will be shown in About box
     *
     * @return the about resource id
     */
    protected abstract int getAboutTextId();

    /**
     * The UUID filter is used to filter out available devices that does not have such UUID in their advertisement packet. See also:
     * {@link #isChangingConfigurations()}.
     *
     * @return the required UUID or <code>null</code>
     */
    protected abstract UUID getFilterUUID();

    /**
     * Shows the scanner fragment.
     *
     * @param filter the UUID filter used to filter out available devices. The fragment will always show all bonded devices as there is no information about their
     *               services
     * @see #getFilterUUID()
     */
    private void showDeviceScanningDialog(final UUID filter) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final ScannerFragment dialog = ScannerFragment.getInstance(filter);
                dialog.show(getSupportFragmentManager(), "scan_fragment");
            }
        });
    }

    /**
     * Returns the log session. Log session is created when the device was selected using the {@link ScannerFragment} and released when user press DISCONNECT.
     *
     * @return the logger session or <code>null</code>
     */
    protected ILogSession getLogSession() {
        return mLogSession;
    }

    private void ensureBLESupported() {
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.no_ble, Toast.LENGTH_LONG).show();
            finish();
        }
    }

    protected boolean isBLEEnabled() {
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        final BluetoothAdapter adapter = bluetoothManager.getAdapter();
        return adapter != null && adapter.isEnabled();
    }

    protected void showBLEDialog() {
        final Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
    }

    private boolean mIsScanning = false;
    private DeviceListAdapter mAdapter;
    private ParcelUuid mUuid;
    private final static long SCAN_DURATION = 5000;
    private final Handler mHandler = new Handler();

    /**
     * Scan for 5 seconds and then stop scanning when a BluetoothLE device is found then mLEScanCallback is activated This will perform regular scan for custom BLE Service UUID and then filter out.
     * using class ScannerServiceParser
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    protected void startScan(String _mac) {
        mStrBlaMacAddress = _mac;
        startScan();
    }

    /**
     * Scan for 5 seconds and then stop scanning when a BluetoothLE device is found then mLEScanCallback is activated This will perform regular scan for custom BLE Service UUID and then filter out.
     * using class ScannerServiceParser
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    protected void startScan() {
        Log.d(TAG, "startScan");
        // Since Android 6.0 we need to obtain either Manifest.permission.ACCESS_COARSE_LOCATION or Manifest.permission.ACCESS_FINE_LOCATION to be able to scan for
        // Bluetooth LE devices. This is related to beacons as proximity devices.
        // On API older than Marshmallow the following code does nothing.
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // When user pressed Deny and still wants to use this functionality, show the rationale
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {

                return;
            }

            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_PERMISSION_REQ_CODE);
            return;
        }

        // Hide the rationale message, we don't need it anymore.


        mAdapter.clearDevices();


        final BluetoothLeScannerCompat scanner = BluetoothLeScannerCompat.getScanner();
        final ScanSettings settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).setReportDelay(1000).setUseHardwareBatchingIfSupported(false).build();
        final List<ScanFilter> filters = new ArrayList<>();
        filters.add(new ScanFilter.Builder().setServiceUuid(mUuid).build());
        scanner.startScan(filters, settings, scanCallback);

        mIsScanning = true;
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mIsScanning) {
                    stopScan();
                }
            }
        }, SCAN_DURATION);
    }

    /**
     * Stop scan if user tap Cancel button
     */
    private void stopScan() {
        Log.d(TAG, "stopScan");
        if (mIsScanning) {
            final BluetoothLeScannerCompat scanner = BluetoothLeScannerCompat.getScanner();
            scanner.stopScan(scanCallback);

            mIsScanning = false;
        }
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                showNotificationAndStartScan();
            }
        }, 1000);
    }

    private void showNotificationAndStartScan() {
        if (!mDeviceConnected) {
            Toast.makeText(BleProfileActivity.this, "Please make sure the device is on.", Toast.LENGTH_LONG).show();
            mHandler.postDelayed(new Runnable() {
                @TargetApi(Build.VERSION_CODES.M)
                @Override
                public void run() {
                    startScan();
                }
            }, 2000);
        }
    }

    protected String mStrBlaMacAddress;
    private ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(final int callbackType, final ScanResult result) {
            // do nothing
        }

        @Override
        public void onBatchScanResults(final List<ScanResult> results) {
//            Log.d(TAG, "onBatchScanResults: " + results.size());
            for (ScanResult item : results) {
                String mac = item.getDevice().getAddress();
                Log.d(TAG, "onBatchScanResults: " + mac);
                if (mStrBlaMacAddress != null) {
                    if (mac.contentEquals(mStrBlaMacAddress)) {
                        Log.d(TAG, "if");
                        stopScan();
                        onDeviceSelected(item.getDevice(), item.getDevice().getName());
                    } else {
                        Log.d(TAG, "else");
                    }
                } else {
                    Log.d(TAG, "mStrBlaMacAddress is null");
                }
            }

            mAdapter.update(results);
        }

        @Override
        public void onScanFailed(final int errorCode) {
            // should never be called
        }
    };

}
