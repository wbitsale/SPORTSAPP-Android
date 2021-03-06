package com.calm_health.sports;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.LoaderManager.LoaderCallbacks;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.calm_health.sports.share.AppSharedPreferences;
import com.tool.sports.com.dfutool.AppHelpFragment;
import com.tool.sports.com.dfutool.DfuService;
import com.tool.sports.com.dfutool.PermissionRationaleFragment;
import com.tool.sports.com.dfutool.fragment.UploadCancelFragment;
import com.tool.sports.com.dfutool.settings.SettingsActivity;
import com.tool.sports.com.dfutool.settings.SettingsFragment;
import com.tool.sports.com.dfutool.utility.FileHelper;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import no.nordicsemi.android.dfu.DfuProgressListener;
import no.nordicsemi.android.dfu.DfuProgressListenerAdapter;
import no.nordicsemi.android.dfu.DfuServiceInitiator;
import no.nordicsemi.android.dfu.DfuServiceListenerHelper;
import no.nordicsemi.android.dfu.package_manager.Package;
import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat;
import no.nordicsemi.android.support.v18.scanner.ScanCallback;
import no.nordicsemi.android.support.v18.scanner.ScanFilter;
import no.nordicsemi.android.support.v18.scanner.ScanResult;
import no.nordicsemi.android.support.v18.scanner.ScanSettings;

/**
 * ActivityOtaDfu is the main DFU activity It implements DFUManagerCallbacks to receive callbacks from DFUManager class It implements
 * DeviceScannerFragment.OnDeviceSelectedListener callback to receive callback when device is selected from scanning dialog The activity supports portrait and
 * landscape orientations
 */
public class ActivityOtaDfu extends AppCompatActivity implements LoaderCallbacks<Cursor>,
        UploadCancelFragment.CancelFragmentListener, PermissionRationaleFragment.PermissionDialogListener {
    private static final String TAG = "ActivityOtaDfulog";

    private static final String PREFS_DEVICE_NAME = "no.nordicsemi.android.nrftoolbox.dfu.PREFS_DEVICE_NAME";
    private static final String PREFS_FILE_NAME = "no.nordicsemi.android.nrftoolbox.dfu.PREFS_FILE_NAME";
    private static final String PREFS_FILE_TYPE = "no.nordicsemi.android.nrftoolbox.dfu.PREFS_FILE_TYPE";
    private static final String PREFS_FILE_SIZE = "no.nordicsemi.android.nrftoolbox.dfu.PREFS_FILE_SIZE";

    private static final String DATA_DEVICE = "device";
    private static final String DATA_FILE_TYPE = "file_type";
    private static final String DATA_FILE_TYPE_TMP = "file_type_tmp";
    private static final String DATA_FILE_PATH = "file_path";
    private static final String DATA_FILE_STREAM = "file_stream";
    private static final String DATA_INIT_FILE_PATH = "init_file_path";
    private static final String DATA_INIT_FILE_STREAM = "init_file_stream";
    private static final String DATA_STATUS = "status";
    private static final String DATA_DFU_COMPLETED = "dfu_completed";
    private static final String DATA_DFU_ERROR = "dfu_error";

    private static final String EXTRA_URI = "uri";

    private static final int PERMISSION_REQ = 25;
    private static final int ENABLE_BT_REQ = 0;
    private static final int SELECT_FILE_REQ = 1;
    private static final int SELECT_INIT_FILE_REQ = 2;

    private TextView mDeviceNameView;
    private TextView mFileNameView;
    private TextView mTextPercentage;
    private TextView mTextUploading;
    private TextView mTextTitle;
    private ProgressBar mProgressBar;
    //================Battery================
    private LinearLayout lyt_battery;
    private TextView tx_battery;

    private Button mUploadButton;

    private BluetoothDevice mSelectedDevice;
    private String mFilePath;
    private Uri mFileStreamUri;
    private String mInitFilePath;
    private Uri mInitFileStreamUri;
    private int mFileType;
    private int mFileTypeTmp; // This value is being used when user is selecting a file not to overwrite the old value (in case he/she will cancel selecting file)
    private boolean mStatusOk;

    /**
     * Flag set to true in {@link #onRestart()} and to false in {@link #onPause()}.
     */
    private boolean mResumed;
    /**
     * Flag set to true if DFU operation was completed while {@link #mResumed} was false.
     */
    private boolean mDfuCompleted;
    /**
     * The error message received from DFU service while {@link #mResumed} was false.
     */
    private String mDfuError;

    /**
     * Package Generator
     */
    private Package mPackage;
    /**
     * The mProgress listener receives events from the DFU Service.
     * If is registered in onCreate() and unregistered in onDestroy() so methods here may also be called
     * when the screen is locked or the app went to the background. This is because the UI needs to have the
     * correct information after user comes back to the activity and this information can't be read from the service
     * as it might have been killed already (DFU completed or finished with error).
     */
    private final DfuProgressListener mDfuProgressListener = new DfuProgressListenerAdapter() {
        @Override
        public void onDeviceConnecting(final String deviceAddress) {
            mProgressBar.setIndeterminate(true);
            mTextPercentage.setText(R.string.dfu_status_connecting);
        }

        @Override
        public void onDfuProcessStarting(final String deviceAddress) {
            mProgressBar.setIndeterminate(true);
            mTextPercentage.setText(R.string.dfu_status_starting);
        }

        @Override
        public void onEnablingDfuMode(final String deviceAddress) {
            mProgressBar.setIndeterminate(true);
            mTextPercentage.setText(R.string.dfu_status_switching_to_dfu);
        }

        @Override
        public void onFirmwareValidating(final String deviceAddress) {
            mProgressBar.setIndeterminate(true);
            mTextPercentage.setText(R.string.dfu_status_validating);
        }

        @Override
        public void onDeviceDisconnecting(final String deviceAddress) {
            mProgressBar.setIndeterminate(true);
            mTextPercentage.setText(R.string.dfu_status_disconnecting);
        }

        @Override
        public void onDfuCompleted(final String deviceAddress) {
            mTextPercentage.setText(R.string.dfu_status_completed);
            if (mResumed) {
                // let's wait a bit until we cancel the notification. When canceled immediately it will be recreated by service again.
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        onTransferCompleted();

                        // if this activity is still open and upload process was completed, cancel the notification
                        final NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                        manager.cancel(DfuService.NOTIFICATION_ID);
                    }
                }, 200);
            } else {
                // Save that the DFU process has finished
                mDfuCompleted = true;
            }
        }

        @Override
        public void onDfuAborted(final String deviceAddress) {
            mTextPercentage.setText(R.string.dfu_status_aborted);
            // let's wait a bit until we cancel the notification. When canceled immediately it will be recreated by service again.
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    onUploadCanceled();

                    // if this activity is still open and upload process was completed, cancel the notification
                    final NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    manager.cancel(DfuService.NOTIFICATION_ID);
                }
            }, 200);
        }

        @Override
        public void onProgressChanged(final String deviceAddress, final int percent, final float speed, final float avgSpeed, final int currentPart, final int partsTotal) {
            mProgressBar.setIndeterminate(false);
            mProgressBar.setProgress(percent);
            mTextPercentage.setText(getString(R.string.dfu_uploading_percentage, percent));
            if (partsTotal > 1)
                mTextUploading.setText(getString(R.string.dfu_status_uploading_part, currentPart, partsTotal));
            else
                mTextUploading.setText(R.string.dfu_status_uploading);
        }

        @Override
        public void onError(final String deviceAddress, final int error, final int errorType, final String message) {
            mProgressBar.setProgress(0);
            mTextPercentage.setText("");
            if (mResumed) {
                showErrorMessage(message);

                // We have to wait a bit before canceling notification. This is called before DfuService creates the last notification.
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // if this activity is still open and upload process was completed, cancel the notification
                        final NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                        manager.cancel(DfuService.NOTIFICATION_ID);
                    }
                }, 200);
            } else {
                mDfuError = message;
            }
        }
    };

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feature_dfu);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(true);
        }

        isBLESupported();
        if (!isBLEEnabled()) {
            showBLEDialog();
        }
        setGUI();
        downloadFirmware();
        // Try to create sample files
        if (FileHelper.newSamplesAvailable(this)) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                FileHelper.createSamples(this);
            } else {
                final DialogFragment dialog = PermissionRationaleFragment.getInstance(R.string.permission_sd_text, Manifest.permission.WRITE_EXTERNAL_STORAGE);
                dialog.show(getSupportFragmentManager(), null);
            }
        }

        // restore saved state
        mFileType = DfuService.TYPE_AUTO; // Default
        if (savedInstanceState != null) {
            mFileType = savedInstanceState.getInt(DATA_FILE_TYPE);
            mFileTypeTmp = savedInstanceState.getInt(DATA_FILE_TYPE_TMP);
            mFilePath = savedInstanceState.getString(DATA_FILE_PATH);
            mFileStreamUri = savedInstanceState.getParcelable(DATA_FILE_STREAM);
            mInitFilePath = savedInstanceState.getString(DATA_INIT_FILE_PATH);
            mInitFileStreamUri = savedInstanceState.getParcelable(DATA_INIT_FILE_STREAM);
            mSelectedDevice = savedInstanceState.getParcelable(DATA_DEVICE);
            mStatusOk = mStatusOk || savedInstanceState.getBoolean(DATA_STATUS);

            if (mSelectedDevice != null && mStatusOk) {
                enableUploadBtn();
            } else {
                disableUploadBtn();
            }
            mDfuCompleted = savedInstanceState.getBoolean(DATA_DFU_COMPLETED);
            mDfuError = savedInstanceState.getString(DATA_DFU_ERROR);
        }

        DfuServiceListenerHelper.registerProgressListener(this, mDfuProgressListener);

        mPackage = new Package();
        getDevice();
    }

    private void getDevice() {
        mStrMacAddress = AppSharedPreferences.getDeviceMacAddress(this);

        Log.d(TAG, "getDevice: " + mStrMacAddress);
        if (mStrMacAddress != "null") {
            // auto Find device after 1 second
            new Handler().postDelayed(
                    new Runnable() {
                        @RequiresApi(api = Build.VERSION_CODES.M)
                        public void run() {
                            Log.i(TAG, "This'll run 1000 milliseconds later");
                            startScan();
                        }
                    },
                    1000);
        } else {
            Toast.makeText(this, "Device is not registered .", Toast.LENGTH_SHORT).show();
        }
    }

    private String mStrMacAddress;

    private static String mStr_file_src = "http://13.113.160.171/firmware/download";
    private static String mStr_file_dest = Environment.getExternalStorageDirectory().toString() + "/firmware.zip";

    private void downloadFirmware() {
        new DownloadFileFromURL().execute(mStr_file_src);
        this.mFilePath = mStr_file_dest;
    }

    private class DownloadFileFromURL extends AsyncTask<String, String, String> {
        /**
         * Before starting background thread Show Progress Bar Dialog
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showLoading("Wait while firmware downloading ...");
            Log.d(TAG, "start download firmware");
        }

        /**
         * Downloading file in background thread
         */
        @Override
        protected String doInBackground(String... f_url) {

            int count;
            try {
                URL url = new URL(f_url[0]);
                URLConnection connection = url.openConnection();
                connection.connect();

                // this will be useful so that you can show a typical 0-100%
                // mProgress bar
                int lengthOfFile = connection.getContentLength();

                // download the file
                InputStream input = new BufferedInputStream(url.openStream(),
                        8192);
                // Output stream
                OutputStream output = new FileOutputStream(mStr_file_dest);

                byte data[] = new byte[1024];

                long total = 0;

                while ((count = input.read(data)) != -1) {
                    total += count;
                    // publishing the mProgress....
                    // After this onProgressUpdate will be called
                    publishProgress("" + (int) ((total * 100) / lengthOfFile));
//                    Log.d(TAG, "" + (int) ((total * 100) / lengthOfFile));
                    // writing data to file
                    output.write(data, 0, count);
                }

                // flushing output
                output.flush();

                // closing streams
                output.close();
                input.close();

            } catch (Exception e) {
                Log.e("Error: ", e.getMessage());
            }

            return null;
        }

        /**
         * Updating mProgress bar
         */
        protected void onProgressUpdate(String... progress) {
            // setting mProgress percentage
//            pDialog.setProgress(Integer.parseInt(mProgress[0]));
            mFileNameView.setText("" + Integer.parseInt(progress[0]));
//
            mTextPercentage.setText("Downloading file. Please wait...");
            mTextPercentage.setEnabled(true);
            final File file = new File(mStr_file_dest);
            mFilePath = mStr_file_dest;
            mFileType = DfuService.TYPE_AUTO;
            updateFileInfo(file.getName(), file.length(), mFileType);
        }

        /**
         * After completing background task Dismiss the mProgress dialog
         **/
        @Override
        protected void onPostExecute(String file_url) {
            hideLoading();
            Log.d(TAG, "Complete firmware download");
        }

    }//End DownloadFileFromURL

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DfuServiceListenerHelper.unregisterProgressListener(this, mDfuProgressListener);
    }

    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(DATA_FILE_TYPE, mFileType);
        outState.putInt(DATA_FILE_TYPE_TMP, mFileTypeTmp);
        outState.putString(DATA_FILE_PATH, mFilePath);
        outState.putParcelable(DATA_FILE_STREAM, mFileStreamUri);
        outState.putString(DATA_INIT_FILE_PATH, mInitFilePath);
        outState.putParcelable(DATA_INIT_FILE_STREAM, mInitFileStreamUri);
        outState.putParcelable(DATA_DEVICE, mSelectedDevice);
        outState.putBoolean(DATA_STATUS, mStatusOk);
        outState.putBoolean(DATA_DFU_COMPLETED, mDfuCompleted);
        outState.putString(DATA_DFU_ERROR, mDfuError);
    }

    private void setGUI() {
        setDeviceInfo();
        mProgress = new ProgressDialog(this);
        lyt_battery = (LinearLayout) findViewById(R.id.lyt_battery);
        tx_battery = (TextView) findViewById(R.id.tx_battery);

        mDeviceNameView = (TextView) findViewById(R.id.device_name);
        mFileNameView = (TextView) findViewById(R.id.file_name);
        mUploadButton = (Button) findViewById(R.id.action_upload);
        mTextPercentage = (TextView) findViewById(R.id.textviewProgress);
        mTextUploading = (TextView) findViewById(R.id.textviewUploading);
        mProgressBar = (ProgressBar) findViewById(R.id.progressbar_file);

        mProgressBar.getIndeterminateDrawable().setColorFilter(
                Color.rgb(00, 0x76, 0xFF),
                PorterDuff.Mode.OVERLAY);

        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (isDfuServiceRunning()) {
            // Restore image file information
            mDeviceNameView.setText(preferences.getString(PREFS_DEVICE_NAME, ""));
            mFileNameView.setText(preferences.getString(PREFS_FILE_NAME, ""));
            mStatusOk = true;
            showProgressBar();
        }
    }

    private void setDeviceInfo() {
        TextView tv_devicename = (TextView) findViewById(R.id.device_name);
        TextView tv_devicemac = (TextView) findViewById(R.id.tf_deviceMac);
        String deviceName = AppSharedPreferences.getDeviceName(this);
        String deviceMac = AppSharedPreferences.getDeviceMacAddress(this);
        deviceName = (deviceName != "null") ? deviceName : "Unknown";
        deviceMac = (deviceMac != "null") ? deviceMac : "Unknown";
        tv_devicename.setText(deviceName);
        tv_devicemac.setText(deviceMac);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mResumed = true;
        if (mDfuCompleted)
            onTransferCompleted();
        if (mDfuError != null)
            showErrorMessage(mDfuError);
        if (mDfuCompleted || mDfuError != null) {
            // if this activity is still open and upload process was completed, cancel the notification
            final NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            manager.cancel(DfuService.NOTIFICATION_ID);
            mDfuCompleted = false;
            mDfuError = null;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mResumed = false;
    }

    @Override
    public void onRequestPermission(final String permission) {
        ActivityCompat.requestPermissions(this, new String[]{permission}, PERMISSION_REQ);
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode, @NonNull final String[] permissions, @NonNull final int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_REQ: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // We have been granted the Manifest.permission.WRITE_EXTERNAL_STORAGE permission. Now we may proceed with exporting.
                    FileHelper.createSamples(this);
                } else {
                    Toast.makeText(this, R.string.no_required_permission, Toast.LENGTH_SHORT).show();
                }
                break;
            }
        }
    }

    private void isBLESupported() {
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            showToast(R.string.no_ble);
            finish();
        }
    }

    private boolean isBLEEnabled() {
        final BluetoothManager manager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        final BluetoothAdapter adapter = manager.getAdapter();
        return adapter != null && adapter.isEnabled();
    }

    private void showBLEDialog() {
        final Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableIntent, ENABLE_BT_REQ);
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.settings_and_about, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        int i = item.getItemId();
        if (i == android.R.id.home) {
            onBackPressed();

        } else if (i == R.id.action_about) {
            final AppHelpFragment fragment = AppHelpFragment.getInstance(R.string.dfu_about_text);
            fragment.show(getSupportFragmentManager(), "help_fragment");

        } else if (i == R.id.action_settings) {
            final Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        }
        return true;
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        if (resultCode != RESULT_OK)
            return;

        switch (requestCode) {
            case SELECT_FILE_REQ: {
                // clear previous data
                mFileType = mFileTypeTmp;
                mFilePath = null;
                mFileStreamUri = null;

                // and read new one
                final Uri uri = data.getData();
            /*
             * The URI returned from application may be in 'file' or 'content' schema. 'File' schema allows us to create a File object and read details from if
			 * directly. Data from 'Content' schema must be read by Content Provider. To do that we are using a Loader.
			 */
                if (uri.getScheme().equals("file")) {
                    // the direct path to the file has been returned
                    final String path = uri.getPath();
                    final File file = new File(path);
                    mFilePath = path;

                    updateFileInfo(file.getName(), file.length(), mFileType);
                } else if (uri.getScheme().equals("content")) {
                    // an Uri has been returned
                    mFileStreamUri = uri;
                    // if application returned Uri for streaming, let's us it. Does it works?
                    // FIXME both Uris works with Google Drive app. Why both? What's the difference? How about other apps like DropBox?
                    final Bundle extras = data.getExtras();
                    if (extras != null && extras.containsKey(Intent.EXTRA_STREAM))
                        mFileStreamUri = extras.getParcelable(Intent.EXTRA_STREAM);

                    // file name and size must be obtained from Content Provider
                    final Bundle bundle = new Bundle();
                    bundle.putParcelable(EXTRA_URI, uri);
                    getLoaderManager().restartLoader(SELECT_FILE_REQ, bundle, this);
                }
                break;
            }
            case SELECT_INIT_FILE_REQ: {
                mInitFilePath = null;
                mInitFileStreamUri = null;

                // and read new one
                final Uri uri = data.getData();
            /*
             * The URI returned from application may be in 'file' or 'content' schema. 'File' schema allows us to create a File object and read details from if
			 * directly. Data from 'Content' schema must be read by Content Provider. To do that we are using a Loader.
			 */
                if (uri.getScheme().equals("file")) {
                    // the direct path to the file has been returned
                    mInitFilePath = uri.getPath();
                } else if (uri.getScheme().equals("content")) {
                    // an Uri has been returned
                    mInitFileStreamUri = uri;
                    // if application returned Uri for streaming, let's us it. Does it works?
                    // FIXME both Uris works with Google Drive app. Why both? What's the difference? How about other apps like DropBox?
                    final Bundle extras = data.getExtras();
                    if (extras != null && extras.containsKey(Intent.EXTRA_STREAM))
                        mInitFileStreamUri = extras.getParcelable(Intent.EXTRA_STREAM);
                }
                break;
            }
            default:
                break;
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(final int id, final Bundle args) {
        final Uri uri = args.getParcelable(EXTRA_URI);
        /*
         * Some apps, f.e. Google Drive allow to select file that is not on the device. There is no "_data" column handled by that provider. Let's try to obtain
		 * all columns and than check which columns are present.
		 */
        // final String[] projection = new String[] { MediaStore.MediaColumns.DISPLAY_NAME, MediaStore.MediaColumns.SIZE, MediaStore.MediaColumns.DATA };
        return new CursorLoader(this, uri, null /* all columns, instead of projection */, null, null, null);
    }

    @Override
    public void onLoaderReset(final Loader<Cursor> loader) {
        mFileNameView.setText(null);
        mFilePath = null;
        mFileStreamUri = null;
        mStatusOk = false;
    }

    @Override
    public void onLoadFinished(final Loader<Cursor> loader, final Cursor data) {
        if (data != null && data.moveToNext()) {
            /*
             * Here we have to check the column indexes by name as we have requested for all. The order may be different.
			 */
            final String fileName = data.getString(data.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME)/* 0 DISPLAY_NAME */);
            final int fileSize = data.getInt(data.getColumnIndex(MediaStore.MediaColumns.SIZE) /* 1 SIZE */);
            String filePath = null;
            final int dataIndex = data.getColumnIndex(MediaStore.MediaColumns.DATA);
            if (dataIndex != -1)
                filePath = data.getString(dataIndex /* 2 DATA */);
            if (!TextUtils.isEmpty(filePath))
                mFilePath = filePath;

            updateFileInfo(fileName, fileSize, mFileType);
        } else {
            mFileNameView.setText(null);
            mFilePath = null;
            mFileStreamUri = null;
            mStatusOk = false;
        }
    }

    /**
     * Updates the file information on UI
     *
     * @param fileName file name
     * @param fileSize file length
     */
    private void updateFileInfo(final String fileName, final long fileSize, final int fileType) {
        mFileNameView.setText(fileName);

        final String extension = mFileType == DfuService.TYPE_AUTO ? "(?i)ZIP" : "(?i)HEX|BIN"; // (?i) =  case insensitive
        final boolean statusOk = mStatusOk = MimeTypeMap.getFileExtensionFromUrl(fileName).matches(extension);

        if (mSelectedDevice != null && statusOk) {
            enableUploadBtn();
        } else {
            disableUploadBtn();
        }

        // Ask the user for the Init packet file if HEX or BIN files are selected. In case of a ZIP file the Init packets should be included in the ZIP.
        if (statusOk && fileType != DfuService.TYPE_AUTO) {
            new AlertDialog.Builder(this).setTitle(R.string.dfu_file_init_title).setMessage(R.string.dfu_file_init_message)
                    .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(final DialogInterface dialog, final int which) {
                            mInitFilePath = null;
                            mInitFileStreamUri = null;
                        }
                    }).setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(final DialogInterface dialog, final int which) {
                    final Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType(DfuService.MIME_TYPE_OCTET_STREAM);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    startActivityForResult(intent, SELECT_INIT_FILE_REQ);
                }
            }).show();
        }
    }

    ProgressDialog mProgress;

    private void showLoading(String str_Msg) {
        hideLoading();
        Log.d(TAG, "s howLoading");

        mProgress.setTitle("Loading");
        mProgress.setMessage(str_Msg); //"Wait while loading..."
        mProgress.setCancelable(false); // disable dismiss by tapping outside of the dialog
//        mProgress.show();
    }

    private void hideLoading() {
        Log.d(TAG, "h ideLoading");
        if (mProgress != null)
            mProgress.dismiss();
    }

    private void disableUploadBtn() {
        mUploadButton.setBackgroundResource(R.drawable.radiusbuttondisable);
        mUploadButton.setEnabled(false);
    }

    private void enableUploadBtn() {
        mUploadButton.setBackgroundResource(R.drawable.radiusbuttonlight);
        mUploadButton.setEnabled(true);
    }

    /**
     * Callback of UPDATE/CANCEL button on ActivityOtaDfu
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void onUploadClicked(final View view) {
        Toast.makeText(this, "UPDATE", Toast.LENGTH_SHORT).show();

        if (!mIsFound) {
            startScan();
            return;
        }
        mProgressBar.setProgress(0);
        if (isDfuServiceRunning()) {
            showUploadCancelDialog();
            return;
        }

        // Check whether the selected file is a HEX file (we are just checking the extension)
        if (!mStatusOk) {
            Toast.makeText(this, R.string.dfu_file_status_invalid_message, Toast.LENGTH_LONG).show();
            return;
        }

        // Save current state in order to restore it if user quit the Activity
        mProgressBar.setIndeterminate(true);

        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        final SharedPreferences.Editor editor = preferences.edit();
        editor.putString(PREFS_DEVICE_NAME, mSelectedDevice.getName());
        editor.putString(PREFS_FILE_NAME, mFileNameView.getText().toString());
        editor.apply();

        showProgressBar();

        final boolean keepBond = preferences.getBoolean(SettingsFragment.SETTINGS_KEEP_BOND, false);
        final boolean forceDfu = preferences.getBoolean(SettingsFragment.SETTINGS_ASSUME_DFU_NODE, false);
        final boolean enablePRNs = preferences.getBoolean(SettingsFragment.SETTINGS_PACKET_RECEIPT_NOTIFICATION_ENABLED, Build.VERSION.SDK_INT < Build.VERSION_CODES.M);
        String value = preferences.getString(SettingsFragment.SETTINGS_NUMBER_OF_PACKETS, String.valueOf(DfuServiceInitiator.DEFAULT_PRN_VALUE));
        int numberOfPackets;
        try {
            numberOfPackets = Integer.parseInt(value);
        } catch (final NumberFormatException e) {
            numberOfPackets = DfuServiceInitiator.DEFAULT_PRN_VALUE;
        }

        final DfuServiceInitiator starter = new DfuServiceInitiator(mSelectedDevice.getAddress())
                .setDeviceName(mSelectedDevice.getName())
                .setKeepBond(keepBond)
                .setForceDfu(forceDfu)
                .setPacketsReceiptNotificationsEnabled(enablePRNs)
                .setPacketsReceiptNotificationsValue(numberOfPackets)
                .setUnsafeExperimentalButtonlessServiceInSecureDfuEnabled(true);
        if (mFileType == DfuService.TYPE_AUTO)
            starter.setZip(mFileStreamUri, mFilePath);
        else {
            starter.setBinOrHex(mFileType, mFileStreamUri, mFilePath).setInitFile(mInitFileStreamUri, mInitFilePath);
        }
        starter.start(this, DfuService.class);

        mTextPercentage.setText("Connecting...");
        mTextPercentage.setVisibility(View.VISIBLE);
    }

    public void onCancelClicked(final View v) {
        Toast.makeText(ActivityOtaDfu.this, "Cancel", Toast.LENGTH_SHORT).show();
        if (isDfuServiceRunning()) {
            showUploadCancelDialog();
        }
    }

    private void showUploadCancelDialog() {
        final LocalBroadcastManager manager = LocalBroadcastManager.getInstance(this);
        final Intent pauseAction = new Intent(DfuService.BROADCAST_ACTION);
        pauseAction.putExtra(DfuService.EXTRA_ACTION, DfuService.ACTION_PAUSE);
        manager.sendBroadcast(pauseAction);

        final UploadCancelFragment fragment = UploadCancelFragment.getInstance();
        fragment.show(getSupportFragmentManager(), TAG);
    }

    public void onDeviceSelected(final BluetoothDevice device, final String name) {
        mSelectedDevice = device;
        mUploadButton.setEnabled(mStatusOk);
        mDeviceNameView.setText(name != null ? name : getString(R.string.not_available));

    }

    private void showProgressBar() {
        mProgressBar.setVisibility(View.VISIBLE);
        mTextPercentage.setVisibility(View.VISIBLE);
        mTextPercentage.setText(null);
        mTextUploading.setText(R.string.dfu_status_uploading);
//		mTextUploading.setVisibility(View.VISIBLE);

        enableUploadBtn();


    }

    private void onTransferCompleted() {
        clearUI(false);// clear ui without device connect
        mTextPercentage.setText("Done");
        showToast(R.string.dfu_success);
        Toast.makeText(this, "Firmware has been updated successfully", Toast.LENGTH_LONG).show();
    }

    public void onUploadCanceled() {
        clearUI(false);
        showToast(R.string.dfu_aborted);
    }

    public void onBootloaderMode(final View view) {

    }

    @Override
    public void onCancelUpload() {
        mProgressBar.setIndeterminate(true);
        mTextUploading.setText(R.string.dfu_status_aborting);
        mTextPercentage.setText(null);
    }

    private void showErrorMessage(final String message) {
        clearUI(false);
        showToast("Upload failed: " + message);
    }

    private void clearUI(final boolean clearDevice) {
//		mProgressBar.setVisibility(View.INVISIBLE);
//		mTextPercentage.setVisibility(View.INVISIBLE);
//		mTextUploading.setVisibility(View.INVISIBLE);

        mProgressBar.setIndeterminate(false);

        disableUploadBtn();
        if (clearDevice) {
            mSelectedDevice = null;
            mDeviceNameView.setText(R.string.dfu_default_name);
        }
        // Application may have lost the right to these files if Activity was closed during upload (grant uri permission). Clear file related values.
//		mFileNameView.setText(null);
//		mFilePath = null;
//		mFileStreamUri = null;
//		mInitFilePath = null;
//		mInitFileStreamUri = null;
//		mStatusOk = false;
    }

    private void showToast(final int messageResId) {
//        Toast.makeText(this, messageResId, Toast.LENGTH_SHORT).show();
    }

    private void showToast(final String message) {
//        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private boolean isDfuServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (DfuService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    // ble
    private final Handler mHandler = new Handler();
    private final static long SCAN_DURATION = 10000;
    private final static int REQUEST_PERMISSION_REQ_CODE = 34; // any 8-bit number
    private boolean mIsScanning = false;
    private boolean mIsFound = false;

    /**
     * Scan for 10 seconds and then stop scanning when a BluetoothLE device is found then mLEScanCallback is activated This will perform regular scan for custom BLE Service UUID and then filter out.
     * using class ScannerServiceParser
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void startScan() {
        Log.d(TAG, "startScan");
        showLoading("Wait while Scanning device...");
//        mTextTitle.setText("Scanning device ...");
        clearUI(false);
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

        final BluetoothLeScannerCompat scanner = BluetoothLeScannerCompat.getScanner();
        final ScanSettings settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).setReportDelay(1000).setUseHardwareBatchingIfSupported(false).build();
        final List<ScanFilter> filters = new ArrayList<>();
        filters.add(new ScanFilter.Builder().setServiceUuid(null).build());
        scanner.startScan(filters, settings, scanCallback);

        mIsScanning = true;
        mIsFound = false;
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "after " + SCAN_DURATION + ", " + mIsScanning);
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
            if (mIsFound) {
                enableUploadBtn();
                Log.d(TAG, "isFound");
//                mTextPercentage.setText("Device Connected");
//                mTextPercentage.setEnabled(true);
            } else {
                Log.d(TAG, "not isFound");
//                mTextTitle.setText("Please make sure the device is on.");
                Toast.makeText(this, "Please make sure the device is on.", Toast.LENGTH_SHORT).show();

                mHandler.postDelayed(new Runnable() {
                    @TargetApi(Build.VERSION_CODES.M)
                    @Override
                    public void run() {
                        Log.d(TAG, "after " + 1000 + ", rescan");
                        if (!mIsScanning) {
                            startScan();
                        }
                    }
                }, 1000);

            }
        }
    }

    private ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(final int callbackType, final ScanResult result) {
            // do nothing
            Log.d(TAG, "onScanResult");
        }

        @Override
        public void onBatchScanResults(final List<ScanResult> results) {
//            Log.d(TAG, "" + results.size());
            for (int i = 0; i < results.size(); i++) {

                String strFoundMac = results.get(i).getDevice().getAddress();
//                Log.d(TAG, strFoundMac + ", source: " + mStrMacAddress);
                if (mStrMacAddress != null) {
                    if (mStrMacAddress.contentEquals(strFoundMac)) {
                        //khs
                        ImageView imgConnec = (ImageView) findViewById(R.id.img_ble_connect);
                        imgConnec.setImageResource(R.drawable.status_connected);
                        Log.d(TAG, "Device selected");
                        onDeviceSelected(results.get(i).getDevice(), results.get(i).getDevice().getName());
//                        mTextTitle.setText("" + results.get(i).getDevice().getName() + " Firmware Update");
                        mIsFound = true;
                        hideLoading();
                        setBattery();

                        enableUploadBtn();
                        stopScan();
                        break;
                    }
                }
            }
        }

        @Override
        public void onScanFailed(final int errorCode) {
            // should never be called
        }
    };

    private void setBattery() {
        final int nBattery = AppSharedPreferences.getBatteryLevel(this);
        tx_battery.setText(nBattery + "%");
//        lyt_battery.post(new Runnable() {
//            @Override
//            public void run() {
        int nWidth = lyt_battery.getWidth();
        int nHeight = lyt_battery.getHeight();

        int newWidth = nWidth * (100 - nBattery) / 100;
        Log.d(TAG, "" + nWidth + ", " + newWidth);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                newWidth, nHeight);
        lyt_battery.setLayoutParams(params);
//            }
//        });
    }
}