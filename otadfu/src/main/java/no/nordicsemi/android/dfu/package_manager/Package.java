package no.nordicsemi.android.dfu.package_manager;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.util.Collections;
import java.util.Vector;

/**
 * Make a package(*.bin + *.dat + manifest.json) from hex file
 * .bin - firmware file as bin format
 * .dat - init packet
 * manifest.json - manifest for package
 */
class FirmwareData {
    public String FIRMWARE_FILENAME;
    public String BIN_FILENAME;
    public InitPacketData mInitPacket;

    FirmwareData() {
        mInitPacket = new InitPacketData();
    }

    class InitPacketData {
        public boolean DEBUG_MODE = true;
        public int HW_VERSION = 2;
        public int FW_VERSION = 3;
        public Vector SD_REQ_ARR = new Vector();
    }
}

public class Package {
    public FirmwareData mFirmwareData;
    public String MANIFEST_FILENAME = "manifest.json";
    public String keyfile;
    public String work_dir;
    public String manifest;
    public String zip_file;

    public Package() {
        Vector sd_req = new Vector();
        sd_req.add(0x98);
        sd_req.add(0xCAFE);
        String app_fw = "content://com.android.externalstorage.documents/document/primary%3ADownload%2F1.hex";

        initFirmwareData(true, 52, 1, sd_req, app_fw, "");

        manifest = null;
        work_dir = null;
    }

    public Package(
            boolean debug_mode,
            int hw_version,
            int app_version,
            Vector sd_req,
            String app_fw,
            String key_file) {
        initFirmwareData(debug_mode, hw_version, app_version, sd_req, app_fw, key_file);
    }

    void initFirmwareData(
            boolean debug_mode,
            int hw_version,
            int app_version,
            Vector sd_req,
            String app_fw,
            String key_file) {
        mFirmwareData = new FirmwareData();
        mFirmwareData.mInitPacket.DEBUG_MODE = debug_mode;
        mFirmwareData.mInitPacket.HW_VERSION = hw_version;
        mFirmwareData.mInitPacket.FW_VERSION = app_version;
        mFirmwareData.mInitPacket.SD_REQ_ARR.setSize(sd_req.size());
        Collections.copy(mFirmwareData.mInitPacket.SD_REQ_ARR, sd_req);

        mFirmwareData.FIRMWARE_FILENAME = app_fw;
        keyfile = key_file;
    }

    public void setFirmwareFile(String filename) {
        mFirmwareData.FIRMWARE_FILENAME = filename;
    }

    public void generatePackage(String filename) {
        zip_file = filename;
        File direct = new File(Environment.getExternalStorageDirectory() + "/dfu_work_dir");

        if(!direct.exists())
        {
            if(direct.mkdir()) {
            }
        }
        work_dir = Environment.getExternalStorageDirectory() + "/dfu_work_dir";
        Log.d("work dir", work_dir);

        mFirmwareData.BIN_FILENAME = normalize_firmware_to_bin();
    }

    public void removeWorkDir() {
        File dir = new File(Environment.getExternalStorageDirectory() + "/dfu_work_dir");

        if(dir.exists())
        {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++)
            {
                new File(dir, children[i]).delete();
            }
            dir.delete();
        }
    }

    private String normalize_firmware_to_bin() {
        Log.d("Hex2Bin", "Started");
        String bin_filename = zip_file + ".bin";
        String bin_filepath = work_dir + '/' + bin_filename;

        nrfHex temp = new nrfHex(mFirmwareData.FIRMWARE_FILENAME);


        return bin_filename;
    }
}
